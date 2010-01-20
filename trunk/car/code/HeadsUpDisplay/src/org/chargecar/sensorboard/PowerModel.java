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
   private final UpdatableVoltages minimumVoltages = UpdatableVoltages.createMinimumVoltages();
   private final UpdatableVoltages maximumVoltages = UpdatableVoltages.createMaximumVoltages();
   private final UpdatableCurrents minimumCurrents = UpdatableCurrents.createMinimumCurrents();
   private final UpdatableCurrents maximumCurrents = UpdatableCurrents.createMaximumCurrents();

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

               // update minimums and maximums
               minimumVoltages.update(voltagesAndCurrents.getVoltages());
               maximumVoltages.update(voltagesAndCurrents.getVoltages());
               minimumCurrents.update(voltagesAndCurrents.getCurrents());
               maximumCurrents.update(voltagesAndCurrents.getCurrents());
               }

            // save the voltages and currents and timestamp
            previousVoltagesAndCurrents = voltagesAndCurrents;
            previousTimestamp = currentTimestamp;

            // notify listeners
            publishEventToListeners(new PowerImpl(voltagesAndCurrents,
                                                  batteryPowerEquation,
                                                  capacitorPowerEquation,
                                                  accessoryPowerEquation,
                                                  minimumVoltages,
                                                  maximumVoltages,
                                                  minimumCurrents,
                                                  maximumCurrents));
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
      private final Voltages minimumVoltages;
      private final Voltages maximumVoltages;
      private final Currents minimumCurrents;
      private final Currents maximumCurrents;

      private PowerImpl(final VoltagesAndCurrents voltagesAndCurrents,
                        final PowerEquation batteryPowerEquation,
                        final PowerEquation capacitorPowerEquation,
                        final PowerEquation accessoryPowerEquation,
                        final Voltages minimumVoltages,
                        final Voltages maximumVoltages,
                        final Currents minimumCurrents,
                        final Currents maximumCurrents)
         {
         this.voltagesAndCurrents = voltagesAndCurrents;
         this.batteryPowerEquation = batteryPowerEquation;
         this.capacitorPowerEquation = capacitorPowerEquation;
         this.accessoryPowerEquation = accessoryPowerEquation;

         // create copies of these for immutability
         this.minimumVoltages = new VoltagesImpl(minimumVoltages);
         this.maximumVoltages = new VoltagesImpl(maximumVoltages);
         this.minimumCurrents = new CurrentsImpl(minimumCurrents);
         this.maximumCurrents = new CurrentsImpl(maximumCurrents);
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

      public Voltages getMinimumVoltages()
         {
         return minimumVoltages;
         }

      public Voltages getMaximumVoltages()
         {
         return maximumVoltages;
         }

      public Currents getMinimumCurrents()
         {
         return minimumCurrents;
         }

      public Currents getMaximumCurrents()
         {
         return maximumCurrents;
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
         if (maximumCurrents != null ? !maximumCurrents.equals(power.maximumCurrents) : power.maximumCurrents != null)
            {
            return false;
            }
         if (maximumVoltages != null ? !maximumVoltages.equals(power.maximumVoltages) : power.maximumVoltages != null)
            {
            return false;
            }
         if (minimumCurrents != null ? !minimumCurrents.equals(power.minimumCurrents) : power.minimumCurrents != null)
            {
            return false;
            }
         if (minimumVoltages != null ? !minimumVoltages.equals(power.minimumVoltages) : power.minimumVoltages != null)
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
         result = 31 * result + (minimumVoltages != null ? minimumVoltages.hashCode() : 0);
         result = 31 * result + (maximumVoltages != null ? maximumVoltages.hashCode() : 0);
         result = 31 * result + (minimumCurrents != null ? minimumCurrents.hashCode() : 0);
         result = 31 * result + (maximumCurrents != null ? maximumCurrents.hashCode() : 0);
         return result;
         }
      }
   }
