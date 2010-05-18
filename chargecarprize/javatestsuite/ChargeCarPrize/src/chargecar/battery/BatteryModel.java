package chargecar.battery;

import java.util.ArrayList;
import java.util.List;

import chargecar.util.PointFeatures;
import chargecar.util.PowerFlowException;
import chargecar.util.PowerFlows;

/**
 * @author Alex Styler
 * DO NOT EDIT
 */
public abstract class BatteryModel {
		public static double MS_PER_HOUR = 3600000;	
		protected final List<Double> temperatureHistory = new ArrayList<Double>();
		protected final List<Double> chargeHistory = new ArrayList<Double>();
		protected final List<Double> efficiencyHistory = new ArrayList<Double>();
		protected final List<Double> currentDrawHistory = new ArrayList<Double>();
		protected final List<Integer> periodHistory = new ArrayList<Integer>();
		protected double temperature;
		protected double efficiency;
		protected double charge;
		protected double current;
		protected int periodMS;
		protected double maxCharge;

		public abstract void drawCurrent(double current, PointFeatures point) throws PowerFlowException;		
		public abstract BatteryModel createClone();
		
		public double getMaxCharge(){
			return this.maxCharge;
		}
		
		public double getCharge() {
			return this.charge;
		}
		
		public double getEfficiency() {
			return this.efficiency;
		}

		public List<Double> getEfficiencyHistory() {
			return this.efficiencyHistory;
		}

		public double getTemperature() {
			return this.temperature;
		}
		
		public double getCurrent(){
			return this.current;
		}
		
		public int getPeriodMS(){
			return this.periodMS;
		}

		public List<Double> getTemperatureHistory()
		{
			return this.temperatureHistory;
		}
		
		public List<Double> getChargeHistory()
		{
			return this.chargeHistory;
		}

		public List<Double> getCurrentDrawHistory()
		{
			return this.currentDrawHistory;
		}
		
		public List<Integer> getPeriodHistory()
		{
			return this.periodHistory;
		}

		protected void recordHistory(PointFeatures point)
		{
			this.temperatureHistory.add(temperature);
			this.chargeHistory.add(charge);
			this.efficiencyHistory.add(efficiency);
			this.periodHistory.add(periodMS);
			this.currentDrawHistory.add(current);			
		}
		
		public Double currentSquaredIntegral(){
			List<Double> currents = this.getCurrentDrawHistory();
			List<Integer> periods = this.getPeriodHistory();
			double integral = 0;
			for(int i=0;i<currents.size();i++){
				double currentSquared = Math.pow(currents.get(i), 2);
				double timeLength = ((double)periods.get(i))/1000.0;
				integral += currentSquared * timeLength;
			}		
			return integral;		
		}
		
		public double getMaxCurrent(int periodMS) {
			return (this.maxCharge - this.charge) / (periodMS / MS_PER_HOUR);
			//100% efficient, max current is the maximum positive current that
			//would fill the capacitor to maximum over the given period
		}

		public double getMinCurrent(int periodMS) {
			return (-1.0) * this.charge / (periodMS / MS_PER_HOUR);
			//100% efficient, min current is maximum negative current that 
			//would empty the current charge over the given period
		}
		
		public boolean check(PowerFlows pf, int periodMS) {
			double current = pf.getCapacitorToMotor() - pf.getBatteryToCapacitor();
			return  current <= getMaxCurrent(periodMS) && current >= getMinCurrent(periodMS);		
		}
	}