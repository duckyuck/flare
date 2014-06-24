(ns flare.clojure-test
  (:require [flare.core :refer [diff report]]
            [clojure.test :as ct]))

(defn install! []

  (defn print-diff
    [diff]
    (let [reports (report diff)]
      (println "")
      (doseq [report reports]
        (println report))))

  (defmethod ct/report :fail [m]
    (ct/with-test-out
      (ct/inc-report-counter :fail)
      (println "\nFAIL in" (ct/testing-vars-str m))
      (when (seq ct/*testing-contexts*) (println (ct/testing-contexts-str)))
      (when-let [message (:message m)] (println message))
      (println "expected:" (pr-str (:expected m)))
      (println "  actual:" (pr-str (:actual m)))
      (if-let [diff (::difference m)]
        (print-diff (::difference m)))))

  (defmethod ct/assert-expr '=
    [msg form]
    (let [args (rest form)]
      `(let [args# (list ~@args)
             result# (apply = args#)]
         (if result#
           (ct/do-report {:type :pass, :message ~msg,
                          :expected '~form, :actual (cons '= args#)})
           (ct/do-report {:type :fail, :message ~msg,
                          :expected '~form, :actual (list '~'not (cons '~'=== args#))
                          ::difference (apply diff args#)}))
         result#))))
