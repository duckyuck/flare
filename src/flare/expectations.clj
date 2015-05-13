(ns flare.expectations
  (:require [flare.core :refer [diff generate-reports]]
            [clojure.string :as str]
            [expectations]))

(defn install!
  []

  (defmethod expectations/compare-expr :expectations/strings [e a str-e str-a]
    {:type    :fail
     :raw [str-e str-a]
     :result (->> (diff e a)
                  generate-reports
                  (map #(str/replace % "\n" "\n           ")))}))
