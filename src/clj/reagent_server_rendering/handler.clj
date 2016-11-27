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



