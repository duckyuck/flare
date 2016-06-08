(ns flare.util)

(defn pluralize [x noun]
  (let [n (if (number? x) x (count x))]
    (if (> n 1)
      (str noun "s")
      noun)))

(defn flatten-when-single
  [coll]
  (if (= 1 (count coll))
    (first coll)
    coll))
