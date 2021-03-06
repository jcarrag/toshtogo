(defproject savagematt.toshtogo/server "0.7.1-SNAPSHOT"

  :description "An asynchronous job manager"

  :dependencies [[org.clojure/clojure "1.8.0"]
                 [savagematt.toshtogo/client "0.7.1-SNAPSHOT"]

                 [org.clojure/clojurescript "1.7.48"]
                 [org.clojure/core.async "0.1.346.0-17112a-alpha"]
                 [org.omcljs/om "0.9.0"]
                 [cljsjs/d3 "3.5.5-3"]
                 [cljs-ajax "0.3.10"]
                 [secretary "1.2.3"]
                 [com.andrewmcveigh/cljs-time "0.3.14"]
                 [com.cemerick/url "0.1.1"]
                 [compojure "1.4.0"]
                 [ring-mock "0.1.5"]
                 [ring/ring-json "0.2.0"]

                 [com.dbdeploy/dbdeploy-core "3.0M3"]
                 [org.clojure/java.jdbc "0.3.2"]
                 [postgresql/postgresql "8.4-702.jdbc4"]
                 [honeysql "0.6.1"]

                 [savagematt/hermit "0.7"]]

  :source-paths ["src/clj"]

  :main toshtogo.server.core

  :ring {:handler toshtogo.server.core/dev-app-instance :reload-paths ["src"]}

  :profiles {:uberjar  {:aot   :all
                        :hooks [leiningen.cljsbuild]}
             :dev {:dependencies [[javax.servlet/servlet-api "2.5"]
                                  [midje "1.7.0"]
                                  [http-kit.fake "0.2.1"]
                                  [figwheel-sidecar "0.4.0"]]

                   :plugins      [[lein-midje "3.1.0"]
                                  [lein-ring "0.8.8"]
                                  [lein-cljsbuild "1.0.5"]
                                  [lein-figwheel "0.4.1"]
                                  [lein-set-version "0.4.1"]
                                  [midje-junit-formatter "0.1.0-SNAPSHOT"]]}}

  :clean-targets ^{:protect false} ["resources/public/js/compiled" "target"]
  :cljsbuild {:builds [{:id "min"
                        :source-paths ["src/cljs"]
                        :compiler {:output-to "resources/public/js/compiled/toshtogo.js"
                                   :foreign-libs  [{:file     "resources/public/js/toastr.min.js"
                                                    :provides ["toastr"]}]
                                   :externs       ["externs.js"]
                                   :main toshtogo.core
                                   :optimizations :advanced
                                   :pretty-print false}
                        :jar true}

                       {:id "dev"
                        :source-paths ["src/cljs"]

                        :figwheel {:on-jsload "toshtogo.core/on-js-reload"}

                        :compiler {:main toshtogo.core
                                   :asset-path "js/compiled/out"
                                   :output-to "resources/public/js/compiled/toshtogo.js"
                                   :output-dir "resources/public/js/compiled/out"
                                   :foreign-libs         [{:file     "resources/public/js/toastr.min.js"
                                                           :provides ["toastr"]}]
                                   :optimizations :none


                                   :source-map true
                                   :source-map-timestamp true
                                   :cache-analysis true}}]}
  :figwheel {:css-dirs ["resources/public/css"]
             :nrepl-port 7888})
