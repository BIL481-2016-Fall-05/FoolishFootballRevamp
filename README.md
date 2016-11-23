Foolish Football
================

Java clone of [Sensible Soccer](https://en.wikipedia.org/wiki/Sensible_Soccer), with aspirations to clone SWOS.

Project Status
================

Master [![Build Status](https://travis-ci.org/BIL481-2016-Fall-05/FoolishFootballRevamp.svg?branch=master)](https://travis-ci.org/BIL481-2016-Fall-05/FoolishFootballRevamp)

Installing
==========

Maven is used for the build system. To compile & run the main game:

```
mvn -pl game compile
mvn -pl game exec:exec
```

To run the 3D Analysis module:

```
mvn -pl analysis compile
mvn -pl analysis exec:exec
```

To run unit tests:

```
mvn clean verify
```

Licence
=======

Copyright (C) 2009-2013 Samuel Halliday

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with this program. If not, see http://www.gnu.org/licenses/
