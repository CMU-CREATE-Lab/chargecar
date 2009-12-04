package org.chargecar.sensorboard.serial.proxy;

import java.io.UnsupportedEncodingException;
import edu.cmu.ri.createlab.serial.CreateLabSerialDeviceCommandStrategy;
import edu.cmu.ri.createlab.serial.CreateLabSerialDeviceReturnValueCommandStrategy;
import edu.cmu.ri.createlab.serial.SerialPortCommandResponse;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * @author Chris Bartley (bartley@cmu.edu)
 */
abstract class ChargeCarSerialDeviceReturnValueCommandStrategy<E> extends CreateLabSerialDeviceReturnValueCommandStrategy
   {
   private static final Log LOG = LogFactory.getLog(ChargeCarSerialDeviceReturnValueCommandStrategy.class);

   private static final String RESPONSE_VALUE_FIELD_DELIMITER = ",";

   /**
    * Creates a <code>ChargeCarSerialDeviceReturnValueCommandStrategy</code> using the default values for read timeout,
    * slurp timeout, and max retries.
    *
    * @see CreateLabSerialDeviceCommandStrategy#CreateLabSerialDeviceCommandStrategy()
    * @see CreateLabSerialDeviceCommandStrategy#DEFAULT_READ_TIMEOUT_MILLIS
    * @see CreateLabSerialDeviceCommandStrategy#DEFAULT_SLURP_TIMEOUT_MILLIS
    * @see CreateLabSerialDeviceCommandStrategy#DEFAULT_MAX_NUMBER_OF_RETRIES
    */
   protected ChargeCarSerialDeviceReturnValueCommandStrategy()
      {
      super();
      }

   /**
    * Creates a <code>ChargeCarSerialDeviceReturnValueCommandStrategy</code> using the given values for read timeout,
    * slurp timeout, and max retries.
    *
    * @see CreateLabSerialDeviceCommandStrategy#CreateLabSerialDeviceCommandStrategy(int, int, int)
    * @see CreateLabSerialDeviceCommandStrategy#DEFAULT_READ_TIMEOUT_MILLIS
    * @see CreateLabSerialDeviceCommandStrategy#DEFAULT_SLURP_TIMEOUT_MILLIS
    * @see CreateLabSerialDeviceCommandStrategy#DEFAULT_MAX_NUMBER_OF_RETRIES
    */
   protected ChargeCarSerialDeviceReturnValueCommandStrategy(final int readTimeoutMillis, final int slurpTimeoutMillis, final int maxNumberOfRetries)
      {
      super(readTimeoutMillis, slurpTimeoutMillis, maxNumberOfRetries);
      }

   public final E convertResponse(final SerialPortCommandResponse response)
      {
      if (response != null)
         {
         final byte[] bytes = response.getData();
         if (bytes != null && bytes.length > 0)
            {
            try
               {
               final String responseStr = new String(bytes, "UTF-8");
               if (responseStr.length() > 2 &&      // greater than 2 because of the beginning and ending delimiters
                   responseStr.startsWith(RESPONSE_VALUE_FIELD_DELIMITER) &&
                   responseStr.endsWith(RESPONSE_VALUE_FIELD_DELIMITER))
                  {
                  try
                     {
                     final String cleanedResponseStr = responseStr.substring(1, responseStr.length() - 1).trim();
                     final String[] values = cleanedResponseStr.split(RESPONSE_VALUE_FIELD_DELIMITER);
                     if ((values != null) && (values.length > 0))
                        {
                        final int numberOfExpectedValues = getNumberOfExpectedValuesInResponse();
                        if (values.length == numberOfExpectedValues)
                           {
                           final String[] trimmedValues = new String[values.length];
                           for (int i = 0; i < values.length; i++)
                              {
                              trimmedValues[i] = (values[i] == null) ? "" : values[i].trim();
                              }
                           final E convertedResponse = convertResponseHelper(trimmedValues);
                           if (LOG.isTraceEnabled())
                              {
                              LOG.trace("ChargeCarSerialDeviceReturnValueCommandStrategy.convertResponse(): returning converted response [" + convertedResponse + "]");
                              }
                           return convertedResponse;
                           }
                        else
                           {
                           LOG.error("ChargeCarSerialDeviceReturnValueCommandStrategy.convertResponse(): response string contains [" + values.length + "] values, expected [" + numberOfExpectedValues + "]");
                           }
                        }
                     else
                        {
                        LOG.error("ChargeCarSerialDeviceReturnValueCommandStrategy.convertResponse(): response string constains no values");
                        }
                     }
                  catch (Exception e)
                     {
                     LOG.error("ChargeCarSerialDeviceReturnValueCommandStrategy.convertResponse(): Exception while trying to read and convert the response", e);
                     }
                  }
               else
                  {
                  LOG.error("ChargeCarSerialDeviceReturnValueCommandStrategy.convertResponse(): Unrecognized response [" + responseStr + "]");
                  }
               }
            catch (UnsupportedEncodingException e)
               {
               LOG.error("ChargeCarSerialDeviceReturnValueCommandStrategy.convertResponse(): UnsupportedEncodingException while trying to convert the response bytes to a String", e);
               }
            }
         else
            {
            LOG.error("ChargeCarSerialDeviceReturnValueCommandStrategy.convertResponse(): Null or empty response");
            }
         }
      return null;
      }

   protected abstract E convertResponseHelper(final String[] values);

   protected abstract int getNumberOfExpectedValuesInResponse();
   }
