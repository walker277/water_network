#!/bin/bash
javadoc -encoding UTF-8 -sourcepath ./src -cp ./src:./lib/Jama-1.0.3.jar -d doc/javadoc -version -author ./src/*.java
