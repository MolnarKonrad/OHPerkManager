package gui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionListener;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingConstants;
import models.User;

public class MemberSelectionPanel extends JPanelWithBackground {

    private JPanel membersPanel;
    private StyledButton backButton;

    public MemberSelectionPanel(List<User> members, ActionListener onMemberSelected, Runnable onBack) {
        setLayout(new BorderLayout());

        JLabel titleLabel = new JLabel("Select a Member", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Times New Roman", Font.BOLD, 25));
        add(titleLabel, BorderLayout.NORTH);

        // Külön panel a tagok számára, margóval   
        membersPanel = new JPanelWithBackground();
        GridBagLayout gridBagLayout = new GridBagLayout();
        membersPanel.setLayout(gridBagLayout);
        membersPanel.setBorder(BorderFactory.createEmptyBorder(25, 25, 25, 25)); // Margó a panel körül  

        JScrollPane scrollPane = new JScrollPane(membersPanel);
        add(scrollPane, BorderLayout.CENTER);

        // Maximális 16 tag (2 oszlop, 8 sor max)  
        int maxMembers = Math.min(members.size(), 16);

        for (int i = 0; i < maxMembers; i++) {
            User member = members.get(i);
            StyledButton memberButton = new StyledButton(member.getUsername());

            // Gomb méretek beállítása (most már használjuk a GridBagConstraints-et)  
            memberButton.setPreferredSize(new Dimension(300, 90)); // Kívánt méret  
            memberButton.addActionListener(e -> {
                onMemberSelected.actionPerformed(e);
            });

            // GridBagConstraints beállítása  
            GridBagConstraints gbc = new GridBagConstraints();
            gbc.gridx = i % 2; // Elrendezés: 2 oszlop  
            gbc.gridy = i / 2; // Sor  
            gbc.insets = new Insets(10, 10, 10, 10); // Margó a gombok körül  
            gbc.anchor = GridBagConstraints.CENTER; // Középre igazítás  

            // Gombok hozzáadása a panelhez  
            membersPanel.add(memberButton, gbc);
        }

        backButton = new StyledButton("Back");
        backButton.addActionListener(e -> onBack.run());
        add(backButton, BorderLayout.SOUTH);
    }
}
