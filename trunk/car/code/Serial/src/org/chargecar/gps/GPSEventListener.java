package org.chargecar.gps;

/**
 * @author Chris Bartley (bartley@cmu.edu)
 */
public interface GPSEventListener
   {
   void handleLocationEvent(final String latitude, final String longitude, final int numSatellitesBeingTracked);

   void handleElevationEvent(final int elevationInFeet);
   }