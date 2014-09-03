(defproject flare/flare-suite "0.3.0-SNAPSHOT"
  :description "Enlightened failure reports for clojure.test and midje"
  :url "http://github.com/andersfurseth/flare"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :plugins [[lein-modules "0.3.8"]]
  :lein-release {:deploy-via :clojars}
  :modules {:subprocess false
            :inherited   {:deploy-repositories [["releases" {:url "https://clojars.org/repo/" :creds :gpg}]]
                          :repositories        [["project:odd upstream"
                                                 "http://repository-projectodd.forge.cloudbees.com/upstream"]]
                          :aliases             {"all" ^:displace ["do" "clean," "test," "install"]
                                                "-f" ["with-profile" "+fast"]}}
            :url          "http://github.com/andersfurseth/flare"
            :scm          {:dir ".."}
            :license      {:name "Eclipse Public License"
                           :url "http://www.eclipse.org/legal/epl-v10.html"}
            :lein-release {:deploy-via :clojars}
            :versions     {;org.clojure/clojure "1.6.3"
                           flare               :version}}
  :release-tasks [["vcs" "assert-committed"]
                  ["modules" "change" "version" "leiningen.release/bump-version" "release"]
                  ["vcs" "commit"]
                  ["vcs" "tag"]
                  ["modules" "deploy"]
                  ["modules" "change" "version" "leiningen.release/bump-version"]
                  ["vcs" "commit"]
                  ["vcs" "push"]])
