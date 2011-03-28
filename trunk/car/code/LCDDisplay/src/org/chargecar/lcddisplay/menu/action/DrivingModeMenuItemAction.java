package org.chargecar.lcddisplay.menu.action;

import edu.cmu.ri.createlab.display.character.CharacterDisplay;
import edu.cmu.ri.createlab.display.character.menu.RepeatingActionCharacterDisplayMenuItemAction;
import edu.cmu.ri.createlab.menu.MenuItem;
import edu.cmu.ri.createlab.menu.MenuStatusManager;
import org.apache.log4j.Logger;
import org.chargecar.gpx.GPSCoordinate;
import org.chargecar.honda.bms.BMSAndEnergy;
import org.chargecar.honda.gps.GPSEvent;
import org.chargecar.lcddisplay.*;
import org.chargecar.lcddisplay.helpers.GeneralHelper;
import org.chargecar.lcddisplay.helpers.PostgesConnect;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * @author Paul Dille (pdille@andrew.cmu.edu)
 */
public final class DrivingModeMenuItemAction extends RepeatingActionCharacterDisplayMenuItemAction {
    private static final Logger LOG = Logger.getLogger(DrivingModeMenuItemAction.class);
    private LCD lcd = null;
    private BMSManager bmsManager = null;
    private BMSAndEnergy bmsData = null;
    private GPSManager gpsManager = null;
    private GPSEvent gpsData = null;
    private int currentState = 1;
    private PostgesConnect postgesConnection = null;
    private static final String roadTable = "pa_2010_priseroads";
    private static final String cityStateTable = "us_2008_uac";
    private static final String roadTablecolumnName = "fullname";
    private static final String cityStatecolumnName = "name";
    private static final String srid = "4269";

    //number of performAction methods there are in this class, set all to true so that
    //the first time we enter the action we print out their headings
    private List<Boolean> printHeadings = Arrays.asList(true, true, true, true, true);

    public DrivingModeMenuItemAction(final MenuItem menuItem,
                                     final MenuStatusManager menuStatusManager,
                                     final CharacterDisplay characterDisplay) {
        super(menuItem, menuStatusManager, characterDisplay, 0, 1, TimeUnit.SECONDS);
    }

    @Override
    protected void performAction() {
        lcd = LCDProxy.getInstance();
        bmsManager = BMSManager.getInstance();
        bmsData = (bmsManager == null) ? null : bmsManager.getData();

        if (bmsManager == null || bmsData == null) {
            LOG.error("DrivingModeMenuItemAction.performAction(): bms is null");
            getCharacterDisplay().setLine(0, "No connection to BMS.");
            getCharacterDisplay().setLine(1, LCDConstants.BLANK_LINE);
            getCharacterDisplay().setLine(2, LCDConstants.BLANK_LINE);
            getCharacterDisplay().setLine(3, LCDConstants.BLANK_LINE);
            return;
        } else if (lcd == null) {
            LOG.error("DrivingModeMenuItemAction.performAction(): lcd is null");
            return;
        }

        if (currentState == 2)
            performAction2();
        else if (currentState == 3)
            performAction3();
        else if (currentState == 4)
            performAction4();
        else if (currentState == 5)
            performAction5();
        else {
            currentState = 1;
            if (printHeadings.get(0)) {
                printHeadings.set(0, false);
                getCharacterDisplay().setLine(0, "^ Charge ");
                getCharacterDisplay().setLine(1, "  Pwr Flow ");
                getCharacterDisplay().setLine(2, "  Efficiency ");
                getCharacterDisplay().setLine(3, "v          miles/kWh");
            }
            //instantaneous
            double powerFlowInKw = (bmsData.getBmsState().getPackTotalVoltage() * bmsData.getBmsState().getLoadCurrentAmps()) / 1000;
            powerFlowInKw = GeneralHelper.round(powerFlowInKw, 2);
            double currentEfficiency = lcd.getTripDistance() / bmsData.getEnergyEquation().getKilowattHours();
            currentEfficiency = GeneralHelper.round(currentEfficiency, 2);
            LOG.trace("DrivingModeMenuItemAction.performAction(): updating kwhMeter");

            getCharacterDisplay().setCharacter(0, 9, GeneralHelper.padLeft(GeneralHelper.round((bmsData.getBmsState().getStateOfChargePercentage() / 2.0), 2) + "%", LCDConstants.NUM_COLS - 9));
            getCharacterDisplay().setCharacter(1, 11, GeneralHelper.padLeft(powerFlowInKw + "kW", LCDConstants.NUM_COLS - 11));
            getCharacterDisplay().setCharacter(2, 13, GeneralHelper.padLeft(String.valueOf(currentEfficiency), LCDConstants.NUM_COLS - 13));

            //getCharacterDisplay().setLine(0, "^ Charge " + GeneralHelper.padLeft(GeneralHelper.round((bmsData.getBmsState().getStateOfChargePercentage() / 2.0), 2) + "%", LCDConstants.NUM_COLS - 9));
            //getCharacterDisplay().setLine(1, "  Pwr Flow " + GeneralHelper.padLeft(powerFlowInKw + "kW", LCDConstants.NUM_COLS - 11));
            //getCharacterDisplay().setLine(2, "  Efficiency " + GeneralHelper.padLeft(String.valueOf(currentEfficiency), LCDConstants.NUM_COLS - 13));
            //getCharacterDisplay().setLine(3, "v          miles/kWh");
        }
    }

    public void performAction2() {
        currentState = 2;
        lcd = LCDProxy.getInstance();
        bmsManager = BMSManager.getInstance();
        bmsData = (bmsManager == null) ? null : bmsManager.getData();

        if (bmsManager == null || bmsData == null) {
            LOG.error("DrivingModeMenuItemAction.performAction2(): bms is null");
            getCharacterDisplay().setLine(0, "No connection to BMS.");
            getCharacterDisplay().setLine(1, LCDConstants.BLANK_LINE);
            getCharacterDisplay().setLine(2, LCDConstants.BLANK_LINE);
            getCharacterDisplay().setLine(3, LCDConstants.BLANK_LINE);
            return;
        } else if (lcd == null) {
            LOG.error("DrivingModeMenuItemAction.performAction2(): lcd is null");
            return;
        }

        if (printHeadings.get(1)) {
            printHeadings.set(1, false);
            getCharacterDisplay().setLine(0, "^ GENRL TEMPERATURES");
            getCharacterDisplay().setLine(1, "  Motor ");
            getCharacterDisplay().setLine(2, "  Controller ");
            getCharacterDisplay().setLine(3, "v                   ");
        }

        getCharacterDisplay().setCharacter(1, 8, GeneralHelper.padLeft(GeneralHelper.round(lcd.getTemperatureInCelsius(lcd.getMotorTemperatureInKelvin()), 2) + "C", LCDConstants.NUM_COLS - 8));
        getCharacterDisplay().setCharacter(2, 13, GeneralHelper.padLeft(GeneralHelper.round(lcd.getTemperatureInCelsius(lcd.getControllerTemperatureInKelvin()), 2) + "C", LCDConstants.NUM_COLS - 13));

        //getCharacterDisplay().setLine(0, "^ " + "GENRL TEMPERATURES");
        //getCharacterDisplay().setLine(1, "  " + "Motor " + GeneralHelper.padLeft(GeneralHelper.round(lcd.getTemperatureInCelsius(lcd.getMotorTemperatureInKelvin()), 2) + "C", LCDConstants.NUM_COLS - 8));
        //getCharacterDisplay().setLine(2, "  " + "Controller " + GeneralHelper.padLeft(GeneralHelper.round(lcd.getTemperatureInCelsius(lcd.getControllerTemperatureInKelvin()), 2) + "C", LCDConstants.NUM_COLS - 13));
        //getCharacterDisplay().setLine(3, "v                   ");
    }

    public void performAction3() {
        currentState = 3;
        lcd = LCDProxy.getInstance();
        bmsManager = BMSManager.getInstance();
        bmsData = (bmsManager == null) ? null : bmsManager.getData();

        if (bmsManager == null || bmsData == null) {
            LOG.error("DrivingModeMenuItemAction.performAction3(): bms is null");
            getCharacterDisplay().setLine(0, "No connection to BMS.");
            getCharacterDisplay().setCharacter(LCDConstants.NUM_ROWS - 1, 0, " ");
            return;
        } else if (lcd == null) {
            LOG.error("DrivingModeMenuItemAction.performAction3(): lcd is null");
            return;
        }

        if (printHeadings.get(2)) {
            printHeadings.set(2, false);
            getCharacterDisplay().setLine(0, "^ BATT TEMPERATURES");
            getCharacterDisplay().setLine(1, "  Avg Temp ");
            getCharacterDisplay().setLine(2, "  Min Temp ");
            getCharacterDisplay().setLine(3, "v Max Temp ");
        }

        getCharacterDisplay().setCharacter(1, 11, GeneralHelper.padLeft((GeneralHelper.round(bmsData.getBmsState().getAverageCellBoardTemp(), 2)) + "C", LCDConstants.NUM_COLS - 11));
        getCharacterDisplay().setCharacter(2, 11, GeneralHelper.padLeft((GeneralHelper.round(bmsData.getBmsState().getMinimumCellBoardTemp(), 2)) + "C", LCDConstants.NUM_COLS - 11));
        getCharacterDisplay().setCharacter(3, 11, GeneralHelper.padLeft((GeneralHelper.round(bmsData.getBmsState().getMaximumCellBoardTemp(), 2)) + "C", LCDConstants.NUM_COLS - 11));

        //getCharacterDisplay().setLine(0, "^ " + "BATT TEMPERATURES");
        //getCharacterDisplay().setLine(1, "  " + "Avg Temp " + GeneralHelper.padLeft((GeneralHelper.round(bmsData.getBmsState().getAverageCellBoardTemp(), 2)) + "C", LCDConstants.NUM_COLS - 11));
        //getCharacterDisplay().setLine(2, "  " + "Min Temp " + GeneralHelper.padLeft((GeneralHelper.round(bmsData.getBmsState().getMinimumCellBoardTemp(), 2)) + "C", LCDConstants.NUM_COLS - 11));
        //getCharacterDisplay().setLine(3, "v " + "Max Temp " + GeneralHelper.padLeft((GeneralHelper.round(bmsData.getBmsState().getMaximumCellBoardTemp(), 2)) + "C", LCDConstants.NUM_COLS - 11));
    }

    public void performAction4() {
        currentState = 4;
        lcd = LCDProxy.getInstance();
        bmsManager = BMSManager.getInstance();
        bmsData = (bmsManager == null) ? null : bmsManager.getData();

        if (bmsManager == null || bmsData == null) {
            LOG.error("DrivingModeMenuItemAction.performAction4(): bms is null");
            getCharacterDisplay().setLine(0, "No connection to BMS.");
            getCharacterDisplay().setLine(1, LCDConstants.BLANK_LINE);
            getCharacterDisplay().setLine(2, LCDConstants.BLANK_LINE);
            getCharacterDisplay().setLine(3, LCDConstants.BLANK_LINE);
            return;
        } else if (lcd == null) {
            LOG.error("DrivingModeMenuItemAction.performAction4(): lcd is null");
            return;
        }

        if (printHeadings.get(3)) {
            printHeadings.set(3, false);
            getCharacterDisplay().setLine(0, "^      VOLTAGES");
            getCharacterDisplay().setLine(1, "  Avg Voltage ");
            getCharacterDisplay().setLine(2, "  Min Voltage ");
            getCharacterDisplay().setLine(3, "v Max Voltage ");
        }

        getCharacterDisplay().setCharacter(1, 14, GeneralHelper.padLeft(String.valueOf(GeneralHelper.round(bmsData.getBmsState().getAverageCellVoltage(), 2)), LCDConstants.NUM_COLS - 14));
        getCharacterDisplay().setCharacter(2, 14, GeneralHelper.padLeft(String.valueOf(GeneralHelper.round(bmsData.getBmsState().getMinimumCellVoltage(), 2)), LCDConstants.NUM_COLS - 14));
        getCharacterDisplay().setCharacter(3, 14, GeneralHelper.padLeft(String.valueOf(GeneralHelper.round(bmsData.getBmsState().getMaximumCellVoltage(), 2)), LCDConstants.NUM_COLS - 14));

        //getCharacterDisplay().setLine(0, "^ " + "     VOLTAGES");
        //getCharacterDisplay().setLine(1, "  " + "Avg Voltage " + GeneralHelper.padLeft(String.valueOf(GeneralHelper.round(bmsData.getBmsState().getAverageCellVoltage(), 2)), LCDConstants.NUM_COLS - 14));
        //getCharacterDisplay().setLine(2, "  " + "Min Voltage " + GeneralHelper.padLeft(String.valueOf(GeneralHelper.round(bmsData.getBmsState().getMinimumCellVoltage(), 2)), LCDConstants.NUM_COLS - 14));
        //getCharacterDisplay().setLine(3, "v " + "Max Voltage " + GeneralHelper.padLeft(String.valueOf(GeneralHelper.round(bmsData.getBmsState().getMaximumCellVoltage(), 2)), LCDConstants.NUM_COLS - 14));
    }

    public void performAction5() {
        currentState = 5;


        lcd = LCDProxy.getInstance();
        bmsManager = BMSManager.getInstance();
        bmsData = (bmsManager == null) ? null : bmsManager.getData();
        gpsManager = GPSManager.getInstance();
        gpsData = (gpsManager == null) ? null : gpsManager.getData();

        if (gpsManager == null || gpsData == null) {
            LOG.error("DrivingModeMenuItemAction.performAction5(): gps is null");
            getCharacterDisplay().setLine(0, "No connection to GPS.");
            getCharacterDisplay().setLine(1, LCDConstants.BLANK_LINE);
            getCharacterDisplay().setLine(2, LCDConstants.BLANK_LINE);
            getCharacterDisplay().setLine(3, LCDConstants.BLANK_LINE);
            return;
        } else if (bmsManager == null || bmsData == null) {
            LOG.error("DrivingModeMenuItemAction.performAction5(): bms is null");
            getCharacterDisplay().setLine(0, "No connection to BMS.");
            getCharacterDisplay().setLine(1, LCDConstants.BLANK_LINE);
            getCharacterDisplay().setLine(2, LCDConstants.BLANK_LINE);
            getCharacterDisplay().setLine(3, LCDConstants.BLANK_LINE);
            return;
        } else if (lcd == null) {
            LOG.error("DrivingModeMenuItemAction.performAction5(): lcd is null");
            return;
        }

        if (printHeadings.get(4)) {
            printHeadings.set(4, false);
            getCharacterDisplay().setLine(0, "^    MY LOCATION");
            getCharacterDisplay().setLine(1, LCDConstants.BLANK_LINE);
            getCharacterDisplay().setLine(3, "v  ");
        }

        if (postgesConnection == null)
            postgesConnection = new PostgesConnect();

        final GPSCoordinate currentTrackPoint = new GPSCoordinate(gpsData.getLongitude(), gpsData.getLatitude());

        if (currentTrackPoint != null && !currentTrackPoint.isNull()) {
            final double lat = currentTrackPoint.getLatitude();
            final double lng = currentTrackPoint.getLongitude();
        }

        final double lat = 40.444583;
        final double lng = -79.942868;

        //http://www.macgeekery.com/hacks/software/using_postgis_reverse_geocode
        final List road = postgesConnection.makeQuery(postgesConnection.getConn(), 1, "SELECT " + roadTablecolumnName + " FROM " + roadTable + " WHERE (the_geom && expand(setsrid(makepoint(" + lng + "," + lat + "), " + srid + "), 1) ) AND distance(setsrid(makepoint(" + lng + "," + lat + "), " + srid + "), the_geom) < 0.001;");
        final List cityState = postgesConnection.makeQuery(postgesConnection.getConn(), 1, "SELECT " + cityStatecolumnName + " FROM " + cityStateTable + " WHERE (the_geom && expand(setsrid(makepoint(" + lng + "," + lat + "), " + srid + "), 1) ) AND distance(setsrid(makepoint(" + lng + "," + lat + "), " + srid + "), the_geom) < 0.001;");

        getCharacterDisplay().setCharacter(2, 0, GeneralHelper.padLeft(String.valueOf(road).replaceAll("\\[|\\]", ""), LCDConstants.NUM_COLS));
        getCharacterDisplay().setCharacter(3, 2, GeneralHelper.padLeft(String.valueOf(cityState.get(0)), LCDConstants.NUM_COLS - 2));
    }

    public void upEvent() {
        if (currentState == 1) {
            currentState = 5;
            printHeadings.set(0, true);
            performAction5();
        } else if (currentState == 2) {
            currentState = 1;
            printHeadings.set(1, true);
            performAction();
        } else if (currentState == 3) {
            currentState = 2;
            printHeadings.set(2, true);
            performAction2();
        } else if (currentState == 4) {
            currentState = 3;
            printHeadings.set(3, true);
            performAction3();
        } else if (currentState == 5) {
            currentState = 4;
            printHeadings.set(4, true);
            performAction4();
        }
    }

    public void downEvent() {
        if (currentState == 1) {
            currentState = 2;
            printHeadings.set(0, true);
            performAction2();
        } else if (currentState == 2) {
            currentState = 3;
            printHeadings.set(1, true);
            performAction3();
        } else if (currentState == 3) {
            currentState = 4;
            printHeadings.set(2, true);
            performAction4();
        } else if (currentState == 4) {
            printHeadings.set(3, true);
            currentState = 5;
            performAction5();
        } else if (currentState == 5) {
            printHeadings.set(4, true);
            currentState = 1;
            performAction();
        }
    }

    protected void postActivate() {
        final int numActions = printHeadings.size();
        for (int i = 0; i < numActions; i++) {
            printHeadings.set(i, true);
        }
    }
}