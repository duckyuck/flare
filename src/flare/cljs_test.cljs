(ns flare.cljs-test
  (:require [flare.report :as report]
            [flare.diff :as diff]
            [cljs.test :as ct]))

(defn render-diff [m]
  (try
    (let [[pred & values] (second (:actual m))]
      (when (and (= pred '=) (= 2 (count values)))
        (when-let [diff (apply diff/diff* values)]
          (println "\n" (clojure.string/join "\n" (report/report* diff))))))
    (catch js/Error e
      (println "*** Oh noes! Flare threw an exception diffing the following values:")
      (println values)
      (println "*** Exception thrown is:" e))))

(defmethod cljs.test/report [:cljs.test/default :fail] [m]
  (ct/inc-report-counter! :fail)
  (println "\nFAIL in" (ct/testing-vars-str m))
  (when (seq (:testing-contexts (ct/get-current-env)))
    (println (ct/testing-contexts-str)))
  (when-let [message (:message m)] (println message))
  (ct/print-comparison m)
  (render-diff m))
