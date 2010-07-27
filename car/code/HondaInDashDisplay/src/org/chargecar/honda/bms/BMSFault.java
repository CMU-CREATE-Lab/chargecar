package org.chargecar.honda.bms;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Chris Bartley (bartley@cmu.edu)
 */
public enum BMSFault
   {
      CODE_0(0, "Normal"),
      CODE_1(1, "Driving off while plugged in"),
      CODE_2(2, "Interlock is tripped"),
      CODE_3(3, "Communication fault with a bank or cell"),
      CODE_4(4, "Charge overcurrent"),
      CODE_5(5, "Discharge overcurrent"),
      CODE_6(6, "Over-temperature"),
      CODE_7(7, "Under voltage"),
      CODE_8(8, "Over voltage"),
      CODE_9(9, "No battery voltage"),
      CODE_10(10, "High voltage B- leak to chassis"),
      CODE_11(11, "High voltage B+ leak to chassis"),
      CODE_12(12, "Relay K1 is shorted"),
      CODE_13(13, "Contactor K2 is shorted"),
      CODE_14(14, "Contactor K3 is shorted"),
      CODE_15(15, "Open K1 or K3, or shorted K2"),
      CODE_16(16, "Open K2"),
      CODE_17(17, "Excessive precharge time"),
      CODE_18(18, "EEPROM stack overflow");

   private static final Map<Integer, BMSFault> CODE_TO_BMS_FAULT_MAP;

   static
      {
      final Map<Integer, BMSFault> codeToBMSFaultMap = new HashMap<Integer, BMSFault>();
      for (final BMSFault bmsFault : BMSFault.values())
         {
         codeToBMSFaultMap.put(bmsFault.getCode(), bmsFault);
         }
      CODE_TO_BMS_FAULT_MAP = Collections.unmodifiableMap(codeToBMSFaultMap);
      }

   public static BMSFault findByCode(final int code)
      {
      return CODE_TO_BMS_FAULT_MAP.get(code);
      }

   private final int code;
   private final String message;

   BMSFault(final int code, final String message)
      {
      this.code = code;
      this.message = message;
      }

   public int getCode()
      {
      return code;
      }

   public String getMessage()
      {
      return message;
      }

   public String getMessageAndCode()
      {
      return message + " (" + code + ")";
      }

   @Override
   public String toString()
      {
      final StringBuilder sb = new StringBuilder();
      sb.append("BMSFault");
      sb.append("{code=").append(code);
      sb.append(", message='").append(message).append('\'');
      sb.append('}');
      return sb.toString();
      }
   }
