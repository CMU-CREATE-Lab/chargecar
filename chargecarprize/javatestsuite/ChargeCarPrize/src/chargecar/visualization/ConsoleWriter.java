package chargecar.visualization;

import java.util.ArrayList;
import java.util.List;

import chargecar.battery.BatteryModel;
import chargecar.util.SimulationResults;
import chargecar.util.Trip;

/**
 * @author Alex Styler
 * 
 */
public class ConsoleWriter implements Visualizer {

	@Override
	public void visualizeSummary(List<SimulationResults> results) 
	{
		List<Double> integrals = new ArrayList<Double>();
		for(SimulationResults r:results){
			double integral = 0.0;
			for(Double d : r.getBatteryCurrentSquaredIntegrals()){
				integral += d;
			}
			integrals.add(integral);
		}
		
		double baseline = integrals.get(0);

		System.out.println("=============");
		System.out.println("Baseline, "+results.get(0).getPolicyName()+", i^2: "+baseline);
		for(int i=1;i<results.size();i++){
			System.out.println(results.get(i).getPolicyName()+ " i^2: "+integrals.get(i));	
		}
		for(int i=1;i<results.size();i++){
			double percent = (100*(integrals.get(i)/baseline));
			System.out.println(results.get(i).getPolicyName()+ " i^2 % of baseline: "+percent);
		}
		
		System.out.println("=============");
		
	}

	@Override
	public void visualizeTrip(Trip trip, BatteryModel battery, BatteryModel capacitor) 
	{		
		//int percentRedux = (int) Math.round(100 - 100*(user/baseline));			
		//System.out.println(""+percentRedux+"%: "+userResults.getTripStrings().get(i)+" :: "+baseline+" vs. "+user);	
		System.out.println(trip);
		System.out.println("Integral of current squared: "+battery.currentSquaredIntegral());
	}

	@Override
	public void visualizeTrips(SimulationResults simResults) 
	{

	}
	}
