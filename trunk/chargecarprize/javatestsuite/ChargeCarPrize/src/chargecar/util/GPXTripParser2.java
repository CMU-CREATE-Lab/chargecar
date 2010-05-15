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
import java.math.BigDecimal;
import java.math.MathContext;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.*;

/**
 * @author Alex Styler
 * DO NOT EDIT
 */
public class GPXTripParser2 {
	List<Calendar> rawTimes;
	List<BigDecimal> rawLats;
	List<BigDecimal> rawLons;
	List<BigDecimal> rawEles;
	Stack<String> elementNames;
    private StringBuilder contentBuffer;
	private int points;
	private int carMass;
	private List<List<PointFeatures>> trips;
	   
	public GPXTripParser2() {
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
	    rawLats = new ArrayList<BigDecimal>();
	  	rawLons = new ArrayList<BigDecimal>();
	  	rawEles = new ArrayList<BigDecimal>();
	}
	   
	public List<List<PointFeatures>> read(File gpxFile, int carMass) throws IOException {
		clear();
		this.carMass = carMass;		
		DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance();
		try {
			DocumentBuilder docBuilder = docBuilderFactory.newDocumentBuilder();
			Document doc = docBuilder.parse(gpxFile);
			processNode(doc);
			//TODO write doc
		} catch (ParserConfigurationException e) {
			e.printStackTrace();
		} catch (SAXException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	    
	    return trips;
	}		
	
	private void processTrips() {
		if(rawTimes.isEmpty()){
			return;
		}
		//clean of duplicate readings
		removeDuplicates();
		
		List<Calendar> times = new ArrayList<Calendar>();
		List<BigDecimal> lats = new ArrayList<BigDecimal>();
		List<BigDecimal> lons = new ArrayList<BigDecimal>();
		List<BigDecimal> eles = new ArrayList<BigDecimal>();
		
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

	private void calculateTrip(List<Calendar> times, List<BigDecimal> lats, List<BigDecimal> lons, List<BigDecimal> eles){		
		//TODO removeTunnels(times, lats, lons, eles);
		//interpolatePoints(times, lats, lons, eles);
		List<PointFeatures> tripPoints = new ArrayList<PointFeatures>(times.size());
		runPowerModel(tripPoints, times, lats, lons, eles, carMass);	
		BigDecimal sumPlanarDist = new BigDecimal(0);
		for(PointFeatures pf : tripPoints){
			sumPlanarDist = sumPlanarDist.add(pf.getPlanarDist());
		}
		if(sumPlanarDist.intValue() > 500){
			trips.add(tripPoints);			
		}
	}

	private void interpolatePoints(List<Calendar> times, List<BigDecimal> lats, List<BigDecimal> lons, List<BigDecimal> eles) {
		//make sure there is a point every 2 seconds, 
		//as this is all gps based without car scantool
		for(int i=1;i<times.size();i++){
			long newTime = times.get(i).getTimeInMillis();
			long oldTime = times.get(i-1).getTimeInMillis();			
			if(newTime - oldTime > 2000){
				BigDecimal timeDiff = (new BigDecimal(newTime-oldTime)).divide(new BigDecimal(1000));
				BigDecimal latps = lats.get(i).subtract(lats.get(i-1)).divide(timeDiff);
				BigDecimal lonps = lons.get(i).subtract(lons.get(i-1)).divide(timeDiff);
				BigDecimal eleps = eles.get(i).subtract(eles.get(i-1)).divide(timeDiff);
				Calendar interpTime = Calendar.getInstance();
				interpTime.setTimeInMillis(oldTime+2000);
				times.add(i, interpTime);
				BigDecimal newLat = lats.get(i-1).add(latps.multiply(new BigDecimal(2)));
				BigDecimal newLon = lons.get(i-1).add(lonps.multiply(new BigDecimal(2)));
				BigDecimal newEle = eles.get(i-1).add(eleps.multiply(new BigDecimal(2)));
				lats.add(i, newLat);
				lons.add(i, newLon);
				eles.add(i, newEle);				
			}			
		}		
	}

	private void runPowerModel(List<PointFeatures> tripPoints,
			List<Calendar> times, List<BigDecimal> lats,
			List<BigDecimal> lons, List<BigDecimal> eles, double carMassKg) {
		
		List<BigDecimal> planarDistances = new ArrayList<BigDecimal>();
		List<BigDecimal> adjustedDistances = new ArrayList<BigDecimal>();
		List<BigDecimal> speeds = new ArrayList<BigDecimal>();
		List<BigDecimal> accelerations = new ArrayList<BigDecimal>();
		List<BigDecimal> powerDemands = new ArrayList<BigDecimal>();
				
		planarDistances.add(new BigDecimal(0));
		adjustedDistances.add(new BigDecimal(0));
		speeds.add(new BigDecimal(0));
		accelerations.add(new BigDecimal(0));
		
		MathContext precision = new MathContext(6);
		
		for(int i=1; i<times.size();i++){
			long msDiff = (times.get(i).getTimeInMillis() - times.get(i-1).getTimeInMillis());
			BigDecimal sDiff = (new BigDecimal(msDiff)).divide(new BigDecimal(1000));
			if(msDiff == 0){
				break;
			}
			double eleDiff = eles.get(i).subtract(eles.get(i-1)).doubleValue();
			double tempDist = Haversine(lats.get(i-1), lons.get(i-1), lats.get(i), lons.get(i));
			planarDistances.add(new BigDecimal(tempDist,precision));
			
			tempDist = Math.sqrt((tempDist*tempDist)+(eleDiff*eleDiff));
			adjustedDistances.add(new BigDecimal(tempDist,precision));			
			double tempSpeed = 1000.0*tempDist/msDiff;
			
			if(tempDist < 1E-6){
				speeds.add(new BigDecimal(0));
			}else{
				speeds.add(new BigDecimal(tempSpeed,precision));
			}		
			BigDecimal accel = speeds.get(i).subtract(speeds.get(i-1)).divide(sDiff);
			accelerations.add(accel);	
		}		
		speeds.set(0, speeds.get(1));
		accelerations.set(1, new BigDecimal(0));


		final double carArea = 1.988;//honda civic 2001 si fronta area in metres sq
		final double carDragCoeff = 0.31;//honda civic 2006 sedan		
		final double mu = 0.015; //#rolling resistance coef
		final double aGravity = 9.81;
        final double offset = -0.35;
        final double ineff = 1/0.85;
        final double rollingRes = mu*carMassKg*aGravity; 
        final double outsideTemp = ((60 + 459.67) * 5/9);//60F to kelvin
       
		for(int i=0;i<accelerations.size();i++)
		{
			double pressure = 101325 * Math.pow((1-((0.0065 * eles.get(i).doubleValue())/288.15)), ((aGravity*0.0289)/(8.314*0.0065)));			
			double rho = (pressure * 0.0289) / (8.314 * outsideTemp);			
			double airResCoeff = 0.5*rho*carArea*carDragCoeff;
			double mgsintheta = 0;

			if (i > 0){
				final BigDecimal eleDiff = eles.get(i).subtract(eles.get(i-1));

				if(planarDistances.get(i).compareTo(new BigDecimal(0)) == 0){
					mgsintheta = 0;
				}
				else if (speeds.get(i).abs().compareTo(new BigDecimal(0.50)) < 0)
				{
					mgsintheta = 0;			
				}
				else if(eles.get(i).compareTo(eles.get(i-1)) > 0)
				{
					mgsintheta = -1*carMassKg * aGravity * Math.sin(Math.atan(eleDiff.divide(planarDistances.get(i)).doubleValue()));
				}
				else if (eles.get(i).compareTo(eles.get(i-1)) < 0)
				{
					mgsintheta = -1*carMassKg * aGravity * Math.sin(Math.atan(eleDiff.divide(planarDistances.get(i)).doubleValue()));
				}
			}

			double airRes = airResCoeff * speeds.get(i).pow(2).doubleValue();
			double force = carMassKg * accelerations.get(i).doubleValue();
			double pwr = 0.0;
			double speed = speeds.get(i).doubleValue();
			if (Math.abs(mgsintheta) < 1E-6)
			{
				if (Math.abs(force) < 1E-6 || force > (rollingRes + airRes))
					pwr = (((force + rollingRes + airRes) * speed) * ineff);
				else if (force <= (rollingRes + airRes))
					pwr = 0.35 * (force - rollingRes - airRes) * speed;
			}		
			//#uphill
			else if (eles.get(i).compareTo(eles.get(i-1)) > 0){
				if (force <= (mgsintheta + rollingRes + airRes))
					pwr = 0.35 * (force - mgsintheta - rollingRes - airRes) * speed;
				else if (force > (mgsintheta + rollingRes + airRes))
					pwr = (((force - rollingRes - airRes - mgsintheta) * speed) * ineff);
				else if (Math.abs(force) <1E-6)
					pwr = (((mgsintheta + rollingRes + airRes)) * ineff);

			}
			//#downhill	
			else if (eles.get(i).compareTo(eles.get(i-1))<0){
				if (force <= (mgsintheta + rollingRes + airRes))
					pwr = 0.35 * (force - mgsintheta - rollingRes - airRes) * speed;
				else if (Math.abs(force) < 1E-6 || force > (mgsintheta + rollingRes + airRes))
					pwr = (((force + rollingRes + airRes - mgsintheta) * speed) * ineff);
			}

			pwr = ((pwr/-1000.0) + offset);

			if (speed > 12.0)
			{
				pwr = ((pwr - (0.056*(speed*speed))) + (0.68*speed));
				}

			powerDemands.add(new BigDecimal(pwr,precision));		

		}
		
		
		for(int i=1;i<times.size();i++){
			int periodMS = (int)(times.get(i).getTimeInMillis() - times.get(i-1).getTimeInMillis());
			tripPoints.add(new PointFeatures(lats.get(i-1), lons.get(i-1), eles.get(i-1), planarDistances.get(i), accelerations.get(i), speeds.get(i), powerDemands.get(i), periodMS, times.get(i-1)));
		}
		PointFeatures endPoint = new PointFeatures(lats.get(lats.size()-1),lons.get(lons.size()-1), eles.get(eles.size()-1), new BigDecimal(0), new BigDecimal(0), new BigDecimal(0), new BigDecimal(0), 1000, times.get(times.size()-1));
		tripPoints.add(endPoint);
		
	}

	private void removeTunnels(List<Calendar> times, List<BigDecimal> lats,	List<BigDecimal> lons, List<BigDecimal> eles) {
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
				if(Haversine(lats.get(i-1),lons.get(i-1),lats.get(i), lons.get(i)) > 50.0){
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

	private double Haversine(BigDecimal lat1, BigDecimal lon1, BigDecimal lat2, BigDecimal lon2) {
		int R =6371000; //earth radius, metres TODO
		double dLat = Math.toRadians(lat2.subtract(lat1).doubleValue());
		double dLon = Math.toRadians(lon2.subtract(lon1).doubleValue());
		double a = Math.sin(dLat/2) * Math.sin(dLat/2) +
		        Math.cos(Math.toRadians(lat1.doubleValue())) * Math.cos(Math.toRadians(lat2.doubleValue())) * 
		        Math.sin(dLon/2) * Math.sin(dLon/2); 
		double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a)); 
		double d = R * c;
		return d;
	}

	private void removeDuplicates() {
		for(int i=1;i<rawTimes.size();){
			if(rawTimes.get(i).getTimeInMillis() - rawTimes.get(i-1).getTimeInMillis() < 500 ){
				rawTimes.remove(i);
				rawLats.remove(i);
				rawLons.remove(i);
				rawEles.remove(i);
			}
			else{
				i++;
			}
			
		}
	}
	   
	
	public void processNode(Document doc){
		NodeList trkElements = doc.getElementsByTagName("trk");
		
		for(int i=0;i<trkElements.getLength();i++){
			Element trkElem = (Element) trkElements.item(i);
			NodeList trkptElements = trkElem.getElementsByTagName("trkpt");
			for(int j=0;j<trkptElements.getLength();j++){
				Element trkptElem = (Element) trkptElements.item(j);
				BigDecimal lat = new BigDecimal(trkptElem.getAttribute("lat"));
				BigDecimal lon = new BigDecimal(trkptElem.getAttribute("lat"));
				NodeList ele = trkptElem.getElementsByTagName("ele");
				Element eleElem =(Element)ele.item(0);
				NodeList time = trkptElem.getElementsByTagName("time");
				Element timeElem =(Element)time.item(0);
				rawLats.add(lat);
				rawLons.add(lon);
				rawEles.add(new BigDecimal(eleElem.getChildNodes().item(0).getNodeValue()));
				rawTimes.add(gmtStringToCalendar(timeElem.getChildNodes().item(0).getNodeValue()));				
			}	
			processTrips();
		}



	}

	   public void endElement(String uri, String localName, String qName) throws SAXException {
	      String currentElement = elementNames.pop();
	      
	      if (points > 0 && currentElement != null) {
	         if (currentElement.compareToIgnoreCase("ele") == 0) {
	            
	         }
	         else if (currentElement.compareToIgnoreCase("time") == 0) {
	        	 ;	      
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
