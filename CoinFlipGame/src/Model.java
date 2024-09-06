
import java.sql.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.TreeMap;


public class Model {
    private String url;
    public Model(){
        url = "jdbc:sqlite:database.db";
        try {
            Connection conn = DriverManager.getConnection(url);
            String createTableCmd = "CREATE TABLE IF NOT EXISTS users (" +
                    "user_id INTEGER PRIMARY KEY," +
                    "username TEXT UNIQUE NOT NULL,"+
                    "password TEXT NOT NULL," +
                    "balance INTEGER NOT NULL);";
            conn.createStatement().executeUpdate(createTableCmd);
            conn.close();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public void createUser(String username, String password){
        try {
            Connection conn = DriverManager.getConnection(url);
            String addUserCmd = """
                        INSERT INTO users (username, password, balance) 
                        VALUES(? ,? ,?);
                    """;
            PreparedStatement preparedStatement = conn.prepareStatement(addUserCmd);
            preparedStatement.setString(1, username);
            preparedStatement.setString(2, password);
            preparedStatement.setInt(3, 500);
            preparedStatement.executeUpdate();
            conn.close();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public void setBalance(String username, int newBalance){
        try {
            System.out.println("in setbalance, updating "+username+ " to newbalance: "+newBalance);
            Connection conn = DriverManager.getConnection(url);
            String updateBalance = """
                    UPDATE users
                    SET balance = ?
                    WHERE username = ?;
                    """;
            PreparedStatement preparedStatement = conn.prepareStatement(updateBalance);
            preparedStatement.setInt(1, newBalance);
            preparedStatement.setString(2, username);
            preparedStatement.executeUpdate();
            System.out.println(username + " after setbalance, balance is: $"+getBalance(username));
            conn.close();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public int getBalance(String username){
        try {
            Connection conn = DriverManager.getConnection(url);
            String selectBalance = """
                    SELECT balance FROM users WHERE username = ?;
                    """;
            PreparedStatement preparedStatement = conn.prepareStatement(selectBalance);
            preparedStatement.setString(1, username);
            ResultSet rs = preparedStatement.executeQuery();

            int balance = rs.getInt("balance");
            conn.close();
            return balance;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

    }
    public boolean authenticateUser(String username, String password) {
        try {
            Connection conn = DriverManager.getConnection(url);
            String query = "SELECT * FROM users WHERE username = ? AND password = ?";
            PreparedStatement preparedStatement = conn.prepareStatement(query);
            preparedStatement.setString(1, username);
            preparedStatement.setString(2, password);
            ResultSet rs = preparedStatement.executeQuery();
            boolean exists = rs.next();
            conn.close();
            return exists;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
    public TreeMap<Integer, String> getAllUserBalances(){
        TreeMap<Integer, String> balanceTreeMap = new TreeMap<Integer, String>(Collections.reverseOrder());

        try {
            Connection conn = DriverManager.getConnection(url);
            String balanceQuery = """
                SELECT balance, username FROM users;
                """;
            ResultSet rs = conn.createStatement().executeQuery(balanceQuery);
            System.out.println("rs from getall userbalances:" + rs);

            while(rs.next()){
                Integer balance = rs.getInt("balance");
                String username = rs.getString("username");
                balanceTreeMap.put(balance, username);
                System.out.println("Username: " + username + ", Balance: " + balance);
            }
            conn.close();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        System.out.println("balancetreemap from model: "+balanceTreeMap);
        return balanceTreeMap;
    }

}
