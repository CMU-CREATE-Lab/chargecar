package org.chargecar.algodev.predictors;

import java.util.List;

import org.chargecar.prize.util.PointFeatures;
import org.chargecar.prize.util.Trip;

public interface Predictor {
    public List<Prediction> predictDuty(PointFeatures state);
    public void addTrip(Trip t);
    public void endTrip();
}
