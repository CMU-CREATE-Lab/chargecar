package chargecar.battery;

import chargecar.util.PointFeatures;
import chargecar.util.PowerFlowException;

/**
 * DO NOT EDIT
 * 
 * Extremely simple, 100% efficient capacitor model.
 * 
 * @author Alex Styler
 */
public class SimpleCapacitor extends BatteryModel
{
    
    public SimpleCapacitor(double maxCharge, double charge)
    {
	this.maxCharge = maxCharge;
	this.charge = charge;
	this.temperature = 0.0;
	this.current = 0.0;
	this.efficiency = 1.0;
    }
    
    @Override
    public void drawCurrent(double current, PointFeatures point)
	    throws PowerFlowException
    {
	this.current = current;
	this.periodMS = point.getPeriodMS();
	// record this current as starting at the current time
	recordHistory(point);
	// after the period is up, update charge, temp, and eff.
	if (current < 0)
	{
	    this.charge = this.charge + (current / this.efficiency)
		    * (periodMS / MS_PER_HOUR);
	} else
	{
	    this.charge = this.charge + (current * this.efficiency)
		    * (periodMS / MS_PER_HOUR);
	}
	
	if (this.charge < -1E-6)
	{
	    throw new PowerFlowException("Capacitor overdrawn: " + this.charge);
	} else if (this.charge - this.maxCharge > 1E-6)
	{
	    throw new PowerFlowException("Capacitor overcharged: "
		    + this.charge);
	}
	// temp and efficiency don't update in simple model
    }
    
    @Override
    public BatteryModel createClone()
    {
	SimpleCapacitor clone = new SimpleCapacitor(this.maxCharge, this.charge);
	clone.charge = this.charge;
	clone.current = this.current;
	clone.efficiency = this.efficiency;
	clone.temperature = this.temperature;
	clone.chargeHistory.addAll(this.chargeHistory);
	clone.temperatureHistory.addAll(this.temperatureHistory);
	clone.currentDrawHistory.addAll(this.currentDrawHistory);
	clone.efficiencyHistory.addAll(this.efficiencyHistory);
	clone.periodHistory.addAll(this.periodHistory);
	
	return clone;
    }
}
