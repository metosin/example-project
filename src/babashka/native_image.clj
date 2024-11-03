(ns native-image
  (:require [babashka.fs :as fs]
            [babashka.http-client :as http]
            [babashka.process :as p]
            [babashka.wait :as wait]
            [cheshire.core :as json]
            [clojure.string :as str]
            [clojure.walk :as walk]))

(defn start-backend [{:keys [http-port extra-env timeout-ms command]}]
  (p/shell "bb db-up")
  (let [command (if (string? command)
                  [command]
                  command)
        backend-process (apply p/process
                               {:inherit true
                                :extra-env extra-env}
                               command)]
    (when (= :timeout (wait/wait-for-port "localhost" http-port
                                          {:timeout timeout-ms
                                           :default :timeout}))
      (p/destroy-tree backend-process)
      (throw (Exception. (str "Backend didn't start in " timeout-ms "ms"))))
    backend-process))

(defn run-training [http-port]
  (let [base-url (format "http://localhost:%s" http-port)
        test-requests [[200 (str base-url "/api/todo")]
                       [200 (str base-url "/api/buddy-test")]]]
    (doall (keep (fn [[expected-status url]]
                   (let [response (http/get url {:throw false})]
                     (when (not= expected-status (:status response))
                       response)))
                 test-requests))))

(defn check-failures [failures]
  (when (not (zero? (count failures)))
    (println "Training requests failed:")
    (doseq [failure failures]
      (prn failure))
    (System/exit 1)))

(defn remove-clojure-name [coll clojure-names]
  (remove (fn [{:keys [type glob]}]
            (let [s (or glob
                        (when (and type
                                   (string? type))
                          type))]
              (if s
                (some #(or (.startsWith s %)
                           (.contains s %))
                      clojure-names)
                false)))
          coll))

(defn post-process-metadata
  "Removes Clojure relates entries from the metadata file, idea is to keep only entries from dependencies

  Apparently, keeping Clojure entries leads into problems with code that modifies dynamic variables such as *warn-on-reflection*"
  [filename]
  (fs/copy filename (str filename ".orig"))
  (let [data (json/parse-string (slurp filename) true)
        clojure-names-atom (atom #{"clojure"})]
    (walk/postwalk (fn [o]
                     (when (and (string? o)
                                (or (.endsWith o ".clj")
                                    (.endsWith o ".cljc")))
                       (let [clojure-name (str/replace o
                                                       (if (.endsWith o ".clj")
                                                         ".clj"
                                                         ".cljc")
                                                       "")]
                         (swap! clojure-names-atom conj clojure-name (str/replace clojure-name "/" "."))))
                     o)
                   data)
    (let [new-data (-> data
                       (update :reflection #(remove-clojure-name % @clojure-names-atom))
                       (update :resources #(remove-clojure-name % @clojure-names-atom))
                       (update :jni #(remove-clojure-name % @clojure-names-atom)))]
      (spit filename (json/generate-string new-data {:pretty true})))))

;; Do a training run to gather metadata via native-image-agent
;; Note that should not refer to any unnecessary test resources on the classpath during training,
;; since those will get included into the metadata
(defn run-with-agent []
  (let [http-port 3322
        tracing-process (start-backend {:http-port http-port
                                        :extra-env {"CONFIG_EDN" "resources/config.edn"
                                                    "HTTP_PORT" http-port}
                                        :timeout-ms 5000
                                        :command ["java" (str "-agentlib:native-image-agent="
                                                              "config-output-dir=target/native-image-configuration/META-INF/native-image")
                                                  "-jar" "target/app.jar"]})
        failures (run-training http-port)]
    (p/destroy-tree tracing-process)
    (check-failures failures)
    ;; Wait for native-image-agent to create the tracing file
    (when (= :timeout (wait/wait-for-path "target/native-image-configuration/META-INF/native-image/reachability-metadata.json"
                                          {:timeout 5000
                                           :default :timeout}))
      (println "Reachability metadata file not created")
      (System/exit 1)))

  (post-process-metadata "target/native-image-configuration/META-INF/native-image/reachability-metadata.json"))
