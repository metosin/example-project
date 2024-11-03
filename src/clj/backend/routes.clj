(ns backend.routes
  (:require [backend.api.todo :as todo]
            [clojure.edn :as edn]
            [clojure.java.io :as io]
            [clojure.tools.logging :as log]
            [cognitect.transit :as transit]
            [common.schema :as schema]
            [hiccup2.core :as hiccup]
            [integrant.core :as ig]
            [muuntaja.core :as m]
            [reitit.coercion.malli :as malli.coercion]
            [reitit.ring :as ring]
            [reitit.ring.coercion :as ring.coercion]
            [reitit.ring.middleware.exception]
            [reitit.ring.middleware.muuntaja :as muuntaja]
            [reitit.swagger :as swagger]
            [reitit.swagger-ui :as swagger-ui]
            [ring.util.http-response :as resp]
            [buddy.sign.jwt :as jwt]
            buddy.sign.util
            [buddy.core.keys :as buddy-keys]))

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

(def buddy-test-route
  ["/buddy-test" {:get (fn [_]
                         (try
                           (let [id-token "eyJraWQiOiJVd3VIdERJeXVYTGs5NytmUlVHeTIzM3pNRnVCMkZPc2ErcURBMUt4OGNRPSIsImFsZyI6IlJTMjU2In0.eyJhdF9oYXNoIjoiQkNkbC16MEowRXkzb25DU1cwUndLZyIsInN1YiI6IjZlNWEwNTE4LTNkNzctNDA2NS05MjVkLWUzZGM3YTU4MjkyYiIsImNvZ25pdG86Z3JvdXBzIjpbImFkbWluIl0sImVtYWlsX3ZlcmlmaWVkIjp0cnVlLCJpc3MiOiJodHRwczpcL1wvY29nbml0by1pZHAudXMtd2VzdC0yLmFtYXpvbmF3cy5jb21cL3VzLXdlc3QtMl9XNDVGSUU0SzciLCJjb2duaXRvOnVzZXJuYW1lIjoia2ltbW9AcmVwbGlhbmNlLmNvbSIsImF1ZCI6ImVyMHFkbGwwODdncnJhY20wM2tsMmU0OGEiLCJldmVudF9pZCI6IjkzMDhiMDU5LTk2Y2EtNGQwNi05NzNmLWI5YzcwNGM3NmVmNCIsInRva2VuX3VzZSI6ImlkIiwiYXV0aF90aW1lIjoxNjY1NzMzODQyLCJleHAiOjE2NjU3NjI2NDIsImlhdCI6MTY2NTczMzg0MiwianRpIjoiZTAyZGE1ZDctMjZlZC00OGJlLWEwMDMtMzJhMGMwMzM1ZDI5IiwiZW1haWwiOiJraW1tb0ByZXBsaWFuY2UuY29tIn0.p54_PyPrK0z_g87FikyKh4pkTpMeITP27TPyeBzc8dSCbNuShR8epGpw2Ag1rcBaKS1Tcig2qicnJdHlCHuG6nkVvhjnu4I3DjH45b1Nx3neV5ii5t6YZ1GNTipzqypVd_Y9pdSxYCZYrwA442LzLSLKgzJlYD5sDB91iL0KAmcYx5JLHJ0Tpm__BILt-SHm3s3nRhbYe6isrSHeOdr19NIFOL5S5UYYS641Tlz5MQyEFHQ9tmSCMv3BudnaaImacpRQVy-uu5zseP5Db9BGjPtCoRHnpYpF8YZC9fLj2hY3AMiBBEkmfXhIh6dUnPahC8bwGaDk36OWnMVw1hxbyA"
                                 jwt-key {:alg "RS256",
                                          :e "AQAB",
                                          :kid "UwuHtDIyuXLk97+fRUGy233zMFuB2FOsa+qDA1Kx8cQ=",
                                          :kty "RSA",
                                          :n "qos6mdmypWvK74e7z9-JT5x-0ua4Sjjo5_uV-AKdkx3s7n_oAV7TfYBIYaFDfRxkTDIfWIy2yQUqUlkkxLHn7MiEF0m6mP7Wwb0tdSxgYOF9TmciPDGnxjSAgdi6E8sb0jHYnN79U0DJI-mpxB1v79MFwR6erxDROikQVtecnj-4vVhhjxc6q098HrrOIRMZIXvyEhdkbB_UTr6u8-OYAAHX2GuDgkmnX6rpUQLOqv66WxSsebWvj88UJZWqQ6KoG9gV2KvzxvwiO0gV0ePnzZ6p8oiEqhK6sxDBksXDY9vASQwW1xDcLMY_iOSJ-YpaeFiTD33Oevkyv0nRzLhe5w",
                                          :use "sig"}]
                             (jwt/unsign id-token
                                         (buddy-keys/jwk->public-key jwt-key)
                                         {:alg :rs256,
                                          :aud "er0qdll087grracm03kl2e48a",
                                          :iss "https://cognito-idp.us-west-2.amazonaws.com/us-west-2_W45FIE4K7"}))
                           (catch Exception e
                             (let [{:keys [type cause] :as data} (ex-data e)]
                               (assert (= :validation type))
                               (assert (= :exp cause))
                               (prn data))))
                         {:status 200})}])

(defn app [env]
  (ring/ring-handler
    (ring/router
      ["/api"
       buddy-test-route
       ["/todo"
        [""
         {:summary "Return a list of todo items"
          :get {:handler #'todo/get-todos
                :responses {200 {:body [:sequential schema/todo]}}}
          :post {:handler #'todo/create-todo
                 :responses {200 {:body schema/todo}}
                 :parameters {:body schema/new-todo}}}]
        ["/:id"
         {:parameters {:path [:map [:id :int]]}
          :put {:parameters {:body schema/update-todo}
                :responses {200 {:body schema/todo}}
                :handler #'todo/update-todo}
          :delete {:handler #'todo/delete-todo}}]]
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
