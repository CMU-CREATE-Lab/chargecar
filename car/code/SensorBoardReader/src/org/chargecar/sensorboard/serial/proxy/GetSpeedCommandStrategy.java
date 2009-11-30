package org.chargecar.sensorboard.serial.proxy;

import java.io.UnsupportedEncodingException;
import edu.cmu.ri.createlab.serial.CreateLabSerialDeviceReturnValueCommandStrategy;
import edu.cmu.ri.createlab.serial.SerialPortCommandResponse;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * @author Chris Bartley (bartley@cmu.edu)
 */
final class GetSpeedCommandStrategy extends CreateLabSerialDeviceReturnValueCommandStrategy
   {
   private static final Log LOG = LogFactory.getLog(GetSpeedCommandStrategy.class);

   /** The command character used to request the speed. */
   private static final String COMMAND_PREFIX = "S";

   /** The size of the expected response, in bytes */
   private static final int SIZE_IN_BYTES_OF_EXPECTED_RESPONSE = 7;

   private static final String RESPONSE_VALUE_FIELD_DELIMITER = ",";

   private final byte[] command;

   GetSpeedCommandStrategy()
      {
      this.command = COMMAND_PREFIX.getBytes();
      }

   protected int getSizeOfExpectedResponse()
      {
      return SIZE_IN_BYTES_OF_EXPECTED_RESPONSE;
      }

   protected byte[] getCommand()
      {
      return command.clone();
      }

   public Integer convertResponseToSpeed(final SerialPortCommandResponse response)
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
                     final int speed = Integer.parseInt(responseStr.substring(1, responseStr.length() - 1).trim());
                     if (LOG.isTraceEnabled())
                        {
                        LOG.trace("GetSpeedCommandStrategy.convertResponseToSpeed(): returning speed = [" + speed + "]");
                        }
                     return speed;
                     }
                  catch (Exception e)
                     {
                     LOG.error("GetSpeedCommandStrategy.convertResponseToSpeed(): Exception while trying to read the speed and convert it to an integer", e);
                     }
                  }
               else
                  {
                  LOG.error("GetSpeedCommandStrategy.convertResponseToSpeed(): Unrecognized response [" + responseStr + "]");
                  }
               }
            catch (UnsupportedEncodingException e)
               {
               LOG.error("GetSpeedCommandStrategy.convertResponseToSpeed(): UnsupportedEncodingException while trying to convert the response bytes to a String", e);
               }
            }
         else
            {
            LOG.error("GetSpeedCommandStrategy.convertResponseToSpeed(): Null or empty response");
            }
         }
      return null;
      }
   }
