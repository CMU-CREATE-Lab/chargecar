package org.chargecar.experiments.hybridBMW;


    import java.io.File;
import java.io.FileOutputStream;
import java.io.ObjectOutputStream;
import java.util.HashMap;
import java.util.Map;

    import org.chargecar.prize.battery.BatteryModel;
import org.chargecar.prize.policies.Policy;
import org.chargecar.prize.util.PointFeatures;
import org.chargecar.prize.util.PowerFlows;
import org.chargecar.prize.util.Trip;
import org.chargecar.prize.util.TripFeatures;

    public class MDPTrainerHybrid implements PolicyHybrid {
        private String currentDriver;
        private final String optFileFolderPath;
        private final double discountFactor = 0.99;
        private final int[] controlsSet = new int[]{0,5,10,15,20,25,30,35,40,45,50};
        private final double[] controlsCost = new double[]{0,1,2,3,4,5,6,7,8,9,10};
        private final Map<Integer,Double> costFunction = new HashMap<Integer,Double>(11);
        
        private final MDPValueGraphHybrid mmdpOpt;
        private Map<Integer, double[][]> tripMap;
        private final String shortName = "dpgtt";
        private final BatteryModel batt;
        
        public MDPTrainerHybrid(String optFileFolderPath, BatteryModel batt, int stateCount){
    		tripMap = new HashMap<Integer,double[][]>();
    		this.optFileFolderPath = optFileFolderPath+"/";
    		this.batt = batt.createClone();
    		for(int i=0;i<controlsSet.length;i++){
    		    costFunction.put(controlsSet[i], controlsCost[i]);
    		}
    		mmdpOpt = new MDPValueGraphHybrid(controlsSet, costFunction, stateCount, discountFactor, batt);
        }    

        @Override
        public String getShortName() {
    		return this.shortName;
        }
        
        public void parseTrip(Trip t){
    		updateTripMap(t);
    		System.out.println(tripMap.size());
        }
        
        private void updateTripMap(Trip trip){
    		String driver = trip.getFeatures().getDriver();
    		if(trip.getPoints().size() > 3600) return;
    		if(currentDriver == null || driver.compareTo(currentDriver) != 0){
    		    if(currentDriver != null) finishTraining();
    		    System.out.println("New driver: "+driver);	    
    		    currentDriver = driver;
    		    tripMap = new HashMap<Integer,double[][]>();
    		}
    	
    		double[][] valueGraph = mmdpOpt.getValues(trip.getPoints());
    		tripMap.put(trip.hashCode(), valueGraph);
    	}
        
        public void writeTable(){
    	System.out.println("Writing table for "+currentDriver);
    	
    	FileOutputStream fos;
    	try {
    	    
    	    File knnTableFile = new File(this.optFileFolderPath+currentDriver+".opt");
    	    knnTableFile.getParentFile().mkdirs();
    	    knnTableFile.createNewFile();
    	    fos = new FileOutputStream(knnTableFile);
    	    ObjectOutputStream oos = new ObjectOutputStream(fos);
    	    oos.writeObject(tripMap);
    	} catch (Exception e) {
    	    e.printStackTrace();
    	}
        }
        
       
        public void finishTraining()
        {
    	writeTable();
        }
        
        private String name = "Table Trainer";
        
        public void beginTrip(TripFeatures tripFeatures, BatteryModel batteryClone) {}
        
        @Override
        public PowerFlows calculatePowerFlows(PointFeatures pf) {
    	return new PowerFlows(0,0,0);
        }
        
        @Override
        public void endTrip(Trip t) {}
        
        @Override
        public String getName() {
    	return name;
        }
        
        @Override
        public void loadState() {}
        
         @Override
        public void clearState() {
    	// TODO Auto-generated method stub
    	
        }
        
    }

