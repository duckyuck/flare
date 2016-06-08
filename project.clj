(defproject flare "0.3.0-SNAPSHOT"
  :description "Enlightened failure reports for clojure.test and Midje"
  :url "http://github.com/andersfurseth/flare"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}

  :min-lein-version "2.6.1"

  :dependencies [[org.clojure/clojure "1.9.0-alpha12"]
                 [org.clojure/clojurescript "1.9.229"]
                 [org.clojars.brenton/google-diff-match-patch "0.1"]
                 [cljsjs/google-diff-match-patch "20121119-1"]]

  :plugins [[lein-figwheel "0.5.4-7"]
            [lein-cljsbuild "1.1.3" :exclusions [[org.clojure/clojure]]]
            [lein-cloverage "1.0.2"]
            [lein-doo "0.1.6"]]

  :source-paths ["src"]

  :clean-targets ^{:protect false} ["resources/public/js/compiled" "target" "out"]

  :profiles {:provided {:dependencies [[midje "1.6.3"]
                                       [expectations "2.1.1"]]}
             :dev {:dependencies [[org.clojure/test.check "0.6.1"]
                                  [binaryage/devtools "0.7.2"]
                                  [figwheel-sidecar "0.5.4-7"]
                                  [com.cemerick/piggieback "0.2.1"]]
                   :source-paths ["src" "dev"]
                   :repl-options {:init (set! *print-length* 50)
                                  :nrepl-middleware [cemerick.piggieback/wrap-cljs-repl]}
                   :injections [(require 'flare.clojure-test)
                                (flare.clojure-test/install!)]}}

  :cljsbuild {:builds
              [{:id "test"
                :source-paths ["src" "test"]
                :compiler {:output-to "resources/public/js/compiled/flare-test.js"
                           :main flare.test-runner
                           :optimizations :none}}]}

  :figwheel {:css-dirs ["resources/public/css"]}

  :lein-release {:deploy-via :clojars})
