MiniGuava: Guava for minimalists and Android
============================================

[![Build Status](https://travis-ci.org/yprsto/miniguava.svg?branch=master)](https://travis-ci.org/ypresto/miniguava)

The MiniGuava library provides a small and decoupled alternative for
[Google Guava](https://github.com/google/guava). Original library is monolithic
and huge ([15000 methods](http://www.methodscount.com/?lib=com.google.guava%3Aguava%3A19.0),
so it is not suitable for Android development.

The Guava project contains several of Google's core libraries that we rely on
in our Java-based projects: collections, caching, primitives support,
concurrency libraries, common annotations, string processing, I/O, and so forth.

Requires JDK 1.6 or higher (as of 12.0).

Some of methods requires API level 9 in Android (e.g. newSetFromMap()).

MODULES
-------

- `net.ypresto.miniguava:miniguava-base:19.0.0-beta1`
  - Joiner
  - Objects#equal, Objects#hashCode
  - MoreObjects
  - Preconditions
- `net.ypresto.miniguava:miniguava-compare:19.0.0-beta1`
  - Ordering
  - ComparisonChain
  - PrimitiveComparison (contains Ints.compare() and etc.)
- `net.ypresto.miniguava:miniguava-collect:19.0.0-beta1`
  - Subset of Lists, Maps and Sets
- `net.ypresto.miniguava:miniguava-collect-immutables:19.0.0-beta1`
  - ImmutableXX from collect package.
  - Almost all things other than BiXX, MultiXX, SortedXX

INSTALLATION
------------

Will soon available from jcenter.
Currently you can try with below snippet.

```groovy
repositories {
    maven {
        url  "http://dl.bintray.com/ypresto/maven"
    }
}
```

POLICY
------

- Use original code as much as possible to track upstream.
- Include really useful things which cannot be replaced by other libraries.
  - Use Java 8 Stream API or backports like [Lightweight-Stream-API](https://github.com/aNNiMON/Lightweight-Stream-API).
- Decouple modules except for depending on miniguava-base.
- Remove less frequently used things (e.g. NavigableXX).
- Remove google specific codes (e.g. GWT support).
- Remove Serializable supports as it is not guaranteed.
  - Except for immutables which are designed to replace standard collection impls.

IMPORTANT WARNINGS
------------------

1. APIs marked with the `@Beta` annotation at the class or method level
are subject to change. They can be modified in any way, or even
removed, at any time. If your code is a library itself (i.e. it is
used on the CLASSPATH of users outside your own control), you should
not use beta APIs, unless you repackage them (e.g. using ProGuard).

2. Deprecated non-beta APIs will be removed two years after the
release in which they are first deprecated. You must fix your
references before this time. If you don't, any manner of breakage
could result (you are not guaranteed a compilation error).

3. Serialized forms of ALL objects are subject to change unless noted
otherwise. Do not persist these and assume they can be read by a
future version of the library.

4. Our classes are not designed to protect against a malicious caller.
You should not use them for communication between trusted and
untrusted code.

5. We unit-test and benchmark the libraries using only OpenJDK 1.7 on
Linux. Some features, especially in `com.google.common.io`, may not work
correctly in other environments.

LICENSE
-------

```
Copyright (C) The Guava Authors
Copyright (C) 2016 Yuya Tanaka

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
```
