(ns flare.cljs-test
  (:require [cljs.test :refer [assert-expr]]
            [flare.diff :as diff]))

(defmethod cljs.test/assert-expr '=
  [menv msg form]
  (let [args (rest form)]
    `(let [values# (list ~@args)
           result# (apply = values#)]
       (if result#
         (cljs.test/do-report
          {:type :pass
           :message ~msg,
           :expected '~form
           :actual (cons '= values#)})
         (cljs.test/do-report
          {:type :fail
           :message ~msg,
           :expected '~form
           :actual (list '~'not (cons '~'=== values#))
           ::difference (when (= 2 (count values#))
                          (apply flare.diff/diff (reverse values#)))}))
       result#)))
