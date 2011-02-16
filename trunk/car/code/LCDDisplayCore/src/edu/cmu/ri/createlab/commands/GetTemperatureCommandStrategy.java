package edu.cmu.ri.createlab.commands;

import edu.cmu.ri.createlab.LCDProxy;
import edu.cmu.ri.createlab.serial.SerialPortCommandResponse;
import edu.cmu.ri.createlab.util.ByteUtils;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

/**
 * @author Paul Dille (pdille@andrew.cmu.edu)
 */
public final class GetTemperatureCommandStrategy extends ReturnValueCommandStrategy<Double>
   {
   /** The command character used to request the temperature */
   private static final byte COMMAND_PREFIX = 'T';

   private final byte[] command;


       public GetTemperatureCommandStrategy(final int sensorNumber)
      {
      this.command = new byte[]{COMMAND_PREFIX,
                                ByteUtils.intToUnsignedByte(sensorNumber),
                                ByteUtils.intToUnsignedByte(LCDProxy.SEQUENCE_NUMBER.next())};
      }

   /** The size of the expected response, in bytes */
   private static final int SIZE_IN_BYTES_OF_EXPECTED_RESPONSE = 2;

   protected int getSizeOfExpectedResponse()
      {
      return SIZE_IN_BYTES_OF_EXPECTED_RESPONSE;
      }

   protected byte[] getCommand()
      {
      return command.clone();
      }

   public Double convertResult(final SerialPortCommandResponse result)
      {
      if (result != null && result.wasSuccessful())
         {
         final byte[] responseData = result.getData();
         final ByteBuffer bb = ByteBuffer.wrap(responseData).order(ByteOrder.LITTLE_ENDIAN);

         if (responseData != null && responseData.length == SIZE_IN_BYTES_OF_EXPECTED_RESPONSE)
            {
                //temperature in Kelvin
                return (577.37*Math.pow(bb.getShort(), -0.104));
            }
         }

      return null;
      }
   }
