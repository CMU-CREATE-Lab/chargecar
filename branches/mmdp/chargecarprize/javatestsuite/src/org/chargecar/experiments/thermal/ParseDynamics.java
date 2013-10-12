package org.chargecar.experiments.thermal;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.ObjectOutputStream;

public class ParseDynamics {
    //static double[] temps = new double[]{30,31,32,33,34,35,36,37,38,39,40,41}; 
    static double[] powers = new double[51];;
    static double[] massFlows = new double[]{0,0.001,0.0015,0.002,0.0025,0.003,0.0035,0.0042};
    
    public static void main(String[] args) throws IOException {
   	if (args == null || args.length < 1) {
   	    System.err.println("ERROR: No GPX directory path provided.");
   	    System.exit(1);
   	}
   	
   	double[] temps = new double[111];
   	for(int i = 0;i<111;i++){
   	    temps[i] = 30+i*0.1;
   	}
   	
   	String csvMatlab = args[0];	
	String dynOut = args[1];
   	
	powers = new double[51];
	double pInit = -45078;
	double pDiff = 3563.6;
	for(int i = 0;i<51;i++){
	    if(i == 13){
		powers[i] = 0;
	    }
	    else if(i > 13){
		powers[i] = pInit + pDiff*(i-1);
	    }
	    else{
		powers[i] = pInit + pDiff*i;
	    }
	    
	}   	
   	
   	double[][][] dynamics = new double[temps.length][powers.length][massFlows.length];
	for(int i=0;i<temps.length;i++){
	    for(int j=0;j<powers.length;j++){
		for(int k=0;k<massFlows.length;k++){
		    dynamics[i][j][k] = -1;
		}
	    }
	}
	
	BufferedReader CSVFile = 
	        new BufferedReader(new FileReader(csvMatlab));

	  String dataRow = CSVFile.readLine(); // Read first line.
	  // The while checks to see if the data is null. If 
	  // it is, we've hit the end of the file. If not, 
	  // process the data.

	  while (dataRow != null){
	   String[] dataArray = dataRow.split(",");
	   double temp = Double.parseDouble(dataArray[0]);
	   double power = Double.parseDouble(dataArray[1]);
	   double massFlow = Double.parseDouble(dataArray[2]);
	   double tFinal = Double.parseDouble(dataArray[3]);
	   
	   int tIndex = -1;
	   for(int i=0;i<temps.length;i++){
	       if(Math.abs(temp - temps[i]) < 0.05){
		   tIndex = i;
	       }
	   }
	   
	   int pIndex = -1;
	   for(int i=0;i<powers.length;i++){
	       if(Math.abs(power - powers[i]) < 100){
		   pIndex = i;
	       }
	   }
	   
	   int mIndex = -1;
	   for(int i=0;i<massFlows.length;i++){
	       if(Math.abs(massFlow - massFlows[i]) < 0.0001){
		   mIndex = i;
	       }
	   }
	   
	   dynamics[tIndex][pIndex][mIndex] = tFinal;


	   dataRow = CSVFile.readLine(); // Read next line of data.
	  }
	  // Close the file once all data has been read.
	  CSVFile.close();

	  File dynamicsFile = new File(dynOut);
	  dynamicsFile.getParentFile().mkdirs();
	  dynamicsFile.createNewFile();
	  FileOutputStream fos = new FileOutputStream(dynamicsFile);
	    ObjectOutputStream oos = new ObjectOutputStream(fos);
	    oos.writeObject(dynamics);
    }
}
