package org.chargecar.serial.config;

/**
 * @author Chris Bartley (bartley@cmu.edu)
 */
public enum QwerkSerialPortDevice
   {
      DEV_TTY_AM0("/dev/ttyAM0", "UART 1"),
      DEV_TTY_AM1("/dev/ttyAM1", "UART 2");

   public static final QwerkSerialPortDevice DEFAULT = DEV_TTY_AM1;

   private final String name;
   private final String secondaryName;

   QwerkSerialPortDevice(final String name, final String secondaryName)
      {
      this.name = name;
      this.secondaryName = secondaryName;
      }

   public String getName()
      {
      return name;
      }

   public String getSecondaryName()
      {
      return secondaryName;
      }

   public String toString()
      {
      return name + " (" + secondaryName + ")";
      }
   }
