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
		protected final List<PointFeatures> tripHistory = new ArrayList<PointFeatures>();
		protected double temperature;
		protected double efficiency;
		protected double charge;
		protected double current;
		
		protected final double MS_PER_HOUR = 3600000;
		
		public abstract void drawCurrent(double current, PointFeatures point);
		
		public double getCharge() {
			return this.charge;
		}
		
		public List<Double> getChargeHistory() {
			return this.chargeHistory;
		}

		public List<Double> getCurrentDrawHistory() {
			return this.currentDrawHistory;
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

		public List<Double> getTemperatureHistory() {
			return this.temperatureHistory;
		}
		
		public List<PointFeatures> getTripHistory(){
			return this.tripHistory;
		}

		protected void recordHistory(PointFeatures point)
		{
			this.temperatureHistory.add(temperature);
			this.chargeHistory.add(charge);
			this.efficiencyHistory.add(efficiency);
			this.tripHistory.add(point);
			this.currentDrawHistory.add(current);			
		}
		
		protected List<Double> cloneCollection(List<Double> collection){
			List<Double> clone = new ArrayList<Double>();
			for(Double d : collection){
				clone.add(new Double(d));
			}			
			return clone;			
		}
		
		protected List<PointFeatures> cloneTripCollection(List<PointFeatures> collection){
			List<PointFeatures> clone = new ArrayList<PointFeatures>();
			for(PointFeatures d : collection){
				clone.add(d.clone());
			}			
			return clone;			
		}
		
		public abstract BatteryModel createClone();
	}