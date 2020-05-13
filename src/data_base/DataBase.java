package data_base;
import ranker.Ranker;

import java.sql.*;

public class DataBase {
    static Connection connection=null;
    // JDBC driver name and database URL
    static final String JDBC_DRIVER = "com.mysql.cj.jdbc.Driver";
    static final String DATA_BASE_NAME ="search_engine";
    static final String DB_URL = "jdbc:mysql://localhost:3306/"+DATA_BASE_NAME+"?createDatabaseIfNotExist=true";
    //  Database credentials
    static final String USER = "root";
    static final String PASS = "12";

    public static final String wordTableName="word";
    public static final String documentTableName="document";
    public static final String documentWordTableName="word_document";
    public static final String userTableName="user";


    static final String wordTableCreate = "CREATE TABLE IF NOT EXISTS  "+wordTableName+
            "(name VARCHAR(255) not NULL, " +
            " idf FLOAT, "+
            "PRIMARY KEY (name));";

    static final String documentTableCreate = "CREATE TABLE IF NOT EXISTS "+documentTableName+
            "(hyper_link VARCHAR(255) not NULL, " +
            " data_modified  DATE ,"+
            "doc_path_file VARCHAR(255) not NULL, "+
            "stream_words TEXT ,"+
            "popularity FLOAT ,"+
            "Title VARCHAR(255),"+
            "is_image BIT,"+
            "PRIMARY KEY (hyper_link));";

    static final String documentWordTableCreate = "CREATE TABLE IF NOT EXISTS  "+documentWordTableName+
            "(word_name VARCHAR(255) not NULL, " +
            " document_hyper_link  VARCHAR(255) , "+
            "tf float ,"+
            "score float ,"+
            " FOREIGN KEY (word_name) REFERENCES word(name),"+
             " FOREIGN KEY (document_hyper_link) REFERENCES document(hyper_link),"+
            "PRIMARY KEY (word_name,document_hyper_link));";

    static final String userTableCreate = "CREATE TABLE IF NOT EXISTS  "+userTableName+
            "(user_name VARCHAR(255) not NULL, " +
            "password VARCHAR(255) not NULL,"+
            "PRIMARY KEY (user_name));";



    public  static void main(String ar[]) throws SQLException, ClassNotFoundException {
        DataBase db=new DataBase();
        db.createConnection();
        db.createTables();
    }


   public void createTables() throws SQLException {
        Statement stmt= connection.createStatement();
        stmt.executeUpdate(wordTableCreate);
        stmt.executeUpdate(documentTableCreate);
        stmt.executeUpdate(documentWordTableCreate);
        stmt.executeUpdate(userTableCreate);
        stmt.close();
    }

    public void createConnection() throws ClassNotFoundException, SQLException {
        Class.forName(JDBC_DRIVER);
        Connection con=DriverManager.getConnection(DB_URL,USER,PASS);
        connection=con;
    }
    public ResultSet selectQuerydb(String sqlStatement) throws SQLException {
        Statement stmt= connection.createStatement();
        ResultSet rs = stmt.executeQuery(sqlStatement);
        //stmt.close();
        return rs;
    }
    public int insertdb(String sqlStatement) throws SQLException {
        Statement stmt= connection.createStatement();
        int rs = stmt.executeUpdate(sqlStatement);
        //stmt.close();
        return rs;
    }
    int deletedb(String sqlStatement) throws SQLException {
        Statement stmt= connection.createStatement();
        int rs = stmt.executeUpdate(sqlStatement);
        return rs;
    }
    public int updatedb(String sqlStatement) throws SQLException {
        Statement stmt= connection.createStatement();
        int rs = stmt.executeUpdate(sqlStatement);
        return rs;
    }
    
}
