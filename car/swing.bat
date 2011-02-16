@ECHO OFF
java -Duse-fake-devices=false -Djava.library.path=./code/LCDDisplay/dist -cp ./code/LCDDisplay/dist/chargecar-lcd-display.jar org.chargecar.lcddisplay.ChargeCarLCD
