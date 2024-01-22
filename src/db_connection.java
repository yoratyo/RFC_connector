import java.sql.*;

public class db_connection {
    //static reference to itself
    private static final db_connection instance = new db_connection();
    String url = "jdbc:mysql://localhost:3306/esm_weighbridge";
    String user = "root";
    String password = "";
    String driverClass = "com.mysql.cj.jdbc.Driver";

    //private constructor
    private db_connection() {
        try {
            Class.forName(driverClass);
        } catch (ClassNotFoundException e) {
            System.err.print(e);
        }
    }

    public static db_connection getInstance() {
        return instance;
    }

    public synchronized Connection getConnection() throws SQLException,
            ClassNotFoundException {
        Connection connection = DriverManager.getConnection(url, user, password);
        return connection;
    }
}
