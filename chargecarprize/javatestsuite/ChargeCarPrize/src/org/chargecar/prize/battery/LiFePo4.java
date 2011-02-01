package org.chargecar.prize.battery;

public class LiFePo4 extends SimpleBattery {
    private double puekertNumber = 1.0;
    public LiFePo4(double maxCharge, double charge, double voltage) {
	super(maxCharge, charge, voltage);
	// TODO Auto-generated constructor stub
    }
    public double calculateEfficiency(double current, int periodMS) {
	current = Math.abs(current);
	double effectiveCurrent = Math.pow(current, puekertNumber);
	if(current == 0.0) return 1;
	return current / effectiveCurrent;
    }
}
