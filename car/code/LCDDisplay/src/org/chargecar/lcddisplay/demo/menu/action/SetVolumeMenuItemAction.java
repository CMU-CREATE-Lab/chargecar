package org.chargecar.lcddisplay.demo.menu.action;

import java.util.Map;
import edu.cmu.ri.createlab.display.character.CharacterDisplay;
import edu.cmu.ri.createlab.display.character.menu.CharacterDisplayMenuItemAction;
import edu.cmu.ri.createlab.menu.MenuItem;
import edu.cmu.ri.createlab.menu.MenuStatusManager;
import org.apache.log4j.Logger;

/**
 * <p>
 * The <code>SetVolumeMenuItemAction</code> class is a {@link CharacterDisplayMenuItemAction} for setting the current
 * volume.
 * </p>
 * <p>
 * Users and subclasses can override the default volume label ("Volume") and the default "Lo", "Hi", and "Off" labels by
 * constructing the instance with a {@link Map} containing keys <code>label.volume</code>, <code>label.low</code>,
 * <code>label.high</code>, and <code>label.off</code>.  The values for those keys will be used instead of the defaults.
 * The messages displayed upon action success or cancel can be similarly customized by specifying
 * <code>action.performed</code> and <code>action.cancelled</code>.  Finally, the character used in the bar graph can
 * be customized by setting the <code>graph.character</code> property.
 * </p>
 *
 * @author Chris Bartley (bartley@cmu.edu)
 */
public final class SetVolumeMenuItemAction extends CharacterDisplayMenuItemAction
   {
   private static final Logger LOG = Logger.getLogger(SetVolumeMenuItemAction.class);

   private static final int MIN_VOLUME = 0;
   private static final int MAX_VOLUME = 10;
   private static final String DEFAULT_LABEL_VOLUME = "Volume";
   private static final String DEFAULT_LABEL_LOW = "Lo";
   private static final String DEFAULT_LABEL_HIGH = "Hi";
   private static final String DEFAULT_LABEL_OFF = "Off";
   private static final String DEFAULT_VOLUME_GRAPH_CHARACTER = "*";
   private static final String DEFAULT_LABEL_ACTION_PERFORMED = "Volume set to ";
   private static final String DEFAULT_LABEL_ACTION_CANCELLED = "Cancelled!";

   private static final String PROPERTY_GRAPH_CHARACTER = "graph.character";
   private static final String PROPERTY_LABEL_VOLUME = "label.volume";
   private static final String PROPERTY_LABEL_HIGH = "label.high";
   private static final String PROPERTY_LABEL_LOW = "label.low";
   private static final String PROPERTY_LABEL_OFF = "label.off";
   private static final String PROPERTY_ACTION_PERFORMED = "action.performed";
   private static final String PROPERTY_ACTION_CANCELLED = "action.cancelled";

   private int volume = 5;
   private int tempVolume = 5;

   public SetVolumeMenuItemAction(final MenuItem menuItem,
                                  final MenuStatusManager menuStatusManager,
                                  final CharacterDisplay characterDisplay)
      {
      this(menuItem, menuStatusManager, characterDisplay, null);
      }

   public SetVolumeMenuItemAction(final MenuItem menuItem,
                                  final MenuStatusManager menuStatusManager,
                                  final CharacterDisplay characterDisplay,
                                  final Map<String, String> properties)
      {
      super(menuItem, menuStatusManager, characterDisplay, properties);
      }

   public void activate()
      {
      tempVolume = getCurrentVolume();
      getCharacterDisplay().setLine(0, generateVolumeLine());
      getCharacterDisplay().setLine(1, generateVolumeGraphLine());
      }

   public final void start()
      {
      setCurrentVolume(tempVolume);
      getCharacterDisplay().setText(getActionPerformedText());
      sleepThenPopUpToParentMenuItem();
      }

   public final void stop()
      {
      getCharacterDisplay().setText(getActionCancelledText());
      sleepThenPopUpToParentMenuItem();
      }

   public void upEvent()
      {
      rightEvent();
      }

   public void downEvent()
      {
      leftEvent();
      }

   public final void rightEvent()
      {
      tempVolume++;
      if (tempVolume > MAX_VOLUME)
         {
         tempVolume = MAX_VOLUME;
         }
      getCharacterDisplay().setLine(0, generateVolumeLine());
      getCharacterDisplay().setLine(1, generateVolumeGraphLine());
      }

   public final void leftEvent()
      {
      tempVolume--;
      if (tempVolume < MIN_VOLUME)
         {
         tempVolume = MIN_VOLUME;
         }
      getCharacterDisplay().setLine(0, generateVolumeLine());
      getCharacterDisplay().setLine(1, generateVolumeGraphLine());
      }

   private String getActionPerformedText()
      {
      return getProperty(PROPERTY_ACTION_PERFORMED, DEFAULT_LABEL_ACTION_PERFORMED) + volume;
      }

   private String getActionCancelledText()
      {
      return getProperty(PROPERTY_ACTION_CANCELLED, DEFAULT_LABEL_ACTION_CANCELLED);
      }

   private int getCurrentVolume()
      {
      // TODO: fetch from wherever we're persisting this
      return volume;
      }

   private void setCurrentVolume(final int newVolume)
      {
      // TODO: persist this
      volume = newVolume;
      }

   private String generateVolumeLine()
      {
      final String volumeLabel = getProperty(PROPERTY_LABEL_VOLUME, DEFAULT_LABEL_VOLUME);
      final String volumeValue = (tempVolume == 0 ? getProperty(PROPERTY_LABEL_OFF, DEFAULT_LABEL_OFF) : String.valueOf(tempVolume));
      return volumeLabel + ": " + volumeValue;
      }

   private String generateVolumeGraphLine()
      {
      final String graphCharacter = getProperty(PROPERTY_GRAPH_CHARACTER, DEFAULT_VOLUME_GRAPH_CHARACTER);

      final StringBuilder s = new StringBuilder();
      for (int i = 0; i < tempVolume; i++)
         {
         s.append(graphCharacter);
         }
      for (int i = tempVolume; i < MAX_VOLUME; i++)
         {
         s.append(" ");
         }
      return getProperty(PROPERTY_LABEL_LOW, DEFAULT_LABEL_LOW) + " " + s + " " + getProperty(PROPERTY_LABEL_HIGH, DEFAULT_LABEL_HIGH);
      }

   private void sleepThenPopUpToParentMenuItem()
      {
      sleep();
      super.stop();
      }

   private void sleep()
      {
      try
         {
         Thread.sleep(2000);
         }
      catch (InterruptedException e)
         {
         LOG.error("SetVolumeMenuItemAction.sleep(): InterruptedException while sleeping", e);
         }
      }
   }