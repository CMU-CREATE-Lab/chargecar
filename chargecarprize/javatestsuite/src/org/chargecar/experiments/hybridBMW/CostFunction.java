package org.chargecar.experiments.hybridBMW;

public class CostFunction {
   
    private static double m =0.00002261562713;
    private static double b = 0.02324145569;
    public static double getCost(int watts){
	double cost = 0.0;
	/*if(watts == 0) cost = 0;
	else*/ if(watts < 25000) cost = m*watts+b;
	else {
	    cost = m*25000+b;
	    cost = cost + (2*m)*(watts - 25000);
	}
	return cost;
    }
}
