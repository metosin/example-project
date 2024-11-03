(ns backend.main
  (:require [aero.core :as aero]
            [backend.routes]
            [clojure.tools.logging :as log]
            [cognitect.transit]
            [hikari-cp.core :as hikari-cp]
            [integrant.core :as ig]
            [next.jdbc.date-time]
            [reitit.ring.middleware.exception]
            [ring.adapter.jetty9 :as jetty]
            #_[ring.adapter.jetty :as jetty])
  (:gen-class))

(set! *warn-on-reflection* true)

(defmethod aero.core/reader 'ig/ref
  [_opts _tag value]
  (ig/ref value))

(defn system-config []
  (aero/read-config (or (System/getenv "CONFIG_EDN")
                        "config.edn")))

(defmethod ig/init-key :adapter/jetty [_ {:keys [port routes] :as jetty-opts}]
  (log/infof "Starting Jetty server on http://localhost:%s" port)
  (jetty/run-jetty routes (-> jetty-opts (dissoc :handler) (assoc :join? false))))

(defmethod ig/halt-key! :adapter/jetty [_ server]
  (log/info "Stopping Jetty")
  (.stop server))

(defmethod ig/init-key :database/pool [_ jdbc-options]
  (log/infof "Starting DB pool, JDBC config is %s" (pr-str (assoc jdbc-options :password "***")))

  ;; receive java.time.Instant from JDBC
  (next.jdbc.date-time/read-as-instant)

  (hikari-cp/make-datasource jdbc-options))

(defmethod ig/halt-key! :database/pool [_ pool]
  (log/infof "Shutting down DB pool")
  (hikari-cp/close-datasource pool))

(defn run-system [config]
  (try
    (let [initialized-system (ig/init config)]
      (.addShutdownHook (Runtime/getRuntime) (Thread. (fn []
                                                        (log/infof "Shutting down")
                                                        (ig/halt! initialized-system))))
      (log/infof "System started")
      initialized-system)
    (catch Throwable t
      (log/error t "Failed to start system"))))

(defn -main [& args]
  (run-system (system-config))
  (when (= "exit-after-start" (first args))
    (log/info "Exiting")
    (System/exit 0)))
