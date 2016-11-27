(defproject reagent-server-rendering "0.1.0-SNAPSHOT"
  :dependencies [[org.clojure/clojure "1.7.0"]
                 [org.clojure/clojurescript "1.7.122"]
                 [reagent "0.5.1"]
                 ;;Ring is needed to start an HTTP server
                 [ring "1.4.0"]
                 ;;provides default middleware for Ring
                 [ring/ring-defaults "0.1.5"]
                 ;;server-side HTML templating
                 [hiccup "1.0.5"]
                 ;;thread pool management
                 [aleph "0.4.0"]]

  :source-paths ["src/clj"]

  :plugins [[lein-cljsbuild "1.0.6"]
            ;;plugin for starting the HTTP server
            [lein-ring "0.9.6"]]

  ;;specifies the HTTP handler
  :ring {:handler reagent-server-rendering.handler/app}

  :clean-targets ^{:protect false} ["resources/public/js/compiled" "target" "test/js"]

  :cljsbuild {:builds [{:id "prod"
                        :source-paths ["src/cljs"]
                        :compiler {:output-to "resources/public/js/compiled/app.js"
                                   :optimizations :advanced
                                   :pretty-print false}}]})
