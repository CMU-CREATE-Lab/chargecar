package org.chargecar.experiments.hybridBMW;

public class PowerControls {
    final int engineWatts;
    final int motorWatts;
  
    public PowerControls(int engineWatts, int motorWatts) {
	super();
	this.engineWatts = engineWatts;
	this.motorWatts = motorWatts;
    }

    public int getEngineWatts() {
        return engineWatts;
    }

    public int getMotorWatts() {
        return motorWatts;
    }     
}
