package chargecar.battery;

import java.util.ArrayList;
import java.util.List;

import chargecar.util.PointFeatures;

/**
 * @author Alex Styler
 * DO NOT EDIT
 */
public abstract class BatteryModel {
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
		
		protected final double MS_PER_HOUR = 3600000;
		
		public abstract void drawCurrent(double current, PointFeatures point);
		
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
		
		protected List<Double> cloneCollection(List<Double> collection){
			List<Double> clone = new ArrayList<Double>();
			for(Double d : collection){
				clone.add(new Double(d));
			}			
			return clone;			
		}
		
		protected List<Integer> clonePeriodCollection(List<Integer> collection){
			List<Integer> clone = new ArrayList<Integer>();
			for(Integer d : collection){
				clone.add(new Integer(d));
			}			
			return clone;			
		}
		
		public abstract BatteryModel createClone();
		
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
	}