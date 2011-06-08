package org.chargecar.algodev.knn;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import org.chargecar.prize.util.PointFeatures;

public class FullFeatureSet extends KdTreeFeatureSet {
    private final int featureCount = 10;
    private final double MAXGPSDIST = 1e-5;
    private final double[] weights = new double[] { 2000, 2000, 1, 5, 1, 1, 10,
	    0, 1 / 240, 1 };
    
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
	    return point.getAcceleration();
	case 8:
	    return point.getTime().get(Calendar.HOUR_OF_DAY) * 60
		    + point.getTime().get(Calendar.MINUTE);
	case 9:
	    return point.getTime().get(Calendar.DAY_OF_WEEK);
	    
	default:
	    System.out.println("Danger!");
	    return 0.0;
	}
    }
    
    public double distance(PointFeatures point1, PointFeatures point2) {
	double gpsDist = calculateGPSDist(point1, point2);
	double dist = gpsDist * weights[0];
	
	for (int i = 2; i < featureCount - 2; i++) {
	    double temp = getValue(point1, i) - getValue(point2, i);
	    dist += temp * temp * weights[i];
	}
	
	double timeDist = calculateTimeDist(point1, point2);
	dist += timeDist * weights[8];
	
	double dayDist = calculateDayDist(point1, point2);	
	dist += dayDist * weights[9];
	
	return dist;
    }

    private double calculateTimeDist(PointFeatures point1, PointFeatures point2) {
	double timeDist = getValue(point1, 8) - getValue(point2, 8);
	while (timeDist > 720)
	    timeDist = timeDist - 1440;
	while (timeDist < -720)
	    timeDist = timeDist + 1440;
	return timeDist;
    }

    private double calculateGPSDist(PointFeatures point1, PointFeatures point2) {
	double gpsDist = Math.pow(getValue(point1, 0) - getValue(point2, 0),
		2.0) + Math.pow(getValue(point1, 1) - getValue(point2, 1), 2.0);
	gpsDist = gpsDist > MAXGPSDIST ? MAXGPSDIST : gpsDist;
	return gpsDist;
    }

    private double calculateDayDist(PointFeatures point1,
	    PointFeatures point2) {
	double dayDist;
	if (getValue(point1, 9) == getValue(point2, 9))
	    dayDist = 0;
	else if ((
		(getValue(point1, 9) == Calendar.MONDAY)
		|| (getValue(point1, 9) == Calendar.TUESDAY)
		|| (getValue(point1, 9) == Calendar.WEDNESDAY)
		|| (getValue(point1, 9) == Calendar.THURSDAY) 
		|| (getValue(point1, 9) == Calendar.FRIDAY))
		&& (
		(getValue(point2, 9) == Calendar.MONDAY)
		|| (getValue(point2, 9) == Calendar.TUESDAY)
		|| (getValue(point2, 9) == Calendar.WEDNESDAY)
		|| (getValue(point2, 9) == Calendar.THURSDAY) 
		|| (getValue(point2, 9) == Calendar.FRIDAY)))
	    dayDist = 1;
	else if ((
		(getValue(point1, 9) == Calendar.SATURDAY) 
		|| (getValue(point1, 9) == Calendar.SUNDAY))
		&& ((getValue(point2, 9) == Calendar.SATURDAY)
		|| (getValue(point2, 9) == Calendar.SUNDAY)))
	    dayDist = 1;
	else dayDist = 2;
	return dayDist;
    }
    
    public double axialDistance(PointFeatures point1, PointFeatures point2,
	    int split) {
	double dist = Math.pow(getValue(point1, split)
		- getValue(point2, split), 2.0);
	
	if (split == 0 || split == 1)
	    dist = calculateGPSDist(point1,point2);
	else if(split == 8)
	    dist = calculateTimeDist(point1,point2);
	else if(split == 9)
	    dist = calculateDayDist(point1,point2);
	
	return dist * weights[split];
    }
    
    public List<Double> estimate(PointFeatures pf, List<KnnPoint> neighbors,
	    List<Double> powers, int lookahead) {
	List<Double> powerSums = new ArrayList<Double>();
	List<Double> pointScales = new ArrayList<Double>();
	for (int i = 0; i < lookahead; i++) {
	    powerSums.add(0.0);
	    pointScales.add(0.0);
	}
	for (int i = 0; i < neighbors.size(); i++) {
	    double dist = distance(pf, neighbors.get(i).getFeatures());
	    double distScaler = 1.0 / (dist + 1e-9);
	    int powerInd = neighbors.get(i).getGroundTruthIndex();
	    for (int j = 0; j < lookahead; j++) {
		Double powerD = powers.get(powerInd + j);
		if (powerD == null) {
		    break;
		}
		powerSums.set(j, powerSums.get(j) + powerD * distScaler);
		pointScales.set(j, pointScales.get(j) + distScaler);
	    }
	}
	for (int i = 0; i < lookahead; i++) {
	    if (pointScales.get(i) == 0)
		powerSums.set(i, 0.0);
	    else powerSums.set(i, powerSums.get(i) / pointScales.get(i));
	}
	return powerSums;
    }
    
    public double getWeight(int splitType) {
	return weights[splitType];
    }
}
