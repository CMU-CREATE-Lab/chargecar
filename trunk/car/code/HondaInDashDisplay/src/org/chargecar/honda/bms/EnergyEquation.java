package org.chargecar.honda.bms;

/**
 * @author Chris Bartley (bartley@cmu.edu)
 * @author Paul Dille (pdille@andrew.cmu.ed)
 */
public interface EnergyEquation {
    double getKilowattHours();

    /**
     * Returns the change in kilowatt hours since the last update.
     */
    double getKilowattHoursDelta();

    double getKilowattHoursUsed();

    double getKilowattHoursRegen();

    /**
     * sets all energy variables to 0
     */
    void reset();
}