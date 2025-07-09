package com.biosteel.teams;

public class TestConnection {
    public static void main(String[] args) {
        try {
            Class.forName("org.postgresql.Driver");
            java.sql.Connection conn = java.sql.DriverManager.getConnection(
                    "jdbc:postgresql://localhost:5432/biosteel",
                    "biosteel",
                    "Mobi.biosteel.1");
            System.out.println("Connected successfully!");
            conn.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}