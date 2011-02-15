package edu.cmu.ri.createlab;

/**
 * @author Chris Bartley (bartley@cmu.edu)
 */
public interface ButtonPanelEventListener
   {
   void handleOKEvent();

   void handleCancelEvent();

   void handleUpEvent();

   void handleRightEvent();

   void handleDownEvent();

   void handleLeftEvent();

   void handleAccessoryOneEvent();

   void handleAccessoryTwoEvent();

   void handleAccessoryThreeEvent();
   }