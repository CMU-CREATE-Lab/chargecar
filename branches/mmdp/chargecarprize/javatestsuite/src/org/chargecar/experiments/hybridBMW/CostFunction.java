package org.chargecar.experiments.hybridBMW;

public class CostFunction {
   
    private static double m =0.00002261562713;
    private static double b = 0.02324145569;
    public static double getCost(int watts){
	return m*watts+b;	
    }
}
