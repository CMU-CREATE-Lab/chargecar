package org.chargecar.ned;

/**
 * @author Chris Bartley (bartley@cmu.edu)
 */
public interface ElevationService
   {
   Double getElevation(double longitude, double latitude);
   }