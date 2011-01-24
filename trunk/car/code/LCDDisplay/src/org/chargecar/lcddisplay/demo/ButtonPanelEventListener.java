package org.chargecar.lcddisplay.demo;

/**
 * @author Chris Bartley (bartley@cmu.edu)
 */
interface ButtonPanelEventListener
   {
   void handleOKEvent();

   void handleCancelEvent();

   void handleUpEvent();

   void handleRightEvent();

   void handleDownEvent();

   void handleLeftEvent();
   }