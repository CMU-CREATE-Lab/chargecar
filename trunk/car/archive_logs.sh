#!/bin/bash
#
#Author: 	Paul Dille
#Purpose:	Archive ChargeCar log files
#
current_date=$(date +"%Y%m%d")
log_path="/home/chargecar/ChargeCar/trunk/car/logs/"
cd $log_path
if [ ! -d "archived_logs" ]; then
  mkdir archived_logs
fi
output="archived_logs/""$current_date""_ChargeCar_logs.zip"
zip -r $output . -i \*.log
#tar cvzf $output --exclude='*.tar.gz' *
if [ -d "$output" ]; then
  find -type f -name "*.log" -exec rm -f {} \;
fi