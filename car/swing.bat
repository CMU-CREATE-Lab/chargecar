@ECHO OFF
java -Djava.awt.headless=true -Duse-fake-devices=true -Djava.library.path=./code/LCDDisplay/dist -cp ./code/LCDDisplay/dist/chargecar-lcd-display.jar org.chargecar.lcddisplay.ChargeCarLCD
