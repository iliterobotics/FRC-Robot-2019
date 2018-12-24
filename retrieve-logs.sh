#!/bin/bash

echo Removing locally stored logs...
rm *.csv

echo Retrieving most recent logs from robot...
scp lvuser@172.22.11.2:*.csv .

echo Cleaning up log files on robot...
ssh lvuser@172.22.11.2 "rm *.csv"