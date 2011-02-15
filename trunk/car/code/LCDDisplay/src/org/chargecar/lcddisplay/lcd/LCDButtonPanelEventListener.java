package org.chargecar.lcddisplay.lcd;

/**
 * @author Paul Dille (pdille@andrew.cmu.edu)
 */
public interface LCDButtonPanelEventListener
   {
   void handleSelectEvent();

   void handleCancelEvent();

   void handleUpEvent();

   void handleRightEvent();

   void handleDownEvent();

   void handleLeftEvent();

   void handleAccessoryOneEvent();

   void handleAccessoryTwoEvent();

   void handleAccessoryThreeOneEvent();
   }
