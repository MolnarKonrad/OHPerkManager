package gui;

import javax.swing.*;

public class CustomDialog {
    
    public static void showWarning(String message) {
        showCustomDialog(message, "Warning");
    }
   
    public static void showError(String message) {
        showCustomDialog(message, "Error");
    }
   
    public static void showInfo(String message) {
        showCustomDialog(message, "Information");
    }

    public static int showConfirm(String message, String title) {
        int option = JOptionPane.showConfirmDialog(null, message, title, JOptionPane.YES_NO_OPTION);
        return option;
    }
   
    private static void showCustomDialog(String message, String title) {
        CustomPopupWindow popup = new CustomPopupWindow(title, message);
        popup.show();
    }
}
