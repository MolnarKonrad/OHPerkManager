package gui;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Font;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import javax.swing.Icon;
import javax.swing.JButton;

public class StyledButtonRed extends JButton{
    
    public StyledButtonRed(String text){
        super(text);
        initialize();
    }
    
    public StyledButtonRed (String text, ActionListener actionListener){
        super(text);
        initialize();
        this.addActionListener(actionListener);
    }
    
    public StyledButtonRed(String text, Icon icon) {  
        super(text, icon);  
        initialize();  
    }  

    public StyledButtonRed(String text, Icon icon, ActionListener actionListener) {  
        super(text, icon);  
        initialize();  
        this.addActionListener(actionListener);  
    }
    
    private void initialize(){
        setBackground(new Color(255, 3, 3));
        setForeground(Color.WHITE);
        setBorder(new RoundedBorder(15, new Color(12, 45, 33), 20));
        setFont(new Font("Arial", Font.BOLD, 19));
        setFocusPainted(false);
        setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        setFocusable(false);
        
        // Hover effekt  
        addMouseListener(new MouseAdapter() {  
        @Override
        public void mouseEntered(java.awt.event.MouseEvent evt) {  
            setBackground(new Color(149, 6, 6)); // Kicsit világosabb sötétkék  
        }  
        @Override
        public void mouseExited(java.awt.event.MouseEvent evt) {  
            setBackground(new Color(255, 3, 3)); // Vissza sötétkékre  
        }
        });
    }
    
    public void setButtonText(String text){
        this.setText(text);
    }
}
