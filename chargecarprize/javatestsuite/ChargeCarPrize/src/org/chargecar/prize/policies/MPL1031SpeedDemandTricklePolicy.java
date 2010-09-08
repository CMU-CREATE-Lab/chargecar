package org.chargecar.prize.policies;

import org.chargecar.prize.battery.BatteryModel;
import org.chargecar.prize.util.PointFeatures;
import org.chargecar.prize.util.PowerFlowException;
import org.chargecar.prize.util.PowerFlows;
import org.chargecar.prize.util.TripFeatures;

/**
 * A supercapacitor policy that uses speed and power demand to determine the trickle rate.
 * @author MPL1031
 */

public class MPL1031SpeedDemandTricklePolicy implements Policy {
    private BatteryModel modelCap;
    private BatteryModel modelBatt;
    private String name = "MPL1031 Speed/Demand Trickle Policy";

	private double capTotalWattsCapacity;
	// 8/31/2010: 30.517% reduction on judging data set; 27.348% reduction reduction on full data set
	private static final double[][] trickleRates = new double[][] {
				{ -39521, -39521, -39521, -29682, -47465, -42269, -35142, -33342, -40187, -51020, -35025, -38996, -43019, },  // speed: 0
				{ -35500, -35500, -35500, -35417, -31656, -30764, -39261, -7094, -42950, -44260, -49762, -54071, -60079, },  // speed: 1
				{ -21868, -21868, -37423, -43848, -37003, -40420, -30269, -47404, -48548, -45602, -43506, -83592, -93337, },  // speed: 2
				{ -34899, -34899, -37470, -61393, -53381, -35289, -42796, -33857, -33625, -34466, -69342, -105649, -105649, },  // speed: 3
				{ -32485, -32485, -29822, -38410, -45525, -38439, -45113, -32101, -35957, -35521, -65119, -95782, -95782, },  // speed: 4
				{ -64956, -104344, -25873, -50867, -34775, -26845, -30567, -43977, -29431, -65708, -58588, -60574, -74956, },  // speed: 5
				{ -35250, -35250, -27356, -36419, -41348, -23428, -26288, -25597, -27466, -32114, -43120, -62486, -73707, },  // speed: 6
				{ -30824, -30824, -20746, -20178, -30446, -19615, -40792, -34777, -20601, -24197, -33770, -47588, -52682, },  // speed: 7
				{ -75040, -75040, -19014, -18111, -19757, -21274, -29011, -15491, -17621, -19425, -25817, -41211, -47294, },  // speed: 8
				{ -18944, -18944, -17509, -16245, -24208, -12339, -12279, -27460, -15387, -19538, -22594, -32180, -34090, },  // speed: 9
				{ -35322, -35332, -17148, -13067, -11920, -12644, -10235, -12126, -13665, -14179, -17827, -31742, -35359, },  // speed: 10
				{ -64628, -64628, -15402, -14028, -17978, -16278, -14705, -11010, -12548, -11815, -17453, -28452, -32099, },  // speed: 11
				{ -28486, -28486, -15271, -11893, -13768, -15150, -9543, -10785, -17030, -14110, -17368, -19185, -22827, },  // speed: 12
				{ -19768, -19678, -14190, -11384, -11786, -8487, -12101, -10655, -11337, -11831, -14621, -17773, -20678, },  // speed: 13
				{ -39191, -39191, -13129, -9680, -10658, -8578, -9704, -9510, -11828, -11309, -15165, -18153, -21448, },  // speed: 14
				{ -31133, -31133, -13153, -10888, -11362, -9280, -7787, -9164, -9894, -11487, -14746, -17778, -21348, },  // speed: 15
				{ -20110, -3894, -21179, -12410, -11511, -13749, -9413, -12885, -9733, -12187, -16033, -17993, -22144, },  // speed: 16
				{ -22457, -14931, -26386, -10835, -10710, -10913, -10277, -11897, -10673, -13445, -17995, -22498, -24457, },  // speed: 17
				{ -24413, -34463, -21832, -23358, -14240, -11010, -28315, -13554, -14082, -19874, -21419, -23611, -27276, },  // speed: 18
				{ -24847, -21996, -30222, -20723, -26238, -19200, -16911, -10640, -21490, -19112, -23748, -27118, -32215, },  // speed: 19
				{ -33340, -29406, -17873, -22042, -28400, -21506, -28584, -21399, -27775, -24105, -29561, -32873, -35489, },  // speed: 20
				{ -26781, -24548, -27781, -23208, -26049, -26282, -32730, -33175, -22299, -36540, -37565, -40259, -42092, },  // speed: 21
				{ -39544, -39544, -26347, -33118, -31207, -40212, -38037, -35665, -28325, -32341, -40540, -40712, -42937, },  // speed: 22
				{ -36749, -24545, -31510, -32087, -28179, -29279, -30859, -30745, -27598, -30706, -36567, -42564, -43622, },  // speed: 23
				{ -36909, -33269, -30723, -31761, -41363, -36862, -36909, -40925, -32314, -32276, -37096, -43744, -45909, },  // speed: 24
				{ -43900, -47304, -36888, -34944, -46275, -36258, -36900, -40900, -35344, -35006, -39118, -46392, -46772, },  // speed: 25
				{ -44020, -40955, -39068, -44321, -31778, -36386, -37020, -40020, -33028, -38025, -40313, -46989, -51538, },  // speed: 26
				{ -44032, -44032, -42346, -55150, -32534, -40434, -47424, -40276, -46503, -44174, -41179, -48177, -48032, },  // speed: 27
				{ -44011, -39467, -64299, -45391, -36540, -36540, -44011, -42011, -44011, -43585, -42699, -49262, -50791, },  // speed: 28
				{ -47996, -47996, -32027, -56433, -38333, -43685, -43685, -43685, -43685, -44329, -44264, -44029, -44083, },  // speed: 29
				{ -44170, -44170, -29053, -42361, -40170, -42170, -45170, -45170, -45170, -38151, -45494, -49349, -51430, },  // speed: 30
				{ -44109, -46109, -34072, -41109, -41109, -45109, -45109, -45109, -45109, -45242, -45395, -46275, -49109, },  // speed: 31
				{ -44120, -44120, -40856, -27367, -42120, -45120, -45120, -45120, -45120, -45120, -46582, -47622, -47622, },  // speed: 32
				{ -44451, -43800, -40451, -40451, -41451, -41451, -41451, -41451, -41451, -41451, -41563, -46794, -46794, },  // speed: 33
				{ -44501, -46329, -44501, -44501, -44501, -44501, -44501, -44501, -44501, -44501, -41292, -41973, -41973, },  // speed: 34
			};
    
    public void beginTrip(TripFeatures tripFeatures, BatteryModel batteryClone,
	    BatteryModel capacitorClone) {
		modelCap = capacitorClone;
		modelBatt = batteryClone;
		capTotalWattsCapacity = modelCap.getMaxPower(1000);
	}
    
    public PowerFlows calculatePowerFlows(PointFeatures pf) {
		int periodMS = pf.getPeriodMS();
		double speed = pf.getSpeed();

		double multiplier = (double) periodMS / 1000;
		// was: double minCapCurrent = modelCap.getMinCurrent(periodMS);
		final double minusCapWattsAvailable = modelCap.getMinPower(periodMS) * multiplier;
		// double maxCapCurrent = modelCap.getMaxCurrent(periodMS);
		final double plusCapWattsToFull = modelCap.getMaxPower(periodMS) * multiplier;

		// leave more room in cap for regen braking from higher speeds?
		// double targetCharge = modelCap.getMaxCharge() - 1.7*speed;
		double minusTargetCapWatts = -capTotalWattsCapacity;

		final double minusMotorWattsUsedOrPlusWattsProvided = pf.getPowerDemand() * multiplier;  // was wattsDemanded

		// speed in meters/sec
		int speedIndex = (int) Math.floor(Math.min(speed, 34.0));  // for trickleRate[35][13]

		// use log scale to be more sensitive near 0; Math.max() eliminate negative logs betwen 0 and 1
		double absWattsLog10 = Math.log10(Math.max(1, Math.abs(minusMotorWattsUsedOrPlusWattsProvided)));
		// leading minus (negate) makes acceleration positive and braking negative,
		//   Math.min(x,6) eliminates a few extreme data points and centers result on 0
		double wattsLog10 = -Math.copySign(Math.min(absWattsLog10, 6.0), minusMotorWattsUsedOrPlusWattsProvided);
		int wattsIndex = (int) Math.round(wattsLog10 + 6.0);  // 0-12: BBBbbb_aaaAAA

		// trickle rate needs to increase when the cap is more empty
		double trickleRate = trickleRates[speedIndex][wattsIndex] * (plusCapWattsToFull / capTotalWattsCapacity);

		double minusCapToMotorOrPlusCapFromMotorWatts = 0.0;  // was capToMotorWatts
		double minusBatteryToCapOrPlusBatteryFromCapWatts = 0.0;  // was batteryToCapWatts
		double minusBatteryToMotorOrPlusBatteryFromMotorWatts = 0.0;  // was batteryToMotorWatts

		// motor is using watts?
		if (minusMotorWattsUsedOrPlusWattsProvided < 0.0) {
			// motor is using (-) more watts than cap can provide?
			if (minusMotorWattsUsedOrPlusWattsProvided < minusCapWattsAvailable) {
				// provide (-) cap watts first, cap now depleted
				minusCapToMotorOrPlusCapFromMotorWatts = minusCapWattsAvailable;
				// provide (-) remainder from battery
				minusBatteryToMotorOrPlusBatteryFromMotorWatts =
						minusMotorWattsUsedOrPlusWattsProvided - minusCapToMotorOrPlusCapFromMotorWatts;
				// room for some trickle (-) from battery to cap?
				if (trickleRate < minusBatteryToMotorOrPlusBatteryFromMotorWatts) {
					minusBatteryToCapOrPlusBatteryFromCapWatts =
							trickleRate + minusBatteryToMotorOrPlusBatteryFromMotorWatts;
				}
			}
			else {  // (cap can provide all motor watts)
				// provide (-) all motor watts from cap
				minusCapToMotorOrPlusCapFromMotorWatts = minusMotorWattsUsedOrPlusWattsProvided;
				// cap watts (-) left after those provided to motor
				double minusCapWattsAfterChange = minusCapWattsAvailable - minusMotorWattsUsedOrPlusWattsProvided;
				double gap = minusTargetCapWatts - minusCapWattsAfterChange;
				// adjust towards cap target, limit with trickle rate
				// replenish (+) battery or (-) cap
				minusBatteryToCapOrPlusBatteryFromCapWatts =
						gap > 0 ? Math.min(gap, -trickleRate) : Math.max(gap, trickleRate);
			}
		}
		else if (minusMotorWattsUsedOrPlusWattsProvided > 0.0) {  // (regeneration is providing watts)
			// watts available (-) after cap replenished
			double minusCapWattsAfterChange = minusCapWattsAvailable - minusMotorWattsUsedOrPlusWattsProvided;
			// cap target not reached after change?
			if (minusTargetCapWatts < minusCapWattsAfterChange) {
				// replenish (+) cap (no need to limit regeneration)
				minusCapToMotorOrPlusCapFromMotorWatts = minusMotorWattsUsedOrPlusWattsProvided;
				// replenish (+) cap towards cap target but limit with trickle rate
				minusBatteryToCapOrPlusBatteryFromCapWatts =
						Math.max(minusTargetCapWatts - minusCapWattsAfterChange, trickleRate);
			}
			else {  // (minusMotorWattsUsedOrPlusWattsProvided will exceed cap target)
				// replenish (+) cap first to minimize charge sent to battery
				minusCapToMotorOrPlusCapFromMotorWatts = Math.min(minusMotorWattsUsedOrPlusWattsProvided, plusCapWattsToFull);
				// replenish (+) battery with remainder, if any
				minusBatteryToMotorOrPlusBatteryFromMotorWatts =
						minusMotorWattsUsedOrPlusWattsProvided - minusCapToMotorOrPlusCapFromMotorWatts;
			}
		}

		// NOTE: / multiplier changes all watt units back to original periodMS units
		if (multiplier != 1.0) {
			minusBatteryToMotorOrPlusBatteryFromMotorWatts /= multiplier;
			minusCapToMotorOrPlusCapFromMotorWatts /= multiplier;
			minusBatteryToCapOrPlusBatteryFromCapWatts /= multiplier;
		}

		//------------------------------------------
		// (DON'T CHANGE THIS) do the work here
		//  (this duplicates what is done in the Simulator class)
		try {
			modelCap.drawPower(minusCapToMotorOrPlusCapFromMotorWatts - minusBatteryToCapOrPlusBatteryFromCapWatts, pf);
			modelBatt.drawPower(minusBatteryToMotorOrPlusBatteryFromMotorWatts + minusBatteryToCapOrPlusBatteryFromCapWatts, pf);
		}
		catch (PowerFlowException e) {
			// (exception was previously eaten but is useful)
			throw new RuntimeException(e);
		}
		return new PowerFlows(minusBatteryToMotorOrPlusBatteryFromMotorWatts,
				minusCapToMotorOrPlusCapFromMotorWatts,
				minusBatteryToCapOrPlusBatteryFromCapWatts);
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
