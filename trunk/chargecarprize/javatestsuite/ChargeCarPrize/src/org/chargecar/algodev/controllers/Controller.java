package org.chargecar.algodev.controllers;

import java.util.List;

import org.chargecar.algodev.predictors.Prediction;

public abstract class Controller {
    public abstract double getControl(List<Prediction> predictedDuty, double capCharge);
}
