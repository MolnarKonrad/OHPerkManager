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
    private StyledButtonCyan backButton;

    public MemberSelectionPanel(List<User> members, ActionListener onMemberSelected, Runnable onBack) {
        setLayout(new BorderLayout());

        JLabel titleLabel = new JLabel("Select a Member", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 25));
        add(titleLabel, BorderLayout.NORTH);
       
        membersPanel = new JPanelWithBackground();
        GridBagLayout gridBagLayout = new GridBagLayout();
        membersPanel.setLayout(gridBagLayout);
        membersPanel.setBorder(BorderFactory.createEmptyBorder(25, 25, 25, 25));

        JScrollPane scrollPane = new JScrollPane(membersPanel);
        add(scrollPane, BorderLayout.CENTER);
       
        int maxMembers = Math.min(members.size(), 16);

        for (int i = 0; i < maxMembers; i++) {
            User member = members.get(i);
            StyledButtonCyan memberButton = new StyledButtonCyan(member.getUsername());
           
            memberButton.setPreferredSize(new Dimension(300, 90)); // Kívánt méret  
            memberButton.addActionListener(e -> {
                onMemberSelected.actionPerformed(e);
            });
           
            GridBagConstraints gbc = new GridBagConstraints();
            gbc.gridx = i % 2;
            gbc.gridy = i / 2;
            gbc.insets = new Insets(10, 10, 10, 10);
            gbc.anchor = GridBagConstraints.CENTER;
           
            membersPanel.add(memberButton, gbc);
        }

        backButton = new StyledButtonCyan("Back");
        backButton.addActionListener(e -> onBack.run());
        add(backButton, BorderLayout.SOUTH);
    }
}
