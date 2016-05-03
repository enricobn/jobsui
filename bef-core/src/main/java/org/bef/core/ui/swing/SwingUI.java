package org.bef.core.ui.swing;

import org.bef.core.ui.UI;
import org.bef.core.ui.UIWindow;

import javax.swing.*;

/**
 * Created by enrico on 11/2/15.
 */
public class SwingUI implements UI {

    @Override
    public void showMessage(String message) {
        JOptionPane.showMessageDialog(null, message, "TGKDevContainer", JOptionPane.INFORMATION_MESSAGE);
    }

    @Override
    public UIWindow createWindow(String title) {
        return new SwingUIWindow(title);
    }

    @Override
    public void log(String log) {
        // TODO
    }

    @Override
    public void log(String message, Throwable th) {
        // TODO
    }
}
