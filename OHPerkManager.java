
import gui.MainFrame;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import utils.SupabaseClient;
import utils.SupabaseClientManager;

public class OHPerkManager {

    public static void main(String[] args) {
        try {
            // Nimbus kinézet beállítása
            for (UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException | IllegalAccessException | InstantiationException | UnsupportedLookAndFeelException e) {
            // Ha nem sikerül, az alapértelmezett kinézetet használjuk.
        }
        
        // Supabase kapcsolat lekérése a Singletonból
        SupabaseClient supabase = SupabaseClientManager.getInstance();
        
        // GUI indítása, a kapcsolatot továbbadva a MainFrame-nek
        SwingUtilities.invokeLater(() -> new MainFrame(supabase));
    }
}


//public class OHPerkManager {
//
//    public static void main(String[] args) {
//        try {
//            // Megpróbáljuk beállítani a Nimbus kinézetet
//            for (UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
//                if ("Nimbus".equals(info.getName())) {
//                    UIManager.setLookAndFeel(info.getClassName());
//                    break;
//                }
//            }
//        } catch (ClassNotFoundException | IllegalAccessException | InstantiationException | UnsupportedLookAndFeelException e) {
//            // It is okay to ignore LookAndFeel exceptions, the default LookAndFeel will be used.
//        }
//
//        try {
//            
//            
//            
//
//            // GUI indítása a főablakkal
//            SwingUtilities.invokeLater(() -> new MainFrame(db));
//
//        } catch (IOException e) {
//            JOptionPane.showMessageDialog(null, "Error reading service account file: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
//        } catch (Exception e) { // Catch other potential Firebase exceptions
//            JOptionPane.showMessageDialog(null, "An unexpected error occurred: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
//        }
//
//    }
//
//}
