package org.chargecar.prize.util;

public class Vehicle {
    private final double mass;
    private final double carCrossArea; // honda civic 2001 si fronta area in metres sq
    private final double carDragCoeff; // honda civic 2006 sedan
    private final double rollingResCoeff; //rolling resistance coef
    
    public Vehicle(double mass, double carCrossArea, double carDragCoeff,
	    double rollingResCoeff) {
	super();
	this.mass = mass;
	this.carCrossArea = carCrossArea;
	this.carDragCoeff = carDragCoeff;
	this.rollingResCoeff = rollingResCoeff;
    }
    
    public double getMass() {
	return mass;
    }
    
    public double getCarCrossArea() {
	return carCrossArea;
    }
    
    public double getCarDragCoeff() {
	return carDragCoeff;
    }
    
    public double getRollingResCoeff() {
	return rollingResCoeff;
    }
    
}
