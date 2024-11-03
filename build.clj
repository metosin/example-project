(ns build
  (:require [babashka.process :as p]
            [babashka.fs :as fs]
            babashka.process.pprint
            [clojure.tools.build.api :as b]
            [shadow.cljs.devtools.api :as shadow]))

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

  (b/compile-clj {:basis @basis
                  :class-dir class-dir
                  :compile-opts {:direct-linking true}})

  (b/uber {:class-dir class-dir
           :uber-file uber-file
           :main 'backend.main
           :basis @basis})

  (println "Uberjar:" uber-file))

(defn native-image [_]
  (println "Compiling GraalVM feature classes")
  (b/javac {:src-dirs ["src/java"]
            :class-dir "target/native-image-configuration/classes"
            :basis @basis
            :javac-opts ["--add-exports" "org.graalvm.nativeimage/org.graalvm.nativeimage.impl=ALL-UNNAMED"
                         "-Xlint:deprecation"]})

  ;; Copy reachability metadata into classpath
  (doseq [path (fs/list-dir "graalvm-reachability-metadata/metadata")]
    (fs/copy-tree path "target/native-image-configuration/META-INF/native-image"))

  (println "Building native image")
  (p/shell "native-image"

           ;; Clojure namespaces
           "--features=clj_easy.graal_build_time.InitClojureClasses"

           ;; Buddy support
           "--add-exports" "org.graalvm.nativeimage/org.graalvm.nativeimage.impl=ALL-UNNAMED"
           "--features=graalvm.features.BouncyCastleFeature"

           ;; Logback related classes
           "--initialize-at-build-time=ch.qos.logback"
           "--initialize-at-build-time=ch.qos.logback.classic.Logger"
           "--initialize-at-build-time=org.xml.sax"

           ;; Don't allow to fall back to launching a VM
           "--no-fallback"

           ;; To make shutdown hooks work
           "--install-exit-handlers"

           #_"--initialize-at-run-time=buddy.core.bytes__init"
           #_"--initialize-at-run-time=clojure.math__init"

           "-H:+UnlockExperimentalVMOptions"
           "-H:IncludeResources=swagger-ui/.*" ;; TODO: Should create META-INF/native-image/metosin/ring-swagger-ui/native-image.properties

           "-H:+PrintClassInitialization"

           "-cp" (str
                  ;; From training run with native-image-agent
                  "target/native-image-configuration"
                  ;; Compiled GraalVM feature classes, buddy support
                  ":target/native-image-configuration/classes")

           "--native-image-info"

           "-jar" "target/app.jar"
           "-o" "target/app")
  (println "native image created: target/app"))
