package util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class conexaoDb {
    public static Connection getConexao() {
        try {
            return DriverManager.getConnection("jdbc:mysql://localhost:3306/petShop", "root", " ");
        } catch (SQLException e) {
            System.out.println("Erro na conexão: " + e.getMessage());
            return null;
        }
    }
}