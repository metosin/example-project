(ns build
  (:require [shadow.cljs.devtools.api :as shadow]
            [clojure.tools.build.api :as b]))

(def version "0.0.1-SNAPSHOT")
(def class-dir "target/classes")
(def uber-file "target/app.jar")

(def basis (delay
             (b/create-basis {:project "deps.edn"
                              :aliases [:backend]})))

(defn clean [_]
  (b/delete {:path "target"}))

(defn uberjar [_]
  (clean nil)
  (b/copy-dir {:src-dirs ["src/clj" "src/cljc" "resources"]
               :target-dir class-dir})

  ;; Build frontend:
  (shadow/release :app)

  #_
  (b/compile-clj {:basis @basis
                  :ns-compile '[backend.main]
                  :class-dir class-dir})

  (b/uber {:class-dir class-dir
           :uber-file uber-file
           ;; :main 'backend.main
           :basis @basis})

  (println "Uberjar:" uber-file))
