package com.meizu.tool.utils;

import javax.swing.*;
import java.awt.*;

public class MessageTipUtils {

    public static void infoTip(Component parentComponent, String title, String message) {
        JOptionPane.showMessageDialog(parentComponent, message, title, JOptionPane.INFORMATION_MESSAGE);
    }


    public static void warnTip(Component parentComponent, String title, String message) {
        JOptionPane.showMessageDialog(parentComponent, message, title, JOptionPane.WARNING_MESSAGE);
    }


    public static void errorTip(Component parentComponent, String title, String message) {
        JOptionPane.showMessageDialog(parentComponent, message, title, JOptionPane.ERROR_MESSAGE);
    }

}
