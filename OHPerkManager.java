
import gui.MainFrame;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import utils.SupabaseClient;
import utils.SupabaseClientManager;

public class OHPerkManager {

    public static void main(String[] args) {
        try {            
            for (UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException | IllegalAccessException | InstantiationException | UnsupportedLookAndFeelException e) {
           
        }
       
        SupabaseClient supabase = SupabaseClientManager.getInstance();
        
        SwingUtilities.invokeLater(() -> new MainFrame(supabase));
    }
}
