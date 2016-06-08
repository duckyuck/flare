(ns flare.expectations
  (:require [clojure.string :as str]
            [expectations]
            [flare.diff :refer [diff]]
            [flare.report :refer [report]]))

(defn install! []
  (defmethod expectations/compare-expr :expectations/strings [e a str-e str-a]
    {:type    :fail
     :raw [str-e str-a]
     :result (->> (diff e a)
                  report
                  (map #(str/replace % "\n" "\n           ")))}))
