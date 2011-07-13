#!/bin/sh
#
#Author: 	Paul Dille
#Purpose:	Move update files from temporary directory to main source directory
#
#cd /root/chargecar/trunk/car/
cp -f /root/chargecar/trunk/car/app.properties /root/chargecar/trunk/car/app_bk1.properties /root/chargecar/trunk/car/app_bk2.properties /root/chargecar/trunk/car_tmp
cp -rf /root/chargecar/trunk/car/logs/ /root/chargecar/trunk/car_tmp/logs
NOWDATE="$(date +%Y%m%d)"
mv /root/chargecar/trunk/car /root/chargecar/trunk/car_$NOWDATE/
mv /root/chargecar/trunk/car_tmp /root/chargecar/trunk/car