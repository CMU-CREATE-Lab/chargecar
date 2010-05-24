package chargecar.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

import org.xml.sax.*;

import com.sun.org.apache.xml.internal.serialize.OutputFormat;
import com.sun.org.apache.xml.internal.serialize.XMLSerializer;

/**
 * DO NOT EDIT
 * 
 * Does the grunt work of reading, modifying, and writing XML
 * files for privitization.  Does event-driven handling system
 * to save memory and increase speed.
 *   
 * @author Alex Styler
 */
public class GPXPrivatizer extends org.xml.sax.helpers.DefaultHandler 
{
	private List<Double> latitudes;
	private List<Double> longitudes; 
	private List<Double> radii;
	private Stack<String> elementNames;
    private StringBuilder contentBuffer;
	private int points;	
	private ContentHandler xmlSAXWriter;
	private boolean writeCurrentElement = true;
	private boolean adjusted = false;
	
	public GPXPrivatizer()
	{
		this.latitudes= new ArrayList<Double>();
		this.longitudes = new ArrayList<Double>();
		this.radii= new ArrayList<Double>();
	}
	
	public void addPrivateLocation(double lat, double lon, double radiusm)
	{
		latitudes.add(lat);
		longitudes.add(lon);
		radii.add(radiusm);
		System.out.println("Protecting location: ("+lat+", "+lon+") by "+radiusm+" metres");
	}
	
	private void reset()
	{
		elementNames = new Stack<String>();
	    contentBuffer = new StringBuilder();
	    points = 0;
	    adjusted = false;
	    writeCurrentElement = true;
	}
	
	public void privatizeGPX(File gpxFile) throws IOException 
	{
		reset();
		//gpx writing
		File privGpxFile = new File(gpxFile.getParentFile().getCanonicalPath()+"\\p"+gpxFile.getName());
		FileOutputStream fos = new FileOutputStream(privGpxFile, false);
		OutputFormat of = new OutputFormat();
		of.setIndent(1);
		of.setIndenting(true);
		XMLSerializer serializer = new XMLSerializer(fos,of);
		xmlSAXWriter = serializer.asContentHandler();

		//gpx reading
		FileInputStream in = new FileInputStream(gpxFile);
	    InputSource source = new InputSource(in);
	    XMLReader parser;	    
		try 
		{
			parser = org.xml.sax.helpers.XMLReaderFactory.createXMLReader();//"org.apache.xerces.parsers.SAXParser");
			parser.setContentHandler(this);
			xmlSAXWriter.startDocument();
		    parser.parse(source);
		    xmlSAXWriter.endDocument();
			
		} catch (SAXException e) 
		{
			System.out.println(gpxFile.getCanonicalPath());
			e.printStackTrace();
			throw new IOException();
		} finally
		{
			in.close();
		}
		fos.close();
		if(!adjusted)
		{
			privGpxFile.delete();
		}
		else
		{
			System.out.println("Privatized: "+gpxFile.getCanonicalPath());
		}
	}		
	
	   
	   /*
	    * DefaultHandler::startElement() fires whenever an XML start tag is encountered
	    * @see org.xml.sax.helpers.DefaultHandler#startElement(java.lang.String, java.lang.String, java.lang.String, org.xml.sax.Attributes)
	    */
	   public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException 
	   {
	         // the <trkpht> element has attributes which specify latitude and longitude (it has child elements that specify the time and elevation)
	         if (localName.compareToIgnoreCase("trkpt") == 0) 
	         {
	        	 double lat = Double.parseDouble(attributes.getValue("lat"));
	        	 double lon = Double.parseDouble(attributes.getValue("lon"));
	        	 points++;
	        	 writeCurrentElement=true;
	        	 for(int i=0;i<latitudes.size();i++)
	        	 {
	        		 if(Haversine(latitudes.get(i),longitudes.get(i),lat,lon) < radii.get(i))
	        		 {
	        			 writeCurrentElement=false;
	        			 adjusted=true;
	        		 }
	        	 }
	         }

	      // Clear content buffer
	      contentBuffer.delete(0, contentBuffer.length());
	      
	      // Store name of current element in stack
	      elementNames.push(qName);
	      
	      if(writeCurrentElement)
	      {
	    	  xmlSAXWriter.startElement(uri, localName, qName, attributes);
	      }
	   }
	   
	   /*
	    * the DefaultHandler::characters() function fires 1 or more times for each text node encountered
	    *
	    */
	   public void characters(char[] ch, int start, int length) throws SAXException 
	   {
	      contentBuffer.append(String.copyValueOf(ch, start, length));
	      if(writeCurrentElement){
	    	  xmlSAXWriter.characters(ch, start, length);
	      }
	   }
	   
	   /*
	    * the DefaultHandler::endElement() function fires for each end tag
	    *
	    */
	   public void endElement(String uri, String localName, String qName) throws SAXException 
	   {
		   String currentElement = elementNames.pop();
	      
		   if (points > 0 && currentElement != null) 
		   {	
			   if(writeCurrentElement)
			   {
				   xmlSAXWriter.endElement(uri, localName, qName);
			   }
		   }	     
		   if (localName.compareToIgnoreCase("trkpt") == 0) 
		   {
			   writeCurrentElement = true;
	       }
	   }
	
	private static double Haversine(double lat1, double lon1, double lat2, double lon2) 
	{
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
