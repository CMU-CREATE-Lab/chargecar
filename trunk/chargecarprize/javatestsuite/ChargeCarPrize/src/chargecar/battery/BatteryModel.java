/**
 * 
 */
package chargecar.battery;

import java.util.ArrayList;
import java.util.List;

/**
 * @author astyler
 *
 */
public abstract class BatteryModel {
		protected final List<Double> temperatureHistory = new ArrayList<Double>();
		protected final List<Double> chargeHistory = new ArrayList<Double>();
		protected final List<Double> efficiencyHistory = new ArrayList<Double>();
		protected final List<Double> currentDrawHistory = new ArrayList<Double>();
		protected final List<Double> timeHistory = new ArrayList<Double>();
		protected double temperature;
		protected double efficiency;
		protected double charge;
		protected double time;
		protected double current;
		
		protected final double MS_PER_HOUR = 3600000;
		
		protected abstract double calculateEfficiency();
		protected abstract double calculateTemperatureAfterDraw(double current, double time);
		protected abstract double calculateChargeAfterDraw(double current, double time);
		
		public abstract void drawCurrent(double current, double time);
		
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
		
		public double getTime(){
			return this.time;
		}
		
		public double getCurrent(){
			return this.current;
		}

		public List<Double> getTemperatureHistory() {
			return this.temperatureHistory;
		}
		
		public List<Double> getTimeHistory(){
			return this.timeHistory;
		}

		protected void recordHistory()
		{
			this.temperatureHistory.add(temperature);
			this.chargeHistory.add(charge);
			this.efficiencyHistory.add(efficiency);
			this.timeHistory.add(time);
			this.currentDrawHistory.add(current);			
		}
		
		protected List<Double> cloneCollection(List<Double> collection){
			List<Double> clone = new ArrayList<Double>();
			for(Double d : collection){
				clone.add(new Double(d));
			}			
			return clone;
			
		}
		
		public abstract BatteryModel createClone();
	}