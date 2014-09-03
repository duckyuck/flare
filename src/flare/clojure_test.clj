(ns flare.clojure-test
  (:require [flare.core :as flare]
            [clojure.test :as ct]))

(defn install!
  ([]
     (install! {}))
  ([opts]
     (defn print-diff
       [diff]
       (println "\n" (flare/generate-reports diff)))

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

     (defn diff
       [args]
       (when (= 2 (count args))
         (let [args (if (= (:expected opts) :first)
                      (reverse args)
                      args)]
           (apply flare/diff args))))

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
                             ::difference (diff args#)}))
            result#)))))
