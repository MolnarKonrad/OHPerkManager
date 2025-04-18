package gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Image;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.List;
import java.util.stream.Collectors;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import models.Perk;
import services.PerkStorage;

public class PerkSearchPanel extends JPanelWithBackground {

    private JTextField searchField;
    private JPanel resultsPanel;
    private StyledButtonCyan backButton;
    private PerkStorage perkStorage;

    public PerkSearchPanel(PerkStorage perkStorage, ActionListener onPerkSelected, Runnable onBack) {
        this.perkStorage = perkStorage;
        setLayout(new BorderLayout());

        // Cím és kereső mező  
        JPanel headerPanel = new JPanel();
        headerPanel.setLayout(new BorderLayout());
        headerPanel.setOpaque(true);
        headerPanel.setBackground(new Color(81, 203, 203, 100));
        JLabel titleLabel = new JLabel("Search Perks", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 25));
        headerPanel.add(titleLabel, BorderLayout.NORTH);

        // Create the search field and customize it  
        searchField = new JTextField();
        searchField.setPreferredSize(new Dimension(300, 60)); // Set preferred size  
        searchField.setFont(new Font("Arial", Font.BOLD, 16)); // Set font  
        searchField.setForeground(Color.BLACK); // Text color  
        searchField.setBackground(new Color(81, 203, 203)); // Light yellow background  
        searchField.setMargin(new Insets(10, 10, 10, 10)); // Add margin for better spacing  



// Add key listener for search functionality  
        searchField.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                String query = searchField.getText();
                updateResults(query, onPerkSelected);
            }
        });

// Add the search field to the header panel  
        headerPanel.add(searchField, BorderLayout.SOUTH);
        add(headerPanel, BorderLayout.NORTH);

        // Eredmények panel  
        resultsPanel = new JPanelWithBackground();
        resultsPanel.setLayout(new GridBagLayout()); // Használj GridBagLayout-ot  
        JScrollPane scrollPane = new JScrollPane(resultsPanel);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setPreferredSize(new Dimension(400, 300)); // ScrollPane mérete  
        add(scrollPane, BorderLayout.CENTER);

        // Vissza gomb  
        backButton = new StyledButtonCyan("Back");
        backButton.addActionListener(e -> onBack.run());
        add(backButton, BorderLayout.SOUTH);
    }

    private void updateResults(String query, ActionListener onPerkSelected) {
        resultsPanel.removeAll(); // Eredmények panel újrafeltöltése  

        List<String> matchingPerks = perkStorage.getAllPerkNames().stream()
                .filter(name -> name.toLowerCase().contains(query.toLowerCase()))
                .collect(Collectors.toList());

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10); // Margók a gombok körül  
        gbc.anchor = GridBagConstraints.CENTER; // Középre igazítás  

        int maxMembers = Math.min(matchingPerks.size(), 10); // Maximum 10 gomb  
        for (int i = 0; i < maxMembers; i++) {
            String perkName = matchingPerks.get(i);
            Perk perk = perkStorage.getPerkByName(perkName);
            if (perk == null) {
                continue;
            }

            StyledButtonCyan perkButton = new StyledButtonCyan(perk.getName());
            perkButton.setPreferredSize(new Dimension(500, 90)); // Gomb pontos mérete 300x90                
            perkButton.setFont(new Font("Arial", Font.BOLD, 17)); // Betűtípus  

            // İkonnak beállítása  
            ImageIcon icon = perkStorage.getPerkIcon(perkName);
            if (icon != null) {
                Image scaledImage = icon.getImage().getScaledInstance(60, 60, Image.SCALE_SMOOTH);
                perkButton.setIcon(new ImageIcon(scaledImage));
                perkButton.setHorizontalTextPosition(SwingConstants.RIGHT); // Ikon jobbra igazítása  
            }

            // Gomb interakció beállítása  
            perkButton.addActionListener(e -> onPerkSelected.actionPerformed(new ActionEvent(perkButton, ActionEvent.ACTION_PERFORMED, perkName)));

            // GridBagConstraints beállítása  
            gbc.gridx = i % 2; // Két oszlop  
            gbc.gridy = i / 2; // Aktuális sor  
            resultsPanel.add(perkButton, gbc);
        }

        resultsPanel.revalidate(); // Frissíti a panelt  
        resultsPanel.repaint(); // Újrarajzolja a panelt  
    }
}
