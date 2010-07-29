package org.chargecar.honda.bms;

import java.awt.Color;
import java.awt.Font;
import java.util.PropertyResourceBundle;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import edu.cmu.ri.createlab.userinterface.GUIConstants;
import edu.cmu.ri.createlab.userinterface.util.SwingUtils;
import org.chargecar.honda.Gauge;
import org.chargecar.honda.HondaConstants;
import org.chargecar.honda.StreamingSerialPortDeviceView;

/**
 * @author Chris Bartley (bartley@cmu.edu)
 */
public final class BMSView extends StreamingSerialPortDeviceView<BMSEvent>
   {
   private static final PropertyResourceBundle RESOURCES = (PropertyResourceBundle)PropertyResourceBundle.getBundle(BMSView.class.getName());

   private final FaultStatusPanel faultStatusPanel = new FaultStatusPanel();

   private final Gauge<Double> packTotalVoltageGauge = new Gauge<Double>(RESOURCES.getString("label.pack-total-voltage"), "%6.2f");
   private final Gauge<Double> minimumCellVoltageGauge = new Gauge<Double>(RESOURCES.getString("label.minimum-cell-voltage"), "%6.2f");
   private final Gauge<Double> maximumCellVoltageGauge = new Gauge<Double>(RESOURCES.getString("label.maximum-cell-voltage"), "%6.2f");
   private final Gauge<Double> averageCellVoltageGauge = new Gauge<Double>(RESOURCES.getString("label.average-cell-voltage"), "%6.2f");
   private final Gauge<Integer> cellNumWithLowestVoltageGauge = new Gauge<Integer>(RESOURCES.getString("label.cell-num-with-lowest-voltage"), "%2d");
   private final Gauge<Integer> cellNumWithHighestVoltageGauge = new Gauge<Integer>(RESOURCES.getString("label.cell-num-with-highest-voltage"), "%2d");

   private final Gauge<Integer> minimumCellTempGauge = new Gauge<Integer>(RESOURCES.getString("label.minimum-cell-temp"), "%3d");
   private final Gauge<Integer> maximumCellTempGauge = new Gauge<Integer>(RESOURCES.getString("label.maximum-cell-temp"), "%3d");
   private final Gauge<Integer> averageCellTempGauge = new Gauge<Integer>(RESOURCES.getString("label.average-cell-temp"), "%3d");
   private final Gauge<Integer> cellNumWithLowestTempGauge = new Gauge<Integer>(RESOURCES.getString("label.cell-num-with-lowest-temp"), "%2d");
   private final Gauge<Integer> cellNumWithHighestTempGauge = new Gauge<Integer>(RESOURCES.getString("label.cell-num-with-highest-temp"), "%2d");

   private final Gauge<Boolean> isLLIMSetGauge = new Gauge<Boolean>(RESOURCES.getString("label.is-llim-set"), "%s");
   private final Gauge<Boolean> isHLIMSetGauge = new Gauge<Boolean>(RESOURCES.getString("label.is-hlim-set"), "%s");

   private final Gauge<Double> sourceCurrentAmpsGauge = new Gauge<Double>(RESOURCES.getString("label.source-current-amps"), "%6.2f");
   private final Gauge<Double> loadCurrentAmpsGauge = new Gauge<Double>(RESOURCES.getString("label.load-current-amps"), "%6.2f");
   private final Gauge<Integer> depthOfDischargeGauge = new Gauge<Integer>(RESOURCES.getString("label.depth-of-discharge"), "%d");
   private final Gauge<Integer> capacityGauge = new Gauge<Integer>(RESOURCES.getString("label.capacity"), "%d");

   private final Gauge<Integer> powerGauge = new Gauge<Integer>(RESOURCES.getString("label.power"), "%d");
   private final Gauge<Integer> stateOfChargeGauge = new Gauge<Integer>(RESOURCES.getString("label.state-of-charge"), "%d");
   private final Gauge<Integer> stateOfHealthGauge = new Gauge<Integer>(RESOURCES.getString("label.state-of-health"), "%d");
   private final Gauge<Integer> lifetimeEnergyInGauge = new Gauge<Integer>(RESOURCES.getString("label.lifetime-energy-in"), "%d");
   private final Gauge<Integer> lifetimeEnergyOutGauge = new Gauge<Integer>(RESOURCES.getString("label.lifetime-energy-out"), "%d");

   private final Gauge<Boolean> isInterlockTrippedGauge = new Gauge<Boolean>(RESOURCES.getString("label.is-interlock-tripped"), "%s");
   private final Gauge<Boolean> isCommunicationFaultWithBankOrCellGauge = new Gauge<Boolean>(RESOURCES.getString("label.is-communication-fault-with-bank-or-cell"), "%s");
   private final Gauge<Boolean> isChargeOvercurrentGauge = new Gauge<Boolean>(RESOURCES.getString("label.is-charge-overcurrent"), "%s");
   private final Gauge<Boolean> isDischargeOvercurrentGauge = new Gauge<Boolean>(RESOURCES.getString("label.is-discharge-overcurrent"), "%s");
   private final Gauge<Boolean> isOverTemperatureGauge = new Gauge<Boolean>(RESOURCES.getString("label.is-over-temperature"), "%s");
   private final Gauge<Boolean> isUnderVoltageGauge = new Gauge<Boolean>(RESOURCES.getString("label.is-under-voltage"), "%s");
   private final Gauge<Boolean> isOverVoltageGauge = new Gauge<Boolean>(RESOURCES.getString("label.is-over-voltage"), "%s");

   public BMSView()
      {
      }

   protected void handleEventInGUIThread(final BMSEvent eventData)
      {
      if (eventData != null)
         {
         faultStatusPanel.setValue(eventData.getBMSFault());

         packTotalVoltageGauge.setValue(eventData.getPackTotalVoltage());
         minimumCellVoltageGauge.setValue(eventData.getMinimumCellVoltage());
         maximumCellVoltageGauge.setValue(eventData.getMaximumCellVoltage());
         averageCellVoltageGauge.setValue(eventData.getAverageCellVoltage());
         cellNumWithLowestVoltageGauge.setValue(eventData.getCellNumWithLowestVoltage());
         cellNumWithHighestVoltageGauge.setValue(eventData.getCellNumWithHighestVoltage());

         minimumCellTempGauge.setValue(eventData.getMinimumCellBoardTemp());
         maximumCellTempGauge.setValue(eventData.getMaximumCellBoardTemp());
         averageCellTempGauge.setValue(eventData.getAverageCellBoardTemp());
         cellNumWithLowestTempGauge.setValue(eventData.getCellBoardNumWithLowestTemp());
         cellNumWithHighestTempGauge.setValue(eventData.getCellBoardNumWithHighestTemp());

         isLLIMSetGauge.setValue(eventData.isLLIMSet(), eventData.isLLIMSet() ? HondaConstants.RED : HondaConstants.GREEN);
         isHLIMSetGauge.setValue(eventData.isHLIMSet(), eventData.isHLIMSet() ? HondaConstants.RED : HondaConstants.GREEN);

         sourceCurrentAmpsGauge.setValue(eventData.getSourceCurrentAmps());
         loadCurrentAmpsGauge.setValue(eventData.getLoadCurrentAmps());
         depthOfDischargeGauge.setValue(eventData.getDepthOfDischarge());
         capacityGauge.setValue(eventData.getCapacity());

         powerGauge.setValue(eventData.getPower());
         stateOfChargeGauge.setValue(eventData.getStateOfChargePercentage());
         stateOfHealthGauge.setValue(eventData.getStateOfHealthPercentage());
         lifetimeEnergyInGauge.setValue(eventData.getTotalEnergyInOfBatterySinceManufacture());
         lifetimeEnergyOutGauge.setValue(eventData.getTotalEnergyOutOfBatterySinceManufacture());

         isInterlockTrippedGauge.setValue(eventData.isInterlockTripped2(), eventData.isInterlockTripped2() ? HondaConstants.RED : HondaConstants.GREEN);
         isCommunicationFaultWithBankOrCellGauge.setValue(eventData.isCommunicationFaultWithBankOrCell(), eventData.isCommunicationFaultWithBankOrCell() ? HondaConstants.RED : HondaConstants.GREEN);
         isChargeOvercurrentGauge.setValue(eventData.isChargeOvercurrent(), eventData.isChargeOvercurrent() ? HondaConstants.RED : HondaConstants.GREEN);
         isDischargeOvercurrentGauge.setValue(eventData.isDischargeOvercurrent(), eventData.isDischargeOvercurrent() ? HondaConstants.RED : HondaConstants.GREEN);
         isOverTemperatureGauge.setValue(eventData.isOverTemperature(), eventData.isOverTemperature() ? HondaConstants.RED : HondaConstants.GREEN);
         isUnderVoltageGauge.setValue(eventData.isUnderVoltage(), eventData.isUnderVoltage() ? HondaConstants.RED : HondaConstants.GREEN);
         isOverVoltageGauge.setValue(eventData.isOverVoltage(), eventData.isOverVoltage() ? HondaConstants.RED : HondaConstants.GREEN);
         }
      else
         {
         faultStatusPanel.setValue(null);

         packTotalVoltageGauge.setValue(null);
         minimumCellVoltageGauge.setValue(null);
         maximumCellVoltageGauge.setValue(null);
         averageCellVoltageGauge.setValue(null);
         cellNumWithLowestVoltageGauge.setValue(null);
         cellNumWithHighestVoltageGauge.setValue(null);

         minimumCellTempGauge.setValue(null);
         maximumCellTempGauge.setValue(null);
         averageCellTempGauge.setValue(null);
         cellNumWithLowestTempGauge.setValue(null);
         cellNumWithHighestTempGauge.setValue(null);

         isLLIMSetGauge.setValue(null);
         isHLIMSetGauge.setValue(null);

         sourceCurrentAmpsGauge.setValue(null);
         loadCurrentAmpsGauge.setValue(null);
         depthOfDischargeGauge.setValue(null);
         capacityGauge.setValue(null);

         powerGauge.setValue(null);
         stateOfChargeGauge.setValue(null);
         stateOfHealthGauge.setValue(null);
         lifetimeEnergyInGauge.setValue(null);
         lifetimeEnergyOutGauge.setValue(null);

         isInterlockTrippedGauge.setValue(null);
         isCommunicationFaultWithBankOrCellGauge.setValue(null);
         isChargeOvercurrentGauge.setValue(null);
         isDischargeOvercurrentGauge.setValue(null);
         isOverTemperatureGauge.setValue(null);
         isUnderVoltageGauge.setValue(null);
         isOverVoltageGauge.setValue(null);
         }
      }

   public JPanel getFaultStatusPanel()
      {
      return faultStatusPanel;
      }

   public Gauge<Double> getPackTotalVoltageGauge()
      {
      return packTotalVoltageGauge;
      }

   public Gauge<Double> getMinimumCellVoltageGauge()
      {
      return minimumCellVoltageGauge;
      }

   public Gauge<Double> getMaximumCellVoltageGauge()
      {
      return maximumCellVoltageGauge;
      }

   public Gauge<Double> getAverageCellVoltageGauge()
      {
      return averageCellVoltageGauge;
      }

   public Gauge<Integer> getCellNumWithLowestVoltageGauge()
      {
      return cellNumWithLowestVoltageGauge;
      }

   public Gauge<Integer> getCellNumWithHighestVoltageGauge()
      {
      return cellNumWithHighestVoltageGauge;
      }

   public Gauge<Integer> getMinimumCellTempGauge()
      {
      return minimumCellTempGauge;
      }

   public Gauge<Integer> getMaximumCellTempGauge()
      {
      return maximumCellTempGauge;
      }

   public Gauge<Integer> getAverageCellTempGauge()
      {
      return averageCellTempGauge;
      }

   public Gauge<Integer> getCellNumWithLowestTempGauge()
      {
      return cellNumWithLowestTempGauge;
      }

   public Gauge<Integer> getCellNumWithHighestTempGauge()
      {
      return cellNumWithHighestTempGauge;
      }

   public Gauge<Boolean> getLLIMSetGauge()
      {
      return isLLIMSetGauge;
      }

   public Gauge<Boolean> getHLIMSetGauge()
      {
      return isHLIMSetGauge;
      }

   public Gauge<Double> getSourceCurrentAmpsGauge()
      {
      return sourceCurrentAmpsGauge;
      }

   public Gauge<Double> getLoadCurrentAmpsGauge()
      {
      return loadCurrentAmpsGauge;
      }

   public Gauge<Integer> getDepthOfDischargeGauge()
      {
      return depthOfDischargeGauge;
      }

   public Gauge<Integer> getCapacityGauge()
      {
      return capacityGauge;
      }

   public Gauge<Integer> getPowerGauge()
      {
      return powerGauge;
      }

   public Gauge<Integer> getStateOfChargeGauge()
      {
      return stateOfChargeGauge;
      }

   public Gauge<Integer> getStateOfHealthGauge()
      {
      return stateOfHealthGauge;
      }

   public Gauge<Integer> getLifetimeEnergyInGauge()
      {
      return lifetimeEnergyInGauge;
      }

   public Gauge<Integer> getLifetimeEnergyOutGauge()
      {
      return lifetimeEnergyOutGauge;
      }

   public Gauge<Boolean> getInterlockTrippedGauge()
      {
      return isInterlockTrippedGauge;
      }

   public Gauge<Boolean> getCommunicationFaultWithBankOrCellGauge()
      {
      return isCommunicationFaultWithBankOrCellGauge;
      }

   public Gauge<Boolean> getChargeOvercurrentGauge()
      {
      return isChargeOvercurrentGauge;
      }

   public Gauge<Boolean> getDischargeOvercurrentGauge()
      {
      return isDischargeOvercurrentGauge;
      }

   public Gauge<Boolean> getOverTemperatureGauge()
      {
      return isOverTemperatureGauge;
      }

   public Gauge<Boolean> getUnderVoltageGauge()
      {
      return isUnderVoltageGauge;
      }

   public Gauge<Boolean> getOverVoltageGauge()
      {
      return isOverVoltageGauge;
      }

   private final class FaultStatusPanel extends JPanel
      {
      private final JLabel value = SwingUtils.createLabel(HondaConstants.UNKNOWN_VALUE,
                                                          new Font(GUIConstants.FONT_NORMAL.getFontName(),
                                                                   Font.BOLD,
                                                                   GUIConstants.FONT_NORMAL.getSize()));

      private FaultStatusPanel()
         {
         this.setBackground(Color.WHITE);
         this.setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
         this.add(Box.createGlue());
         this.add(SwingUtils.createLabel(RESOURCES.getString("label.bms-status") + ":"));
         this.add(SwingUtils.createRigidSpacer());
         this.add(value);
         this.add(Box.createGlue());
         }

      private void setValue(final BMSFault bmsFault)
         {
         if (bmsFault != null)
            {
            if (BMSFault.CODE_0.equals(bmsFault))
               {
               value.setForeground(HondaConstants.GREEN);
               value.setText(bmsFault.getMessage());
               }
            else
               {
               value.setForeground(HondaConstants.RED);
               value.setText(bmsFault.getMessageAndCode());
               }
            }
         else
            {
            value.setForeground(HondaConstants.RED);
            value.setText(HondaConstants.UNKNOWN_VALUE);
            }
         }
      }
   }
