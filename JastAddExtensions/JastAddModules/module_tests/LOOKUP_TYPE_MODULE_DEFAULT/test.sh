#!/bin/sh
../testlib/pretest.sh
../testlib/ja-modules.sh -debug -d classes -instance-module m1 *.module *.java pack/P.java m1pack/P.java ../testlib/*.module 2>&1 > out
../testlib/posttestclasses.sh
