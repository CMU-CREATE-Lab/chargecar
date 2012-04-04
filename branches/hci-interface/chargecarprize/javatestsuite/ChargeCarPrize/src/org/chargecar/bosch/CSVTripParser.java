package org.chargecar.bosch;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import org.chargecar.prize.util.PointFeatures;
import org.chargecar.prize.util.Vehicle;

public class CSVTripParser {
    static List<PointFeatures> parseTrips(File file, Vehicle vehicle) throws IOException{
	List<Calendar> times = new ArrayList<Calendar>();
	List<Double> vels = new ArrayList<Double>();
	
	BufferedReader bufRdr = new BufferedReader(new FileReader(file));	
	String line = null;
	while((line = bufRdr.readLine()) != null){
		String[] vals = line.split(",");
		double time_s = Double.parseDouble(vals[0]);
		double vel_ms = Double.parseDouble(vals[1]);
		
		vels.add(vel_ms);
		times.add(doubleSTimeToCalendar(time_s));
	}	
	return BoschTripBuilder.calculateTrip(times, vels, vehicle);
    }
    
    private static Calendar doubleSTimeToCalendar(double sTime) {
	int millis = (int)(sTime*1000)%1000;
	int year = 2010;
	int month = 7;
	int day = 20;
	int hour = (int) (Math.floor(sTime/(60*60))) % 24;
	int minute = (int) (Math.floor(sTime/60)) % 60;
	int second = (int)(Math.floor(sTime)) % 60;
	
	Calendar calTime = Calendar.getInstance();
	calTime.setTimeInMillis(millis);
	calTime.set(year, month, day, hour, minute, second);
	
	return calTime;
    }
}
