package org.chargecar.experiments.thermal;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.ArrayList;
import java.util.List;

import org.chargecar.prize.util.Vehicle;

/**
 * DO NOT EDIT Runs the simulation of an electric car running over a commute
 * defined by GPX file from real world commutes. Uses a compound energy storage
 * Policy to decide whether to get/store power in either the capacitor or
 * battery inside the car.
 * 
 * Competitors need only modify UserPolicy with their algorithm.
 * 
 * @author Alex Styler
 * 
 */
public class TestCaseGraphTrainer {
    static Vehicle civic = new Vehicle(1200, 1.988, 0.31, 0.015);
    static double systemVoltage = 120;
    
   // static double[] temps = new double[]{30,31,32,33,34,35,36,37,38,39,40,41}; 
    static double[] powers = new double[51];;
    static double[] massFlows = new double[]{0,0.001,0.0015,0.002,0.0025,0.003,0.0035,0.0042};
    
    /**
     * @param args
     *            A pathname to a GPX file or folder containing GPX files (will
     *            be recursively traversed)
     *            Alternate policies to test, either in a referenced JAR file or 
     *            within the project
     *        	  e.g. java Simulator "C:\testdata\may" org.chargecar.policies.SpeedPolicy
     *        	       java Simulator "C:\testdata" NaiveBufferPolicy SpeedPolicy
     * @throws IOException
     * @throws ClassNotFoundException 
     */
    public static void main(String[] args) throws IOException, ClassNotFoundException {
	if (args == null || args.length < 1) {
	    System.err.println("Provide .POW directory, Output Folder, Dynamics File");
	    System.exit(1);
	}
	
   	double[] temps = new double[111];
   	for(int i = 0;i<111;i++){
   	    temps[i] = 30+i*0.1;
   	}
   	
   	
	powers = new double[51];
	double pInit = -45078;
	double pDiff = 3563.6;//magic numbers to make an even distribution from -45708 to +100k something
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
	
	String dynFilePath = args[2];
	FileInputStream fis = new FileInputStream(new File(dynFilePath));
	ObjectInputStream ois = new ObjectInputStream(fis);
	double[][][] dynamics = (double[][][])ois.readObject(); 
	ois.close();
	ThermalBattery theBatt = new ThermalBattery(30, temps,powers,massFlows, dynamics);
	
	String powFolder = args[0];	
	String outFolder = args[1];
	File folder = new File(powFolder);
	List<File> powFilesT = getPOWFiles(folder);
	List<File> powFiles = new ArrayList<File>(powFilesT.size());
	for(int i = powFilesT.size() - 1; i >= 0; i--){
	    powFiles.add(powFilesT.get(i));
	}
	
	System.out.println("Training on "+powFiles.size()+" POW files.");
	
	
	int count = 0;
	for (File powFile : powFiles) {
	    List<Double> powers = parsePow(powFile);
	    ThermalValueGraph tvg = new ThermalValueGraph(temps, massFlows, 0.99, theBatt,1); 		
	    double[][] values = tvg.getValues(powers);	
	    String name = powFile.getName().split("\\.")[0];
	    System.out.println("Testing Trip: "+name);
	    writeTrip(values,outFolder,name);
	    count++;
	    
	}	
	
	System.out.println("Complete. Trips trained on: "+count);
    }    
    
    public static void writeTrip(double[][] vg, String optFolder, String name){  	
	File vgFile = new File(optFolder+"/"+name+".tvg");	
	writeValueGraph(vg,vgFile);	
      }
      

    public static void writeValueGraph(double[][] vg, File vgFile) {
	FileWriter fstream;
   	try {
   	    vgFile.getParentFile().mkdirs();
   	    //vgFile.createNewFile();
   	    fstream = new FileWriter(vgFile);
   	    BufferedWriter out = new BufferedWriter(fstream);
   	    for(int x = 0; x < vg.length ; x++){
   		out.write(vg[x][0]+"");
   		for(int y=1;y < vg[x].length;y++){
   		    out.write(","+vg[x][y]); 
   		}
   		out.write("\n");   		
   	    }
   	    out.close();
   	} catch (IOException e) {
   	    // TODO Auto-generated catch block
   	    e.printStackTrace();
   	}
    }     

    
    private static List<Double> parsePow(File powFile) throws IOException {
	List<Double> powers = new ArrayList<Double>();
	FileReader fstream;
	BufferedReader in = null;
   	try {
   	    fstream = new FileReader(powFile);
   	    in = new BufferedReader(fstream);
   	    String line;
   	    while((line = in.readLine()) != null){
   		String power = line.split(",")[1];
   		powers.add(Double.parseDouble(power));   		
   	    }
	} catch (FileNotFoundException e) {
		e.printStackTrace();
	} catch (IOException e) {
		e.printStackTrace();
	} finally {
		if (in != null) {
			try {
				in.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
   	
   	return powers;

    }
    
    static List<File> getPOWFiles(File powFolder) {
	List<File> powFiles = new ArrayList<File>();
	File[] files = powFolder.listFiles();
	for (File f : files) {
	    if (f.isDirectory()) {
		powFiles.addAll(getPOWFiles(f));
	    } else if (f.isFile()
		    && (f.getAbsolutePath().endsWith("pow") || f
			    .getAbsolutePath().endsWith("POW"))) {
		powFiles.add(f);
	    }
	}
	return powFiles;
    }

    
}
