package org.chargecar.prize;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.chargecar.prize.util.GPXPrivatizer;


/**
 * DO NOT EDIT
 * 
 * Modifies GPX files, removing any points in a track that fall within a certain
 * radius of a desired "private-point"
 * 
 * @author Alex Styler
 */
public class PrivatizeGPX {
    /**
     * @param args
     *            Directory of GPX files to privatized, followed by GPS
     *            coordinates and radii.
     */
    public static void main(String[] args) throws IOException {
	if (args.length < 4 || (args.length - 1) % 3 != 0) {
	    System.out
		    .println("Args must be of the format: \"directory lat1 lon1 radius1 lat2 lon2 radius2...\"");
	    System.out.println("Radii must be in meters");
	    System.out.println(args.length);
	    System.exit(1);
	}
	
	String gpxFolder = args[0];
	File folder = new File(gpxFolder);
	List<File> gpxFiles = Simulator.getGPXFiles(folder);
	
	GPXPrivatizer gpxPrivatizer = new GPXPrivatizer();
	for (int i = 1; i < args.length; i = i + 3) {
	    double lat = Double.parseDouble(args[i]);
	    double lon = Double.parseDouble(args[i + 1]);
	    double rad = Double.parseDouble(args[i + 2]);
	    gpxPrivatizer.addPrivateLocation(lat, lon, rad);
	}
	
	for (File f : gpxFiles) {
	    try {
		if(!f.getName().startsWith("p")){
		    if(gpxPrivatizer.privatizeGPX(f)){
			System.out.println();
			System.out.println("Privatized: "+f.getParentFile().getParentFile().getName()+"\\"+f.getParentFile().getName()+"\\"+f.getName());
		    }
		    else{
			System.out.print('.');
		    }
		}
	    } catch (IOException x) {
	    }
	}
	System.out.println();
	System.out.println("Complete!");
    }
}
