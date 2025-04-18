package gui;

import java.awt.Graphics;  
import java.awt.LayoutManager;
import java.awt.image.BufferedImage;  
import java.io.IOException;  
import java.io.InputStream;  
import javax.imageio.ImageIO;  
import javax.swing.JPanel;  

public class JPanelWithBackground extends JPanel {  
    private BufferedImage backgroundImage;
   
    public JPanelWithBackground(LayoutManager layout) {  
        super(layout);
        loadBackgroundImage();
    }  
  
    public JPanelWithBackground() {
        super();
        loadBackgroundImage();
    } 
  
    private void loadBackgroundImage() {  
        try (InputStream inputStream = getClass().getResourceAsStream("/perk_manager_background.png")) {  
            if (inputStream != null) {  
                backgroundImage = ImageIO.read(inputStream);  
            } else {  
                System.err.println("Background image not found.");  
            }  
        } catch (IOException e) {  
            e.printStackTrace();
        }  
    }    
   
    @Override  
    protected void paintComponent(Graphics g) {  
        super.paintComponent(g);
        if (backgroundImage != null) {           
            g.drawImage(backgroundImage, 0, 0, getWidth(), getHeight(), this);  
        }  
    }  
}
