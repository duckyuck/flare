(ns flare.core
  (:require [flare.atom :as atom]
            [flare.diff :refer [Diff compose-diffs] :as diff]
            [flare.map :as map]
            [flare.report]
            [flare.sequential :as sequential]
            [flare.set :as set]
            [flare.string :as string]))

(def diff diff/diff)

(def generate-reports flare.report/generate-reports)

(def default-diffs
  {nil              atom/diff-atom
   java.lang.Object atom/diff-atom
   java.lang.String string/diff-string
   java.util.Set    set/diff-set
   java.util.Map    (compose-diffs map/diff-values map/diff-keys)
   java.util.List   (compose-diffs sequential/diff-size sequential/diff-by-index)})

(defn install!
  ([type->diff]
     (doseq [entry type->diff]
       (apply install! entry)))
  ([type diff-fn]
     (extend type Diff {:diff-similar diff-fn})))

(install! default-diffs)

;; For backwards compatibility only. Used by Midje 1.7.x

(def report flare.report/report)
(def flatten-keys flare.report/flatten-keys)
(def join-with-newlines flare.report/join-with-newlines)
(def generate-report-for-keyed-diff flare.report/generate-report-for-keyed-diff)
