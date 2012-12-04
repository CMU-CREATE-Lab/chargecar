package org.chargecar.algodev.controllers;

import java.util.List;

import org.chargecar.algodev.predictors.Prediction;
import org.chargecar.prize.battery.BatteryModel;

public abstract class Controller {
    public abstract double getControl(List<Prediction> predictedDuty, BatteryModel battery, BatteryModel cap, int periodMS);
}
