package org.chargecar.lcddisplay.menu.action;

import edu.cmu.ri.createlab.display.character.CharacterDisplay;
import edu.cmu.ri.createlab.display.character.menu.CharacterDisplayMenuItemAction;
import edu.cmu.ri.createlab.menu.MenuItem;
import edu.cmu.ri.createlab.menu.MenuStatusManager;
import edu.cmu.ri.createlab.util.VersionNumberReader;
import org.apache.log4j.Logger;
import org.chargecar.lcddisplay.LCD;
import org.chargecar.lcddisplay.LCDConstants;
import org.chargecar.lcddisplay.LCDProxy;

/**
 * @author Paul Dille (pdille@andrew.cmu.edu)
 */
public final class VersionInfoMenuItemAction extends CharacterDisplayMenuItemAction {
    private static final Logger LOG = Logger.getLogger(ErrorCodesMenuItemAction.class);

    public VersionInfoMenuItemAction(final MenuItem menuItem,
                                     final MenuStatusManager menuStatusManager,
                                     final CharacterDisplay characterDisplay) {
        super(menuItem, menuStatusManager, characterDisplay);
    }

    public void activate() {
        final LCD lcd = LCDProxy.getInstance();

        if (lcd == null) {
            LOG.error("VersionInfoMenuItemAction.activate(): lcd is null");
            return;
        }
        final VersionNumberReader.VersionDetails versionDetails = VersionNumberReader.getVersionDetails();
        if (versionDetails != null) {
            getCharacterDisplay().setLine(0, "ChargeCar Display");
            getCharacterDisplay().setLine(1, "Version: " + versionDetails.getMajorMinorRevision());
            getCharacterDisplay().setLine(2, LCDConstants.BLANK_LINE);
            getCharacterDisplay().setLine(3, LCDConstants.BLANK_LINE);
        } else {
            LOG.error("VersionInfoMenuItemAction.activate(): versionDetails is null");
            return;
        }
    }
}
