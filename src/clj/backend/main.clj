(ns backend.main
  (:require [aero.core :as aero]
            [backend.api.todo :as todo]
            [clojure.java.io :as io]
            [clojure.tools.logging :as log]
            [cognitect.transit]
            [common.schema :as schema]
            [hikari-cp.core :as hikari-cp]
            [integrant.core :as ig]
            [malli.core :as malli]
            [malli.experimental.time :as malli.time]
            [malli.registry :as malli.registry]
            [muuntaja.core :as m]
            [next.jdbc.date-time]
            [reitit.coercion.malli :as malli.coercion]
            [reitit.ring :as ring]
            [reitit.ring.coercion :as ring.coercion]
            [reitit.ring.middleware.exception]
            [reitit.ring.middleware.muuntaja :as muuntaja]
            [reitit.swagger :as swagger]
            [reitit.swagger-ui :as swagger-ui]
            [ring.adapter.jetty :as jetty]))

(malli.registry/set-default-registry!
  (malli.registry/composite-registry
    (malli/default-schemas)
    (malli.time/schemas)))

(def muuntaja-instance
  (m/create (-> m/default-options
                (assoc-in [:formats "application/transit+json" :encoder-opts :handlers java.time.Instant]
                          (cognitect.transit/write-handler (constantly "Instant") #(.toString %)))
                (assoc-in [:formats "application/transit+json" :decoder-opts :handlers "Instant"]
                          (cognitect.transit/read-handler #(java.time.Instant/parse %))))))

(defn wrap-database-middleware
  "Return a middleware that associates our database instance to the request map."
  [handler database]
  (fn
    ([request]
     (handler (assoc request :db database)))
    ([request respond raise]
     (handler (assoc request :db database)
              respond
              raise))))

(defn default-error-handler
  "Default safe handler for any exception."
  [^Exception e _]
  (prn e)
  {:status 500
   :body {:type "exception"
          :class (.getName (.getClass e))}})

(defn app [env]
  (ring/ring-handler
    (ring/router
      ["/api"
       ["/todo" {:summary "Return a list of todo items"
                 :get {:handler #'todo/get-todo
                       :responses {200 {:body [:sequential schema/todo]}}}}]
       ["/swagger.json" {:no-doc true
                         :get (swagger/create-swagger-handler)}]
       ["/docs/*" {:no-doc true
                   :get (swagger-ui/create-swagger-ui-handler {:url "/api/swagger.json"})}]]
      {:data {:muuntaja muuntaja-instance
              :coercion malli.coercion/coercion
              :middleware [muuntaja/format-middleware
                           (reitit.ring.middleware.exception/create-exception-middleware {:reitit.ring.middleware.exception/default default-error-handler})
                           ring.coercion/coerce-exceptions-middleware
                           ring.coercion/coerce-request-middleware
                           ring.coercion/coerce-response-middleware
                           [wrap-database-middleware (:db env)]]}})
    (ring/routes
      (ring/create-resource-handler {:path "/assets"
                                     :root "public/assets"})
      (fn [_req]
        {:status 200
         :body "add index.html here"}))))

(defmethod aero.core/reader 'ig/ref
  [_opts _tag value]
  (ig/ref value))

(defn system-config []
  (aero/read-config (io/resource "config.edn")))

(defmethod ig/init-key :adapter/jetty [_ {:keys [port routes] :as jetty-opts}]
  (log/infof "Starting Jetty server on http://localhost:%s" port)
  (jetty/run-jetty routes (-> jetty-opts (dissoc :handler) (assoc :join? false))))

(defmethod ig/halt-key! :adapter/jetty [_ server]
  (log/info "Stopping Jetty")
  (.stop server))

(defmethod ig/init-key :web/routes [_ env]
  (app env))

(defmethod ig/halt-key! :web/routes [_ _routes]
  (log/info "Shutting down web routes"))

(defmethod ig/init-key :database/pool [_ jdbc-options]
  (log/infof "Starting DB pool, JDBC config is %s" (pr-str (assoc jdbc-options :password "***")))

  ;; receive java.time.Instant from JDBC
  (next.jdbc.date-time/read-as-instant)

  (hikari-cp/make-datasource jdbc-options))

(defmethod ig/halt-key! :database/pool [_ pool]
  (log/infof "Shutting down DB pool")
  (hikari-cp/close-datasource pool))

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

(defn -main []
  (run-system (system-config)))
