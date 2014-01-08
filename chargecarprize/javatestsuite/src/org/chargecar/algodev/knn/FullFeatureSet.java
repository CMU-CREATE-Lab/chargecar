package org.chargecar.algodev.knn;

import java.util.Calendar;

import org.chargecar.prize.util.PointFeatures;

public class FullFeatureSet extends KdTreeFeatureSet {
    private final int featureCount = 7;
    private final double MAXGPSDIST = 3e-4;
    private final double[] weights = new double[] { 
	    4e4, //lat 
	    4e4, //lon
	    1, //speed
	    5,  //ele
	    1, //bearing
	    1, //inst power
	    10, //total power
	    0.0001, //time
	    1, //day
	    .01 } //acceleration
    ;
    
    public int getFeatureCount() {
	return featureCount;
    }
    
    public double getValue(PointFeatures point, int splitType) {
	switch (splitType) {
	case 0:
	    return point.getLatitude();
	case 1:
	    return point.getLongitude();
	case 2:
	    return point.getSpeed();
	case 3:
	    return point.getElevation();
	case 4:
	    return point.getBearing();
	case 5:
	    return point.getPowerDemand();
	case 6:
	    return point.getTotalPowerUsed();
	case 7:
	    return (point.getTime().get(Calendar.HOUR_OF_DAY) * 60
		    + point.getTime().get(Calendar.MINUTE));
	    //gets total minutes of time.  4:30am=270minutes
	case 8:
	    return point.getTime().get(Calendar.DAY_OF_WEEK)%7;
	case 9:
	    return point.getAcceleration();
	    
	default:
	    System.err.println("Invalid feature request!");
	    return 0.0;
	}
    }
    
    public double distance(PointFeatures point1, PointFeatures point2) {
	double dist = 0;
	for (int i = 0; i < 9; i++) {
	    dist += axialDistance(point1,point2,i);
	}
	return dist;
    }
    

    public double axialDistance(PointFeatures point1, PointFeatures point2,
	    int split) {
	double dist = Math.pow(getValue(point1, split)- getValue(point2, split),2.0);
	
	if (split == 0 || split == 1)
	    dist = dist > MAXGPSDIST ? MAXGPSDIST : dist;
	else if(split == 7)
	    dist = calculateTimeDist(point1,point2);
	else if(split == 8)
	    dist = calculateDayDist(point1,point2);

	return dist * weights[split];
    }
    

    private double calculateTimeDist(PointFeatures point1, PointFeatures point2) {
	double timeDist = getValue(point1, 7) - getValue(point2, 7);
	while (timeDist > 720)
	    timeDist = timeDist - 720;
	while (timeDist < -720)
	    timeDist = timeDist + 720;
	return Math.pow(timeDist,2.0);
    }

    private double calculateGPSDist(PointFeatures point1, PointFeatures point2) {
	double gpsDist = 
	    Math.pow(getValue(point1, 0) - getValue(point2, 0),2.0) +
	    Math.pow(getValue(point1, 1) - getValue(point2, 1), 2.0);
	//gpsDist = gpsDist > 2*MAXGPSDIST ? 2*MAXGPSDIST : gpsDist;
	return gpsDist;
    }

    private double calculateDayDist(PointFeatures point1,
	    PointFeatures point2) {
	double dayDist;
	double v1 = getValue(point1,8);
	double v2 = getValue(point2,8);
	if(v1 == Calendar.SATURDAY % 7) v1 = Calendar.SATURDAY;
	if(v2 == Calendar.SATURDAY % 7) v2 = Calendar.SATURDAY;
	
	if (v1 == v2)
	    dayDist = 0;
	else if ((
		(v1 == Calendar.MONDAY)
		|| (v1 == Calendar.TUESDAY)
		|| (v1 == Calendar.WEDNESDAY)
		|| (v1 == Calendar.THURSDAY) 
		|| (v1 == Calendar.FRIDAY))
		&& (
		(v2 == Calendar.MONDAY)
		|| (v2 == Calendar.TUESDAY)
		|| (v2 == Calendar.WEDNESDAY)
		|| (v2 == Calendar.THURSDAY) 
		|| (v2 == Calendar.FRIDAY)))
	    dayDist = 1;
	else if ((
		(v1 == Calendar.SATURDAY) 
		|| (v1 == Calendar.SUNDAY))
		&& ((v2 == Calendar.SATURDAY)
		|| (v2 == Calendar.SUNDAY)))
	    dayDist = 1;
	else dayDist = 2;
	return Math.pow(dayDist,2.0);
    }

    public double getWeight(int splitType) {
	return weights[splitType];
    }
}
