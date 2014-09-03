# Flare [![Build Status](https://travis-ci.org/andersfurseth/flare.svg?branch=master)](https://travis-ci.org/andersfurseth/flare)

Flare brings enlightened failure reports to your [clojure.test](http://richhickey.github.io/clojure/clojure.test-api.html) and [midje](https://github.com/marick/Midje) tests.

Latest version is `0.3.0`.

## Usage with clojure.test

Add `[flare/flare-clojure-test "0.3.0"]` to your development `:dependencies` in your `project.clj`. Flare is activated by calling `flare.clojure-test/install!`.

To use Flare with [Leiningen](http://leiningen.org/), merge the following with your project.clj:

```clojure
{:profiles
 {:dev
  {:injections
   [(require 'flare.clojure-test)
    (flare.clojure-test/install!)]}}}
```

## Usage with midje

Add `[flare/flare-midje "0.3.0"]` to your development `:dependencies` in your `project.clj`. Flare is activated by calling `flare.midje/install!`.

To use Flare with [Leiningen](http://leiningen.org/), merge the following with your project.clj:

```clojure
{:profiles
 {:dev
  {:injections
   [(require 'flare.midje)
    (flare.midje/install!)]}}}
```

Run your tests, using your favourite test runner, and enjoy the enlightenment Flare brings to your failing tests.

## License

Copyright © 2014 Anders Furseth

Distributed under the Eclipse Public License either version 1.0 or (at
your option) any later version.
