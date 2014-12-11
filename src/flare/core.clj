(ns flare.core
  (:require [flare.atom :as atom]
            [flare.diff :refer [Diff] :as diff]
            [flare.map :as map]
            [flare.report]
            [flare.sequential :as sequential]
            [flare.set :as set]
            [flare.string :as string]))

(def diff diff/diff)

(def generate-reports flare.report/generate-reports)

(extend-protocol Diff
  nil
  (diff-similar [a b]
    (atom/diff-atom a b))

  java.util.Set
  (diff-similar [a b]
    (set/diff-set a b))

  java.util.Map
  (diff-similar [a b]
    (map/diff-map a b))

  java.util.List
  (diff-similar [a b]
    (sequential/diff-sequential a b))

  java.lang.Object
  (diff-similar [a b]
    (atom/diff-atom a b))

  java.lang.String
  (diff-similar [a b]
    (string/diff-string a b)))

;; For backwards compatibility only. Used by Midje 1.7.x

(def report flare.report/report)
(def flatten-keys flare.report/flatten-keys)
(def join-with-newlines flare.report/join-with-newlines)
(def generate-report-for-keyed-diff flare.report/generate-report-for-keyed-diff)
