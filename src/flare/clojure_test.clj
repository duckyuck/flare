(ns flare.clojure-test
  (:require [clojure.test :as ct]
            [flare.diff :as diff]
            [flare.report :as report]))

(defn report
  [diff]
  (println (clojure.string/join "\n" (report/report diff))))

(defn diff
  [args opts]
  (when (= 2 (count args))
    (let [args (if (= (:expected opts) :first)
                 args
                 (reverse args))]
      (apply diff/diff args))))

(defn install!
  ([]
   (install! {}))
  ([opts]
   (defmethod ct/report :fail [m]
     (ct/with-test-out
       (ct/inc-report-counter :fail)
       (println "\nFAIL in" (ct/testing-vars-str m))
       (when (seq ct/*testing-contexts*) (println (ct/testing-contexts-str)))
       (when-let [message (:message m)] (println message))
       (println "expected:" (pr-str (:expected m)))
       (println "  actual:" (pr-str (:actual m)))
       (when-let [diff (::difference m)]
         (report diff))))

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
                           ::difference (diff args# ~opts)}))
          result#)))))
