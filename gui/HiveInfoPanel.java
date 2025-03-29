package gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Image;
import java.awt.Insets;
import java.util.List;
import java.util.Map;
import javax.swing.*;
import models.Perk;
import models.User;
import services.PerkStorage;

public final class HiveInfoPanel extends JPanel {

    private JPanelWithBackground membersPanel;
    private JLabel hiveNameLabel;

    public HiveInfoPanel(MainFrame mainFrame, String hiveName, List members, Map<String, List<Perk>> memberPerks, PerkStorage perkStorage, Runnable onBack) {
        setLayout(new BorderLayout());

        hiveNameLabel = new JLabel("HIVE: " + hiveName, SwingConstants.CENTER);
        hiveNameLabel.setFont(new Font("Times New Roman", Font.BOLD, 24));
        hiveNameLabel.setOpaque(true);
        hiveNameLabel.setBackground(new Color(81, 203, 203, 100));
        add(hiveNameLabel, BorderLayout.NORTH);

        membersPanel = new JPanelWithBackground(new GridBagLayout());
        JScrollPane scrollPane = new JScrollPane(membersPanel);
        add(scrollPane, BorderLayout.CENTER);

        updateData(members, memberPerks, perkStorage);

        StyledButton backButton = new StyledButton("Back");
        backButton.addActionListener(e -> {
            if (onBack != null) {
                onBack.run();
            } else {
                System.err.println("⚠️ onBack Runnable is null!");
            }
        });
        add(backButton, BorderLayout.SOUTH);

        setPreferredSize(new Dimension(1280, 720));
    }

    public void updateHiveName(String hiveName) {
        hiveNameLabel.setText("HIVE: " + hiveName);
    }

    public void updateData(List<User> members, Map<String, List<Perk>> memberPerks, PerkStorage perkStorage) {
        membersPanel.removeAll();

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.BOTH;
        gbc.gridy = 0;

        for (User member : members) {
            gbc.gridx = members.indexOf(member) % 8;
            gbc.gridwidth = 1;
            gbc.weightx = 1;
            gbc.weighty = 0.1;

            JLabel memberLabel = new JLabel(member.getUsername(), SwingConstants.CENTER);
            memberLabel.setFont(new Font("Times New Roman", Font.BOLD, 20));
            memberLabel.setPreferredSize(new Dimension(45, 15));
            membersPanel.add(memberLabel, gbc);

            gbc.gridy++;
            gbc.gridheight = 10;
            gbc.weighty = 1;

            JPanel memberPanel = new JPanel(new GridBagLayout());
            memberPanel.setBackground(new Color(81, 203, 203, 20));
            GridBagConstraints perkGbc = new GridBagConstraints();
            perkGbc.gridx = 0;
            perkGbc.gridy = 0;
            perkGbc.fill = GridBagConstraints.BOTH;
            perkGbc.weightx = 1;
            perkGbc.weighty = 1;

            List<Perk> perks = memberPerks.get(member.getUsername());
            for (int i = 0; i < 10; i++) {
                if (perks != null && i < perks.size()) {
                    Perk perk = perks.get(i);
                    JPanel perkPanel = new JPanel(new GridBagLayout());
                    GridBagConstraints innerGbc = new GridBagConstraints();
                    innerGbc.gridx = 0;
                    innerGbc.gridy = 0;
                    innerGbc.fill = GridBagConstraints.BOTH;
                    innerGbc.weightx = 0.3;
                    innerGbc.weighty = 1;

                    JPanel perkInfoPanel = new JPanel(new GridBagLayout());
                    perkInfoPanel.setBackground(new Color(81, 203, 203, 40));
                    perkInfoPanel.setPreferredSize(new Dimension(140, 140));

                    ImageIcon icon = perkStorage.getPerkIcon(perk.getName());
                    JLabel iconLabel = new JLabel();
                    if (icon != null) {
                        Image image = icon.getImage().getScaledInstance(70, 70, Image.SCALE_SMOOTH);
                        iconLabel.setIcon(new ImageIcon(image));
                    } else {
                        iconLabel.setText("❌");
                    }
                    GridBagConstraints gbcPerk = new GridBagConstraints();
                    gbcPerk.gridx = 0;
                    gbcPerk.gridy = 0;
                    gbcPerk.anchor = GridBagConstraints.CENTER;
                    perkInfoPanel.add(iconLabel, gbcPerk);

                    JLabel nameLabel = new JLabel("<html>" + formatPerkName(perk.getName()) + "</html>", SwingConstants.CENTER);
                    nameLabel.setFont(new Font("Times New Roman", Font.BOLD, 16));
                    gbcPerk.gridy = 1;
                    perkInfoPanel.add(nameLabel, gbcPerk);

                    perkPanel.add(perkInfoPanel, innerGbc);

                    innerGbc.gridx = 1;
                    innerGbc.weightx = 0.7;
                    JTextArea descriptionArea = new JTextArea(perk.getDescription());
                    descriptionArea.setLineWrap(true);
                    descriptionArea.setWrapStyleWord(true);
                    descriptionArea.setEditable(false);
                    descriptionArea.setFont(new Font("Times New Roman", Font.PLAIN, 15));
                    descriptionArea.setPreferredSize(new Dimension(140, 50));
                    perkPanel.add(descriptionArea, innerGbc);

                    perkGbc.gridy = i;
                    memberPanel.add(perkPanel, perkGbc);
                } else {
                    perkGbc.gridy = i;
                    memberPanel.add(new JLabel(""), perkGbc);
                }
            }
            membersPanel.add(memberPanel, gbc);
            gbc.gridy = 0;
        }

        membersPanel.revalidate();
        membersPanel.repaint();
    }

    private String formatPerkName(String perkName) {
        return perkName.replaceAll("(.{10,20})(\\s|$)", "$1<br>").trim();
    }
}
