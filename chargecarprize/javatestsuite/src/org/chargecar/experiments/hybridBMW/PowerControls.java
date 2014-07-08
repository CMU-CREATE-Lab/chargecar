package org.chargecar.experiments.hybridBMW;

public class PowerControls {
    final int engineWatts;
    final int motorWatts;
    final double cost;
  
    public PowerControls(int engineWatts, int motorWatts, double cost) {
	super();
	this.engineWatts = engineWatts;
	this.motorWatts = motorWatts;
	this.cost = cost;
    }

    public int getEngineWatts() {
        return engineWatts;
    }

    public int getMotorWatts() {
        return motorWatts;
    }

    public double getCost() {
        return cost;
    }
    
    
     
}
