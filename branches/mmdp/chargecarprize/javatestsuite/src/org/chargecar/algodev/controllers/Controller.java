package org.chargecar.algodev.controllers;

import java.util.List;

import org.chargecar.algodev.predictors.Prediction;
import org.chargecar.prize.battery.BatteryModel;
import org.chargecar.prize.util.Trip;

public interface Controller {
    public double getControl(List<Prediction> predictedDuty, BatteryModel battery, BatteryModel cap, int periodMS, double powerDemand);
    public void addTrip(Trip t);
}
