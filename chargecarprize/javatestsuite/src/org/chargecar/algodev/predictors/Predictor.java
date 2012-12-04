package org.chargecar.algodev.predictors;

import java.util.List;

import org.chargecar.prize.util.PointFeatures;

public abstract class Predictor {
    public abstract List<Prediction> predictDuty(PointFeatures state);
}
