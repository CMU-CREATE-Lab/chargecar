package org.chargecar.lcddisplay.lcd;


import org.chargecar.serial.streaming.BaseStreamingSerialPortEvent;

import java.util.Date;

/**
 * @author Paul Dille (pdille@andrew.cmu.edu)
 */
public class LCDEvent extends BaseStreamingSerialPortEvent {
    private final boolean isCarRunning;
    private final boolean isCarChargining;
    private final double motorControllerTemperature;
    private final double motorTemperature;
    private final int motorControllerErrorCode;
    private final int rpm;

    public LCDEvent(final Date timestamp,
                    final boolean isCarRunning,
                    final boolean isCarChargining,
                    final double motorControllerTemperature,
                    final double motorTemperature,
                    final int motorControllerErrorCode,
                    final int rpm
    ) {
        super(timestamp);
        this.isCarRunning = isCarRunning;
        this.isCarChargining = isCarChargining;
        this.motorControllerTemperature = motorControllerTemperature;
        this.motorTemperature = motorTemperature;
        this.motorControllerErrorCode = motorControllerErrorCode;
        this.rpm = rpm;
    }
    
    public String toLoggingString() {
        final StringBuilder sb = new StringBuilder();
        sb.append("LCDEvent");
        sb.append('{');
        sb.append(getTimestampMilliseconds()).append(TO_STRING_DELIMITER);
        sb.append(isCarRunning).append(TO_STRING_DELIMITER);
        sb.append(isCarChargining).append(TO_STRING_DELIMITER);
        sb.append(motorControllerTemperature).append(TO_STRING_DELIMITER);
        sb.append(motorTemperature).append(TO_STRING_DELIMITER);
        sb.append(motorControllerErrorCode).append(TO_STRING_DELIMITER);
        sb.append(rpm).append(TO_STRING_DELIMITER);
        sb.append('}');
        return sb.toString();
    }
}
