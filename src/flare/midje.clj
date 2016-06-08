(ns flare.midje
  (:require [flare.diff :refer [diff]]
            [flare.report :refer [report]]
            [midje.emission.plugins.default :as default]
            [midje.emission.plugins.util :as util]
            [midje.emission.state :as state]))

(defn install! []

  (defn emit-reports
    [reports]
    (when (seq reports)
      (util/emit-one-line "")
      (doseq [report reports]
        (util/emit-one-line report))))

  (def default-fail-emissioner (:fail default/emission-map))

  (defn flare-fail-emissioner
    [failure]
    (default-fail-emissioner failure)
    (when (= (:type failure) :actual-result-did-not-match-expected-value)
      (some-> (diff (:expected-result failure) (:actual failure))
              report
              emit-reports)))

  (def emission-map (assoc default/emission-map
                      :fail flare-fail-emissioner))

  (state/install-emission-map-wildly emission-map))
