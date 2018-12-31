#!/bin/bash

psql -h $1 -p $2 -U $3 -c "DROP DATABASE IF EXISTS dictation_error_classifier"
psql -h $1 -p $2 -U $3 -c "DROP ROLE IF EXISTS dictation_error_classifier"

psql -h $1 -p $2 -U $3 -c "CREATE ROLE dictation_error_classifier PASSWORD 'password' SUPERUSER INHERIT LOGIN"
psql -h $1 -p $2 -U $3 -c "CREATE DATABASE dictation_error_classifier OWNER dictation_error_classifier"
