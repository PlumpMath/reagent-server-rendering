(ns reagent-server-rendering.handler
  (:require [aleph.flow :as flow]
            [clojure.java.io :as io]
            [hiccup.core :refer [html]]
            [hiccup.page :refer [include-js include-css]]
            [compojure.core :refer [GET defroutes]]
            [compojure.route :refer [not-found resources]]
            [ring.middleware.defaults :refer [site-defaults wrap-defaults]])
  (:import [io.aleph.dirigiste Pools]
           [javax.script ScriptEngineManager Invocable]))

;; The create-js-engine function will setup a new instance of the engine that will run
;; the compiled Javascript produced by compiling Clojurescript
(defn- create-js-engine []
  (doto (.getEngineByName (ScriptEngineManager.) "nashorn")
    (.eval "var global = this")
    (.eval (-> "public/js/compiled/app.js"
               io/resource
               io/reader))))

(def ^:private js-engine-key "js-engine")

;; The js-engine-pool keeps a pool of running engines
(def ^:private js-engine-pool
  (flow/instrumented-pool
    {:generate (fn [_] (create-js-engine))
     :controller (Pools/utilizationController 0.9 10000 10000)}))

;; The render-page function will acquire an engine from the pool and call
;; the render-page function from the reagent-server-rendering.core Clojurescript
;; namespace. It will pass this function the page id as the argument. Once the
;; page is rendered the Javascript engine is released back into the pool
(defn- render-page [page-id]
  (let [js-engine @(flow/acquire js-engine-pool js-engine-key)]
    (try (.invokeMethod
           ^Invocable js-engine
           (.eval js-engine "reagent_server_rendering.core")
           "render_page" (object-array [page-id]))
         (finally (flow/release js-engine-pool js-engine-key js-engine)))))

;; Create a function to render the HTML page using Hiccup
;; The page function uses Hiccup to generate a static page and inject the
;; generated HTML produced by the render-page function into it.
(defn page [page-id]
  (html
    [:html
     [:head
      [:meta {:charset "utf-8"}]
      [:meta {:name "viewport"
              :content "width=device-width, initial-scale=1"}]
      (include-css "css/site.css")]
     [:body
      [:div#app
       (render-page page-id)]
      (include-js "js/compiled/app.js")
      [:script {:type "text/javascript"}
       (str "reagent_server_rendering.core.main('" page-id "');")]]]))

;; Define server routes for the application
;; We'll now define routes for / and /about pages. Each route will pass a unique
;; page id to the page function
(defroutes app-routes
           (GET "/" [] (page "home"))
           (GET "/about" [] (page "about"))
           (resources "/")
           (not-found "Not Found"))

(def app (wrap-defaults app-routes site-defaults))



