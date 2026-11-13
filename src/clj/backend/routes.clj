(ns backend.routes 
  (:require [backend.api.todo :as todo]
            [clojure.edn :as edn]
            [clojure.java.io :as io]
            [clojure.tools.logging :as log]
            [cognitect.transit :as transit]
            [common.schema :as schema]
            [hiccup2.core :as hiccup]
            [integrant.core :as ig]
            ;; JSON Schema (OpenAPI) mappings for :time/* schemas
            [malli.experimental.time.json-schema]
            [malli.json-schema :as json-schema]
            [muuntaja.core :as m]
            [reitit.coercion.malli :as malli.coercion]
            [reitit.openapi :as openapi]
            [reitit.ring :as ring]
            [reitit.ring.coercion :as ring.coercion]
            [reitit.ring.middleware.exception]
            [reitit.ring.middleware.muuntaja :as muuntaja]
            [reitit.swagger-ui :as swagger-ui]
            [ring.util.http-response :as resp]))

;; malli.experimental.time.json-schema doesn't cover :time/instant.
;; Instants are serialized as ISO-8601 strings in JSON.
(defmethod json-schema/accept :time/instant [_ _ _ _] {:type "string" :format "date-time"})

(def muuntaja-instance
  (m/create (-> m/default-options
                (assoc-in [:formats "application/transit+json" :encoder-opts :handlers java.time.Instant]
                          (transit/write-handler (constantly "Instant") #(.toString %)))
                (assoc-in [:formats "application/transit+json" :decoder-opts :handlers "Instant"]
                          (transit/read-handler #(java.time.Instant/parse %))))))

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

(defn main-js-file []
  (-> (or (io/resource "public/js/manifest.edn")
          (io/file "target/dev/public/js/manifest.edn"))
      slurp
      edn/read-string
      first
      :output-name))

(defn index []
  (hiccup/html {:mode :html}
    (hiccup/raw "<!DOCTYPE html>\n")
    [:html
     {:lang "en"}
     [:head
      [:title "Example"]
      [:meta {:charset "utf-8"}]
      [:meta {:name "viewport" :content "width=device-width, initial-scale=1, shrink-to-fit=no"}]]
     [:body
      [:div#app
       [:div.loading
        [:h1 "Loading, please wait..."]]]
      [:script {:type "text/javascript" :src (str "/js/" (main-js-file))}]]]))

(defn app [env]
  (ring/ring-handler
    (ring/router
      ["/api"
       ["/todo"
        [""
         {:get {:summary "Return a list of todo items"
                :handler #'todo/get-todos
                :responses {200 {:body [:sequential schema/todo]}}}
          :post {:summary "Create a todo item"
                 :handler #'todo/create-todo
                 :responses {200 {:body schema/todo}}
                 :parameters {:body schema/new-todo}}}]
        ["/:id"
         {:parameters {:path [:map [:id :int]]}
          :put {:parameters {:body schema/update-todo}
                :responses {200 {:body schema/todo}}
                :handler #'todo/update-todo}
          :delete {:handler #'todo/delete-todo}}]]
       ["/openapi.json" {:no-doc true
                         :get {:openapi {:info {:title "Example API"
                                                :version "1.0.0"}}
                               :handler (openapi/create-openapi-handler)}}]
       ["/docs/*" {:no-doc true
                   :get (swagger-ui/create-swagger-ui-handler {:url "/api/openapi.json"})}]]
      {:data {:muuntaja muuntaja-instance
              :coercion malli.coercion/coercion
              :middleware [muuntaja/format-middleware
                           (reitit.ring.middleware.exception/create-exception-middleware {:reitit.ring.middleware.exception/default default-error-handler})
                           ring.coercion/coerce-exceptions-middleware
                           ring.coercion/coerce-request-middleware
                           ring.coercion/coerce-response-middleware
                           [wrap-database-middleware (:db env)]]}})
    ;; Default handler - handle resources (js files), index.html and 404 for API endpoints
    (ring/routes
      (ring/create-resource-handler {:path ""
                                     :root "public"})
      (ring/ring-handler
        (ring/router
          [""
           ["/api/*" {:handler (fn [_req]
                                 (resp/not-found))}]
           ;; Return index.html for any non-API routes for History API routing
           ["/*" {:get {:handler (fn [_req] (resp/ok (str (index))))}}]]
          {:conflicts nil})))))

(defmethod ig/init-key :web/routes [_ env]
  (app env))

(defmethod ig/halt-key! :web/routes [_ _routes]
  (log/info "Shutting down web routes"))
