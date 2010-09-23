package org.chargecar.honda.bms;

import org.apache.log4j.Logger;
import org.chargecar.honda.StreamingSerialPortDeviceModel;

/**
 * <p>
 * <code>BMSModel</code> keeps track of BMS data.
 * </p>
 *
 * @author Chris Bartley (bartley@cmu.edu)
 */
public final class BMSModel extends StreamingSerialPortDeviceModel<BMSEvent, BMSAndEnergy>
   {
   private static final Logger LOG = Logger.getLogger(BMSModel.class);
   public static final double HOURS_PER_MILLISECOND = 1 / 3600000.0;
   private static final double MILLIS_TO_KILOHOURS_MULTIPLIER = HOURS_PER_MILLISECOND / 1000;

   private final byte[] dataSynchronizationLock = new byte[0];
   private BMSEvent previousBMSEvent = null;
   private final EnergyEquationImpl batteryEnergyEquation = new EnergyEquationImpl();

   public BMSAndEnergy update(final BMSEvent bmsEvent)
      {
      synchronized (dataSynchronizationLock)
         {
         // if the previous BMSEvent isn't null, then calculate the energy change
         if (previousBMSEvent != null)
            {
            final long elapsedMilliseconds = bmsEvent.getTimestampMilliseconds() - previousBMSEvent.getTimestampMilliseconds();
            final double elapsedKiloHours = elapsedMilliseconds * MILLIS_TO_KILOHOURS_MULTIPLIER;
            final double batteryKwh = bmsEvent.getPackTotalVoltage() *
                                      bmsEvent.getLoadCurrentAmps() *
                                      elapsedKiloHours;

            batteryEnergyEquation.addKilowattHours(batteryKwh);
            }

         // save the BMSEvent
         previousBMSEvent = bmsEvent;

         final BMSAndEnergyImpl bmsAndEnergy = new BMSAndEnergyImpl(bmsEvent, batteryEnergyEquation);

         if (LOG.isInfoEnabled())
            {
            LOG.info(bmsEvent.toLoggingString());
            LOG.info(batteryEnergyEquation.toLoggingString(bmsEvent.getTimestampMilliseconds()));
            }

         publishEventToListeners(bmsAndEnergy);

         return bmsAndEnergy;
         }
      }

   public void resetBatteryEnergyEquation()
      {
      synchronized (dataSynchronizationLock)
         {
         batteryEnergyEquation.reset();
         }
      }

   private static final class BMSAndEnergyImpl implements BMSAndEnergy
      {
      private final BMSEvent bmsEvent;
      private final EnergyEquation energyEquation;

      private BMSAndEnergyImpl(final BMSEvent bmsEvent, final EnergyEquation energyEquation)
         {
         this.bmsEvent = bmsEvent;
         this.energyEquation = energyEquation;
         }

      public BMSEvent getBmsState()
         {
         return bmsEvent;
         }

      public EnergyEquation getEnergyEquation()
         {
         return energyEquation;
         }
      }

   private static final class EnergyEquationImpl implements EnergyEquation
      {
      private static final String TO_STRING_DELIMITER = "\t";

      private double kwhDelta = 0.0;
      private double kwhUsed = 0.0;
      private double kwhRegen = 0.0;

      public double getKilowattHours()
         {
         return kwhUsed + kwhRegen;
         }

      public double getKilowattHoursDelta()
         {
         return kwhDelta;
         }

      public double getKilowattHoursUsed()
         {
         return kwhUsed;
         }

      public double getKilowattHoursRegen()
         {
         return kwhRegen;
         }

      private void addKilowattHours(final double kwh)
         {
         kwhDelta = kwh;

         if (Double.compare(kwh, 0.0) > 0)
            {
            kwhUsed += kwh;
            }
         else if (Double.compare(kwh, 0.0) < 0)
            {
            kwhRegen += kwh;
            }
         }

      private void reset()
         {
         LOG.info("BMSModel$EnergyEquationImpl.reset(): Resetting battery energy equation");
         kwhDelta = 0.0;
         kwhUsed = 0.0;
         kwhRegen = 0.0;
         }

      @Override
      public String toString()
         {
         final StringBuilder sb = new StringBuilder();
         sb.append("EnergyEquation");
         sb.append("{kwhDelta=").append(kwhDelta);
         sb.append(", kwhUsed=").append(kwhUsed);
         sb.append(", kwhRegen=").append(kwhRegen);
         sb.append('}');
         return sb.toString();
         }

      private String toLoggingString(final long timestampMilliseconds)
         {
         final StringBuilder sb = new StringBuilder();
         sb.append("EnergyEquation");
         sb.append('{');
         sb.append(timestampMilliseconds).append(TO_STRING_DELIMITER);
         sb.append(getKilowattHours()).append(TO_STRING_DELIMITER);
         sb.append(kwhUsed).append(TO_STRING_DELIMITER);
         sb.append(kwhRegen).append(TO_STRING_DELIMITER);
         sb.append(kwhDelta);
         sb.append('}');
         return sb.toString();
         }
      }
   }
