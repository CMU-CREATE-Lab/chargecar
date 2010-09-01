package org.chargecar.bombardier;

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
	List<Double> xposs = new ArrayList<Double>();
	List<Double> yposs = new ArrayList<Double>();
	List<Double> eles = new ArrayList<Double>();
	
	BufferedReader bufRdr = new BufferedReader(new FileReader(file));	
	String line = null;
	while((line = bufRdr.readLine()) != null){
		String[] vals = line.split(",");
		int time_s = Integer.parseInt(vals[0]);
		double xpos_m = Double.parseDouble(vals[1]);
		double ele_m = Double.parseDouble(vals[2]);
		
		xposs.add(xpos_m);
		yposs.add(0.0);
		eles.add(ele_m);
		times.add(intSTimeToCalendar(time_s));
	}	
	return BombardierTripBuilder.calculateTrip(times, xposs, yposs, eles, vehicle);
    }
    
    private static Calendar intSTimeToCalendar(int sTime) {
	int year = 2010;
	int month = 7;
	int day = 20;
	int hour = (int) (Math.floor(sTime/(60*60))) % 24;
	int minute = (int) (Math.floor(sTime/60)) % 60;
	int second = sTime % 60;
	
	Calendar calTime = Calendar.getInstance();
	calTime.setTimeInMillis(0);
	calTime.set(year, month, day, hour, minute, second);
	
	return calTime;
    }
}
