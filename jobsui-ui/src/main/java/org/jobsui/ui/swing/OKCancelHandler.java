package org.jobsui.ui.swing;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;

/**
 * Created by enrico on 5/16/16.
 */
class OKCancelHandler {
    private final JButton okButton = new JButton("OK");
    private boolean ok;

    public OKCancelHandler(final Window window, JComponent parent, JComponent content) {
        {
            GridBagConstraints constraints = new GridBagConstraints();
            constraints.fill = GridBagConstraints.BOTH;
            constraints.weightx = 1.0;
            constraints.weighty = 1.0;
            //            containersPanel.setLayout(new GridBagLayout());
            parent.add(content, constraints);
        }

        // buttons

        JPanel buttonsPanel = new JPanel();
        buttonsPanel.setLayout(new GridBagLayout());

        {
            GridBagConstraints constraints;
            constraints = new GridBagConstraints();
            constraints.weightx = 1.0;
            constraints.gridy = 1;
            constraints.anchor = GridBagConstraints.SOUTH;
            constraints.fill = GridBagConstraints.HORIZONTAL;
            parent.add(buttonsPanel, constraints);
        }

        // filler
        {
            GridBagConstraints constraints;
            constraints = new GridBagConstraints();
            constraints.fill = GridBagConstraints.HORIZONTAL;
            constraints.gridx = 0;
            constraints.weightx = 1.0;
            buttonsPanel.add(new JPanel(), constraints);
        }

        // OK
        {
            GridBagConstraints constraints;
            constraints = new GridBagConstraints();
            constraints.fill = GridBagConstraints.NONE;
            constraints.gridx = 1;
            constraints.insets.top = 5;
            constraints.insets.bottom = 5;
            constraints.insets.right = 5;

            buttonsPanel.add(okButton, constraints);
        }

        okButton.addActionListener(e -> {
            window.setVisible(false);
            window.dispose();
            ok = true;
        });

        // Cancel
        final JButton cancelButton = new JButton("Cancel");
        {
            GridBagConstraints constraints;
            constraints = new GridBagConstraints();
            constraints.fill = GridBagConstraints.NONE;
            constraints.gridx = 2;
            constraints.insets.top = 5;
            constraints.insets.bottom = 5;
            constraints.insets.right = 5;

            buttonsPanel.add(cancelButton, constraints);
        }

        cancelButton.addActionListener(new ActionListener() {
            {
                // to let OK button be the triggered on ESC
                cancelButton.registerKeyboardAction(this, KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0),
                        JComponent.WHEN_IN_FOCUSED_WINDOW);
            }

            @Override
            public void actionPerformed(ActionEvent e) {
                window.setVisible(false);
                window.dispose();
                ok = false;
            }
        });

        // to let OK button be the triggered on ENTER
        SwingUtilities.getRootPane(okButton).setDefaultButton(okButton);

        // to let OK button be the triggered on ESC
        cancelButton.setMnemonic(KeyEvent.VK_ESCAPE);
    }

    public boolean isOk() {
        return ok;
    }

    public void setOKButtonEnabled(boolean enabled) {
        okButton.setEnabled(enabled);
    }

    public void triggerOK() {
        okButton.doClick();
    }
}
