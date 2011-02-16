package org.chargecar.lcddisplay.commands;

import edu.cmu.ri.createlab.serial.CreateLabSerialDeviceReturnValueCommandStrategy;
import edu.cmu.ri.createlab.serial.SerialPortCommandResponse;

/**
 * @author Paul Dille (pdille@andrew.cmu.edu)
 */
public abstract class ReturnValueCommandStrategy<T> extends CreateLabSerialDeviceReturnValueCommandStrategy
   {
   public abstract T convertResult(final SerialPortCommandResponse result);
   }
