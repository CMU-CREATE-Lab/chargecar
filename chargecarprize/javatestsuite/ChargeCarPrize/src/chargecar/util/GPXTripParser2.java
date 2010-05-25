package chargecar.util;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.*;

/**
 * DO NOT EDIT
 * 
 * DOM implementation for GPX parsing.  Not used.
 * 
 * @author Alex Styler
 */
public class GPXTripParser2 {
	List<Calendar> rawTimes;
	List<Double> rawLats;
	List<Double> rawLons;
	List<Double> rawEles;
	private int carMassKg;
	private List<List<PointFeatures>> trips;
	   
	public GPXTripParser2() {
		clear();
	}
	   
	public void clear() {
	    trips = new ArrayList<List<PointFeatures>>();
	    clearRawData();	  	
	}
	
	private void clearRawData(){
	    rawTimes = new ArrayList<Calendar>();
	    rawLats = new ArrayList<Double>();
	  	rawLons = new ArrayList<Double>();
	  	rawEles = new ArrayList<Double>();
	}
	   
	public List<List<PointFeatures>> read(File gpxFile, int carMassKg) throws IOException {
		clear();
		this.carMassKg = carMassKg;		
		DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance();
		try {
			DocumentBuilder docBuilder = docBuilderFactory.newDocumentBuilder();
			Document doc = docBuilder.parse(gpxFile);
			processNode(doc);
		} catch (ParserConfigurationException e) {
			e.printStackTrace();
		} catch (SAXException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	    
		for(int i=0;i<trips.size();){
			double sumPlanarDist = 0.0;
			for(PointFeatures p:trips.get(i)){
				sumPlanarDist+= p.getPlanarDist(); 
			}
			if(sumPlanarDist < 500.0){
				trips.remove(i);
			}
			else{
				i++;
			}
		}
	    return trips;
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
				double lat = Double.parseDouble(trkptElem.getAttribute("lat"));
				double lon = Double.parseDouble(trkptElem.getAttribute("lon"));
				NodeList ele = trkptElem.getElementsByTagName("ele");
				Element eleElem =(Element)ele.item(0);
				NodeList time = trkptElem.getElementsByTagName("time");
				Element timeElem =(Element)time.item(0);
				rawLats.add(lat);
				rawLons.add(lon);
				rawEles.add(Double.parseDouble(eleElem.getChildNodes().item(0).getNodeValue()));
				rawTimes.add(gmtStringToCalendar(timeElem.getChildNodes().item(0).getNodeValue()));				
			}	
			processTrips();
		}
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
				trips.add(TripBuilder.calculateTrip(times,lats,lons,eles, carMassKg));
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
			trips.add(TripBuilder.calculateTrip(times,lats,lons,eles, carMassKg));		
		}
		
		clearRawData();
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
		calTime.setTimeInMillis(0);
		calTime.set(year, month, day, hour, minute, second);
		return calTime;
	}
}
