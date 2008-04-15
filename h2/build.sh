#!/bin/sh
if [ -z "$JAVA_HOME" ] ; then
  echo "Error: JAVA_HOME is not defined."
fi
if [ ! -f "bin/org/h2/build/Build.class" ] ; then
  if [ ! -d "bin" ] ; then
    mkdir bin
  fi
  javac -sourcepath src/tools -d bin src/tools/org/h2/build/*.java
fi
java -cp "bin:$JAVA_HOME/lib/tools.jar:target" org.h2.build.Build $@
