(ns flare.core
  (:require [flare.atom :as atom]
            [flare.diff :refer [Diff] :as diff]
            [flare.map :as map]
            [flare.report :as report]
            [flare.sequential :as sequential]
            [flare.set :as set]
            [flare.string :as string]))

(def diff diff/diff)

(def generate-reports report/generate-reports)

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
