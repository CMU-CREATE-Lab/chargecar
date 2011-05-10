#!/bin/sh
cd /home/chargecar/ChargeCar/trunk/car/
sudo mv app.properties app_bk1.properties app_bk2.properties /home/chargecar/ChargeCar/trunk/car_tmp
sudo mv /home/chargecar/ChargeCar/trunk/car/logs /home/chargecar/ChargeCar/trunk/car_tmp/logs 
sudo rm -rf /home/chargecar/ChargeCar/trunk/car 
sudo mv /home/chargecar/ChargeCar/trunk/car_tmp /home/chargecar/ChargeCar/trunk/car
