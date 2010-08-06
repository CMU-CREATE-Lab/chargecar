package org.chargecar.honda.bms;

/**
 * @author Chris Bartley (bartley@cmu.edu)
 */
public interface BMSEvent
   {
   long getTimestampMilliseconds();

   BMSFault getBMSFault();

   int getNumOnOffCycles();

   int getTimeSincePowerUpInSecs();

   double getSourceCurrentAmps();

   double getLoadCurrentAmps();

   boolean isFanOn();

   boolean isLLIMSet();

   boolean isHLIMSet();

   boolean isCANContactorRequest();

   boolean isHardwareContactorRequest();

   boolean isInterlockTripped();

   boolean isPowerFromLoad();

   boolean isPowerFromSource();

   double getRelativeChargeCurrentLimitPercentage();

   double getRelativeDischargeCurrentLimitPercentage();

   boolean isAreRelaysOn();

   int getStateOfChargePercentage();

   int getNumOfMissingBanks();

   int getNumOfAMissingBank();

   int getNumMissingCells();

   int getNumOfAMissingCell();

   double getPackTotalVoltage();

   double getMinimumCellVoltage();

   double getMaximumCellVoltage();

   double getAverageCellVoltage();

   int getCellNumWithLowestVoltage();

   int getCellNumWithHighestVoltage();

   int getMinimumCellBoardTemp();

   int getMaximumCellBoardTemp();

   int getAverageCellBoardTemp();

   int getCellBoardNumWithLowestTemp();

   int getCellBoardNumWithHighestTemp();

   int getNumLoadsOn();

   double getCellVoltageAboveWhichWeTurnOnItsLoad();

   boolean isDrivingOffWhilePluggedIn();

   boolean isInterlockTripped2();

   boolean isCommunicationFaultWithBankOrCell();

   boolean isChargeOvercurrent();

   boolean isDischargeOvercurrent();

   boolean isOverTemperature();

   boolean isUnderVoltage();

   boolean isOverVoltage();

   int getTotalEnergyInOfBatterySinceManufacture();

   int getTotalEnergyOutOfBatterySinceManufacture();

   int getDepthOfDischarge();

   int getCapacity();

   int getStateOfHealthPercentage();

   int getPackTotalResistance();

   int getMinimumCellResistance();

   int getMaximumCellResistance();

   int getAverageCellResistance();

   int getCellNumWithMinimumResistance();

   int getCellNumWithMaximumResistance();

   int getNumberOfCellsSeen();

   int getPower();

   String toLoggingString();
   }