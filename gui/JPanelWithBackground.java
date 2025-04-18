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

    // Konstruktőr, amely a layout-ot várja  
    public JPanelWithBackground(LayoutManager layout) {  
        super(layout); // Hívja a szülő JPanel konstruktort a layout paraméterrel  
        loadBackgroundImage(); // Háttérkép betöltése  
    }  

    // Alapértelmezett konstruktor  
    public JPanelWithBackground() {
        super();
        loadBackgroundImage(); // Háttérkép betöltése  
    }    

    // Háttérkép betöltésének metódusa  
    private void loadBackgroundImage() {  
        try (InputStream inputStream = getClass().getResourceAsStream("/perk_manager_background.png")) {  
            if (inputStream != null) {  
                backgroundImage = ImageIO.read(inputStream);  
            } else {  
                System.err.println("Background image not found.");  
            }  
        } catch (IOException e) {  
            e.printStackTrace(); // Hiba kiírása, ha nem sikerült betölteni az ikont  
        }  
    }  
    
    // A háttérkép rajzolása  
    @Override  
    protected void paintComponent(Graphics g) {  
        super.paintComponent(g); // A szülő panel rajzolása  
        if (backgroundImage != null) {  
            // A háttérkép átméretezése az aktuális panel méretéhez  
            g.drawImage(backgroundImage, 0, 0, getWidth(), getHeight(), this);  
        }  
    }  
}
