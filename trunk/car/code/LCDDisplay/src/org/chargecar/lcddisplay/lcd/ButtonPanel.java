package org.chargecar.lcddisplay.lcd;


import edu.cmu.ri.createlab.ButtonPanelEventListener;
import org.jdesktop.layout.GroupLayout;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * @author Chris Bartley (bartley@cmu.edu)
 */
final class ButtonPanel
   {
   private static final String FONT_NAME = "Monaco";
   private static final Font FONT_NORMAL = new Font(FONT_NAME, 0, 11);

   private final JPanel panel = new JPanel();
   private final JButton okButton;
   private final JButton cancelButton;
   private final JButton upButton;
   private final JButton rightButton;
   private final JButton downButton;
   private final JButton leftButton;

   private final Set<ButtonPanelEventListener> buttonPanelEventListeners = new HashSet<ButtonPanelEventListener>();

   private final ExecutorService executor = Executors.newCachedThreadPool();

   ButtonPanel()
      {
      okButton = createButton("OK",
                              new Runnable()
                              {
                              public void run()
                                 {
                                 for (final ButtonPanelEventListener listener : buttonPanelEventListeners)
                                    {
                                    listener.handleOKEvent();
                                    }
                                 }
                              });
      cancelButton = createButton("X",
                                  new Runnable()
                                  {
                                  public void run()
                                     {
                                     for (final ButtonPanelEventListener listener : buttonPanelEventListeners)
                                        {
                                        listener.handleCancelEvent();
                                        }
                                     }
                                  });
      upButton = createButton("",
                              new Runnable()
                              {
                              public void run()
                                 {
                                 for (final ButtonPanelEventListener listener : buttonPanelEventListeners)
                                    {
                                    listener.handleUpEvent();
                                    }
                                 }
                              });
      rightButton = createButton("",
                                 new Runnable()
                                 {
                                 public void run()
                                    {
                                    for (final ButtonPanelEventListener listener : buttonPanelEventListeners)
                                       {
                                       listener.handleRightEvent();
                                       }
                                    }
                                 });
      downButton = createButton("",
                                new Runnable()
                                {
                                public void run()
                                   {
                                   for (final ButtonPanelEventListener listener : buttonPanelEventListeners)
                                      {
                                      listener.handleDownEvent();
                                      }
                                   }
                                });
      leftButton = createButton("",
                                new Runnable()
                                {
                                public void run()
                                   {
                                   for (final ButtonPanelEventListener listener : buttonPanelEventListeners)
                                      {
                                      listener.handleLeftEvent();
                                      }
                                   }
                                });
      final Component spacer = Box.createRigidArea(new Dimension(20, 1));

      final JPanel directionButtonPanel = new JPanel();
      final GroupLayout directionButtonGroupLayout = new GroupLayout(directionButtonPanel);
      directionButtonPanel.setLayout(directionButtonGroupLayout);
      directionButtonPanel.setBackground(Color.WHITE);

      directionButtonGroupLayout.setHorizontalGroup(
            directionButtonGroupLayout.createSequentialGroup()
                  .add(leftButton)
                  .add(directionButtonGroupLayout.createParallelGroup(GroupLayout.CENTER)
                             .add(upButton)
                             .add(downButton))
                  .add(rightButton)
                  .add(spacer)
                  .add(directionButtonGroupLayout.createParallelGroup(GroupLayout.CENTER)
                             .add(okButton)
                             .add(cancelButton))
      );
      directionButtonGroupLayout.setVerticalGroup(
            directionButtonGroupLayout.createSequentialGroup()
                  .add(directionButtonGroupLayout.createParallelGroup(GroupLayout.CENTER)
                             .add(upButton)
                             .add(okButton))
                  .add(directionButtonGroupLayout.createParallelGroup(GroupLayout.CENTER)
                             .add(leftButton)
                             .add(rightButton)
                             .add(spacer))
                  .add(directionButtonGroupLayout.createParallelGroup(GroupLayout.CENTER)
                             .add(downButton)
                             .add(cancelButton))
      );

      // ---------------------------------------------------------------------------------------------------------------

      panel.setBackground(Color.WHITE);
      panel.add(directionButtonPanel);
      }

   private JButton createButton(final String label, final Runnable task)
      {
      final JButton button = new JButton(label);
      button.setFont(FONT_NORMAL);
      if (task != null)
         {
         button.addActionListener(new ButtonActionListener(task));
         }
      return button;
      }

   public JComponent getComponent()
      {
      return panel;
      }

   public void addButtonPanelEventListener(final ButtonPanelEventListener listener)
      {
      if (listener != null)
         {
         buttonPanelEventListeners.add(listener);
         }
      }

   public void removeButtonPanelEventListener(final ButtonPanelEventListener listener)
      {
      if (listener != null)
         {
         buttonPanelEventListeners.remove(listener);
         }
      }

   public void setEnabled(final boolean isEnabled)
      {
      okButton.setEnabled(isEnabled);
      cancelButton.setEnabled(isEnabled);
      upButton.setEnabled(isEnabled);
      rightButton.setEnabled(isEnabled);
      downButton.setEnabled(isEnabled);
      leftButton.setEnabled(isEnabled);
      }

   private final class ButtonActionListener implements ActionListener
      {
      private final Runnable task;

      private ButtonActionListener(final Runnable task)
         {
         this.task = task;
         }

      public void actionPerformed(final ActionEvent e)
         {
         executor.execute(task);
         }
      }
   }