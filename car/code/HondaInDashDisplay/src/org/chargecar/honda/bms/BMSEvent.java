package org.chargecar.honda.bms;

import java.math.BigInteger;
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
   private final int relativeChargeCurrentLimitPercentage;     // Maximum regen and charging current accepted. FFh = 100%, 00h = 0%
   private final int relativeDischargeCurrentLimitPercentage;  // Maximum discharging current accepted. FFh = 100%, 00h = 0%
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
   private final int cellBoardNumWithLowestTemp;               // in degrees C
   private final int cellBoardNumWithHighestTemp;              // in degrees C
   private final int numLoadsOn;
   private final double cellVoltageAboveWhichWeTurnOnItsLoad;  // in V, 2.0V is min
   private final byte auxDataState;
   private final byte faultLevelFlags;
   private final int totalEnergyInOfBatterySinceManufacture;   // kWh
   private final int totalEnergyOutOfBatterySinceManufacture;  // kWh
   private final int depthOfDischarge;                         // Ah
   private final int capacity;                                 // Ah
   private final int stateOfHealthPercentage;                  // u Ohms
   private final int packTotalResistance;                      // u Ohms
   private final int minimumCellResistance;                    // u Ohms
   private final int maximumCellResistance;                    // u Ohms
   private final int averageCellResistance;                    // u Ohms
   private final int cellNumWithMinimumResistance;
   private final int cellNumWithMaximumResistance;
   private final int numberOfCellsSeen;
   private final int power;                                    // W
   private final double[] cellVoltages;                           // in V, 2.0V is min
   private final int[] cellTemperatures;                       // in degrees C
   private final int[] cellResistances;                        // u Ohms

   public BMSEvent(final Date timestamp,
                   final BMSFault bmsFault,
                   final int numOnOffCycles,
                   final int timeSincePowerUpInSecs,
                   final double sourceCurrentAmps,
                   final double loadCurrentAmps,
                   final byte variousIOStateBits,
                   final int relativeChargeCurrentLimitPercentage,
                   final int relativeDischargeCurrentLimitPercentage,
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
                   final byte faultLevelFlags,
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
      this.faultLevelFlags = faultLevelFlags;
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

   @Override
   public String toString()
      {
      final StringBuilder sb = new StringBuilder();
      sb.append("BMSEvent");
      sb.append("{bmsFault=").append(bmsFault);
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
      sb.append(", faultLevelFlags=").append(faultLevelFlags);
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
      sb.append(", " + label + "=").append("[");
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
      sb.append(", " + label + "=").append("[");
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
      // TODO
      return null;
      }
   }
