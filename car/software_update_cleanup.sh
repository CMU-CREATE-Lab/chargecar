#!/bin/sh
#
#Author: 	Paul Dille
#Purpose:	Move update files from temporary directory to main source directory
#
cd /root/chargecar/trunk/car/
mv app.properties app_bk1.properties app_bk2.properties /root/chargecar/trunk/car_tmp
mv /root/chargecar/trunk/car/logs /root/chargecar/trunk/car_tmp/logs
rm -rf /root/chargecar/trunk/car
mv /root/chargecar/trunk/car_tmp /root/chargecar/trunk/car