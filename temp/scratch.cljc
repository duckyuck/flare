(ns scratch)

(defn diff
  [a b]
  (if-let [diff (flare/diff a b)]
    (flare/generate-reports diff)
    [(type a) (type b)]))

(defn diff-match-patch-string
  [a b]
  (let [dmp (js/diff_match_patch.)
        diff (.diff_main dmp a b)]
    diff))

