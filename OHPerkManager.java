
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
