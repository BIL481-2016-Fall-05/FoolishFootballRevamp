Foolish Football
================

Java clone of [Sensible Soccer](https://en.wikipedia.org/wiki/Sensible_Soccer), with aspirations to clone SWOS.

Project Status
================

Module Opponent [![Build Status](https://travis-ci.org/dyanikoglu/FoolishFootballRevamp.svg?branch=master)](https://travis-ci.org/dyanikoglu/FoolishFootballRevamp) [![Codacy Badge](https://api.codacy.com/project/badge/Grade/89d61947f97445a989012b17514fc9f5)](https://www.codacy.com/app/dyanikoglu/FoolishFootballRevamp?utm_source=github.com&amp;utm_medium=referral&amp;utm_content=BIL481-2016-Fall-05/FoolishFootballRevamp&amp;utm_campaign=Badge_Grade)

Installing
==========

Maven is used for the build system. To compile & run the main game:

```
mvn -pl game compile
mvn -pl game exec:exec
```

To run the 3D Analysis module:

```
mvn install
mvn -pl analysis compile
mvn -pl analysis exec:exec
```

To run unit tests:

```
mvn clean verify
```

Control Layout
==========
Up,Down,Left,Right Arrows => Movement

CTRL => Steal Ball

A => Head

S => Pass

D => Shoot

Enter => Tackle

Space => Change Player

ESC => Exit

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
