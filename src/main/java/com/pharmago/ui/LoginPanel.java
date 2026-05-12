package com.pharmago.ui;

import com.pharmago.dao.UserDao;
import com.pharmago.model.User;
import com.pharmago.util.Session;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagLayout;
import java.sql.SQLException;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.UIManager;
import javax.swing.border.EmptyBorder;

public class LoginPanel {
    private static final Color APP_BG = new Color(242, 246, 252);
    private static final Color PANEL_BG = new Color(255, 255, 255);
    private static final Color PRIMARY = new Color(14, 116, 144);
    private static final Color ACCENT = new Color(19, 78, 94);
    private static final Color DANGER = new Color(185, 28, 28);

    private final UserDao userDao;
    private final Runnable onSuccess;

    private JFrame frame;
    private JTextField usernameField;
    private JPasswordField passwordField;
    private JLabel errorLabel;

    public LoginPanel(UserDao userDao, Runnable onSuccess) {
        this.userDao = userDao;
        this.onSuccess = onSuccess;
        setLookAndFeel();
        buildUI();
    }

    public void show() {
        frame.setVisible(true);
        usernameField.requestFocusInWindow();
    }

    private void buildUI() {
        frame = new JFrame("PharmaGo — Login");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(420, 460);
        frame.setLocationRelativeTo(null);
        frame.setResizable(false);
        frame.setLayout(new BorderLayout());
        frame.getContentPane().setBackground(APP_BG);

        frame.add(buildHeader(), BorderLayout.NORTH);
        frame.add(buildForm(), BorderLayout.CENTER);
    }

    private JComponent buildHeader() {
        JPanel header = new JPanel();
        header.setBackground(ACCENT);
        header.setBorder(new EmptyBorder(28, 24, 28, 24));
        header.setLayout(new BoxLayout(header, BoxLayout.Y_AXIS));

        JLabel title = new JLabel("PharmaGo");
        title.setForeground(Color.WHITE);
        title.setFont(new Font("Segoe UI", Font.BOLD, 28));
        title.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel subtitle = new JLabel("Sign in to continue");
        subtitle.setForeground(new Color(214, 234, 248));
        subtitle.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        subtitle.setAlignmentX(Component.CENTER_ALIGNMENT);

        header.add(title);
        header.add(Box.createVerticalStrut(6));
        header.add(subtitle);
        return header;
    }

    private JComponent buildForm() {
        JPanel wrapper = new JPanel(new GridBagLayout());
        wrapper.setBackground(APP_BG);

        JPanel card = new JPanel();
        card.setBackground(PANEL_BG);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(220, 228, 238)),
                new EmptyBorder(28, 28, 28, 28)
        ));
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setPreferredSize(new Dimension(340, 260));

        usernameField = new JTextField();
        passwordField = new JPasswordField();

        card.add(createFieldBlock("Username", usernameField));
        card.add(Box.createVerticalStrut(16));
        card.add(createFieldBlock("Password", passwordField));
        card.add(Box.createVerticalStrut(20));

        errorLabel = new JLabel(" ");
        errorLabel.setForeground(DANGER);
        errorLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        errorLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        card.add(errorLabel);
        card.add(Box.createVerticalStrut(8));

        JButton loginButton = new JButton("Sign In");
        loginButton.setBackground(PRIMARY);
        loginButton.setForeground(Color.BLACK);
        loginButton.setFont(new Font("Segoe UI", Font.BOLD, 14));
        loginButton.setFocusPainted(false);
        loginButton.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));
        loginButton.setAlignmentX(Component.LEFT_ALIGNMENT);
        loginButton.addActionListener(event -> handleLogin());

        card.add(loginButton);

        usernameField.addActionListener(event -> passwordField.requestFocusInWindow());
        passwordField.addActionListener(event -> handleLogin());

        wrapper.add(card);
        return wrapper;
    }

    private JPanel createFieldBlock(String labelText, JComponent input) {
        JPanel block = new JPanel();
        block.setOpaque(false);
        block.setLayout(new BoxLayout(block, BoxLayout.Y_AXIS));
        block.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel label = new JLabel(labelText);
        label.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        label.setForeground(new Color(51, 65, 85));
        label.setAlignmentX(Component.LEFT_ALIGNMENT);

        input.setMaximumSize(new Dimension(Integer.MAX_VALUE, 30));
        input.setAlignmentX(Component.LEFT_ALIGNMENT);
        input.setFont(new Font("Segoe UI", Font.PLAIN, 13));

        block.add(label);
        block.add(Box.createVerticalStrut(4));
        block.add(input);
        return block;
    }

    private void handleLogin() {
        String username = usernameField.getText().trim();
        String password = new String(passwordField.getPassword()).trim();

        if (username.isEmpty() || password.isEmpty()) {
            showError("Username and password are required.");
            return;
        }

        try {
            User user = userDao.login(username, password);
            if (user == null) {
                showError("Invalid username or password.");
                passwordField.setText("");
                passwordField.requestFocusInWindow();
                return;
            }
            Session.setCurrentUser(user);
            frame.dispose();
            onSuccess.run();
        } catch (SQLException exception) {
            showError("Database error: " + exception.getMessage());
        }
    }

    private void showError(String message) {
        errorLabel.setText(message);
    }

    private void setLookAndFeel() {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) {
        }
    }
}
