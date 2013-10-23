package org.chargecar.experiments.thermal;

import org.chargecar.prize.util.PowerFlowException;

    /**
     * DO NOT EDIT
     * 
    * 
     * @author Alex Styler
     * 
     */
	public class ThermalBattery {
        
	    protected double[][][] dynamics;
	    protected double temp;
	    
	    private final double[] temps;
	    private final double[] powers;
	    private final double[] massFlows;
	    
        public ThermalBattery(double temp, double[] temps, double[] powers, double[] massFlows, double[][][] dyn) {
    		this.temp = temp;
    		this.dynamics = dyn;
    		this.temps = temps;
    		this.powers = powers;
    		this.massFlows = massFlows;        
    		}
        
        public ThermalBattery createClone() {
            	final ThermalBattery clone = new ThermalBattery(this.temp, temps, powers, massFlows, this.dynamics);
    		return clone;
        }

        public double calculateTemperature(double current, double massFlow) {
    		double tempInit = this.temp;
    		double powerInit = current * 1.0;
    		
    		int mfIndex = -1;
    		for(int i=0;i < massFlows.length; i++){
    		    if(Math.abs(massFlow - massFlows[i]) < 0.0001){
    			mfIndex = i;
    			break;
    		    }
    		}
    		
    		int tempIndexLow = temps.length - 1;
    		int tempIndexHigh = temps.length - 1;
    		for(int i=0; i < temps.length; i ++){
    		    if(tempInit < temps[i]){
    			tempIndexLow = i-1;
    			tempIndexHigh = i;
    			break;
    		    }
    		}
    		if(tempIndexLow < 0){
    		    tempIndexHigh = tempIndexLow = 0;  			  
    		}
    		
    		int pIndexLow = powers.length -1;
    		int pIndexHigh = powers.length -1 ;
    		for(int i=0; i < powers.length; i ++){
    		    if(powerInit < powers[i]){
    			pIndexLow = i-1;
    			pIndexHigh = i;
    			break;
    		    }
    		}
    		if(pIndexLow < 0){
    		    pIndexHigh = pIndexLow = 0;    			  
    		}
    		
    		double tempScale = Math.abs(temps[tempIndexHigh] - temps[tempIndexLow]);
    		double powerScale = Math.abs(powers[pIndexHigh] - powers[pIndexLow]);
    		
    		double f1,f2,interpV;
    		if(tempIndexHigh == tempIndexLow){
    		    f1 = this.dynamics[tempIndexLow][pIndexLow][mfIndex];
    		    f2 = this.dynamics[tempIndexLow][pIndexHigh][mfIndex];
    		}
    		else{
    		    f1 = ((temps[tempIndexHigh] - tempInit)/tempScale)*this.dynamics[tempIndexLow][pIndexLow][mfIndex] + ((tempInit - temps[tempIndexLow])/tempScale)*this.dynamics[tempIndexHigh][pIndexLow][mfIndex];
    		    f2 = ((temps[tempIndexHigh] - tempInit)/tempScale)*this.dynamics[tempIndexLow][pIndexHigh][mfIndex] + ((tempInit - temps[tempIndexLow])/tempScale)*this.dynamics[tempIndexHigh][pIndexHigh][mfIndex];
    		}
    		if(pIndexHigh == pIndexLow){
    		    interpV = f1;
    		}
    		else{
    		    interpV = ((powers[pIndexHigh] - powerInit)/powerScale)*f1 + ((powerInit - powers[pIndexLow])/powerScale)*f2;    
    		}
//    		if(powerInit < 1 && tempInit > 34){
//    		System.out.println("Battery Temp: "+tempInit+" between ("+temps[tempIndexLow]+", "+temps[tempIndexHigh]+")");
//    		System.out.println("Power: "+powerInit+" between ("+powers[pIndexLow]+", "+powers[pIndexHigh]+")");
//    		System.out.println("Temp. scale: " + tempScale+ "  Power scale: "+ powerScale);
//    		System.out.println("Mass Flow: "+massFlow);
//    		System.out.println("Resulting interpolated temp: "+interpV);
//    		}
    		return interpV;
        }
    
        public void drawPower(double power, double massFlow){ 
		double ttemp = calculateTemperature(power, massFlow);
		this.temp = ttemp;
	}
    }
    

