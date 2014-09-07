(defproject flare "0.2.5-SNAPSHOT"
  :description "Enlightened failure reports for clojure.test"
  :url "http://github.com/andersfurseth/flare"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.6.0"]
                 [org.clojars.brenton/google-diff-match-patch "0.1"]
                 [clansi "1.0.0"]]
  :profiles {:provided {:dependencies [[midje "1.6.3"]]}
             :dev {:injections [(require 'flare.clojure-test)
                                (flare.clojure-test/install!)]}}
  :lein-release {:deploy-via :clojars})
