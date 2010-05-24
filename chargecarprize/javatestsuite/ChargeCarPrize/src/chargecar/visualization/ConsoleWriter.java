package chargecar.visualization;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

import chargecar.battery.BatteryModel;
import chargecar.util.SimulationResults;
import chargecar.util.Trip;

/**
 * DO NOT EDIT
 * 
 * A simple visualizer that outputs simulation details to the console screen.
 * 
 * @author Alex Styler
 */
public class ConsoleWriter implements Visualizer {
	DecimalFormat d = new DecimalFormat("0.000E0");
	DecimalFormat p = new DecimalFormat("0.000%");
	@Override
	public void visualizeSummary(List<SimulationResults> results) {
		List<Double> integrals = new ArrayList<Double>();
		for (SimulationResults r : results) {
			double integral = 0.0;
			for (Double d : r.getBatteryCurrentSquaredIntegrals()) {
				integral += d;
			}
			integrals.add(integral);
		}

		double baseline = integrals.get(0);

		System.out.println("=============");
		System.out.println("Baseline, " + results.get(0).getPolicyName()
				+ ", i^2: " + d.format(baseline));
		for (int i = 1; i < results.size(); i++) {
			System.out.println(results.get(i).getPolicyName() + " i^2: "
					+ d.format(integrals.get(i)));
		}
		for (int i = 1; i < results.size(); i++) {
			double percent = integrals.get(i) / baseline;
			System.out.println(results.get(i).getPolicyName()
					+ " i^2 % of baseline: " +  p.format(percent));
		}

		System.out.println("=============");

	}

	@Override
	public void visualizeTrip(Trip trip, BatteryModel battery,
			BatteryModel capacitor) {
		System.out.println(trip);
		System.out.println("Integral of current squared: "
				+  d.format(battery.currentSquaredIntegral()));
	}

	@Override
	public void visualizeTrips(SimulationResults simResults) 
	{
		String policyName = simResults.getPolicyName();
		System.out.println("=============");
		System.out.println(policyName+ " i^2 results:");
		for(int i=0;i<simResults.getTripStrings().size();i++){
			String trip = simResults.getTripStrings().get(i);
			double currentSquaredIntegral = simResults.getBatteryCurrentSquaredIntegrals().get(i);
			System.out.println(d.format(currentSquaredIntegral) + "; "+trip);			
		}
	}
}
