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

    private static final String PERK_FILE = "/perks.csv"; // A fájl az 'resources' mappában van
    private Map<String, Perk> perkMap;
    private Map<String, ImageIcon> perkIcons;  // 🔹 Ikonok gyorsítótára

    public PerkStorage() {
        this.perkMap = new HashMap<>();
        this.perkIcons = new HashMap<>();
        loadPerks();
    }

    // 🔹 CSV fájl beolvasása és perkMap feltöltése
    private void loadPerks() {
        try (InputStream inputStream = getClass().getResourceAsStream(PERK_FILE); BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))) {

            String line;
            boolean firstLine = true;

            while ((line = reader.readLine()) != null) {
                if (firstLine) {
                    firstLine = false; // Skip header
                    continue;
                }

                // Clean up the line: remove extra commas at the end and quotes around the text
                line = line.replaceAll(",+$", ""); // Remove trailing commas
                line = line.replaceAll("^\"(.*)\"$", "$1"); // Remove quotes around the entire line

                // Now, split by comma, but ensure we handle commas inside quotes
                String[] parts = line.split("\",\"");

                // Handle case where splitting with comma inside quotes didn't work perfectly
                if (parts.length != 3) {
                    // Try manually splitting by comma
                    String[] manualParts = line.split(",", 3); // Split into three parts
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

                    // Perk creation and storage
                    perkMap.put(name, new Perk(name, description, iconPath));

                    // Load icon if it exists
                    String iconFilePath = iconPath;  // Just use the icon path as is
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
            // A resources könyvtárat az osztályúton keresve érjük el.
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

    /**
     * Visszaadja a megadott felhasználó által birtokolt perkeket. A felhasználó
     * objektumban tárolt perk neveket használja a részletes perkadatok
     * kikeresésére.
     *
     * @param user a felhasználó, akinek a perkeit szeretnénk lekérni
     * @return a felhasználó perkeinek listája
     */
   public List<Perk> getPerksForUser(User user) {
        return user.getPerks();
}


    // 🔹 Egy adott perk lekérése
    public Perk getPerkByName(String perkName) {
        return perkMap.get(perkName);
    }

    // 🔹 Egy adott perk ikonjának lekérése
    public ImageIcon getPerkIcon(String perkName) {
        return perkIcons.getOrDefault(perkName, null);
    }

    // 🔹 Összes perk név lekérése kereséshez
    public List<String> getAllPerkNames() {
        return new ArrayList<>(perkMap.keySet());
    }
}