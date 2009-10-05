package org.chargecar.gpx;

import org.chargecar.xml.XmlObject;
import org.jdom.Attribute;
import org.jdom.Element;
import org.jdom.Namespace;

/**
 * @author Chris Bartley (bartley@cmu.edu)
 */
public class GPXFile extends XmlObject
   {
   public static final Namespace GPX_NAMESPACE = Namespace.getNamespace("http://www.topografix.com/GPX/1/1");
   public static final Namespace XSI_NAMESPACE = Namespace.getNamespace("xsi", "http://www.w3.org/2001/XMLSchema-instance");
   private static final String CREATOR_ATTR = "creator";
   private static final String CREATOR_VALUE = "chargecar.org";
   private static final String VERSION_ATTR = "version";
   private static final String VERSION_VALUE = "1.1";
   private static final String SCHEMA_LOCATION_ATTR = "schemaLocation";
   private static final String SCHEMA_LOCATION_VALUE = "http://www.topografix.com/GPX/1/1 http://www.topografix.com/GPX/1/1/gpx.xsd";

   public static final String GPX_ELEMENT_NAME = "gpx";
   public static final String TRACK_ELEMENT_NAME = "trk";
   public static final String NAME_ELEMENT_NAME = "name";
   public static final String TRACK_SEGMENT_ELEMENT_NAME = "trkseg";
   public static final String TRACK_POINT_ELEMENT_NAME = "trkpt";
   public static final String LATITUDE_ATTR = "lat";
   public static final String LONGITUDE_ATTR = "lon";
   public static final String ELEVATION_ELEMENT_NAME = "ele";
   public static final String TIME_ELEMENT_NAME = "time";

   public GPXFile()
      {
      getElement().setName(GPX_ELEMENT_NAME);
      getElement().setNamespace(GPX_NAMESPACE);
      getElement().setAttribute(CREATOR_ATTR, CREATOR_VALUE);
      getElement().setAttribute(VERSION_ATTR, VERSION_VALUE);
      getElement().setAttribute(new Attribute(SCHEMA_LOCATION_ATTR, SCHEMA_LOCATION_VALUE, XSI_NAMESPACE));
      }

   public Element createTrack(final String trackName)
      {
      final Element track = new Element(TRACK_ELEMENT_NAME, GPX_NAMESPACE);
      final Element nameElement = new Element(NAME_ELEMENT_NAME, GPX_NAMESPACE);
      nameElement.addContent(trackName);
      track.addContent(nameElement);
      getElement().addContent(track);
      return track;
      }

   public Element createTrackSegment(final Element track)
      {
      final Element trackSegment = new Element(TRACK_SEGMENT_ELEMENT_NAME, GPX_NAMESPACE);
      track.addContent(trackSegment);
      return trackSegment;
      }

   public void createTrackPoint(final Element trackSegment, final TrackPoint trackPoint)
      {
      trackSegment.addContent(trackPoint.toElement());
      }
   }
