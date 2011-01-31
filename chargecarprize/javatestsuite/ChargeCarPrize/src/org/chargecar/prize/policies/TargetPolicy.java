//Copyright (c) 2010 Jurgen van Djik
//
//Permission is hereby granted, free of charge, to any person obtaining a copy
//of this software and associated documentation files (the "Software"), to deal
//in the Software without restriction, including without limitation the rights
//to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
//copies of the Software, and to permit persons to whom the Software is
//furnished to do so, subject to the following conditions:
//
//The above copyright notice and this permission notice shall be included in
//all copies or substantial portions of the Software.
//
//THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
//IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
//FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
//AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
//LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
//OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
//THE SOFTWARE.

package org.chargecar.prize.policies;

import org.chargecar.prize.battery.BatteryModel;
import org.chargecar.prize.util.PointFeatures;
import org.chargecar.prize.util.PowerFlowException;
import org.chargecar.prize.util.PowerFlows;
import org.chargecar.prize.util.TripFeatures;

/**
 * 49.077% reduction (Judging)
 * 47.804% reduction (Judging+Training)
 * 
 * @author Jurgen van Dijk
 */

public class TargetPolicy implements Policy {
    private final static String name = "Target Policy";
    private String driver;
    private BatteryModel modelCap;
    private BatteryModel modelBatt;
    private double capMaxCharge;
    private final static String drivers[] = {"alik", "arnold", "gary", "illah", "mike", "nikolay", "rich", "thor"};
    private final static double[][][] driverTable = {
        {{-22050.0, -450.0}, {-33175.0, -14950.0}}, // alik
        {{-14525.0, -5000.0}, {-22125.0, -6075.0}}, // arnold
        {{-21725.0, -9700.0}, {-27600.0, -8100.0}}, // gary
        {{-12850.0, -4400.0}, {-22600.0, -6175.0}}, // illah
        {{-17475.0, -8400.0}, {-24000.0, -10025.0}}, // mike
        {{-17975.0, -5550.0}, {-20750.0, -6400.0}}, // nikolay
        {{-12825.0, -4375.0}, {-18700.0, -6025.0}}, // rich
        {{-17200.0, -6950.0}, {-15975.0, -6350.0}}, // thor
        {{-17100.0, -5850.0}, {-23000.0, -6375.0}} // other driver
    };
    private double[][] speedTable;
    
    private int getDriverIndex(String driver) {
        for (int i = 0; i < drivers.length; i++) {
            if (driver.equalsIgnoreCase(drivers[i])) {
                return i;
            }
        }        
        return drivers.length;
    }
    
    public void beginTrip(TripFeatures tripFeatures, BatteryModel batteryClone, BatteryModel capacitorClone) {
        driver = tripFeatures.getDriver();
        modelCap = capacitorClone;
        modelBatt = batteryClone;
        capMaxCharge = modelCap.getMaxCharge();
        speedTable = driverTable[getDriverIndex(driver)];
    }

    public PowerFlows calculatePowerFlows(PointFeatures pf) {
        final double capCharge = modelCap.getCharge();

        final int periodMS = pf.getPeriodMS(); // period in milliseconds
        final double speed = pf.getSpeed(); // speed in meters per second
        final double motorPower = pf.getPowerDemand(); // demanded power in watts

        final double capMinPower = modelCap.getMinPower(periodMS);
        final double capMaxPower = modelCap.getMaxPower(periodMS);
        final double battMinPower = modelBatt.getMinPower(periodMS);
        final double battMaxPower = modelBatt.getMaxPower(periodMS);

        final int speedIndex = (speed < 16.666666667 ? 0 : 1);
        final double battToCapLimit = speedTable[speedIndex][0];
        final double battTargetBase = speedTable[speedIndex][1];

        final double battToCapTarget = Math.max((1.0 - capCharge / capMaxCharge) * battToCapLimit, Math.max(-capMaxPower, battMinPower));
        final double battPowerTarget = Math.min(battToCapTarget, Math.max(battTargetBase, battMinPower));

        double battToMotor = 0.0;
        double capToMotor = 0.0;
        double battToCap = 0.0;

        if (motorPower < 0.0) {
            // motor is demanding power
            if (motorPower < battPowerTarget) {
                // motor is demanding more power than battery power target
                // cap powers motor
                capToMotor = Math.max(motorPower - battPowerTarget, capMinPower);
                // battery powers motor
                battToMotor = Math.max(motorPower - capToMotor, battMinPower);
            } else if (motorPower > battPowerTarget) {
                // motor is demanding less power than battery power target
                // battery powers motor
                battToMotor = motorPower;
                // battery charges cap
                battToCap = Math.max(Math.max(battToCapTarget, battPowerTarget - motorPower), Math.max(battMinPower - motorPower, -capMaxPower));
            } else {
                // motor is exactly demanding battery power target
                // battery powers motor
                battToMotor = motorPower;
            }
        } else if (motorPower > 0.0) {
            // motor is generating power
            if (motorPower > capMaxPower) {
                // motor is generating more power than cap can store
                // motor charges cap
                capToMotor = capMaxPower;
                // motor charges battery
                battToMotor = Math.min(motorPower - capToMotor, battMaxPower);
            } else if (motorPower < capMaxPower) {
                // motor is generating less power than cap can store
                // motor charges cap
                capToMotor = motorPower;
                // battery charges cap
                battToCap = Math.max(battToCapTarget, motorPower - capMaxPower);
            } else {
                // motor is exactly generating all power that cap can store
                // motor charges cap
                capToMotor = motorPower;
            }
        } else {
            // motor is not demanding or generating power
        }

        try {
            modelCap.drawPower(capToMotor - battToCap, pf);
            modelBatt.drawPower(battToMotor + battToCap, pf);
        } catch (PowerFlowException e) {
            System.err.println(e);
        }

        return new PowerFlows(battToMotor, capToMotor, battToCap);
    }

    public void endTrip() {
        modelCap = null;
        modelBatt = null;
    }

    public void loadState() {
        // nothing to do
    }

    public String getName() {
        return name;
    }
}
