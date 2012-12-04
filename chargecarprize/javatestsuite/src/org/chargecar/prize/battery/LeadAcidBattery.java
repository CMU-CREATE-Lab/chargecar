package org.chargecar.prize.battery;

public class LeadAcidBattery extends SimpleBattery {
    private double puekertNumber = 1.121;
    public LeadAcidBattery(double maxCharge, double charge, double voltage) {
	super(maxCharge, charge, voltage);
	// TODO Auto-generated constructor stub
    }
    public double calculateEfficiency(double power, int periodMS) {
	double current = Math.abs(powerToCurrent(power));
	double effectiveCurrent = Math.pow(current, puekertNumber);
	if(effectiveCurrent <= 1.0) return 1;
	return current / effectiveCurrent;
    }
}
