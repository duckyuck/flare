(ns flare.cljs-test
  (:require [cljs.test :refer [assert-expr]]
            [flare.diff :as diff]))

(defmethod cljs.test/assert-expr '=
  [menv msg form]
  (let [args (rest form)]
    `(let [args# (list ~@args)
           result# (apply = args#)]
       (if result#
         (cljs.test/do-report
          {:type :pass
           :message ~msg,
           :expected '~form
           :actual (cons '= args#)})
         (cljs.test/do-report
          {:type :fail
           :message ~msg,
           :expected '~form
           :actual (list '~'not (cons '~'=== args#))
           ::difference (when (= 2 (count args#))
                          (apply flare.diff/diff args#))}))
       result#)))
