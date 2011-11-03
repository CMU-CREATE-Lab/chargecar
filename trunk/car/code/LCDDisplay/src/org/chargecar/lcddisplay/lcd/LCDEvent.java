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
        return toString("", TO_STRING_DELIMITER, TO_STRING_DELIMITER, TO_STRING_DELIMITER, TO_STRING_DELIMITER, TO_STRING_DELIMITER, TO_STRING_DELIMITER);
    }

    @Override
    public String toString() {
        return toString("timestamp=",
                ", isCarRunning=",
                ", isCarChargining=",
                ", motorControllerTemperature=",
                ", motorTemperature=",
                ", motorControllerErrorCode=",
                ", rpm=");
    }

    private String toString(final String field1, final String field2, final String field3, final String field4, final String field5, final String field6, final String field7) {
        final StringBuilder sb = new StringBuilder();
        sb.append("LCDEvent");
        sb.append("{");
        sb.append(field1).append(getTimestampMilliseconds());
        sb.append(field2).append(isCarRunning);
        sb.append(field3).append(isCarChargining);
        sb.append(field4).append(motorControllerTemperature);
        sb.append(field5).append(motorTemperature);
        sb.append(field6).append(motorControllerErrorCode);
        sb.append(field7).append(rpm);
        sb.append('}');
        return sb.toString();
    }

}
