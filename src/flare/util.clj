(ns flare.util)

(defprotocol Pluralizable
  (pluralize [this noun]))

(extend-protocol Pluralizable
  java.util.Collection
  (pluralize [this noun]
    (pluralize (count this) noun))

  java.lang.Integer
  (pluralize [this noun]
    (pluralize (long this) noun))

  java.lang.Long
  (pluralize [this noun]
    (if (> this 1)
      (str noun "s")
      noun)))

(defn flatten-when-single
  [coll]
  (if (= 1 (count coll))
    (first coll)
    coll))
