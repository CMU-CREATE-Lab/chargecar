package org.chargecar.honda.halleffect;

import org.chargecar.serial.streaming.BaseStreamingSerialPortEvent;

import java.util.Date;

/**
 * @author Chris Bartley (bartley@cmu.edu)
 */
public final class HallEffectEvent extends BaseStreamingSerialPortEvent {
    private final int speed;
    private final double motorTemp;
    private final double controllerTemp;

    public HallEffectEvent(final Date timestamp, final int speed, final double motorTemp, final double controllerTemp) {
        super(timestamp);
        this.speed = speed;
        this.motorTemp = motorTemp;
        this.controllerTemp = controllerTemp;
    }

    public Integer getSpeed() {
        return speed;
    }

    public Double getMotorTemp() {
        return motorTemp;
    }

    public Double getControllerTemp() {
        return controllerTemp;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        if (!super.equals(o)) {
            return false;
        }

        final HallEffectEvent that = (HallEffectEvent) o;

        if (Double.compare(that.controllerTemp, controllerTemp) != 0) {
            return false;
        }
        if (Double.compare(that.motorTemp, motorTemp) != 0) {
            return false;
        }
        if (speed != that.speed) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        long temp;
        temp = motorTemp != +0.0d ? Double.doubleToLongBits(motorTemp) : 0L;
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        temp = controllerTemp != +0.0d ? Double.doubleToLongBits(controllerTemp) : 0L;
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        result = 31 * result + speed;
        return result;
    }

    @Override
    public String toString() {
        return toString("timestamp=", ", speed=", ", motorTemp=", ", controllerTemp=");
    }

    public String toLoggingString() {
        return toString("", TO_STRING_DELIMITER, TO_STRING_DELIMITER, TO_STRING_DELIMITER);
    }

    private String toString(final String field1, final String field2, final String field3, final String field4) {
        final StringBuilder sb = new StringBuilder();
        sb.append("HallEffectEvent");
        sb.append("{");
        sb.append(field1).append(getTimestampMilliseconds());
        sb.append(field2).append(speed);
        sb.append(field3).append(motorTemp);
        sb.append(field4).append(controllerTemp);
        sb.append('}');
        return sb.toString();
    }
}
