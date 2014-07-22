package org.chargecar.experiments.hybridBMW;


    import org.chargecar.prize.battery.BatteryModel;
    import org.chargecar.prize.util.PointFeatures;
    import org.chargecar.prize.util.PowerFlowException;
    import org.chargecar.prize.util.TripFeatures;

    public class NaivePolicyHybrid extends OptPolicyHybrid {
    protected BatteryModel modelBatt;
        private final String name = "Naive Policy";
        private final String shortName = "naivepoly";
        
        public NaivePolicyHybrid(){
            super("/",null);
    	}

        public void beginTrip(TripFeatures tripFeatures, BatteryModel batteryClone) {
            modelBatt = batteryClone;
    	}
        
        public PowerControls calculatePowerFlows(PointFeatures pf, int i) {
    	int idealEngineWatts = 0;	
    	int wattsDemanded = (int)pf.getPowerDemand();
    	
    	int periodMS = pf.getPeriodMS();
    	
    	int minBattPower = (int) modelBatt.getMinPowerDrawable(periodMS);
    	int maxBattPower = (int) modelBatt.getMaxPowerDrawable(periodMS);	
    	
    	int motorWatts = wattsDemanded-idealEngineWatts;
    	
    	motorWatts = motorWatts > maxBattPower ? maxBattPower : motorWatts;
    	motorWatts = motorWatts < minBattPower ? minBattPower : motorWatts;
    	
    	int engineWatts = wattsDemanded - motorWatts;
    	
    	try {
    	    modelBatt.drawPower(motorWatts, periodMS);
    	} catch (PowerFlowException e) {
    	    System.err.println("Battery Capacity violated in policy model");
    	}
    	

    	return new PowerControls(engineWatts, motorWatts);
        }       


        public String getName() {
    	return this.name;
        }
       
        
        public String getShortName() {
    	return this.shortName;
        }
       
    }


