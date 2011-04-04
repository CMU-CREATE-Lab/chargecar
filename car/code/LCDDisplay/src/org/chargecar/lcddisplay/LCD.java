package org.chargecar.lcddisplay;

import edu.cmu.ri.createlab.device.CreateLabDeviceProxy;

import java.util.Map;

/**
 * @author Paul Dille (pdille@andrew.cmu.edu)
 */
public interface LCD extends CreateLabDeviceProxy {

    Map<Object, Object> getPropertiesInstance();

    void setSavedProperty(String key, String value);

    String getSavedProperty(String key);

    boolean openSavedProperties(String file);

    void writeSavedProperties();

    String getCurrentPropertiesFileName();

    int getNumberOfSavedProperties();

    void setTripDistance(double newTripDistance);

    double getTripDistance();

    double getChargingTime();

    double getDrivingTime();

    double getCostOfElectricity();

    void setCostOfElectricity(double newCostOfElectricity);

    double getCostOfGas();

    void setCostOfGas(double newCostOfGas);

    int getCarMpg();

    void setCarMpg(int newCarMpg);

    String getAccessoryButtonOne();

    String getAccessoryButtonTwo();

    String getAccessoryButtonThree();

    void setAccessoryButtonOne(String newAccessoryButtonOne);

    void setAccessoryButtonTwo(String newAccessoryButtonTwo);

    void setAccessoryButtonThree(String newAccessoryButtonThree);

    void setCurrentPropertiesFileName(String newPropertiesFileName);

    /**
     * Displays characters on a single line of the display
     *
     * @return <code>true</code> if the write was successfully, <code>false</code> otherwise
     */
    boolean setText(int row, int column, String displayString);

    boolean setText(int row, int column, String displayString, boolean doAscii);

    /**
     * Returns the current temperature of the controller; returns <code>null</code> if the temperature could not be read.
     *
     * @return The raw controller temperature reading in Kelvin
     */
    Double getControllerTemperatureInKelvin();

    /**
     * Returns the current temperature of the motor; returns <code>null</code> if the temperature could not be read.
     *
     * @return The raw motor temperature reading in Kelvin
     */
    Double getMotorTemperatureInKelvin();

    /**
     * Converts Kelvin to Celsius
     *
     * @param temperatureInKelvin temperature in Kelvin
     * @return temperature in Celsius
     */
    Double getTemperatureInCelsius(double temperatureInKelvin);

    /**
     * Converts Kelvin to Fahrenheit
     *
     * @param temperatureInKelvin temperature in Kelvin
     * @return temperature in Fahrenheit
     */
    Double getTemperatureInFahrenheit(double temperatureInKelvin);

    /**
     * Returns the running state of the car
     *
     * @return <code>true</code> if the car is running, <code>false</code> otherwise
     */
    boolean isCarRunning(int[] inputs);

    /**
     * Returns the charging state of the car
     *
     * @return <code>true</code> if the car is charging, <code>false</code> otherwise
     */
    boolean isCarCharging(int[] inputs);

    /**
     * Returns <code>true</code> if the up button was pressed on the display, <code>false</code>
     * otherise.  Returns <code>null</code> if the up button state could not be read.
     *
     * @return <code>true</code> if the up button was pressed, <code>false</code> otherwise, <code>null</code> if read was unsuccessful
     */
    boolean wasUpButtonPressed(int[] inputs);

    /**
     * Returns <code>true</code> if the down button was pressed on the display, <code>false</code>
     * otherise.  Returns <code>null</code> if the down button state could not be read.
     *
     * @return <code>true</code> if the down button was pressed, <code>false</code> otherwise, <code>null</code> if read was unsuccessful
     */
    boolean wasDownButtonPressed(int[] inputs);

    /**
     * Returns <code>true</code> if the left button was pressed on the display, <code>false</code>
     * otherise.  Returns <code>null</code> if the left button state could not be read.
     *
     * @return <code>true</code> if the left button was pressed, <code>false</code> otherwise, <code>null</code> if read was unsuccessful
     */
    boolean wasLeftButtonPressed(int[] inputs);

    /**
     * Returns <code>true</code> if the right button was pressed on the display, <code>false</code>
     * otherise.  Returns <code>null</code> if the right button state could not be read.
     *
     * @return <code>true</code> if the right button was pressed, <code>false</code> otherwise, <code>null</code> if read was unsuccessful
     */
    boolean wasRightButtonPressed(int[] inputs);

    /**
     * Returns <code>true</code> if the select button was pressed on the display, <code>false</code>
     * otherise.  Returns <code>null</code> if the select button state could not be read.
     *
     * @return <code>true</code> if the select button was pressed, <code>false</code> otherwise, <code>null</code> if read was unsuccessful
     */
    boolean wasSelectButtonPressed(int[] inputs);

    /**
     * Returns <code>true</code> if the cancel button was pressed on the display, <code>false</code>
     * otherise.  Returns <code>null</code> if the cancel button state could not be read.
     *
     * @return <code>true</code> if the cancel button was pressed, <code>false</code> otherwise, <code>null</code> if read was unsuccessful
     */
    boolean wasCancelButtonPressed(int[] inputs);

    /**
     * Returns <code>true</code> if the accessory one button was pressed on the display, <code>false</code>
     * otherise.  Returns <code>null</code> if the accessory one button state could not be read.
     *
     * @return <code>true</code> if the accessory one button was pressed, <code>false</code> otherwise, <code>null</code> if read was unsuccessful
     */
    boolean wasAccessoryOneButtonPressed(int[] inputs);

    /**
     * Returns <code>true</code> if the accessory two button was pressed on the display, <code>false</code>
     * otherise.  Returns <code>null</code> if the accessory two button state could not be read.
     *
     * @return <code>true</code> if the accessory two button was pressed, <code>false</code> otherwise, <code>null</code> if read was unsuccessful
     */
    boolean wasAccessoryTwoButtonPressed(int[] inputs);

    /**
     * Returns <code>true</code> if the accessory three button was pressed on the display, <code>false</code>
     * otherise.  Returns <code>null</code> if the accessory three button state could not be read.
     *
     * @return <code>true</code> if the accessory three button was pressed, <code>false</code> otherwise, <code>null</code> if read was unsuccessful
     */
    boolean wasAccessoryThreeButtonPressed(int[] inputs);

    /**
     * Turns on the air conditioning
     *
     * @return <code>true</code> if the call was made successfully, <code>false</code> otherwise
     */
    boolean turnOnAirConditioning();

    /**
     * Turns on the power steering
     *
     * @return <code>true</code> if the call was made successfully, <code>false</code> otherwise
     */
    boolean turnOnPowerSteering();

    /**
     * Turns on the cabin heat
     *
     * @return <code>true</code> if the call was made successfully, <code>false</code> otherwise
     */
    boolean turnOnCabinHeat();

    /**
     * Turns on the display back light
     *
     * @return <code>true</code> if the call was made successfully, <code>false</code> otherwise
     */
    boolean turnOnDisplayBackLight();

    /**
     * Turns on the brake light
     *
     * @return <code>true</code> if the call was made successfully, <code>false</code> otherwise
     */
    boolean turnOnBrakeLight();

    /**
     * Turns on the battery cooling
     *
     * @return <code>true</code> if the call was made successfully, <code>false</code> otherwise
     */
    boolean turnOnBatteryCooling();

    /**
     * Turns on the battery heating
     *
     * @return <code>true</code> if the call was made successfully, <code>false</code> otherwise
     */
    boolean turnOnBatteryHeating();

    /**
     * Turns on the accessory one LED
     *
     * @return <code>true</code> if the call was made successfully, <code>false</code> otherwise
     */
    boolean turnOnAccessoryOneLED();

    /**
     * Turns on the accessory two LED
     *
     * @return <code>true</code> if the call was made successfully, <code>false</code> otherwise
     */
    boolean turnOnAccessoryTwoLED();

    /**
     * Turns on the accessory three LED
     *
     * @return <code>true</code> if the call was made successfully, <code>false</code> otherwise
     */
    boolean turnOnAccessoryThreeLED();

    /**
     * Turns off the air conditioning
     *
     * @return <code>true</code> if the call was made successfully, <code>false</code> otherwise
     */
    boolean turnOffAirConditioning();

    /**
     * Turns off the power steering
     *
     * @return <code>true</code> if the call was made successfully, <code>false</code> otherwise
     */
    boolean turnOffPowerSteering();

    /**
     * Turns off the cabin heat
     *
     * @return <code>true</code> if the call was made successfully, <code>false</code> otherwise
     */
    boolean turnOffCabinHeat();

    /**
     * Turns off the display back light
     *
     * @return <code>true</code> if the call was made successfully, <code>false</code> otherwise
     */
    boolean turnOffDisplayBackLight();

    /**
     * Turns off the brake light
     *
     * @return <code>true</code> if the call was made successfully, <code>false</code> otherwise
     */
    boolean turnOffBrakeLight();

    /**
     * Turns off the battery cooling
     *
     * @return <code>true</code> if the call was made successfully, <code>false</code> otherwise
     */
    boolean turnOffBatteryCooling();

    /**
     * Turns off the battery heating
     *
     * @return <code>true</code> if the call was made successfully, <code>false</code> otherwise
     */
    boolean turnOffBatteryHeating();

    /**
     * Turns off the accessory one LED
     *
     * @return <code>true</code> if the call was made successfully, <code>false</code> otherwise
     */
    boolean turnOffAccessoryOneLED();

    /**
     * Turns off the accessory two LED
     *
     * @return <code>true</code> if the call was made successfully, <code>false</code> otherwise
     */
    boolean turnOffAccessoryTwoLED();

    /**
     * Turns off the accessory three LED
     *
     * @return <code>true</code> if the call was made successfully, <code>false</code> otherwise
     */
    boolean turnOffAccessoryThreeLED();

    /**
     * Returns the current RPMMenuItemAction of the motor; returns <code>null</code> if the RPMMenuItemAction could not be read.
     *
     * @return The raw motor RPMMenuItemAction reading
     */
    Integer getRPM();

    /**
     * Returns the current error code of the controller; returns <code>null</code> if the error code could not be read.
     *
     * @return The error code
     */
    Integer getMotorControllerErrorCodes();

    /**
     * Puts the display back in startup mode
     *
     * @return The error code
     */
    boolean resetDisplay();


    int getBatteryHeaterCutoffTemp();

    void setBatteryHeaterCutoffTemp(int newBatteryHeaterTurnOnValue);

    void addButtonPanelEventListener(ButtonPanelEventListener listener);

    void removeButtonPanelEventListener(ButtonPanelEventListener listener);
}