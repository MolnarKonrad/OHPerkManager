package services;

import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import models.Perk;
import models.User;

public class PerkStorage {

    private static final String PERK_FILE = "/perks.csv";
    private Map<String, Perk> perkMap;
    private Map<String, ImageIcon> perkIcons; 

    public PerkStorage() {
        this.perkMap = new HashMap<>();
        this.perkIcons = new HashMap<>();
        loadPerks();
    }
    
    private void loadPerks() {
        try (InputStream inputStream = getClass().getResourceAsStream(PERK_FILE); BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))) {

            String line;
            boolean firstLine = true;

            while ((line = reader.readLine()) != null) {
                if (firstLine) {
                    firstLine = false;
                    continue;
                }
               
                line = line.replaceAll(",+$", "");
                line = line.replaceAll("^\"(.*)\"$", "$1");
               
                String[] parts = line.split("\",\"");
               
                if (parts.length != 3) {                   
                    String[] manualParts = line.split(",", 3);
                    if (manualParts.length == 3) {
                        parts = manualParts;
                    } else {
                        continue;
                    }
                }

                if (parts.length == 3) {
                    String name = parts[0].trim();
                    String description = parts[1].trim();
                    String iconPath = parts[2].trim();
                   
                    perkMap.put(name, new Perk(name, description, iconPath));
                   
                    String iconFilePath = iconPath;
                    ImageIcon icon = loadImage(iconFilePath);
                    if (icon != null) {
                        perkIcons.put(name, icon);
                    }
                }
            }
        } catch (IOException e) {

        }
    }

    private ImageIcon loadImage(String filePath) {
        try {           
            InputStream inputStream = getClass().getClassLoader().getResourceAsStream(filePath);

            if (inputStream == null) {
                return null;
            }

            BufferedImage image = ImageIO.read(inputStream);
            return new ImageIcon(image);
        } catch (IOException e) {
            return null;
        }
    }
   
   public List<Perk> getPerksForUser(User user) {
        return user.getPerks();
   }
    
    public Perk getPerkByName(String perkName) {
        return perkMap.get(perkName);
    }
   
    public ImageIcon getPerkIcon(String perkName) {
        return perkIcons.getOrDefault(perkName, null);
    }
    
    public List<String> getAllPerkNames() {
        return new ArrayList<>(perkMap.keySet());
    }
}
