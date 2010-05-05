package chargecar.visualization;

import java.util.List;

import chargecar.battery.BatteryModel;
import chargecar.capacitor.CapacitorModel;
import chargecar.util.PointFeatures;
import chargecar.util.SimulationResults;
import chargecar.util.Trip;

public class ConsoleWriter implements Visualizer {

	@Override
	public void visualizeSummary(SimulationResults simResults) {
		double integralsSum = 0.0;
		for(BatteryModel battery : simResults.getBatteries()){
			integralsSum += currentSquaredIntegral(battery);
		}
	}

	@Override
	public void visualizeTrip(Trip trip, BatteryModel battery,
			CapacitorModel capacitor) {		
		System.out.println(trip);
		System.out.println("Integral of current squared: "+currentSquaredIntegral(battery));
	}
	


	@Override
	public void visualizeTrips(SimulationResults simResults) {
		for(int i=0;i<simResults.getTrips().size();i++){
			visualizeTrip(simResults.getTrips().get(i),simResults.getBatteries().get(i),simResults.getCapacitors().get(i));
			
		}
	}
	
	private static Double currentSquaredIntegral(BatteryModel battery){
		List<Double> currents = battery.getCurrentDrawHistory();
		List<PointFeatures> points = battery.getTripHistory();
		double integral = 0;
		for(int i=0;i<currents.size();i++){
			double currentSquared = Math.pow(currents.get(i), 2);
			double timeLength = ((double)points.get(i).getPeriodMS())/1000.0;
			integral += currentSquared * timeLength;
		}		
		return integral;		
	}

}
