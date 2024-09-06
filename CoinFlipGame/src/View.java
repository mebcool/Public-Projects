import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.util.ArrayList;

public class View {
    private JTextField inputUsernameField, inputPasswordField,betAmountFieldCF,betAmountFieldDG;
    private JTextArea leaderboardTextArea;
    private JFrame frame;
    private JTabbedPane tabPanel;
    private JLabel userInfoLabel;
    private ActionListener actionListener;
    private Controller controller;
    private ButtonGroup diceBetGroup;
    private ButtonGroup coinBetGroup;

    public View(Controller controller) {
        this.controller = controller;
        this.actionListener = controller;
        frame = new JFrame("Game Client");
        frame.setLayout(new BorderLayout());
        leaderboardTextArea = new JTextArea(10, 30);
        leaderboardTextArea.setEditable(false);

        userInfoLabel = new JLabel("User: ");
        frame.add(userInfoLabel, BorderLayout.NORTH);

        tabPanel = new JTabbedPane();
        createLoginTab();

        frame.add(tabPanel, BorderLayout.CENTER);
        frame.setSize(900, 600);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setVisible(true);
    }

    private void createLoginTab() {
        JPanel loginPanel = new JPanel();
        inputUsernameField = new JTextField(15);
        inputPasswordField = new JTextField(15);
        JButton loginButton = new JButton("LOGIN");
        loginButton.setActionCommand("LOGIN");
        loginButton.addActionListener(actionListener);

        loginPanel.add(new JLabel("Username:"));
        loginPanel.add(inputUsernameField);
        loginPanel.add(new JLabel("Password:"));
        loginPanel.add(inputPasswordField);
        loginPanel.add(loginButton);

        tabPanel.addTab("Login", loginPanel);
    }

    public void showGameTab(String username, int balance, ArrayList<String> leaderboardData) {
        tabPanel.removeAll();

        updateUserInfo(username, balance);

        JPanel coinFlipPanel = createCoinFlipPanel();
        JPanel diceGamePanel = createDiceGamePanel();
        JPanel leaderboardPanel = createLeaderboardPanel();

        tabPanel.addTab("Coin Flip", coinFlipPanel);
        tabPanel.addTab("Dice", diceGamePanel);
        tabPanel.addTab("Leaderboard", leaderboardPanel);

        createLogoutTab();
    }

    private JPanel createCoinFlipPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        JToggleButton headsButton = new JToggleButton(new ImageIcon(getClass().getResource("/heads.jpg")));
        headsButton.setActionCommand("Heads");
        headsButton.setBackground(Color.BLACK);
        headsButton.setForeground(Color.WHITE);

        JToggleButton tailsButton = new JToggleButton(new ImageIcon(getClass().getResource("/tails.jpg")));
        tailsButton.setActionCommand("Tails");
        tailsButton.setBackground(Color.BLACK);
        tailsButton.setForeground(Color.WHITE);

        coinBetGroup = new ButtonGroup();
        coinBetGroup.add(headsButton);
        coinBetGroup.add(tailsButton);

        JPanel optionsPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        optionsPanel.add(headsButton, gbc);
        optionsPanel.add(tailsButton, gbc);

        betAmountFieldCF = new JTextField(5);
        JButton placeBetButton = new JButton("Flip Coin");
        placeBetButton.setBackground(Color.BLACK);
        placeBetButton.setForeground(Color.WHITE);

        JPanel betPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        betPanel.add(new JLabel("Bet Amount:"));
        betPanel.add(betAmountFieldCF);
        betPanel.add(placeBetButton);
        placeBetButton.setActionCommand("PLACE_BET_COIN");
        placeBetButton.addActionListener(e -> {
            String betAmountCF = getBetCF();
            if (betAmountCF.trim().isEmpty()) {
                JOptionPane.showMessageDialog(frame, "You must place a bet", "No Bet Placed", JOptionPane.ERROR_MESSAGE);
            } else {
                try {
                    controller.placeCoinBet(headsButton.isSelected() ? "Heads" : "Tails", betAmountCF);
                } catch (IOException ex) {
                    JOptionPane.showMessageDialog(frame, "Error communicating with the server: " + ex.getMessage(), "Connection Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        panel.add(optionsPanel, BorderLayout.CENTER);
        panel.add(betPanel, BorderLayout.SOUTH);

        return panel;
    }

    private JPanel createDiceGamePanel() {
        JPanel panel = new JPanel(new BorderLayout());
        JPanel optionsPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);

        diceBetGroup = new ButtonGroup();
        JToggleButton[] diceButtons = new JToggleButton[6];
        for (int i = 1; i <= 6; i++) {
            ImageIcon icon = new ImageIcon(getClass().getResource("/" + i + ".jpg"));
            diceButtons[i - 1] = new JToggleButton(icon);
            diceButtons[i - 1].setActionCommand(String.valueOf(i));
            diceButtons[i - 1].setBackground(Color.BLACK);
            diceButtons[i - 1].setForeground(Color.WHITE);
            diceBetGroup.add(diceButtons[i - 1]);
            optionsPanel.add(diceButtons[i - 1], gbc);
        }
        diceButtons[0].setSelected(true);

        JPanel betPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        betAmountFieldDG = new JTextField(5);
        JButton placeBetButton = new JButton("Roll Dice");
        placeBetButton.setBackground(Color.BLACK);
        placeBetButton.setForeground(Color.WHITE);
        placeBetButton.setActionCommand("PLACE_BET_DICE");
        placeBetButton.addActionListener(e -> {
            String betAmount = betAmountFieldDG.getText();
            if (betAmount.trim().isEmpty()) {
                JOptionPane.showMessageDialog(frame, "You must place a bet", "No Bet Placed", JOptionPane.ERROR_MESSAGE);
            } else {
                String selectedDiceNumber = getSelectedDiceBet();
                try {
                    controller.placeDiceBet(selectedDiceNumber, betAmount);
                } catch (IOException ex) {
                    JOptionPane.showMessageDialog(frame, "Error in placing dice bet: " + ex.getMessage());
                }
            }
        });

        betPanel.add(new JLabel("Bet Amount:"));
        betPanel.add(betAmountFieldDG);
        betPanel.add(placeBetButton);

        panel.add(optionsPanel, BorderLayout.CENTER);
        panel.add(betPanel, BorderLayout.SOUTH);

        return panel;
    }

    private JPanel createLeaderboardPanel() {
        JPanel leaderboardPanel = new JPanel(new BorderLayout());
        leaderboardTextArea.setFont(new Font("SansSerif", Font.PLAIN, 22)); // You can choose any font
        JButton refreshButton = new JButton("Refresh Leaderboard");
        refreshButton.addActionListener(e -> {
            try {
                controller.requestLeaderboard();
            } catch (IOException ex) {
                JOptionPane.showMessageDialog(frame, "Error refreshing leaderboard: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        });
        leaderboardPanel.add(new JScrollPane(leaderboardTextArea), BorderLayout.CENTER);
        leaderboardPanel.add(refreshButton, BorderLayout.SOUTH);

        return leaderboardPanel;
    }

    public void updateGameResult(String result, int winnings) {
        if(result.equals("Loss")){
            JOptionPane.showMessageDialog(frame, "You Lose!!! | you lose your bet: $" + winnings);
        }
        else if(result.equals("Win")){
            JOptionPane.showMessageDialog(frame, "You Win!!! | payout: $" + winnings);
        }
        else{
            JOptionPane.showMessageDialog(frame,"No loss or win");
        }
    }

    public void updateUserInfo(String username, int balance) {
        userInfoLabel.setText("User: " + username + " | Balance: $" + balance);
    }

    private void createLogoutTab() {
        JPanel logoutPanel = new JPanel(new GridBagLayout());
        JButton logoutButton = new JButton("LOGOUT");
        logoutButton.addActionListener(e -> frame.dispose());

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 1;
        gbc.weighty = 1;
        gbc.fill = GridBagConstraints.NONE;

        logoutPanel.add(logoutButton, gbc);
        tabPanel.addTab("Logout", logoutPanel);
    }

    public void showLoginError() {
        JOptionPane.showMessageDialog(frame, "Invalid username or password!");
    }

    public JFrame getFrame() {
        return frame;
    }

    public String getBetCF() {
        return betAmountFieldCF.getText();
    }

    public String getBetDG() {
        return betAmountFieldDG.getText();
    }
    public String getUsername() {
        return inputUsernameField.getText();
    }

    public String getPassword() {
        return inputPasswordField.getText();
    }

    public String getSelectedCoinBet() {
        ButtonModel selectedModel = coinBetGroup.getSelection();
        if (selectedModel != null) {
            return selectedModel.getActionCommand();
        }
        return null;
    }
    public String getSelectedDiceBet() {
        ButtonModel selectedModel = diceBetGroup.getSelection();
        if (selectedModel != null) {
            return selectedModel.getActionCommand();
        }
        return null;
    }

    public void updateLeaderboardTab(ArrayList<String> leaderboardData) {
        leaderboardTextArea.setText("");
        for (String entry : leaderboardData) {
            leaderboardTextArea.append(entry + "\n");
        }
    }
}
