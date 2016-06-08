(ns flare.atom)

(defn report-atom-diff [{:keys [a b]} & rest]
  [(str "expected " (pr-str a) ", was " (pr-str b))])

(defn diff-atom
  [a b _]
  [{:type :atom
    :a a
    :b b}])
