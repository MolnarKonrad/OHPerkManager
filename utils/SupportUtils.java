package utils;

import java.awt.Desktop;
import java.net.URI;
import javax.swing.JOptionPane;

public class SupportUtils {
    public static void openPatreonLink() {
        String patreonUrl = "https://www.patreon.com/IamMKey"; // Cseréld ki a te Patreon linkedre!
        try {
            Desktop.getDesktop().browse(new URI(patreonUrl));
        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(null, "Unable to open Patreon link.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}
