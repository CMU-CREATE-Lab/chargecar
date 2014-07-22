package org.chargecar.experiments.hybridBMW.fakeTrips;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import org.chargecar.prize.util.PointFeatures;
import org.chargecar.prize.util.Trip;
import org.chargecar.prize.util.TripFeatures;
import org.chargecar.prize.util.Vehicle;

public class FakeTripMaker {
    public static Trip createTrip(String driver, int day,
	    List<Double> powersWatts, List<Integer> durationSeconds,
	    Vehicle vehicle) {
	List<PointFeatures> fakeTripPoints = new ArrayList<PointFeatures>();
	Calendar c = Calendar.getInstance();
	c.setTimeInMillis(0);
	c.set(1970, 1, day, 1, 0, 0);
	double totalPowerUsed = 0;
	int index = 0;
	for (int i = 0; i < powersWatts.size(); i++) {
	    double power = powersWatts.get(i);
	    int duration = durationSeconds.get(i);
	    for (int j = 0; j < duration; j++) {
		double gps = index;
		totalPowerUsed += power;
		fakeTripPoints.add(new PointFeatures(gps, gps, 0, 0, 0, 0, 0,
			power, totalPowerUsed, 1000, c));
		index++;
		c.add(Calendar.SECOND, 1);
	    }
	}
	
	TripFeatures fakeTripFeatures = new TripFeatures(driver, driver
		+ "197001" + day, vehicle, fakeTripPoints.get(0));
	Trip fakeTrip = new Trip(fakeTripFeatures, fakeTripPoints);
	return fakeTrip;
    }
    
    public static Trip getTrip(Vehicle vehicle, int i) {
	List<Double> powersWatts = new ArrayList<Double>();
	List<Integer> durationSeconds = new ArrayList<Integer>();
	if (i == 3) {
	    powersWatts.add(40000.0);
	    durationSeconds.add(300);
	    powersWatts.add(2000.0);
	    durationSeconds.add(600);
	}
	if (i == 4) {
	    powersWatts.add(2000.0);
	    durationSeconds.add(600);
	    powersWatts.add(40000.0);
	    durationSeconds.add(300);
	}
	return createTrip("JohnDoe", i, powersWatts, durationSeconds, vehicle);
    }
}
