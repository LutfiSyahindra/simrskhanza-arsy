/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package customArsy;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

/**
 *
 * @author USER
 */
public class DialogInputNoHP extends JDialog {
    private JTextField inputField;
    private boolean confirmed = false;

    public DialogInputNoHP(Frame parent, String defaultNumber) {
        super(parent, "Masukkan Nomor Telepon", true);
        setLayout(new BorderLayout(10, 10));
        getContentPane().setBackground(Color.WHITE);

        // Label dan Input
        JLabel label = new JLabel("Nomor Telepon:");
        label.setFont(new Font("Segoe UI", Font.PLAIN, 14));

        inputField = new JTextField(defaultNumber, 20);
        inputField.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        inputField.setPreferredSize(new Dimension(200, 30));
        inputField.setBorder(BorderFactory.createLineBorder(Color.GRAY));
        
        // Validasi input hanya angka
        inputField.addKeyListener(new KeyAdapter() {
            public void keyTyped(KeyEvent e) {
                char c = e.getKeyChar();
                if (!Character.isDigit(c) && c != KeyEvent.VK_BACK_SPACE && c != '+') {
                    e.consume();
                }
            }
        });

        JPanel inputPanel = new JPanel(new GridBagLayout());
        inputPanel.setBackground(Color.WHITE);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.gridx = 0;
        gbc.gridy = 0;
        inputPanel.add(label, gbc);

        gbc.gridx = 1;
        inputPanel.add(inputField, gbc);

        // Tombol OK dan Batal
        JButton btnOK = new JButton("✔ Kirim");
        btnOK.setBackground(new Color(0, 153, 76));
        btnOK.setForeground(Color.WHITE);
        btnOK.setFocusPainted(false);

        JButton btnCancel = new JButton("✖ Batal");
        btnCancel.setBackground(new Color(204, 0, 0));
        btnCancel.setForeground(Color.WHITE);
        btnCancel.setFocusPainted(false);

        JPanel buttonPanel = new JPanel();
        buttonPanel.setBackground(Color.WHITE);
        buttonPanel.add(btnOK);
        buttonPanel.add(btnCancel);

        // Event tombol
        btnOK.addActionListener(e -> {
            if (inputField.getText().trim().isEmpty()) {
                JOptionPane.showMessageDialog(this, "Nomor telepon tidak boleh kosong.", "Validasi", JOptionPane.WARNING_MESSAGE);
                return;
            }
            confirmed = true;
            dispose();
        });

        btnCancel.addActionListener(e -> dispose());

        // Tambahkan ke layout
        add(inputPanel, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);

        pack();
        setResizable(false);
        setLocationRelativeTo(parent);
    }

    public boolean isConfirmed() {
        return confirmed;
    }

    public String getInput() {
        return inputField.getText().trim();
    }
}
