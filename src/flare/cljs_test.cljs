(ns flare.cljs-test
  (:require [flare.report :as report]
            [flare.diff :as diff]
            [cljs.test :as ct]))

(defmethod cljs.test/report [:cljs.test/default :fail] [m]
  (ct/inc-report-counter! :fail)
  (println "\nFAIL in" (ct/testing-vars-str m))
  (when (seq (:testing-contexts (ct/get-current-env)))
    (println (ct/testing-contexts-str)))
  (when-let [message (:message m)] (println message))
  (ct/print-comparison m)
  (let [[pred & values] (second (:actual m))]
    (when (and (= pred '=) (= 2 (count values)))
      (when-let [diff (apply diff/diff values)]
        (println "\n" (clojure.string/join "\n" (report/report diff)))))))
