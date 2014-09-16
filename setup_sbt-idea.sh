#!/bin/bash
git clone https://github.com/jberkel/android-plugin.git&&
(cd android-plugin && sbt publish-local)&&
rm -rf android-plugin&&

git clone https://github.com/fxthomas/sbt-idea.git&&
(cd sbt-idea&&
git checkout android-support&&
cp ../.sbt-idea-build project/Build.scala&&
sbt publish-local)&&
rm -rf sbt-idea&&
sbt gen-idea
