package chargecar;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import chargecar.util.GPXPrivatizer;

public class PrivatizeGPX {
	public static void main(String[] args) throws IOException {	
		//arg format: directory lat1 lon1 radius1 lat2 lon2 radius2....
		if(args.length < 4 || (args.length-1)%3 != 0){
			System.out.println("Args must be of the format: \"directory lat1 lon1 radius1 lat2 lon2 radius2...\"");
			System.out.println("Radii must be in meters");
			System.out.println(args.length);
			System.exit(1);
		}	
		
		String gpxFolder = args[0];
		File folder = new File(gpxFolder);
		List<File> gpxFiles = Simulator.getGPXFiles(folder);
		
		GPXPrivatizer gpxPrivatizer = new GPXPrivatizer();
		for(int i=1;i<args.length;i=i+3)
		{
			double lat = Double.parseDouble(args[i]);
			double lon = Double.parseDouble(args[i+1]);
			double rad = Double.parseDouble(args[i+2]);
			gpxPrivatizer.addPrivateLocation(lat, lon, rad);
		}
		
		for(File f:gpxFiles)
		{
			try
			{
				if(!f.getName().startsWith("p")){
					gpxPrivatizer.privatizeGPX(f);
					System.out.print('.');
				}
			}
			catch(IOException x){}
			
		}
	}
}
