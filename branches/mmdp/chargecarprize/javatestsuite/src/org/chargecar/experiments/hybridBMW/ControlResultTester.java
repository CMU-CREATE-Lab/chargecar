package org.chargecar.experiments.hybridBMW;

import org.chargecar.experiments.hybridBMW.MDPValueGraphHybrid.ControlResult;
import org.chargecar.prize.battery.BatteryModel;
import org.chargecar.prize.battery.SimpleBattery;
import org.chargecar.prize.util.Vehicle;

public class ControlResultTester {
    static Vehicle civic = new Vehicle(1200, 1.988, 0.31, 0.015);
     static double systemVoltage = 120;
    static double batteryWhr = 5000;
    //5 kWh battery
    
    /**
     * @param args
     *        
     */
    public static void main(String[] args) {
	int[] controlsSet = new int[]{0,5000,10000,15000,20000,25000,30000,35000,40000,45000,50000};
	
	BatteryModel batt = new SimpleBattery(batteryWhr, 0, systemVoltage);
	
	for(int i=0;i<controlsSet.length;i++){
	    int control = controlsSet[i];
	    for(int j=-50;j<60;j=j+25){
		ControlResult cr = MDPValueGraphHybrid.testControl(batt, j*1000, control);
		System.out.println("Con: "+control + " Draw: "+j+" Cost: "+cr.cost+ " PctRem: "+cr.pCharge);
	    }
	}

    }
}