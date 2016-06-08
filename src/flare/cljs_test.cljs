(ns flare.cljs-test
  (:require [flare.report :as report]
            [flare.diff :as diff]
            [cljs.test :as ct])
  (:require-macros [flare.cljs-test]))

(defn report
  [diff]
  (println "")
  (let [the-report (report/report diff)]
    (println (clojure.string/join "\n" the-report))))

(defmethod cljs.test/report [:cljs.test/default :fail] [m]
  (ct/inc-report-counter! :fail)
  (println "\nFAIL in" (ct/testing-vars-str m))
  (when (seq (:testing-contexts (ct/get-current-env)))
    (println (ct/testing-contexts-str)))
  (when-let [message (:message m)] (println message))
  (ct/print-comparison m)
  (when-let [diff (::difference m)]
    (report diff)))
