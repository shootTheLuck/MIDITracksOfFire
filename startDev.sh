#!/bin/bash
# rebuild and run
#ant jar && java -jar build/TracksOfFire.jar
ant clean-jar && java -jar build/TracksOfFire.jar
