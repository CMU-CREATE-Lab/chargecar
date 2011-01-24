package org.chargecar.lcddisplay.demo.menu.action;

import java.util.Map;
import edu.cmu.ri.createlab.display.character.CharacterDisplay;
import edu.cmu.ri.createlab.display.character.menu.TwoOptionMenuItemAction;
import edu.cmu.ri.createlab.menu.MenuItem;
import edu.cmu.ri.createlab.menu.MenuStatusManager;

/**
 * @author Chris Bartley (bartley@cmu.edu)
 */
public final class ResetAllMenuItemAction extends TwoOptionMenuItemAction
   {
   public ResetAllMenuItemAction(final MenuItem menuItem,
                                 final MenuStatusManager menuStatusManager,
                                 final CharacterDisplay characterDisplay)
      {
      this(menuItem, menuStatusManager, characterDisplay, null);
      }

   public ResetAllMenuItemAction(final MenuItem menuItem,
                                 final MenuStatusManager menuStatusManager,
                                 final CharacterDisplay characterDisplay,
                                 final Map<String, String> properties)
      {
      super(menuItem, menuStatusManager, characterDisplay, properties);
      }

   protected boolean shouldOption1BeSelectedUponActivation()
      {
      return false;
      }

   protected void executeOption1Action()
      {
      // todo
      }

   protected void executeOption2Action()
      {
      // do nothing
      }
   }