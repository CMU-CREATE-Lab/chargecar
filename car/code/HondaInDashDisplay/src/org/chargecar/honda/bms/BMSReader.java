package org.chargecar.honda.bms;

import java.nio.ByteBuffer;
import java.util.Date;
import edu.cmu.ri.createlab.serial.config.BaudRate;
import edu.cmu.ri.createlab.serial.config.CharacterSize;
import edu.cmu.ri.createlab.serial.config.FlowControl;
import edu.cmu.ri.createlab.serial.config.Parity;
import edu.cmu.ri.createlab.serial.config.SerialIOConfiguration;
import edu.cmu.ri.createlab.serial.config.StopBits;
import edu.cmu.ri.createlab.util.ByteUtils;
import org.apache.log4j.Logger;
import org.chargecar.honda.HondaConstants;
import org.chargecar.serial.streaming.DefaultSerialIOManager;
import org.chargecar.serial.streaming.SerialIOManager;
import org.chargecar.serial.streaming.StreamingSerialPortReader;

/**
 * @author Chris Bartley (bartley@cmu.edu)
 */
class BMSReader extends StreamingSerialPortReader<BMSEvent>
   {
   private static final Logger LOG = Logger.getLogger(BMSReader.class);
   private static final Character SENTENCE_DELIMETER = 0x1B;      // ESC
   private static final Character SENTENCE_START_CHAR_1 = 0x5B;   // [
   private static final Character SENTENCE_START_CHAR_2 = 0x48;   // H
   private static final Character GROUP_DELIMETER = ' ';          // SPACE
   private static final int SENTENCE_LENGTH_IN_BYTES = 1653;

   private static final int CONTENT_GROUP_OFFSET = 2;
   private static final int CONTENT_GROUP_LENGTH_IN_BYTES = 64;

   private static final int AUX_GROUP_OFFSET = 67;
   private static final int AUX_GROUP_LENGTH_IN_BYTES = 46;

   private static final int CELL_DATA_GROUP_1_OFFSET = 114;
   private static final int CELL_DATA_GROUP_2_OFFSET = 627;
   private static final int CELL_DATA_GROUP_3_OFFSET = 1140;
   private static final int CELL_DATA_GROUP_LENGTH_IN_BYTES = 512;

   BMSReader(final String serialPortName)
      {
      this(new DefaultSerialIOManager(BMSReader.class.getClass().getSimpleName(),
                                      new SerialIOConfiguration(serialPortName,
                                                                BaudRate.BAUD_19200,
                                                                CharacterSize.EIGHT,
                                                                Parity.NONE,
                                                                StopBits.ONE,
                                                                FlowControl.NONE)));
      }

   BMSReader(final SerialIOManager serialIOManager)
      {
      super(serialIOManager, SENTENCE_DELIMETER);
      }

   protected void processSentence(final Date timestamp, final byte[] sentenceBytes)
      {
      if (sentenceBytes != null)
         {
         // do some quick sanity checks to make sure the sentence is valid
         if (sentenceBytes.length == SENTENCE_LENGTH_IN_BYTES &&
             SENTENCE_START_CHAR_1.equals((char)sentenceBytes[0]) &&
             SENTENCE_START_CHAR_2.equals((char)sentenceBytes[1]) &&
             GROUP_DELIMETER.equals((char)sentenceBytes[66]) &&
             GROUP_DELIMETER.equals((char)sentenceBytes[113]) &&
             GROUP_DELIMETER.equals((char)sentenceBytes[626]) &&
             GROUP_DELIMETER.equals((char)sentenceBytes[1139]) &&
             GROUP_DELIMETER.equals((char)sentenceBytes[1652]))
            {
            // convert the content group bytes
            final byte[] contentGroup = ByteUtils.asciiHexBytesToByteArray(sentenceBytes, CONTENT_GROUP_OFFSET, CONTENT_GROUP_LENGTH_IN_BYTES);

            // convert the auxiliary group bytes
            final byte[] auxiliaryGroup = ByteUtils.asciiHexBytesToByteArray(sentenceBytes, AUX_GROUP_OFFSET, AUX_GROUP_LENGTH_IN_BYTES);

            // convert the cell data voltages group bytes
            final byte[] cellDataVoltages = ByteUtils.asciiHexBytesToByteArray(sentenceBytes, CELL_DATA_GROUP_1_OFFSET, CELL_DATA_GROUP_LENGTH_IN_BYTES);

            // convert the cell data temperatures group bytes
            final byte[] cellDataTemperatures = ByteUtils.asciiHexBytesToByteArray(sentenceBytes, CELL_DATA_GROUP_2_OFFSET, CELL_DATA_GROUP_LENGTH_IN_BYTES);

            // convert the cell data resistances group bytes
            final byte[] cellDataResistances = ByteUtils.asciiHexBytesToByteArray(sentenceBytes, CELL_DATA_GROUP_3_OFFSET, CELL_DATA_GROUP_LENGTH_IN_BYTES);

            publishDataEvent(createBMSEvent(timestamp,
                                            contentGroup,
                                            auxiliaryGroup,
                                            cellDataVoltages,
                                            cellDataTemperatures,
                                            cellDataResistances));
            }
         else
            {
            LOG.error("BMSReader.processSentence(): Unrecognized sentence format");
            }
         }
      else
         {
         LOG.error("BMSReader.processSentence(): Null sentence");
         }
      }

   private BMSEvent createBMSEvent(final Date timestamp,
                                   final byte[] contentGroup,
                                   final byte[] auxiliaryGroup,
                                   final byte[] cellDataVoltages,
                                   final byte[] cellDataTemperatures,
                                   final byte[] cellDataResistances)
      {

      final ByteBuffer contentGroupByteBuffer = ByteBuffer.wrap(contentGroup);

      final BMSFault bmsFault = BMSFault.findByCode(ByteUtils.unsignedByteToInt(contentGroupByteBuffer.get(0)));

      final int numOnOffCycles = ByteUtils.unsignedShortToInt(contentGroupByteBuffer.getShort(1));

      final int timeSincePowerUpInSecs = convertBytesToInt(contentGroupByteBuffer.get(3),
                                                           contentGroupByteBuffer.get(4),
                                                           contentGroupByteBuffer.get(5));

      // reported value is in 100 mA units, so divide by 10 to get A
      final double sourceCurrentAmps = contentGroupByteBuffer.getShort(6) / 10.0;

      // reported value is in 100 mA units, so divide by 10 to get A
      final double loadCurrentAmps = contentGroupByteBuffer.getShort(8) / 10.0;

      final byte variousIOState = contentGroupByteBuffer.get(10);

      final double relativeChargeCurrentLimitPercentage = (double)ByteUtils.unsignedByteToInt(contentGroupByteBuffer.get(11)) / 255.0 * 100.0;

      final double relativeDischargeCurrentLimitPercentage = (double)ByteUtils.unsignedByteToInt(contentGroupByteBuffer.get(12)) / 255.0 * 100.0;

      final boolean areRelaysOn = contentGroupByteBuffer.get(13) != 0;

      final int stateOfChargePercentage = ByteUtils.unsignedByteToInt(contentGroupByteBuffer.get(14));

      // reported value is in 100 mV units, so divide by 10 to get V
      final double packTotalVoltage = ByteUtils.unsignedShortToInt(contentGroupByteBuffer.getShort(15)) / 10.0;

      final byte missingBankInfo = contentGroupByteBuffer.get(17);
      final int numOfMissingBanks = missingBankInfo & 0xF;           // low nibble is number of missing banks
      final int numOfAMissingBank = (missingBankInfo >> 4) & 0xF;    // high nibble is number of a missing bank

      final int numMissingCells = ByteUtils.unsignedByteToInt(contentGroupByteBuffer.get(18));

      final int numOfAMissingCell = ByteUtils.unsignedByteToInt(contentGroupByteBuffer.get(19));

      // reported value is in 10 mV units with a min of 2.0 V, so divide by 100 and add 2 to get V
      final double minimumCellVoltage = ByteUtils.unsignedByteToInt(contentGroupByteBuffer.get(20)) / 100.0 + 2.0;

      final int cellNumWithLowestVoltage = ByteUtils.unsignedByteToInt(contentGroupByteBuffer.get(21));

      // reported value is in 10 mV units with a min of 2.0 V, so divide by 100 and add 2 to get V
      final double averageCellVoltage = ByteUtils.unsignedByteToInt(contentGroupByteBuffer.get(22)) / 100.0 + 2.0;

      // reported value is in 10 mV units with a min of 2.0 V, so divide by 100 and add 2 to get V
      final double maximumCellVoltage = ByteUtils.unsignedByteToInt(contentGroupByteBuffer.get(23)) / 100.0 + 2.0;

      final int cellNumWithHighestVoltage = ByteUtils.unsignedByteToInt(contentGroupByteBuffer.get(24));

      // reported value is in degrees C + 0x80, value is signed -127 to 127 (e.g. 0x80 = 0 degrees C, 0x81 = 1 degrees C, 0x7F = -1)
      final int minimumCellBoardTemp = (byte)(contentGroupByteBuffer.get(25) - 0x80);

      final int cellBoardNumWithLowestTemp = ByteUtils.unsignedByteToInt(contentGroupByteBuffer.get(26));

      // reported value is in degrees C + 0x80, value is signed -127 to 127 (e.g. 0x80 = 0 degrees C, 0x81 = 1 degrees C, 0x7F = -1)
      final int averageCellBoardTemp = (byte)(contentGroupByteBuffer.get(27) - 0x80);

      // reported value is in degrees C + 0x80, value is signed -127 to 127 (e.g. 0x80 = 0 degrees C, 0x81 = 1 degrees C, 0x7F = -1)
      final int maximumCellBoardTemp = (byte)(contentGroupByteBuffer.get(28) - 0x80);

      final int cellBoardNumWithHighestTemp = ByteUtils.unsignedByteToInt(contentGroupByteBuffer.get(29));

      final int numLoadsOn = ByteUtils.unsignedByteToInt(contentGroupByteBuffer.get(30));

      // reported value is in 10 mV units with a min of 2.0 V, so divide by 100 and add 2 to get V
      final double cellVoltageAboveWhichWeTurnOnItsLoad = ByteUtils.unsignedByteToInt(contentGroupByteBuffer.get(31)) / 100.0 + 2.0;

      // ---------------------------------------------------------------------------------------------------------------

      final ByteBuffer auxiliaryGroupByteBuffer = ByteBuffer.wrap(auxiliaryGroup);

      // TODO: do something with this value (also verify whether it's supposed to be unsigned)
      final byte auxDataState = auxiliaryGroupByteBuffer.get(0);

      final byte levelFaultFlags = auxiliaryGroupByteBuffer.get(1);

      // total energy in of the battery, since manufacture. Unsigned, overflows back to 0 [kWH]
      final int totalEnergyInOfBatterySinceManufacture = convertBytesToInt(auxiliaryGroupByteBuffer.get(2),
                                                                           auxiliaryGroupByteBuffer.get(3),
                                                                           auxiliaryGroupByteBuffer.get(4));

      // total energy out of the battery, since manufacture. Unsigned, overflows back to 0 [kWH]
      final int totalEnergyOutOfBatterySinceManufacture = convertBytesToInt(auxiliaryGroupByteBuffer.get(5),
                                                                            auxiliaryGroupByteBuffer.get(6),
                                                                            auxiliaryGroupByteBuffer.get(7));

      final int depthOfDischarge = ByteUtils.unsignedShortToInt(auxiliaryGroupByteBuffer.getShort(8));

      final int capacity = ByteUtils.unsignedShortToInt(auxiliaryGroupByteBuffer.getShort(10));

      final int stateOfHealthPercentage = ByteUtils.unsignedByteToInt(auxiliaryGroupByteBuffer.get(12));

      // reported value is in 100 u Ohms units (where 0x01 = 100 uOhms), so multiply by 100 to get uOhms
      final int packTotalResistance = ByteUtils.unsignedShortToInt(auxiliaryGroupByteBuffer.getShort(13)) * 100;

      // reported value is in 100 u Ohms units (where 0x01 = 100 uOhms), so multiply by 100 to get uOhms
      final int minimumCellResistance = ByteUtils.unsignedByteToInt(auxiliaryGroupByteBuffer.get(15)) * 100;

      final int cellNumWithMinimumResistance = ByteUtils.unsignedByteToInt(auxiliaryGroupByteBuffer.get(16));

      // reported value is in 100 u Ohms units (where 0x01 = 100 uOhms), so multiply by 100 to get uOhms
      final int averageCellResistance = ByteUtils.unsignedByteToInt(auxiliaryGroupByteBuffer.get(17)) * 100;

      // reported value is in 100 u Ohms units (where 0x01 = 100 uOhms), so multiply by 100 to get uOhms
      final int maximumCellResistance = ByteUtils.unsignedByteToInt(auxiliaryGroupByteBuffer.get(18)) * 100;

      final int cellNumWithMaximumResistance = ByteUtils.unsignedByteToInt(auxiliaryGroupByteBuffer.get(19));

      final int numberOfCellsSeen = ByteUtils.unsignedByteToInt(auxiliaryGroupByteBuffer.get(20));

      // reported value is in 100 W units, so multiply by 100 to get W
      final int power = (int)auxiliaryGroupByteBuffer.getShort(21) * 100;

      // ---------------------------------------------------------------------------------------------------------------

      // reported values are in 10 mV units with a min of 2.0 V, so divide by 100 and add 2 to get V
      final double[] cellVoltages = new double[HondaConstants.NUM_BATTERIES];
      for (int i = 0; i < Math.min(cellDataVoltages.length, HondaConstants.NUM_BATTERIES); i++)
         {
         cellVoltages[i] = ByteUtils.unsignedByteToInt(cellDataVoltages[i]) / 100.0 + 2.0;
         }

      // ---------------------------------------------------------------------------------------------------------------

      // reported values are in degrees C + 0x80, value is signed -127 to 127 (e.g. 0x80 = 0 degrees C, 0x81 = 1 degrees C, 0x7F = -1)
      final int[] cellTemperatures = new int[HondaConstants.NUM_BATTERIES];
      for (int i = 0; i < Math.min(cellDataTemperatures.length, HondaConstants.NUM_BATTERIES); i++)
         {
         cellTemperatures[i] = (byte)(cellDataTemperatures[i] - 0x80);
         }

      // ---------------------------------------------------------------------------------------------------------------

      // reported values are in 100 u Ohms units (where 0x01 = 100 uOhms), so multiply by 100 to get uOhms
      final int[] cellResistances = new int[HondaConstants.NUM_BATTERIES];
      for (int i = 0; i < Math.min(cellDataResistances.length, HondaConstants.NUM_BATTERIES); i++)
         {
         cellResistances[i] = ByteUtils.unsignedByteToInt(cellDataResistances[i]) * 100;
         }

      // ---------------------------------------------------------------------------------------------------------------

      return new BMSEventImpl(timestamp,
                              bmsFault,
                              numOnOffCycles,
                              timeSincePowerUpInSecs,
                              sourceCurrentAmps,
                              loadCurrentAmps,
                              variousIOState,
                              relativeChargeCurrentLimitPercentage,
                              relativeDischargeCurrentLimitPercentage,
                              areRelaysOn,
                              stateOfChargePercentage,
                              numOfMissingBanks,
                              numOfAMissingBank,
                              numMissingCells,
                              numOfAMissingCell,
                              packTotalVoltage,
                              minimumCellVoltage,
                              maximumCellVoltage,
                              averageCellVoltage,
                              cellNumWithLowestVoltage,
                              cellNumWithHighestVoltage,
                              minimumCellBoardTemp,
                              maximumCellBoardTemp,
                              averageCellBoardTemp,
                              cellBoardNumWithLowestTemp,
                              cellBoardNumWithHighestTemp,
                              numLoadsOn,
                              cellVoltageAboveWhichWeTurnOnItsLoad,
                              auxDataState,
                              levelFaultFlags,
                              totalEnergyInOfBatterySinceManufacture,
                              totalEnergyOutOfBatterySinceManufacture,
                              depthOfDischarge,
                              capacity,
                              stateOfHealthPercentage,
                              packTotalResistance,
                              minimumCellResistance,
                              maximumCellResistance,
                              averageCellResistance,
                              cellNumWithMinimumResistance,
                              cellNumWithMaximumResistance,
                              numberOfCellsSeen,
                              power,
                              cellVoltages,
                              cellTemperatures,
                              cellResistances);
      }

   private static int convertBytesToInt(final byte b2, final byte b1, final byte b0)
      {
      return (((0 & 0xff) << 24) |
              ((b2 & 0xff) << 16) |
              ((b1 & 0xff) << 8) |
              ((b0 & 0xff) << 0));
      }
   }
