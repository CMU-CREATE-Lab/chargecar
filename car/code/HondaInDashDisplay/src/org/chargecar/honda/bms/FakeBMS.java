package org.chargecar.honda.bms;

import org.chargecar.serial.streaming.FakeStreamingSerialDevice;

/**
 * @author Chris Bartley (bartley@cmu.edu)
 */
class FakeBMS extends FakeStreamingSerialDevice
   {
   FakeBMS()
      {
      super(FakeBMS.class.getResourceAsStream("/org/chargecar/honda/bms/honda-bms-300secs.bin"));
      }
   }
