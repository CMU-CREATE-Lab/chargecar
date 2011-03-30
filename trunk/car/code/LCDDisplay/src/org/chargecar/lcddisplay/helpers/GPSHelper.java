package org.chargecar.lcddisplay.helpers;

import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

/**
 * @author Paul Dille (pdille@andrew.cmu.edu)
 */
public class GPSHelper {

    private GPSHelper() {
    }

    /*
       Compute distance in miles using the Haversine method
    */
    public static float distFrom(final Double lat1, final Double lng1, final Double lat2, final Double lng2) {
        final double earthRadius = 3958.75;
        final double dLat = Math.toRadians(lat2 - lat1);
        final double dLng = Math.toRadians(lng2 - lng1);
        final double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
                        Math.sin(dLng / 2) * Math.sin(dLng / 2);
        final double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        final double dist = earthRadius * c;

        return new Float(dist).floatValue();
    }

    /*
        Convert degree decimal minutes to decimal degrees
     */
    public static List<Double> degreeDecimalMinutesToDecimalDegrees(final String lat, final String lng) {
        StringTokenizer tokenizer = new StringTokenizer(lat, " °'?");
        List<String> gpsTokens = new ArrayList<String>(4);
        while (tokenizer.hasMoreTokens()) {
            final String token = tokenizer.nextToken();
            if (token.length() > 1 && "S0".equals(token.substring(0, 2))) {
                gpsTokens.add(token.substring(0, 1));
                gpsTokens.add(token.substring(1));
            } else {
                gpsTokens.add(token);
            }
        }

        if (gpsTokens.size() < 3) return null;

        double latDecimalDegrees = Double.valueOf(gpsTokens.get(1)) + Double.valueOf(gpsTokens.get(2)) / 60;
        if ("S".equals(gpsTokens.get(0).substring(0, 1))) {
            latDecimalDegrees *= -1;
        }

        tokenizer = new StringTokenizer(lng, " °'?");
        gpsTokens = new ArrayList<String>(4);
        while (tokenizer.hasMoreTokens()) {
            final String token = tokenizer.nextToken();
            if (token.length() > 1 && "W0".equals(token.substring(0, 2))) {
                gpsTokens.add(token.substring(0, 1));
                gpsTokens.add(token.substring(1));
            } else {
                gpsTokens.add(token);
            }
        }

        if (gpsTokens.size() < 3) return null;

        double lngDecimalDegrees = Double.valueOf(gpsTokens.get(1)) + Double.valueOf(gpsTokens.get(2)) / 60;
        if ("W".equals(gpsTokens.get(0).substring(0, 1))) {
            lngDecimalDegrees *= -1;
        }
        final List<Double> latLng = new ArrayList<Double>(2);

        latLng.add(latDecimalDegrees);
        latLng.add(lngDecimalDegrees);

        return latLng;
    }
}
