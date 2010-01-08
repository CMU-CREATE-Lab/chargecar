package org.chargecar.sensorboard;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * <p>
 * <code>PowerModel</code> keeps track of voltage, current, and power data.
 * </p>
 *
 * @author Chris Bartley (bartley@cmu.edu)
 */
public final class PowerModel extends Model<VoltagesAndCurrents, Power>
   {
   private static final Log LOG = LogFactory.getLog(PowerModel.class);

   private final byte[] dataSynchronizationLock = new byte[0];
   private VoltagesAndCurrents previousVoltagesAndCurrents = null;
   private long previousTimestamp = 0;
   private final PowerEquationImpl batteryPowerEquation = new PowerEquationImpl();
   private final PowerEquationImpl capacitorPowerEquation = new PowerEquationImpl();
   private final PowerEquationImpl accessoryPowerEquation = new PowerEquationImpl();

   public void update(final VoltagesAndCurrents voltagesAndCurrents)
      {
      if (voltagesAndCurrents != null)
         {
         synchronized (dataSynchronizationLock)
            {
            // if the previous voltages and currents isn't null, then calculate the power change
            long currentTimestamp = System.currentTimeMillis();
            if (previousVoltagesAndCurrents != null && voltagesAndCurrents.getVoltages() != null && voltagesAndCurrents.getCurrents() != null)
               {
               // compute the timestamp by taking the average--semi-lame, but what else can we do?
               currentTimestamp = (voltagesAndCurrents.getVoltages().getTimestampMilliseconds() + voltagesAndCurrents.getCurrents().getTimestampMilliseconds()) / 2;
               final long elapsedMilliseconds = currentTimestamp - previousTimestamp;
               final double elapsedKiloHours = elapsedMilliseconds * SensorBoardConstants.HOURS_PER_MILLISECOND / 1000;
               final double batteryKwh = voltagesAndCurrents.getVoltages().getBatteryVoltage() *
                                         voltagesAndCurrents.getCurrents().getBatteryCurrent() *
                                         elapsedKiloHours;
               final double capacitorKwh = voltagesAndCurrents.getVoltages().getCapacitorVoltage() *
                                           voltagesAndCurrents.getCurrents().getCapacitorCurrent() *
                                           elapsedKiloHours;
               final double accessoryKwh = voltagesAndCurrents.getVoltages().getAccessoryVoltage() *
                                           voltagesAndCurrents.getCurrents().getAccessoryCurrent() *
                                           elapsedKiloHours;
               if (LOG.isInfoEnabled())
                  {
                  LOG.info("PowerModel.update(): elapsedMilliseconds = [" + elapsedMilliseconds + "]");
                  LOG.info("PowerModel.update(): batteryKwh = [" + batteryKwh + "]");
                  LOG.info("PowerModel.update(): capacitorKwh = [" + capacitorKwh + "]");
                  LOG.info("PowerModel.update(): accessoryKwh = [" + accessoryKwh + "]");
                  }
               batteryPowerEquation.addKilowattHours(batteryKwh);
               capacitorPowerEquation.addKilowattHours(capacitorKwh);
               accessoryPowerEquation.addKilowattHours(accessoryKwh);
               }

            // save the voltages and currents and timestamp
            previousVoltagesAndCurrents = voltagesAndCurrents;
            previousTimestamp = currentTimestamp;

            // notify listeners
            publishEventToListeners(new PowerImpl(voltagesAndCurrents,
                                                  batteryPowerEquation,
                                                  capacitorPowerEquation,
                                                  accessoryPowerEquation));
            }
         }
      }

   private static final class PowerEquationImpl implements PowerEquation
      {
      private double kwhUsed = 0.0;
      private double kwhRegen = 0.0;

      public double getKilowattHours()
         {
         return kwhUsed + kwhRegen;
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
         if (Double.compare(kwh, 0.0) < 0)
            {
            kwhUsed += kwh;
            }
         else if (Double.compare(kwh, 0.0) > 0)
            {
            kwhRegen += kwh;
            }
         }

      @Override
      public String toString()
         {
         final StringBuilder sb = new StringBuilder();
         sb.append("PowerEquation");
         sb.append("{kwhUsed=").append(kwhUsed);
         sb.append(", kwhRegen=").append(kwhRegen);
         sb.append('}');
         return sb.toString();
         }
      }

   private static final class PowerImpl implements Power
      {
      private final VoltagesAndCurrents voltagesAndCurrents;
      private final PowerEquation batteryPowerEquation;
      private final PowerEquation capacitorPowerEquation;
      private final PowerEquation accessoryPowerEquation;

      private PowerImpl(final VoltagesAndCurrents voltagesAndCurrents,
                        final PowerEquation batteryPowerEquation,
                        final PowerEquation capacitorPowerEquation,
                        final PowerEquation accessoryPowerEquation)
         {
         this.voltagesAndCurrents = voltagesAndCurrents;
         this.batteryPowerEquation = batteryPowerEquation;
         this.capacitorPowerEquation = capacitorPowerEquation;
         this.accessoryPowerEquation = accessoryPowerEquation;
         }

      public Voltages getVoltages()
         {
         return voltagesAndCurrents.getVoltages();
         }

      public Currents getCurrents()
         {
         return voltagesAndCurrents.getCurrents();
         }

      public boolean isCapacitorOverVoltage()
         {
         return voltagesAndCurrents.isCapacitorOverVoltage();
         }

      public PowerEquation getBatteryPowerEquation()
         {
         return batteryPowerEquation;
         }

      public PowerEquation getCapacitorPowerEquation()
         {
         return capacitorPowerEquation;
         }

      public PowerEquation getAccessoryPowerEquation()
         {
         return accessoryPowerEquation;
         }

      @Override
      public boolean equals(final Object o)
         {
         if (this == o)
            {
            return true;
            }
         if (o == null || getClass() != o.getClass())
            {
            return false;
            }

         final PowerImpl power = (PowerImpl)o;

         if (accessoryPowerEquation != null ? !accessoryPowerEquation.equals(power.accessoryPowerEquation) : power.accessoryPowerEquation != null)
            {
            return false;
            }
         if (batteryPowerEquation != null ? !batteryPowerEquation.equals(power.batteryPowerEquation) : power.batteryPowerEquation != null)
            {
            return false;
            }
         if (capacitorPowerEquation != null ? !capacitorPowerEquation.equals(power.capacitorPowerEquation) : power.capacitorPowerEquation != null)
            {
            return false;
            }
         if (voltagesAndCurrents != null ? !voltagesAndCurrents.equals(power.voltagesAndCurrents) : power.voltagesAndCurrents != null)
            {
            return false;
            }

         return true;
         }

      @Override
      public int hashCode()
         {
         int result = voltagesAndCurrents != null ? voltagesAndCurrents.hashCode() : 0;
         result = 31 * result + (batteryPowerEquation != null ? batteryPowerEquation.hashCode() : 0);
         result = 31 * result + (capacitorPowerEquation != null ? capacitorPowerEquation.hashCode() : 0);
         result = 31 * result + (accessoryPowerEquation != null ? accessoryPowerEquation.hashCode() : 0);
         return result;
         }

      @Override
      public String toString()
         {
         final StringBuilder sb = new StringBuilder();
         sb.append("PowerImpl");
         sb.append("{voltagesAndCurrents=").append(voltagesAndCurrents);
         sb.append(", batteryPowerEquation=").append(batteryPowerEquation);
         sb.append(", capacitorPowerEquation=").append(capacitorPowerEquation);
         sb.append(", accessoryPowerEquation=").append(accessoryPowerEquation);
         sb.append('}');
         return sb.toString();
         }
      }
   }
