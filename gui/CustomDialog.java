package gui;

import javax.swing.*;

public class CustomDialog {

    // Static method for warning messages
    public static void showWarning(String message) {
        showCustomDialog(message, "Warning");
    }

    // Static method for error messages
    public static void showError(String message) {
        showCustomDialog(message, "Error");
    }

    // Static method for informational messages
    public static void showInfo(String message) {
        showCustomDialog(message, "Information");
    }

    public static int showConfirm(String message, String title) {
        int option = JOptionPane.showConfirmDialog(null, message, title, JOptionPane.YES_NO_OPTION);
        return option;
    }

    // Method to create a custom dialog
    private static void showCustomDialog(String message, String title) {
        CustomPopupWindow popup = new CustomPopupWindow(title, message);
        popup.show(); // Display the custom popup window
    }
}
