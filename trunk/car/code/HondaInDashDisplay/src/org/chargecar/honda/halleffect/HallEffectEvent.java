package org.chargecar.honda.halleffect;

import org.chargecar.serial.streaming.BaseStreamingSerialPortEvent;

import java.util.Date;

/**
 * @author Chris Bartley (bartley@cmu.edu)
 */
public final class HallEffectEvent extends BaseStreamingSerialPortEvent {
    private final int value;

    public HallEffectEvent(final Date timestamp, final int value) {
        super(timestamp);
        this.value = value;
    }

    public int getValue() {
        return value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;

        HallEffectEvent that = (HallEffectEvent) o;

        if (value != that.value) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + value;
        return result;
    }

    @Override
    public String toString() {
        return toString("timestamp=", ", value=");
    }

    public String toLoggingString() {
        return toString("", TO_STRING_DELIMITER);
    }

    private String toString(final String field1, final String field2) {
        final StringBuilder sb = new StringBuilder();
        sb.append("HallEffectEvent");
        sb.append("{");
        sb.append(field1).append(getTimestampMilliseconds());
        sb.append(field2).append(value);
        sb.append('}');
        return sb.toString();
    }
}
