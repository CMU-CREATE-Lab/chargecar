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
   private final double sourceCurrentAmps;             // signed, positive when discharging
   private final double loadCurrentAmps;               // signed, positive when discharging
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
   private final double minimumCellVoltage;                    // 2.0V is min
   private final double maximumCellVoltage;                    // 2.0V is min
   private final double averageCellVoltage;                    // 2.0V is min
   private final int cellNumWithLowestVoltage;
   private final int cellNumWithHighestVoltage;
   private final int minimumCellBoardTemp;                     // in degrees C
   private final int maximumCellBoardTemp;                     // in degrees C
   private final int averageCellBoardTemp;                     // in degrees C
   private final int cellBoardNumWithLowestTemp;               // in degrees C
   private final int cellBoardNumWithHighestTemp;              // in degrees C
   private final int numLoadsOn;
   private final double cellVoltageAboveWhichWeTurnOnItsLoad;  // 2.0V is min
   private final byte auxDataState;
   private final byte faultLevelFlags;
   private final int totalEnergyInOfBatterySinceManufacture;
   private final int totalEnergyOutOfBatterySinceManufacture;
   private final int depthOfDischarge;
   private final int capacity;
   private final int stateOfHealthPercentage;

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
                   final int stateOfHealthPercentage)
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
