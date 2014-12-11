(ns flare.generators
  (:require [clojure.test.check.generators :as gen]))

(defn distinct-values
  [& generators]
  (gen/such-that (comp (partial = (count generators)) count set)
                 (apply gen/tuple generators)))
