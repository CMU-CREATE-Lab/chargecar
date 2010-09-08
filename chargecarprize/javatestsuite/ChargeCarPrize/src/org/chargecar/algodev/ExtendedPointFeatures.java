package org.chargecar.algodev;

import java.util.Calendar;

import org.chargecar.prize.util.PointFeatures;

public class ExtendedPointFeatures extends PointFeatures {

    private final double speedHistVar;
    private final double accelHistVar;
    
    public ExtendedPointFeatures(double latitude, double longitude,
	    double elevation, double planarDist, double acceleration,
	    double speed, double powerDemand, double speedHistVar, double accelHistVar, int periodMS, Calendar time) {
	super(latitude, longitude, elevation, planarDist, acceleration, speed,
		powerDemand, periodMS, time);
	this.speedHistVar = speedHistVar;
	this.accelHistVar = accelHistVar;
    }
    
    public ExtendedPointFeatures(PointFeatures pf, double speedHistVar, double accelHistVar) {
	super(pf.getLatitude(), pf.getLongitude(), pf.getElevation(), pf.getPlanarDist(), pf.getAcceleration(), pf.getSpeed(), pf.getPowerDemand(), pf.getPeriodMS(), pf.getTime());
	this.speedHistVar = speedHistVar;
	this.accelHistVar = accelHistVar;
    }
    
    public double getSpeedHistVar() {
        return speedHistVar;
    }

    public double getAccelHistVar() {
        return accelHistVar;
    }
    
}
