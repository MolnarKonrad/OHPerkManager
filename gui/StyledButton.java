package gui;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Font;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import javax.swing.Icon;
import javax.swing.JButton;

public class StyledButton extends JButton{
    
    public StyledButton(String text){
        super(text);
        initialize();
    }
    
    public StyledButton (String text, ActionListener actionListener){
        super(text);
        initialize();
        this.addActionListener(actionListener);
    }
    
    public StyledButton(String text, Icon icon) {  
        super(text, icon);  
        initialize();  
    }  

    public StyledButton(String text, Icon icon, ActionListener actionListener) {  
        super(text, icon);  
        initialize();  
        this.addActionListener(actionListener);  
    }
    
    private void initialize(){
        setBackground(new Color(81, 203, 203));
        setForeground(Color.BLACK);
        setBorder(new RoundedBorder(15, new Color(12, 45, 33), 20));
        setFont(new Font("Times New Roman", Font.BOLD, 19));
        setFocusPainted(false);
        setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        setFocusable(false);
        
        // Hover effekt  
        addMouseListener(new MouseAdapter() {  
        @Override
        public void mouseEntered(java.awt.event.MouseEvent evt) {  
            setBackground(new Color(41, 124, 122)); // Kicsit világosabb sötétkék  
        }  
        @Override
        public void mouseExited(java.awt.event.MouseEvent evt) {  
            setBackground(new Color(81, 203, 203)); // Vissza sötétkékre  
        }
        });
    }
    
    public void setButtonText(String text){
        this.setText(text);
    }
}
