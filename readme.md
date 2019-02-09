
[![Build Status](https://travis-ci.org/jeremiepinard/dictation-error-classifier.svg?branch=master)](https://travis-ci.org/jeremiepinard/dictation-error-classifier)

# Purpose

Support tool for collecting dictations from students and analyzing error types.

# DB Installation instructions
```
./create.sh 127.0.0.1 5432 postgres
sbt flywayMigrate
```