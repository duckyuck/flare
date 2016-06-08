(ns flare.test-runner
  (:require [doo.runner :refer-macros [doo-tests]]
            [flare.cljs-test]
            [flare.string-test]
            [flare.indexed-test]
            [flare.atom-test]
            [flare.map-test]
            [flare.sequential-test]
            [flare.set-test]))

(doo-tests
 'flare.string-test
 'flare.indexed-test
 'flare.atom-test
 'flare.map-test
 'flare.sequential-test
 'flare.set-test)
