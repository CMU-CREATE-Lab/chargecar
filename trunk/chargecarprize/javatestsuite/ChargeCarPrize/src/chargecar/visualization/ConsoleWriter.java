package chargecar.visualization;

import java.util.List;

import chargecar.battery.BatteryModel;
import chargecar.capacitor.CapacitorModel;
import chargecar.util.PointFeatures;
import chargecar.util.SimulationResults;
import chargecar.util.Trip;

/**
 * @author Alex Styler
 * 
 */
public class ConsoleWriter implements Visualizer {

	@Override
	public void visualizeSummary(SimulationResults userResults, SimulationResults baselineResults) 
	{
		System.out.println("Per trip percent i^2 reduction vs. baseline:");
		double integralsSumUser = 0.0;
		double integralsSumBaseline = 0.0;
		for(int i=0; i<userResults.getBatteryCurrentSquaredIntegrals().size();i++)
		{
			double user = userResults.getBatteryCurrentSquaredIntegrals().get(i);
			double baseline = baselineResults.getBatteryCurrentSquaredIntegrals().get(i);
			
			int percentRedux = (int) Math.round(100 - 100*(user/baseline));
			
			System.out.println(""+percentRedux+"%: "+userResults.getTripStrings().get(i)+" :: "+baseline+" vs. "+user);
			
			integralsSumBaseline += baseline;
			integralsSumUser += user;
		}
		
		double percentRedux = (100 - 100*(integralsSumUser/integralsSumBaseline));

		System.out.println("=============");
		System.out.println("Total baseline i^2: "+integralsSumBaseline);
		System.out.println("Total user i^2: "+integralsSumUser);
		System.out.println("Overall reduction: "+percentRedux+"%");
		System.out.println("=============");
		
	}

	@Override
	public void visualizeTrip(Trip trip, BatteryModel battery,CapacitorModel capacitor) 
	{		
		System.out.println(trip);
		System.out.println("Integral of current squared: "+battery.currentSquaredIntegral());
	}

	@Override
	public void visualizeTrips(SimulationResults simResults) 
	{

	}
	}
