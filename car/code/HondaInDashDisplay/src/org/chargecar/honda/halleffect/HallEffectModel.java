package org.chargecar.honda.halleffect;

import org.apache.log4j.Logger;
import org.chargecar.honda.StreamingSerialPortDeviceModel;

/**
 * <p>
 * <code>HallEffectModel</code> keeps track of Hall effect sensor data.
 * </p>
 *
 * @author Chris Bartley (bartley@cmu.edu)
 */
public class HallEffectModel extends StreamingSerialPortDeviceModel<HallEffectEvent, HallEffectEvent> {
    private static final Logger DATA_LOG = Logger.getLogger("DataLog");

    private final byte[] dataSynchronizationLock = new byte[0];

    public HallEffectEvent update(final HallEffectEvent data) {
        synchronized (dataSynchronizationLock) {
            if (DATA_LOG.isInfoEnabled()) {
                DATA_LOG.info(data.toLoggingString());
            }

            publishEventToListeners(data);

            return data;
        }
    }
}
