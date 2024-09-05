import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.ArrayList;

public class Controller implements ActionListener {
    private View view;
    private Socket clientSocket;
    private PrintWriter out;
    private BufferedReader in;

    public Controller() {
        this.view = new View(this);
        try {
            clientSocket = new Socket("localhost", 5000);
            out = new PrintWriter(clientSocket.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
        } catch (IOException e) {
            JOptionPane.showMessageDialog(null, "Error connecting to server, please start the server: " + e.getMessage());
        }
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        try {
            if ("LOGIN".equals(e.getActionCommand())) {
                performLogin(view.getUsername(), view.getPassword());
            } else if ("PLACE_BET_COIN".equals(e.getActionCommand())) {
                placeCoinBet(view.getSelectedCoinBet(), view.getBetCF());
            } else if ("PLACE_BET_DICE".equals(e.getActionCommand())) {
                placeDiceBet(view.getSelectedDiceBet(), view.getBetDG());
            } else if ("GET_LEADERBOARD".equals(e.getActionCommand())) {
                requestLeaderboard();
            }
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(null, "Communication error: " + ex.getMessage());
        }
    }

    private void performLogin(String username, String password) throws IOException {
        out.println("LOGIN " + username + " " + password);
        String response = in.readLine();
        System.out.println("response in perform login: " + response);
        if (response.startsWith("LOGIN_SUCCESS")) {
            view.showGameTab(username, extractBalance(response), new ArrayList<>());
        } else {
            view.showLoginError();
        }
    }

    private int extractBalance(String response) {
        System.out.println("extract balance: " + response);
        return Integer.parseInt(response.split(" ")[2]);
    }

    public void placeCoinBet(String selectedBet,String betAmount) throws IOException {
        String username = view.getUsername();
        System.out.println("getbet:"+ betAmount);
        System.out.println("Controller placecoinbet received:"+ username + selectedBet+betAmount);
        out.println("PLACE_BET_COIN " + username +" "+ selectedBet + " " + betAmount);
        String response = in.readLine();
        System.out.println("controller placedicebet response: "+ response );
        String response2 = in.readLine();
        System.out.println("controller placecoinbet response2: "+ response2 );
        if (response.equals("INSUFFICIENT_BALANCE")) {
            JOptionPane.showMessageDialog(view.getFrame(), "Insufficient balance for this bet", "Bet Error", JOptionPane.WARNING_MESSAGE);
        }
        if (response2.startsWith("BET_PLACED")) {
            String[] parts = response.split(" ");
            String[] parts2 = response2.split(" ");
            view.updateGameResult(parts2[1], Integer.parseInt(parts2[2]));
            view.updateUserInfo(username, Integer.parseInt(parts[2]));
        }
        else {
            System.out.println("Error in playcoinbet");
        }
    }

    public void placeDiceBet(String selectedBet, String betAmount) throws IOException {
        String username = view.getUsername();
        System.out.println("Controller placeDiceBet received: " + username + " " + selectedBet + " " + betAmount);
        out.println("PLACE_BET_DICE " +username +" "+ selectedBet + " " + betAmount);
        String response = in.readLine();
        System.out.println("controller placedicebet response: "+ response );
        String response2 = in.readLine();
        if (response.equals("INSUFFICIENT_BALANCE")) {
            JOptionPane.showMessageDialog(view.getFrame(), "Insufficient balance for this bet", "Bet Error", JOptionPane.WARNING_MESSAGE);
        }
        System.out.println("controller placedicebet response2: "+ response2 );
        if (response2.startsWith("BET_PLACED")) {
            String[] parts = response.split(" ");
            String[] parts2 = response2.split(" ");
            view.updateGameResult(parts2[1], Integer.parseInt(parts2[2]));
            view.updateUserInfo(username, Integer.parseInt(parts[2]));
        } else {
            System.out.println("Error in playdicebet");
        }
    }

    public void requestLeaderboard() throws IOException {
        out.println("GET_LEADERBOARD");
        ArrayList<String> leaderboardData = new ArrayList<>();
        String entry;
        while (!(entry = in.readLine()).equals("END")) {
            leaderboardData.add(entry);
        }
        view.updateLeaderboardTab(leaderboardData);
    }
}
