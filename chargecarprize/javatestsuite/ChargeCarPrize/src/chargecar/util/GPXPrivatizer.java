package chargecar.util;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

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
public class GPXPrivatizer {
	private List<Double> latitudes;
	private List<Double> longitudes; 
	private List<Double> radii;
		
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
	
	public void privatizeDocument(String filename) {
		DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance();
		try {
			DocumentBuilder docBuilder = docBuilderFactory.newDocumentBuilder();
			Document doc = docBuilder.parse(new File(filename));
			processNode(doc);
			//TODO write doc
		} catch (ParserConfigurationException e) {
			e.printStackTrace();
		} catch (SAXException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}		
		
	}
	
	public boolean processNode(Node node){
		if(node.getNodeName().compareToIgnoreCase("trkpt") !=0){
			// if this isnt a gpx point, process its children
			if(node.hasChildNodes() == true){
				NodeList children = node.getChildNodes();
				int i=0;
				while(i < children.getLength()){
					Node child = children.item(i);
					if(processNode(child)){
						//we should remove this child, don't increment counter
						node.removeChild(child);
					}
					else{						
						//if we don't remove the child, increment the counter
						i++;
					}
				}
			}
		}
		else{
			Element element = (Element) node;
			double lat = Double.parseDouble(element.getAttribute("lat"));
			double lon = Double.parseDouble(element.getAttribute("lat"));
			for(int i=0;i<latitudes.size();i++){
				if(Haversine(lat, lon, latitudes.get(i), longitudes.get(i)) < radii.get(i)){
					return true;
				}
			}
		}
		return false;
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
