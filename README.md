# Flare [![Build Status](https://travis-ci.org/andersfurseth/flare.svg?branch=master)](https://travis-ci.org/andersfurseth/flare)

Flare brings enlightened failure reports to your [clojure.test](http://richhickey.github.io/clojure/clojure.test-api.html) and [midje](https://github.com/marick/Midje) tests.

Latest version is `[flare "0.2.2"]`.

## Usage

Flare is activated by calling `flare.clojure-test/install!` (for use with `clojure.test`) or `flare.midje/install!` (for use with `midje`).

To use Flare with [Leiningen](http://leiningen.org/), merge the following with your project.clj:

```clojure
{:profiles
 {:dev
  {:injections
   [(require 'flare.clojure-test)
    (flare.clojure-test/install!)]}}}
```

To use with midje, require and invoke `install!` from the `flare.midje` namespace.

Run your tests, using your favourite test runner, and enjoy the enlightenment Flare brings to your failing tests.

## License

Copyright © 2014 Anders Furseth

Distributed under the Eclipse Public License either version 1.0 or (at
your option) any later version.
