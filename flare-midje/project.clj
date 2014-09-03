(defproject flare/flare-midje "0.3.0-SNAPSHOT"
  :description "Enlightened failure reports for clojure.test and midje"
  :url "http://github.com/andersfurseth/flare"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.6.0"]
                 [flare/flare-core "0.3.0-SNAPSHOT"]]
  :lein-release {:deploy-via :clojars})
