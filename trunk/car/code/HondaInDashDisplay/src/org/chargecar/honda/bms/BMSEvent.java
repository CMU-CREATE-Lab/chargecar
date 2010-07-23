package org.chargecar.honda.bms;

import java.util.Date;
import org.chargecar.serial.streaming.BaseStreamingSerialPortEvent;

/**
 * @author Chris Bartley (bartley@cmu.edu)
 */
public class BMSEvent extends BaseStreamingSerialPortEvent
   {
   private final BMSFault bmsFault;
   private final int numOnOffCycles;
   private final int timeSincePowerUpInSecs;

   public BMSEvent(final Date timestamp,
                   final BMSFault bmsFault,
                   final int numOnOffCycles,
                   final int timeSincePowerUpInSecs)
      {
      super(timestamp);
      this.bmsFault = bmsFault;
      this.numOnOffCycles = numOnOffCycles;
      this.timeSincePowerUpInSecs = timeSincePowerUpInSecs;
      }

   @Override
   public String toString()
      {
      final StringBuilder sb = new StringBuilder();
      sb.append("BMSEvent");
      sb.append("{bmsFault=").append(bmsFault);
      sb.append(", numOnOffCycles=").append(numOnOffCycles);
      sb.append(", timeSincePowerUpInSecs=").append(timeSincePowerUpInSecs);
      sb.append('}');
      return sb.toString();
      }

   @Override
   public String toLoggingString()
      {
      // TODO
      return null;
      }
   }
