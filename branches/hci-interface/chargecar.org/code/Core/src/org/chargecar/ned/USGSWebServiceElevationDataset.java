package org.chargecar.ned;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import edu.cmu.ri.createlab.xml.XmlHelper;
import org.apache.log4j.Logger;
import org.jdom.Element;
import org.jdom.JDOMException;

/**
 * <p>
 * <code>USGSWebServiceElevationDataset</code> is a singleton which aids in fetching elevation data from the USGS
 * elevation web service.  Elevations are obtained from the best available dataset.
 * </p>
 *
 * @author Chris Bartley (bartley@cmu.edu)
 */
public final class USGSWebServiceElevationDataset implements ElevationDataset
   {
   private static final Logger LOG = Logger.getLogger(USGSWebServiceElevationDataset.class);

   private static final USGSWebServiceElevationDataset INSTANCE = new USGSWebServiceElevationDataset();

   public static USGSWebServiceElevationDataset getInstance()
      {
      return INSTANCE;
      }

   private USGSWebServiceElevationDataset()
      {
      // private to prevent instantiation
      }

   public void open()
      {
      // nothing to do
      }

   public Double getElevation(final double longitude, final double latitude)
      {
      try
         {
         final URL url = new URL("http://gisdata.usgs.net/xmlwebservices2/elevation_service.asmx/getElevation?X_Value=" + longitude + "&Y_Value=" + latitude + "&Elevation_Units=meters&Elevation_Only=true&Source_Layer=-1");
         final InputStream is = url.openStream();
         final Element elevationElement = XmlHelper.createElementNoValidate(is);
         if (elevationElement != null)
            {
            final String value = elevationElement.getTextTrim();
            if (value != null && !"".equals(value))
               {
               return Double.parseDouble(value);
               }
            }
         }
      catch (NumberFormatException e)
         {
         LOG.error("NumberFormatException while parsing the elevation as a double", e);
         }
      catch (IOException e)
         {
         LOG.error("IOException while reading the USGS web service", e);
         }
      catch (JDOMException e)
         {
         LOG.error("JDOMException while parsing the USGS web service response", e);
         }
      return null;
      }

   public void close()
      {
      // nothing to do
      }
   }
