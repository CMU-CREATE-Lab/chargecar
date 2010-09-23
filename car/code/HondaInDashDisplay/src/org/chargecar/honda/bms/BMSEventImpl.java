package org.chargecar.honda.bms;

import java.math.BigInteger;
import java.util.Date;
import edu.cmu.ri.createlab.util.ArrayUtils;
import org.chargecar.serial.streaming.BaseStreamingSerialPortEvent;

/**
 * @author Chris Bartley (bartley@cmu.edu)
 */
final class BMSEventImpl extends BaseStreamingSerialPortEvent implements BMSEvent
   {
   private final BMSFault bmsFault;
   private final int numOnOffCycles;
   private final int timeSincePowerUpInSecs;
   private final double sourceCurrentAmps;                     // signed, positive when discharging
   private final double loadCurrentAmps;                       // signed, positive when discharging
   private final boolean isFanOn;
   private final boolean isLLIMSet;
   private final boolean isHLIMSet;
   private final boolean isCANContactorRequest;
   private final boolean isHardwareContactorRequest;
   private final boolean isInterlockTripped;
   private final boolean isPowerFromLoad;
   private final boolean isPowerFromSource;
   private final double relativeChargeCurrentLimitPercentage;     // Maximum regen and charging current accepted. FFh = 100%, 00h = 0%
   private final double relativeDischargeCurrentLimitPercentage;  // Maximum discharging current accepted. FFh = 100%, 00h = 0%
   private final boolean areRelaysOn;
   private final int stateOfChargePercentage;
   private final int numOfMissingBanks;
   private final int numOfAMissingBank;
   private final int numMissingCells;
   private final int numOfAMissingCell;
   private final double packTotalVoltage;                      // 0 to 6.55 kV
   private final double minimumCellVoltage;                    // in V, 2.0V is min
   private final double maximumCellVoltage;                    // in V, 2.0V is min
   private final double averageCellVoltage;                    // in V, 2.0V is min
   private final int cellNumWithLowestVoltage;
   private final int cellNumWithHighestVoltage;
   private final int minimumCellBoardTemp;                     // in degrees C
   private final int maximumCellBoardTemp;                     // in degrees C
   private final int averageCellBoardTemp;                     // in degrees C
   private final int cellBoardNumWithLowestTemp;
   private final int cellBoardNumWithHighestTemp;
   private final int numLoadsOn;
   private final double cellVoltageAboveWhichWeTurnOnItsLoad;  // in V, 2.0V is min
   private final byte auxDataState;
   private final boolean isDrivingOffWhilePluggedIn;
   private final boolean isInterlockTripped2;
   private final boolean isCommunicationFaultWithBankOrCell;
   private final boolean isChargeOvercurrent;
   private final boolean isDischargeOvercurrent;
   private final boolean isOverTemperature;
   private final boolean isUnderVoltage;
   private final boolean isOverVoltage;
   private final int totalEnergyInOfBatterySinceManufacture;   // kWh
   private final int totalEnergyOutOfBatterySinceManufacture;  // kWh
   private final int depthOfDischarge;                         // Ah
   private final int capacity;                                 // Ah
   private final int stateOfHealthPercentage;
   private final int packTotalResistance;                      // u Ohms
   private final int minimumCellResistance;                    // u Ohms
   private final int maximumCellResistance;                    // u Ohms
   private final int averageCellResistance;                    // u Ohms
   private final int cellNumWithMinimumResistance;
   private final int cellNumWithMaximumResistance;
   private final int numberOfCellsSeen;
   private final int power;                                    // W
   private final double[] cellVoltages;                        // in V, 2.0V is min
   private final int[] cellTemperatures;                       // in degrees C
   private final int[] cellResistances;                        // u Ohms

   BMSEventImpl(final Date timestamp,
                final BMSFault bmsFault,
                final int numOnOffCycles,
                final int timeSincePowerUpInSecs,
                final double sourceCurrentAmps,
                final double loadCurrentAmps,
                final byte variousIOStateBits,
                final double relativeChargeCurrentLimitPercentage,
                final double relativeDischargeCurrentLimitPercentage,
                final boolean areRelaysOn,
                final int stateOfChargePercentage,
                final int numOfMissingBanks,
                final int numOfAMissingBank,
                final int numMissingCells,
                final int numOfAMissingCell,
                final double packTotalVoltage,
                final double minimumCellVoltage,
                final double maximumCellVoltage,
                final double averageCellVoltage,
                final int cellNumWithLowestVoltage,
                final int cellNumWithHighestVoltage,
                final int minimumCellBoardTemp,
                final int maximumCellBoardTemp,
                final int averageCellBoardTemp,
                final int cellBoardNumWithLowestTemp,
                final int cellBoardNumWithHighestTemp,
                final int numLoadsOn,
                final double cellVoltageAboveWhichWeTurnOnItsLoad,
                final byte auxDataState,
                final byte levelFaultFlags,
                final int totalEnergyInOfBatterySinceManufacture,
                final int totalEnergyOutOfBatterySinceManufacture,
                final int depthOfDischarge,
                final int capacity,
                final int stateOfHealthPercentage,
                final int packTotalResistance,
                final int minimumCellResistance,
                final int maximumCellResistance,
                final int averageCellResistance,
                final int cellNumWithMinimumResistance,
                final int cellNumWithMaximumResistance,
                final int numberOfCellsSeen,
                final int power,
                final double[] cellVoltages,
                final int[] cellTemperatures,
                final int[] cellResistances)
      {
      super(timestamp);
      this.bmsFault = bmsFault;
      this.numOnOffCycles = numOnOffCycles;
      this.timeSincePowerUpInSecs = timeSincePowerUpInSecs;
      this.sourceCurrentAmps = sourceCurrentAmps;
      this.loadCurrentAmps = loadCurrentAmps;
      final BigInteger variousIOState = BigInteger.valueOf(variousIOStateBits);
      this.isFanOn = variousIOState.testBit(7);
      this.isLLIMSet = variousIOState.testBit(6);
      this.isHLIMSet = variousIOState.testBit(5);
      this.isCANContactorRequest = variousIOState.testBit(4);
      this.isHardwareContactorRequest = variousIOState.testBit(3);
      this.isInterlockTripped = variousIOState.testBit(2);
      this.isPowerFromLoad = variousIOState.testBit(1);
      this.isPowerFromSource = variousIOState.testBit(0);
      this.relativeChargeCurrentLimitPercentage = relativeChargeCurrentLimitPercentage;
      this.relativeDischargeCurrentLimitPercentage = relativeDischargeCurrentLimitPercentage;
      this.areRelaysOn = areRelaysOn;
      this.stateOfChargePercentage = stateOfChargePercentage;
      this.numOfMissingBanks = numOfMissingBanks;
      this.numOfAMissingBank = numOfAMissingBank;
      this.numMissingCells = numMissingCells;
      this.numOfAMissingCell = numOfAMissingCell;
      this.packTotalVoltage = packTotalVoltage;
      this.minimumCellVoltage = minimumCellVoltage;
      this.maximumCellVoltage = maximumCellVoltage;
      this.averageCellVoltage = averageCellVoltage;
      this.cellNumWithLowestVoltage = cellNumWithLowestVoltage;
      this.cellNumWithHighestVoltage = cellNumWithHighestVoltage;
      this.minimumCellBoardTemp = minimumCellBoardTemp;
      this.maximumCellBoardTemp = maximumCellBoardTemp;
      this.averageCellBoardTemp = averageCellBoardTemp;
      this.cellBoardNumWithLowestTemp = cellBoardNumWithLowestTemp;
      this.cellBoardNumWithHighestTemp = cellBoardNumWithHighestTemp;
      this.numLoadsOn = numLoadsOn;
      this.cellVoltageAboveWhichWeTurnOnItsLoad = cellVoltageAboveWhichWeTurnOnItsLoad;
      this.auxDataState = auxDataState;
      final BigInteger levelFault = BigInteger.valueOf(levelFaultFlags);
      this.isDrivingOffWhilePluggedIn = levelFault.testBit(0);
      this.isInterlockTripped2 = levelFault.testBit(1);
      this.isCommunicationFaultWithBankOrCell = levelFault.testBit(2);
      this.isChargeOvercurrent = levelFault.testBit(3);
      this.isDischargeOvercurrent = levelFault.testBit(4);
      this.isOverTemperature = levelFault.testBit(5);
      this.isUnderVoltage = levelFault.testBit(6);
      this.isOverVoltage = levelFault.testBit(7);
      this.totalEnergyInOfBatterySinceManufacture = totalEnergyInOfBatterySinceManufacture;
      this.totalEnergyOutOfBatterySinceManufacture = totalEnergyOutOfBatterySinceManufacture;
      this.depthOfDischarge = depthOfDischarge;
      this.capacity = capacity;
      this.stateOfHealthPercentage = stateOfHealthPercentage;
      this.packTotalResistance = packTotalResistance;
      this.minimumCellResistance = minimumCellResistance;
      this.maximumCellResistance = maximumCellResistance;
      this.averageCellResistance = averageCellResistance;
      this.cellNumWithMinimumResistance = cellNumWithMinimumResistance;
      this.cellNumWithMaximumResistance = cellNumWithMaximumResistance;
      this.numberOfCellsSeen = numberOfCellsSeen;
      this.power = power;
      this.cellVoltages = cellVoltages.clone();
      this.cellTemperatures = cellTemperatures.clone();
      this.cellResistances = cellResistances.clone();
      }

   public BMSFault getBMSFault()
      {
      return bmsFault;
      }

   public int getNumOnOffCycles()
      {
      return numOnOffCycles;
      }

   public int getTimeSincePowerUpInSecs()
      {
      return timeSincePowerUpInSecs;
      }

   /** Returns a signed value in amps, positive when discharging. */
   public double getSourceCurrentAmps()
   {
   return sourceCurrentAmps;
   }

   /** Returns a signed value in amps, positive when discharging. */
   public double getLoadCurrentAmps()
   {
   return loadCurrentAmps;
   }

   public boolean isFanOn()
      {
      return isFanOn;
      }

   public boolean isLLIMSet()
      {
      return isLLIMSet;
      }

   public boolean isHLIMSet()
      {
      return isHLIMSet;
      }

   public boolean isCANContactorRequest()
      {
      return isCANContactorRequest;
      }

   public boolean isHardwareContactorRequest()
      {
      return isHardwareContactorRequest;
      }

   public boolean isInterlockTripped()
      {
      return isInterlockTripped;
      }

   public boolean isPowerFromLoad()
      {
      return isPowerFromLoad;
      }

   public boolean isPowerFromSource()
      {
      return isPowerFromSource;
      }

   /** Returns the maximum regen and charging current accepted as a percentage. */
   public double getRelativeChargeCurrentLimitPercentage()
   {
   return relativeChargeCurrentLimitPercentage;
   }

   /** Returns the maximum discharging current accepted as a percentage. */
   public double getRelativeDischargeCurrentLimitPercentage()
   {
   return relativeDischargeCurrentLimitPercentage;
   }

   public boolean isAreRelaysOn()
      {
      return areRelaysOn;
      }

   public int getStateOfChargePercentage()
      {
      return stateOfChargePercentage;
      }

   public int getNumOfMissingBanks()
      {
      return numOfMissingBanks;
      }

   public int getNumOfAMissingBank()
      {
      return numOfAMissingBank;
      }

   public int getNumMissingCells()
      {
      return numMissingCells;
      }

   public int getNumOfAMissingCell()
      {
      return numOfAMissingCell;
      }

   /** Returns the total voltage of the pack, within the range [0, 6.55] kV. */
   public double getPackTotalVoltage()
   {
   return packTotalVoltage;
   }

   /** Returns the minimum cell voltage, with the lowest possible value being 2.0 volts. */
   public double getMinimumCellVoltage()
   {
   return minimumCellVoltage;
   }

   /** Returns the maximum cell voltage, with the lowest possible value being 2.0 volts. */
   public double getMaximumCellVoltage()
   {
   return maximumCellVoltage;
   }

   /** Returns the average cell voltage, with the lowest possible value being 2.0 volts. */
   public double getAverageCellVoltage()
   {
   return averageCellVoltage;
   }

   public int getCellNumWithLowestVoltage()
      {
      return cellNumWithLowestVoltage;
      }

   public int getCellNumWithHighestVoltage()
      {
      return cellNumWithHighestVoltage;
      }

   /** Returns the minimum cell temperature in degrees Celsius. */
   public int getMinimumCellBoardTemp()
   {
   return minimumCellBoardTemp;
   }

   /** Returns the maximum cell temperature in degrees Celsius. */
   public int getMaximumCellBoardTemp()
   {
   return maximumCellBoardTemp;
   }

   /** Returns the average cell temperature in degrees Celsius. */
   public int getAverageCellBoardTemp()
   {
   return averageCellBoardTemp;
   }

   public int getCellBoardNumWithLowestTemp()
      {
      return cellBoardNumWithLowestTemp;
      }

   public int getCellBoardNumWithHighestTemp()
      {
      return cellBoardNumWithHighestTemp;
      }

   public int getNumLoadsOn()
      {
      return numLoadsOn;
      }

   /** Returns the voltage, with the lowest possible value being 2.0 volts. */
   public double getCellVoltageAboveWhichWeTurnOnItsLoad()
   {
   return cellVoltageAboveWhichWeTurnOnItsLoad;
   }

   public boolean isDrivingOffWhilePluggedIn()
      {
      return isDrivingOffWhilePluggedIn;
      }

   public boolean isInterlockTripped2()
      {
      return isInterlockTripped2;
      }

   public boolean isCommunicationFaultWithBankOrCell()
      {
      return isCommunicationFaultWithBankOrCell;
      }

   public boolean isChargeOvercurrent()
      {
      return isChargeOvercurrent;
      }

   public boolean isDischargeOvercurrent()
      {
      return isDischargeOvercurrent;
      }

   public boolean isOverTemperature()
      {
      return isOverTemperature;
      }

   public boolean isUnderVoltage()
      {
      return isUnderVoltage;
      }

   public boolean isOverVoltage()
      {
      return isOverVoltage;
      }

   /** Returns the total energy in (kWh) of the battery since manufacture. */
   public int getTotalEnergyInOfBatterySinceManufacture()
   {
   return totalEnergyInOfBatterySinceManufacture;
   }

   /** Returns the total energy out (kWh) of the battery since manufacture. */
   public int getTotalEnergyOutOfBatterySinceManufacture()
   {
   return totalEnergyOutOfBatterySinceManufacture;
   }

   /** Returns the depth of discharge in Ah. */
   public int getDepthOfDischarge()
   {
   return depthOfDischarge;
   }

   /** Returns the capacity in Ah. */
   public int getCapacity()
   {
   return capacity;
   }

   public int getStateOfHealthPercentage()
      {
      return stateOfHealthPercentage;
      }

   /** Returns the total resistance of the pack in u Ohms. */
   public int getPackTotalResistance()
   {
   return packTotalResistance;
   }

   /** Returns the minimum cell resistance u Ohms. */
   public int getMinimumCellResistance()
   {
   return minimumCellResistance;
   }

   /** Returns the maximum cell resistance u Ohms. */
   public int getMaximumCellResistance()
   {
   return maximumCellResistance;
   }

   /** Returns the average cell resistance u Ohms. */
   public int getAverageCellResistance()
   {
   return averageCellResistance;
   }

   public int getCellNumWithMinimumResistance()
      {
      return cellNumWithMinimumResistance;
      }

   public int getCellNumWithMaximumResistance()
      {
      return cellNumWithMaximumResistance;
      }

   public int getNumberOfCellsSeen()
      {
      return numberOfCellsSeen;
      }

   /** Returns the power in W. */
   public int getPower()
   {
   return power;
   }

   @Override
   public String toString()
      {
      final StringBuilder sb = new StringBuilder();
      sb.append("BMSEvent");
      sb.append("{");
      sb.append("timestamp=").append(getTimestampMilliseconds());
      sb.append(", bmsFault=").append(bmsFault);
      sb.append(", numOnOffCycles=").append(numOnOffCycles);
      sb.append(", timeSincePowerUpInSecs=").append(timeSincePowerUpInSecs);
      sb.append(", sourceCurrentAmps=").append(sourceCurrentAmps);
      sb.append(", loadCurrentAmps=").append(loadCurrentAmps);
      sb.append(", isFanOn=").append(isFanOn);
      sb.append(", isLLIMSet=").append(isLLIMSet);
      sb.append(", isHLIMSet=").append(isHLIMSet);
      sb.append(", isCANContactorRequest=").append(isCANContactorRequest);
      sb.append(", isHardwareContactorRequest=").append(isHardwareContactorRequest);
      sb.append(", isInterlockTripped=").append(isInterlockTripped);
      sb.append(", isPowerFromLoad=").append(isPowerFromLoad);
      sb.append(", isPowerFromSource=").append(isPowerFromSource);
      sb.append(", relativeChargeCurrentLimitPercentage=").append(relativeChargeCurrentLimitPercentage);
      sb.append(", relativeDischargeCurrentLimitPercentage=").append(relativeDischargeCurrentLimitPercentage);
      sb.append(", areRelaysOn=").append(areRelaysOn);
      sb.append(", stateOfChargePercentage=").append(stateOfChargePercentage);
      sb.append(", numOfMissingBanks=").append(numOfMissingBanks);
      sb.append(", numOfAMissingBank=").append(numOfAMissingBank);
      sb.append(", numMissingCells=").append(numMissingCells);
      sb.append(", numOfAMissingCell=").append(numOfAMissingCell);
      sb.append(", packTotalVoltage=").append(packTotalVoltage);
      sb.append(", minimumCellVoltage=").append(minimumCellVoltage);
      sb.append(", maximumCellVoltage=").append(maximumCellVoltage);
      sb.append(", averageCellVoltage=").append(averageCellVoltage);
      sb.append(", cellNumWithLowestVoltage=").append(cellNumWithLowestVoltage);
      sb.append(", cellNumWithHighestVoltage=").append(cellNumWithHighestVoltage);
      sb.append(", minimumCellBoardTemp=").append(minimumCellBoardTemp);
      sb.append(", maximumCellBoardTemp=").append(maximumCellBoardTemp);
      sb.append(", averageCellBoardTemp=").append(averageCellBoardTemp);
      sb.append(", cellBoardNumWithLowestTemp=").append(cellBoardNumWithLowestTemp);
      sb.append(", cellBoardNumWithHighestTemp=").append(cellBoardNumWithHighestTemp);
      sb.append(", numLoadsOn=").append(numLoadsOn);
      sb.append(", cellVoltageAboveWhichWeTurnOnItsLoad=").append(cellVoltageAboveWhichWeTurnOnItsLoad);
      sb.append(", auxDataState=").append(auxDataState);
      sb.append(", isDrivingOffWhilePluggedIn=").append(isDrivingOffWhilePluggedIn);
      sb.append(", isInterlockTripped2=").append(isInterlockTripped2);
      sb.append(", isCommunicationFaultWithBankOrCell=").append(isCommunicationFaultWithBankOrCell);
      sb.append(", isChargeOvercurrent=").append(isChargeOvercurrent);
      sb.append(", isDischargeOvercurrent=").append(isDischargeOvercurrent);
      sb.append(", isOverTemperature=").append(isOverTemperature);
      sb.append(", isUnderVoltage=").append(isUnderVoltage);
      sb.append(", isOverVoltage=").append(isOverVoltage);
      sb.append(", totalEnergyInOfBatterySinceManufacture=").append(totalEnergyInOfBatterySinceManufacture);
      sb.append(", totalEnergyOutOfBatterySinceManufacture=").append(totalEnergyOutOfBatterySinceManufacture);
      sb.append(", depthOfDischarge=").append(depthOfDischarge);
      sb.append(", capacity=").append(capacity);
      sb.append(", stateOfHealthPercentage=").append(stateOfHealthPercentage);
      sb.append(", packTotalResistance=").append(packTotalResistance);
      sb.append(", minimumCellResistance=").append(minimumCellResistance);
      sb.append(", maximumCellResistance=").append(maximumCellResistance);
      sb.append(", averageCellResistance=").append(averageCellResistance);
      sb.append(", cellNumWithMinimumResistance=").append(cellNumWithMinimumResistance);
      sb.append(", cellNumWithMaximumResistance=").append(cellNumWithMaximumResistance);
      sb.append(", numberOfCellsSeen=").append(numberOfCellsSeen);
      sb.append(", power=").append(power);

      appendArrayData(sb, "voltages", cellVoltages);
      appendArrayData(sb, "temperatures", cellTemperatures);
      appendArrayData(sb, "resistances", cellResistances);

      sb.append('}');
      return sb.toString();
      }

   private void appendArrayData(final StringBuilder sb, final String label, final double[] a)
      {
      sb.append(", ").append(label).append("=").append("[");
      for (int i = 0; i < a.length; i++)
         {
         sb.append(a[i]);
         if (i < a.length - 1)
            {
            sb.append(", ");
            }
         }
      sb.append("").append("]");
      }

   private void appendArrayData(final StringBuilder sb, final String label, final int[] a)
      {
      sb.append(", ").append(label).append("=").append("[");
      for (int i = 0; i < a.length; i++)
         {
         sb.append(a[i]);
         if (i < a.length - 1)
            {
            sb.append(", ");
            }
         }
      sb.append("").append("]");
      }

   @Override
   public String toLoggingString()
      {
      final StringBuilder sb = new StringBuilder();
      sb.append("BMSEvent");
      sb.append('{');
      sb.append(getTimestampMilliseconds()).append(TO_STRING_DELIMITER);
      sb.append(bmsFault.getCode()).append(TO_STRING_DELIMITER);
      sb.append(isLLIMSet).append(TO_STRING_DELIMITER);
      sb.append(isHLIMSet).append(TO_STRING_DELIMITER);

      sb.append(minimumCellBoardTemp).append(TO_STRING_DELIMITER);
      sb.append(maximumCellBoardTemp).append(TO_STRING_DELIMITER);
      sb.append(averageCellBoardTemp).append(TO_STRING_DELIMITER);
      sb.append(cellBoardNumWithLowestTemp).append(TO_STRING_DELIMITER);
      sb.append(cellBoardNumWithHighestTemp).append(TO_STRING_DELIMITER);

      sb.append(minimumCellVoltage).append(TO_STRING_DELIMITER);
      sb.append(maximumCellVoltage).append(TO_STRING_DELIMITER);
      sb.append(averageCellVoltage).append(TO_STRING_DELIMITER);
      sb.append(cellNumWithLowestVoltage).append(TO_STRING_DELIMITER);
      sb.append(cellNumWithHighestVoltage).append(TO_STRING_DELIMITER);

      sb.append(packTotalVoltage).append(TO_STRING_DELIMITER);
      sb.append(sourceCurrentAmps).append(TO_STRING_DELIMITER);
      sb.append(loadCurrentAmps).append(TO_STRING_DELIMITER);
      sb.append(depthOfDischarge).append(TO_STRING_DELIMITER);
      sb.append(capacity).append(TO_STRING_DELIMITER);

      sb.append(power).append(TO_STRING_DELIMITER);
      sb.append(stateOfChargePercentage).append(TO_STRING_DELIMITER);
      sb.append(stateOfHealthPercentage).append(TO_STRING_DELIMITER);
      sb.append(totalEnergyInOfBatterySinceManufacture).append(TO_STRING_DELIMITER);
      sb.append(totalEnergyOutOfBatterySinceManufacture).append(TO_STRING_DELIMITER);

      sb.append(isOverTemperature).append(TO_STRING_DELIMITER);
      sb.append(isUnderVoltage).append(TO_STRING_DELIMITER);
      sb.append(isOverVoltage).append(TO_STRING_DELIMITER);
      sb.append(isChargeOvercurrent).append(TO_STRING_DELIMITER);
      sb.append(isDischargeOvercurrent).append(TO_STRING_DELIMITER);
      sb.append(isCommunicationFaultWithBankOrCell).append(TO_STRING_DELIMITER);
      sb.append(isInterlockTripped2).append(TO_STRING_DELIMITER);
      sb.append(ArrayUtils.arrayToString(cellVoltages, TO_STRING_DELIMITER)).append(TO_STRING_DELIMITER);
      sb.append(ArrayUtils.arrayToString(cellTemperatures, TO_STRING_DELIMITER));
      sb.append('}');
      return sb.toString();
      }
   }
