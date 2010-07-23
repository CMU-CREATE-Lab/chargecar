package org.chargecar.honda.bms;

import org.chargecar.honda.FakeSerialDevice;

/**
 * @author Chris Bartley (bartley@cmu.edu)
 */
public class FakeBMS extends FakeSerialDevice
   {
   public FakeBMS()
      {
      super(FakeBMS.class.getResourceAsStream("/org/chargecar/honda/bms/honda-bms-300secs.bin"));
      }
   }
