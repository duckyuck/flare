# Flare [![Build Status](https://travis-ci.org/andersfurseth/flare.svg?branch=master)](https://travis-ci.org/andersfurseth/flare) [![Coverage Status](https://img.shields.io/coveralls/andersfurseth/flare.svg)](https://coveralls.io/r/andersfurseth/flare)

Flare brings enlightened failure reports to your [clojure.test](http://richhickey.github.io/clojure/clojure.test-api.html), [Midje](https://github.com/marick/Midje) and [expectations](https://github.com/jaycfields/expectations) tests.

Latest version is `[flare "0.2.9"]`.

## Usage

Flare is activated by calling `flare.clojure-test/install!` (for use with `clojure.test`), `flare.midje/install!` (for use with `Midje`) or `flare.expectations/install!` (for use with `expectations`).

To use Flare with [Leiningen](http://leiningen.org/), merge the following with your project.clj:

```clojure
{:profiles
 {:dev
  {:injections
   [(require 'flare.clojure-test)
    (flare.clojure-test/install!)]}}}
```

Run your tests, using your favourite test runner, and enjoy the enlightenment Flare brings to your failing tests.

## License

Copyright © 2015 Anders Furseth

Distributed under the Eclipse Public License either version 1.0 or (at
your option) any later version.
