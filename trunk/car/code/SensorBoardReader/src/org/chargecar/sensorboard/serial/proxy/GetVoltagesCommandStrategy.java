package org.chargecar.sensorboard.serial.proxy;

import java.util.ArrayList;
import edu.cmu.ri.createlab.serial.SerialPortCommandResponse;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * @author Chris Bartley (bartley@cmu.edu)
 */
final class GetVoltagesCommandStrategy extends ChargeCarSerialDeviceReturnValueCommandStrategy<ArrayList<Double>>
   {
   private static final Log LOG = LogFactory.getLog(GetVoltagesCommandStrategy.class);

   /** The command character used to request the voltages. */
   private static final String COMMAND_PREFIX = "V";

   /** The size of the expected response, in bytes */
   private static final int SIZE_IN_BYTES_OF_EXPECTED_RESPONSE = 49;

   private static final double VOLTAGE_CONVERSION_FACTOR = 52.11;

   private final byte[] command;
   private final ResponseConversionStrategy<ArrayList<Double>> responseConversionStrategy =
         new ResponseConversionStrategy<ArrayList<Double>>()
         {
         public ArrayList<Double> convert(final String[] values)
            {
            final ArrayList<Double> voltages = new ArrayList<Double>();
            for (final String value : values)
               {
               double rawVoltage = 0.0;
               try
                  {
                  rawVoltage = Double.parseDouble(value.trim());
                  }
               catch (NumberFormatException e)
                  {
                  LOG.error("GetVoltagesCommandStrategy.ResponseConversionStrategy.convert(): NumberFormatException while converting [" + value + "] to a double", e);
                  }
               voltages.add(rawVoltage / VOLTAGE_CONVERSION_FACTOR);
               }
            return voltages;
            }
         };

   GetVoltagesCommandStrategy()
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

   public ArrayList<Double> convertResponse(final SerialPortCommandResponse response)
      {
      return convertResponse(response, responseConversionStrategy);
      }
   }