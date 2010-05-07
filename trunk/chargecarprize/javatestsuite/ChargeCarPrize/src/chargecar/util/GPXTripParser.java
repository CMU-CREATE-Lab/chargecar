package chargecar.util;

import java.io.File;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Stack;
import java.io.*;
import org.xml.sax.*;

/**
 * @author Alex Styler
 * DO NOT EDIT
 */
public class GPXTripParser extends org.xml.sax.helpers.DefaultHandler {
	List<Calendar> rawTimes;
	List<Double> rawLats;
	List<Double> rawLons;
	List<Double> rawEles;
	Stack<String> elementNames;
    private StringBuilder contentBuffer;
	private int points;
	private double carMass;
	private List<List<PointFeatures>> trips;
	   
	public GPXTripParser() {
		clear();
	}
	   
	public void clear() {
		elementNames = new Stack<String>();
	    contentBuffer = new StringBuilder();
	    trips = new ArrayList<List<PointFeatures>>();
	    clearRawData();	  	
	  	points = 0;
	}
	
	private void clearRawData(){
	    rawTimes = new ArrayList<Calendar>();
	    rawLats = new ArrayList<Double>();
	  	rawLons = new ArrayList<Double>();
	  	rawEles = new ArrayList<Double>();
	}
	   
	public List<List<PointFeatures>> read(String filename, double carMass) throws IOException {
		clear();
		this.carMass = carMass;
		FileInputStream in = new FileInputStream(new File(filename));
	    InputSource source = new InputSource(in);
	    XMLReader parser;
		try {
			parser = org.xml.sax.helpers.XMLReaderFactory.createXMLReader();//"org.apache.xerces.parsers.SAXParser");
			parser.setContentHandler(this);
		    parser.parse(source);
			
		} catch (SAXException e) {
			e.printStackTrace();
			throw new IOException();
		}
	    in.close();	    
	    
	    return trips;
	}		
	
	private void processTrips() {
		if(rawTimes.isEmpty()){
			return;
		}
		//clean of duplicate readings
		removeDuplicates();
		
		List<Calendar> times = new ArrayList<Calendar>();
		List<Double> lats = new ArrayList<Double>();
		List<Double> lons = new ArrayList<Double>();
		List<Double> eles = new ArrayList<Double>();
		
		times.add(rawTimes.get(0));
		lats.add(rawLats.get(0));
		lons.add(rawLons.get(0));
		eles.add(rawEles.get(0));
		
		for(int i=1;i<rawTimes.size();i++){
			long msDiff = rawTimes.get(i).getTimeInMillis() - rawTimes.get(i-1).getTimeInMillis();
			if(msDiff > 360000)
			{
				//if enough time has passed between points (360 seconds)
				//consider them disjoint trips
				calculateTrip(times,lats,lons,eles);
				times.clear();
				lats.clear();
				lons.clear();
				eles.clear();
			}		
			
			times.add(rawTimes.get(i));
			lats.add(rawLats.get(i));
			lons.add(rawLons.get(i));
			eles.add(rawEles.get(i));			
		}
		
		if(times.size() > 60){
			//get last trip
			calculateTrip(times,lats,lons,eles);			
		}
		
		clearRawData();

	}

	private void calculateTrip(List<Calendar> times, List<Double> lats, List<Double> lons, List<Double> eles){		
		//TODO removeTunnels(times, lats, lons, eles);
		//interpolatePoints(times, lats, lons, eles);
		List<PointFeatures> tripPoints = new ArrayList<PointFeatures>(times.size());
		runPowerModel(tripPoints, times, lats, lons, eles, carMass);	
		double sumPlanarDist = 0.0;
		for(PointFeatures pf : tripPoints){
			sumPlanarDist += pf.getPlanarDist();
		}
		if(sumPlanarDist > 500.0){
			trips.add(tripPoints);			
		}
	}

	private void interpolatePoints(List<Calendar> times, List<Double> lats,
			List<Double> lons, List<Double> eles) {
		//make sure there is a point every 2 seconds, 
		//as this is all gps based without car scantool
		for(int i=1;i<times.size();i++){
			long newTime = times.get(i).getTimeInMillis();
			long oldTime = times.get(i-1).getTimeInMillis();
			if(newTime - oldTime > 2000){
				double latps = (lats.get(i) - lats.get(i-1))/(newTime - oldTime);
				double lonps = (lons.get(i) - lons.get(i-1))/(newTime - oldTime);
				double eleps = (eles.get(i) - eles.get(i-1))/(newTime - oldTime);
				Calendar interpTime = Calendar.getInstance();
				interpTime.setTimeInMillis(oldTime+2000);
				times.add(i, interpTime);
				lats.add(i, lats.get(i-1)+2*latps);
				lons.add(i, lons.get(i-1)+2*lonps);
				eles.add(i, eles.get(i-1)+2*eleps);				
			}			
		}		
	}

	private void runPowerModel(List<PointFeatures> tripPoints,
			List<Calendar> times, List<Double> lats,
			List<Double> lons, List<Double> eles, double carMassKg) {
		
		List<Double> planarDistances = new ArrayList<Double>();
		List<Double> adjustedDistances = new ArrayList<Double>();
		List<Double> speeds = new ArrayList<Double>();
		List<Double> accelerations = new ArrayList<Double>();
		List<Double> powerDemands = new ArrayList<Double>();
				
		planarDistances.add(0.0);
		adjustedDistances.add(0.0);
		speeds.add(0.0);
		accelerations.add(0.0);

		for(int i=1; i<times.size();i++){
			long sDiff = (times.get(i).getTimeInMillis() - times.get(i-1).getTimeInMillis())/1000;
			double eleDiff = eles.get(i) - eles.get(i-1);
			double tempDist = Haversine(lats.get(i-1), lons.get(i-1), lats.get(i), lons.get(i));
			planarDistances.add(tempDist);			
			tempDist = Math.sqrt((tempDist*tempDist)+(eleDiff*eleDiff));
			adjustedDistances.add(tempDist);			
			double tempSpeed = tempDist/(sDiff);
			
			if(tempDist < 1E-6){
				speeds.add(0.0);
			}else{
				speeds.add(tempSpeed);
			}		
			accelerations.add((speeds.get(i) - speeds.get(i-1))/sDiff);	
		}		
		speeds.set(0, speeds.get(1));
		accelerations.set(1, 0.0);


		final double carArea =1.988;//honda civic 2001 si fronta area in metres sq
		final double carDragCoeff=0.31;//honda civic 2006 sedan		
		final double mu = 0.015; //#rolling resistance coef
		final double aGravity = 9.81;
        final double offset = -0.35;
        final double ineff = 1/0.85;
        final double rollingRes = mu*carMassKg*aGravity; 
        final double outsideTemp = ((60 + 459.67) * 5/9);//60F to kelvin
       
		for(int i=0;i<accelerations.size();i++)
		{
			double pressure = 101325 * Math.pow((1-((0.0065 * eles.get(i))/288.15)), ((aGravity*0.0289)/(8.314*0.0065)));			
			double rho = (pressure * 0.0289) / (8.314 * outsideTemp);			
			double airResCoeff = 0.5*rho*carArea*carDragCoeff;
			double mgsintheta = 0;

			if (i > 0){
				final double eleDiff = eles.get(i) - eles.get(i-1);

				if(planarDistances.get(i) < 1E-6){
					mgsintheta = 0;
				}
				else if (Math.abs(speeds.get(i)) < 0.50){
					mgsintheta = 0;			
				}
				else if(eles.get(i) > eles.get(i-1))
				{
					mgsintheta = (carMassKg * aGravity * Math.sin(Math.atan(eleDiff/planarDistances.get(i)))) * -1;
				}
				else if (eles.get(i) < eles.get(i-1))
				{
					mgsintheta = (carMassKg * aGravity * Math.sin(Math.atan(eleDiff/planarDistances.get(i)))) * -1;
				}
			}

			double airRes = airResCoeff * speeds.get(i)*speeds.get(i);
			double force = carMassKg * accelerations.get(i);
			double pwr = 0.0;
			double speed = speeds.get(i);
			if (Math.abs(mgsintheta) < 1E-6)
			{
				if (Math.abs(force) < 1E-6 || force > (rollingRes + airRes))
					pwr = (((force + rollingRes + airRes) * speed) * ineff);
				else if (force <= (rollingRes + airRes))
					pwr = 0.35 * (force - rollingRes - airRes) * speed;
			}		
			//#uphill
			else if (eles.get(i) > eles.get(i-1)){
				if (force <= (mgsintheta + rollingRes + airRes))
					pwr = 0.35 * (force - mgsintheta - rollingRes - airRes) * speed;
				else if (force > (mgsintheta + rollingRes + airRes))
					pwr = (((force - rollingRes - airRes - mgsintheta) * speed) * ineff);
				else if (Math.abs(force) <1E-6)
					pwr = (((mgsintheta + rollingRes + airRes)) * ineff);

			}
			//#downhill	
			else if (eles.get(i) < eles.get(i-1)){
				if (force <= (mgsintheta + rollingRes + airRes))
					pwr = 0.35 * (force - mgsintheta - rollingRes - airRes) * speed;
				else if (Math.abs(force) < 1E-6 || force > (mgsintheta + rollingRes + airRes))
					pwr = (((force + rollingRes + airRes - mgsintheta) * speed) * ineff);
			}

			pwr = ((pwr/-1000.0) + offset);

			if (speed > 12.0)
				pwr = ((pwr - (0.056*(speed*speed))) + (0.68*speed));

			powerDemands.add(pwr);		
		}
		
		
		for(int i=1;i<times.size();i++){
			int periodMS = (int)(times.get(i).getTimeInMillis() - times.get(i-1).getTimeInMillis());
			tripPoints.add(new PointFeatures(lats.get(i-1), lons.get(i-1), eles.get(i-1), planarDistances.get(i), accelerations.get(i), speeds.get(i), powerDemands.get(i), periodMS, times.get(i-1)));
		}
		PointFeatures endPoint = new PointFeatures(lats.get(lats.size()-1),lons.get(lons.size()-1), eles.get(eles.size()-1), 0.0, 0.0, 0.0, 0.0, 1000, times.get(times.size()-1));
		tripPoints.add(endPoint);
		
	}

	private void removeTunnels(List<Calendar> times, List<Double> lats,
			List<Double> lons, List<Double> eles) {
		//removes tunnel points, tunnels will be fixed later by interpolation
		int consecutiveCounter = 0;
		for(int i=1;i<times.size();i++){
			if(lats.get(i).compareTo(lats.get(i-1)) == 0 &&
					lons.get(i).compareTo(lons.get(i-1)) == 0){
				//consecutive readings at the same position
				consecutiveCounter++;				
			}
			else if(consecutiveCounter > 0){
				//position has changed, after consectuive readings at same position
				//can be tunnel, red light, etc...		
				if(Haversine(lats.get(i-1),lons.get(i-1),lats.get(i), lons.get(i)).compareTo(50.0) > 0){
					//if traveled at least 50 metres, assume tunnel
					times.subList(i-consecutiveCounter, i).clear();
					lats.subList(i-consecutiveCounter, i).clear();
					lons.subList(i-consecutiveCounter, i).clear();
					eles.subList(i-consecutiveCounter, i).clear();
					i = i - consecutiveCounter;
				}
				consecutiveCounter = 0;
			}
		}
		
	}

	private Double Haversine(double lat1, double lon1, double lat2, double lon2) {
	 	double R = 6371000; //earth radius, metres
		double dLat = Math.toRadians(lat2-lat1);
		double dLon = Math.toRadians(lon2-lon1); 
		double a = Math.sin(dLat/2) * Math.sin(dLat/2) +
		        Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) * 
		        Math.sin(dLon/2) * Math.sin(dLon/2); 
		double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a)); 
		double d = R * c;
		return d;
	}

	private void removeDuplicates() {
		int length = rawTimes.size();
		for(int i=1;i<length;i++){
			if(rawTimes.get(i).compareTo(rawTimes.get(i-1))<=0){
				rawTimes.remove(i);
				rawLats.remove(i);
				rawLons.remove(i);
				rawEles.remove(i);
				i--;
				length--;
			}
		}
	}
	   
	   /*
	    * DefaultHandler::startElement() fires whenever an XML start tag is encountered
	    * @see org.xml.sax.helpers.DefaultHandler#startElement(java.lang.String, java.lang.String, java.lang.String, org.xml.sax.Attributes)
	    */
	   public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
	         // the <trkpht> element has attributes which specify latitude and longitude (it has child elements that specify the time and elevation)
	         if (localName.compareToIgnoreCase("trkpt") == 0) {
	        	 rawLats.add(Double.parseDouble(attributes.getValue("lat")));
	        	 rawLons.add(Double.parseDouble(attributes.getValue("lon")));
	        	 points++;
	         }
	      // Clear content buffer
	      contentBuffer.delete(0, contentBuffer.length());
	      
	      // Store name of current element in stack
	      elementNames.push(qName);
	   }
	   
	   /*
	    * the DefaultHandler::characters() function fires 1 or more times for each text node encountered
	    *
	    */
	   public void characters(char[] ch, int start, int length) throws SAXException {
	      contentBuffer.append(String.copyValueOf(ch, start, length));
	   }
	   
	   /*
	    * the DefaultHandler::endElement() function fires for each end tag
	    *
	    */
	   public void endElement(String uri, String localName, String qName) throws SAXException {
	      String currentElement = elementNames.pop();
	      
	      if (points > 0 && currentElement != null) {
	         if (currentElement.compareToIgnoreCase("ele") == 0) {
	            rawEles.add(Double.parseDouble(contentBuffer.toString()));
	         }
	         else if (currentElement.compareToIgnoreCase("time") == 0) {
	        	 rawTimes.add(gmtStringToCalendar(contentBuffer.toString()));	      
	         }
	         else if(currentElement.compareToIgnoreCase("trk")==0){
	        	 processTrips();
	         }
	      }	      
	   }

	private static Calendar gmtStringToCalendar(String dateTimeString) {
		//incoming format: 2010-02-25T22:44:57Z
		String dateString = dateTimeString.substring(0,dateTimeString.indexOf('T'));
		String[] dates = dateString.split("-");
		int year = Integer.parseInt(dates[0]);
		int month = Integer.parseInt(dates[1]) - 1;//0 indexed
		int day = Integer.parseInt(dates[2]);
		//format: 22:44:57
		String timeString = dateTimeString.substring(dateTimeString.indexOf('T')+1, dateTimeString.indexOf('Z'));
		String[] times = timeString.split(":");
		int hour = Integer.parseInt(times[0]);
		int minute = Integer.parseInt(times[1]);
		int second = Integer.parseInt(times[2]);
		
		Calendar calTime = Calendar.getInstance();
		calTime.set(year, month, day, hour, minute, second);
		return calTime;
	}
}
