package org.chargecar.lcddisplay.demo.menu.action;

import java.util.Map;
import edu.cmu.ri.createlab.display.character.CharacterDisplay;
import edu.cmu.ri.createlab.display.character.menu.TwoOptionMenuItemAction;
import edu.cmu.ri.createlab.menu.MenuItem;
import edu.cmu.ri.createlab.menu.MenuStatusManager;

/**
 * @author Chris Bartley (bartley@cmu.edu)
 */
public final class ToggleAudioAlertsMenuItemAction extends TwoOptionMenuItemAction
   {
   private boolean isAlertsEnabled = false;

   public ToggleAudioAlertsMenuItemAction(final MenuItem menuItem,
                                          final MenuStatusManager menuStatusManager,
                                          final CharacterDisplay characterDisplay)
      {
      this(menuItem, menuStatusManager, characterDisplay, null);
      }

   public ToggleAudioAlertsMenuItemAction(final MenuItem menuItem,
                                          final MenuStatusManager menuStatusManager,
                                          final CharacterDisplay characterDisplay,
                                          final Map<String, String> properties)
      {
      super(menuItem, menuStatusManager, characterDisplay, properties);
      }

   protected boolean shouldOption1BeSelectedUponActivation()
      {
      // todo: fetch this from wherever we do persistence
      return isAlertsEnabled;
      }

   protected void executeOption1Action()
      {
      setAlertsEnabled(true);
      }

   protected void executeOption2Action()
      {
      setAlertsEnabled(false);
      }

   private void setAlertsEnabled(final boolean isAlertsEnabled)
      {
      // todo: write this this to wherever we do persistence
      this.isAlertsEnabled = isAlertsEnabled;
      }
   }