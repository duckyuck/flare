## 0.2.9

Features:

  - Support for integration with [expectations](https://github.com/jaycfields/expectations). At the time being, `flare` Only overriding how string diffs get reported in `expectations` tests.
  
## 0.2.8

Changes:

  - Longer equal parts of string diffs get elided.

## 0.2.7

Changes:

  - Changed how string diffs are reported. No more ANSI colored diffs.

## 0.2.6

Bugfixes:

  - Sequences of unequal sizes now generate a diff.

## 0.2.5

Bugfixes:

  - Swallowing all exceptions (from `diff` and `generate-reports`) to insulating users from having exceptions from flare blow up in their faces.

## 0.2.4

Features:

  - sorting diffs by natural ordering of 'key path'
  - dependency to `midje` is now declared as provided
