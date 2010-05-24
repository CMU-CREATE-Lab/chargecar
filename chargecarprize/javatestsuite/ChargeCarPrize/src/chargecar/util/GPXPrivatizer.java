package chargecar.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Stack;

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
public class GPXPrivatizer extends org.xml.sax.helpers.DefaultHandler {
	private List<Double> latitudes;
	private List<Double> longitudes; 
	private List<Double> radii;
	private Stack<String> elementNames;
    private StringBuilder contentBuffer;
	private int points;	
	public GPXPrivatizer(){
		this.latitudes= new ArrayList<Double>();
		this.longitudes = new ArrayList<Double>();
		this.radii= new ArrayList<Double>();
	}
	
	public void addPrivateLocation(double lat, double lon, double radiusm){
		latitudes.add(lat);
		longitudes.add(lon);
		radii.add(radiusm);
	}
	
	public void privatizeGPX(File gpxFile) throws IOException {
		FileInputStream in = new FileInputStream(gpxFile);
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
	}		
	
	   
	   /*
	    * DefaultHandler::startElement() fires whenever an XML start tag is encountered
	    * @see org.xml.sax.helpers.DefaultHandler#startElement(java.lang.String, java.lang.String, java.lang.String, org.xml.sax.Attributes)
	    */
	   public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
	         // the <trkpht> element has attributes which specify latitude and longitude (it has child elements that specify the time and elevation)
	         if (localName.compareToIgnoreCase("trkpt") == 0) {
	        //	 rawLats.add(Double.parseDouble(attributes.getValue("lat")));
	        //	 rawLons.add(Double.parseDouble(attributes.getValue("lon")));
	        //	 points++;
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
	   //         rawEles.add(Double.parseDouble(contentBuffer.toString()));
	         }
	         else if (currentElement.compareToIgnoreCase("time") == 0) {
	    //    	 rawTimes.add(gmtStringToCalendar(contentBuffer.toString()));	      
	         }
	         else if(currentElement.compareToIgnoreCase("trk")==0){
	   //     	 processTrips();
	         }
	      }	      
	   }
	
	private static double Haversine(double lat1, double lon1, double lat2, double lon2) {
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
}
