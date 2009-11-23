package org.chargecar.gpx;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.Properties;
import java.util.PropertyResourceBundle;
import edu.cmu.ri.createlab.xml.XmlHelper;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;

/**
 * <p>
 * <code>GPXValidator</code> is a singleton which validates GPX files.  It can validate both version 1.0 and 1.1 GPXs.
 * </p>
 * <p>
 * Upon construction, this class looks for a system property named "<code>gpx-xsd.directory</code>" which defines the
 * directory to look in for GPX XSD files.  If the system property is not defined, the class defaults to using the
 * directory defined in the <code>GPXValidator.properties</code> file.  No matter how the XSD directory is defined, if
 * the path is not actually a directory or does not exist, an {@link IllegalArgumentException} is thrown.  If the
 * XSD directory is valid, then the class checks every file whose name ends with ".xsd" (case insensitive), looking
 * for the <code>&lt;csd:schema&gt;</code> tag and the <code>targetNamespace</code> attribute.  The class uses this
 * information to validate GPX files against the found XSD files.
 * </p>
 *
 * @author Chris Bartley (bartley@cmu.edu)
 */
public final class GPXValidator
   {
   public interface ValidationResult
      {
      boolean isValid();

      String getErrorMessage();
      }

   private static final Log LOG = LogFactory.getLog(GPXValidator.class);

   private static final PropertyResourceBundle RESOURCES = (PropertyResourceBundle)PropertyResourceBundle.getBundle(GPXValidator.class.getName());
   private static final File XSD_DIRECTORY;
   private static final String GPX_XSD_DIRECTORY_PROPERTY_KEY = "gpx-xsd.directory";

   static
      {
      File xsdDirectory;
      try
         {
         // get the GPX XSD directory from a system property, if defined, otherwise use the value from the properties file.
         final Properties systemProperties = System.getProperties();
         if (systemProperties.containsKey(GPX_XSD_DIRECTORY_PROPERTY_KEY))
            {
            xsdDirectory = new File(systemProperties.getProperty(GPX_XSD_DIRECTORY_PROPERTY_KEY));
            if (!xsdDirectory.exists() || !xsdDirectory.isDirectory())
               {
               xsdDirectory = new File(RESOURCES.getString(GPX_XSD_DIRECTORY_PROPERTY_KEY));
               }
            }
         else
            {
            xsdDirectory = new File(RESOURCES.getString(GPX_XSD_DIRECTORY_PROPERTY_KEY));
            }
         }
      catch (Exception e)
         {
         LOG.error("Exception while trying to determin the XSD directory", e);
         xsdDirectory = null;
         }

      // make sure the XSD directory is actually a directory and that it exists
      if (xsdDirectory == null || !xsdDirectory.exists() || !xsdDirectory.isDirectory())
         {
         throw new IllegalArgumentException("The XSD directory [" + xsdDirectory + "] either does not exist or is not a directory.");
         }

      XSD_DIRECTORY = xsdDirectory;
      }

   private static final GPXValidator INSTANCE = new GPXValidator();

   public static GPXValidator getInstance()
      {
      return INSTANCE;
      }

   private final String schemaLocationPropertyValue;

   private GPXValidator()
      {
      if (LOG.isTraceEnabled())
         {
         LOG.trace("GPXValidator checking XSD directory [" + XSD_DIRECTORY + "]");
         }

      // get an array of XSD files
      final File[] xsdFiles = XSD_DIRECTORY.listFiles(
            new FilenameFilter()
            {
            public boolean accept(final File dir, final String name)
               {
               return name.toLowerCase().endsWith(".xsd");
               }
            });

      // check each XSD file to build the schema location property value which will be used by the SAX builder
      final StringBuilder stringBuilder = new StringBuilder();
      for (final File xsdFile : xsdFiles)
         {
         if (xsdFile != null && xsdFile.exists() && xsdFile.isFile())
            {
            try
               {
               final Element element = XmlHelper.createElementNoValidate(xsdFile);
               if (element != null && "xsd".equals(element.getNamespacePrefix()) && "schema".equals(element.getName()))
                  {
                  final String targetNamespace = element.getAttributeValue("targetNamespace");
                  if (targetNamespace != null)
                     {
                     if (LOG.isTraceEnabled())
                        {
                        LOG.trace("Found XSD file [" + xsdFile.getName() + "] for target namespace [" + targetNamespace + "]");
                        }
                     stringBuilder.append(targetNamespace).append(' ').append(xsdFile.getAbsolutePath()).append(' ');
                     }
                  }
               }
            catch (Exception e)
               {
               if (LOG.isErrorEnabled())
                  {
                  LOG.error("Exception while examining file [" + xsdFile + "]...ignoring.", e);
                  }
               }
            }
         }

      // define the external-schemaLocation property value
      schemaLocationPropertyValue = stringBuilder.toString().trim();
      if (LOG.isTraceEnabled())
         {
         LOG.trace("schemaLocationPropertyValue = [" + schemaLocationPropertyValue + "]");
         }
      }

   public boolean isValid(final File gpxFile)
      {
      return validate(gpxFile).isValid();
      }

   public ValidationResult validate(final File gpxFile)
      {
      // true activates validation
      final SAXBuilder saxBuilder = new SAXBuilder(true);

      // enable schema validation
      saxBuilder.setFeature("http://apache.org/xml/features/validation/schema", true);

      // define the set of namespaces and XSDs
      saxBuilder.setProperty("http://apache.org/xml/properties/schema/external-schemaLocation", schemaLocationPropertyValue);

      // validate the XML file
      String message = null;
      try
         {
         saxBuilder.build(gpxFile);

         return ValidationResultImpl.createValidResult();
         }
      catch (JDOMException e)
         {
         message = e.getMessage();
         LOG.error("JDOMException while validating: " + message);
         }
      catch (IOException e)
         {
         message = e.getMessage();
         LOG.error("IOException while validating: " + message);
         }
      catch (Exception e)
         {
         message = e.getMessage();
         LOG.error("Exception while validating: " + message);
         }

      return ValidationResultImpl.createInvalidResult(message);
      }

   private static final class ValidationResultImpl implements ValidationResult
      {
      private final boolean isValid;
      private final String message;

      private static ValidationResult createValidResult()
         {
         return new ValidationResultImpl(true, null);
         }

      private static ValidationResult createInvalidResult(final String message)
         {
         return new ValidationResultImpl(false, message);
         }

      private ValidationResultImpl(final boolean isValid, final String message)
         {
         this.isValid = isValid;
         this.message = message;
         }

      public boolean isValid()
         {
         return isValid;
         }

      public String getErrorMessage()
         {
         return message;
         }
      }
   }
