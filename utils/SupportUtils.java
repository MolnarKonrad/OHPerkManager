package utils;

import java.awt.Desktop;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import javax.swing.JOptionPane;

public class SupportUtils {
    public static void openPatreonLink() {
        String patreonUrl = "https://www.patreon.com/PerkManager";
        try {
            Desktop.getDesktop().browse(new URI(patreonUrl));
        } catch (IOException | URISyntaxException ex) {
            JOptionPane.showMessageDialog(null, "Unable to open Patreon link.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    public static void openUserGuideLink() {
        String userGuideUrl = "https://www.patreon.com/posts/perk-manager-for-126121595";
        try {
            Desktop.getDesktop().browse(new URI(userGuideUrl));
        } catch (IOException | URISyntaxException ex) {
            JOptionPane.showMessageDialog(null, "Unable to open Patreon link.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}
