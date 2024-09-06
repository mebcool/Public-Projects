import javax.swing.*;
import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class Server {
    private static final int PORT = 5000;
    private static ServerSocket serverSocket;
    private static ConcurrentHashMap<Integer, ClientHandler> clients = new ConcurrentHashMap<>();
    private static Model model = new Model();

    public static void main(String[] args) {
        try {
            serverSocket = new ServerSocket(PORT);
            System.out.println("Server is listening on port " + PORT);

            while (true) {
                Socket socket = serverSocket.accept();
                ClientHandler clientHandler = new ClientHandler(socket);
                clients.put(socket.getPort(), clientHandler);
                new Thread(clientHandler).start();
            }
        } catch (IOException e) {
            System.out.println("Server exception: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static class ClientHandler implements Runnable {
        private Socket socket;
        private PrintWriter out;
        private BufferedReader in;

        public ClientHandler(Socket socket) {
            this.socket = socket;
            try {
                out = new PrintWriter(socket.getOutputStream(), true);
                in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            } catch (IOException e) {
                System.out.println("Error setting up client handler: " + e.getMessage());
            }
        }

        @Override
        public void run() {
            try {
                String inputLine;
                while ((inputLine = in.readLine()) != null) {
                    processCommand(inputLine);
                }
            } catch (IOException e) {
                System.out.println("Client disconnected.");
            } finally {
                closeConnection();
            }
        }

        private void processCommand(String line) {
            String[] tokens = line.split(" ");
            switch (tokens[0]) {
                case "LOGIN":
                    performLogin(tokens[1], tokens[2]);
                    break;
                case "PLACE_BET_COIN":
                    placeCoinBet(tokens);
                    break;
                case "PLACE_BET_DICE":
                    placeDiceBet(tokens);
                    break;
                case "GET_LEADERBOARD":
                    System.out.println("<server leaderboard called");
                    sendLeaderboard();
                    break;
                case "LOGOUT":
                    closeConnection();
                    break;
                default:
                    out.println("Invalid command");
                    break;
            }
        }

        private void performLogin(String username, String password) {
            if (model.authenticateUser(username, password)) {
                int balance = model.getBalance(username);
                out.println("LOGIN_SUCCESS " + username + " " + balance);
            } else{
                model.createUser(username,password);
                int balance = model.getBalance(username);
                out.println("LOGIN_SUCCESS " + username + " " + balance);
            }

        }

        private void placeCoinBet(String[] tokens) {
            System.out.println("server placecoinbet tokens: "+ Arrays.toString(tokens));
            String username = tokens[1];
            String selectedBet = tokens[2];
            String betAmountText = tokens[3];

            try {
                int betAmount = Integer.parseInt(betAmountText);
                int currentBalance = model.getBalance(username);

                if (betAmount > currentBalance) {
                    System.out.println("insufficient balance error in coinbet");
                    out.println("INSUFFICIENT_BALANCE");
                    out.println("INSUFFICIENT_BALANCE");
                    return;
                }

                FlipCoinGame game = new FlipCoinGame();
                String gameOutcome = game.playGame();
                String result = game.result(gameOutcome, selectedBet);
                int payout = game.payout(result, betAmount);
                updateBalanceAfterBet(username, result, payout, betAmount);
                out.println("BET_PLACED " + result + " " + payout);
            } catch (NumberFormatException e) {
                out.println("ERROR " + e.getMessage());
            }
        }


        private void placeDiceBet(String[] tokens) {
            System.out.println("Server placeDiceBet tokens: " + Arrays.toString(tokens));
            String username = tokens[1];
            String selectedBet = tokens[2];
            String betAmountText = tokens[3];

            try {
                int betAmount = Integer.parseInt(betAmountText);
                int currentBalance = model.getBalance(username);

                if (betAmount > currentBalance) {
                    System.out.println("insufficient balance error in coinbet");
                    out.println("INSUFFICIENT_BALANCE");
                    return;
                }
                DiceGame game = new DiceGame();
                String gameOutcome = game.playGame();
                String result = game.result(gameOutcome, selectedBet);
                int payout = game.payout(result, betAmount);
                updateBalanceAfterBet(username, result, payout, betAmount);
                out.println("BET_PLACED " + result + " " + payout);
            } catch (NumberFormatException e) {
                out.println("ERROR " + e.getMessage());
            }
        }

        private void updateBalanceAfterBet(String username, String outcome, int winnings, int betAmount) {
            int currentBalance = model.getBalance(username);
            if (outcome.equals("Win")) {
                currentBalance += winnings-betAmount;
            } else {
                currentBalance -= betAmount;
            }
            if (currentBalance <= 0) {
                currentBalance = 500;
            }
            model.setBalance(username, currentBalance);
            out.println("BET_RESULT " + outcome + " " + currentBalance);
        }

        private void sendLeaderboard() {
            Map<Integer, String> allBalances = model.getAllUserBalances();
            System.out.println("Server: the getAllUserBalances, allBalances: " + allBalances);
            TreeMap<Integer, String> reverseOrderedBalances = new TreeMap<>(Collections.reverseOrder());
            reverseOrderedBalances.putAll(allBalances);

            ArrayList<String> formattedLeaderboard = formatLeaderboard(reverseOrderedBalances);
            System.out.println("Formatted Leaderboard: " + formattedLeaderboard);

            for (String entry : formattedLeaderboard) {
                out.println(entry);
            }
            out.println("END");
        }

        private ArrayList<String> formatLeaderboard(TreeMap<Integer, String> leaderboardData) {
            ArrayList<String> formattedEntries = new ArrayList<>();
            int place = 1;
            for (Map.Entry<Integer, String> entry : leaderboardData.entrySet()) {
                String formattedEntry = place + ".) " + entry.getValue() + " : $" + entry.getKey();
                formattedEntries.add(formattedEntry);
                place++;
            }
            return formattedEntries;
        }

        private void closeConnection() {
            try {
                if (in != null) in.close();
                if (out != null) out.close();
                if (socket != null) socket.close();
            } catch (IOException e) {
                System.out.println("Error while closing the connection: " + e.getMessage());
            } finally {
                clients.remove(socket.getPort());
            }
        }
    }
}
