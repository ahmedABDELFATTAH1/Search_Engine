package data_base;

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


    public static final String documentTableName="document";
    public static final String documentWordTableName="word_document";
    public static final String imageTableName="image";
    public static final String imageWordTableName="word_image";
    public static final String indexTableName="word_index";
    public static final String trendsTableName="trends";
    public static final String suggestionTableName="suggestion";




    static final String documentTableCreate = "CREATE TABLE IF NOT EXISTS "+documentTableName+
            "(hyper_link VARCHAR(255) not NULL, " +
            "data_modified  DATE ,"+
            "doc_path_file VARCHAR(255) not NULL, "+
            "stream_words TEXT ,"+
            "popularity FLOAT ,"+
            "Title VARCHAR(255),"+
            "PRIMARY KEY (hyper_link));";

    static final String documentWordTableCreate = "CREATE TABLE IF NOT EXISTS  "+documentWordTableName+
            "(word_name VARCHAR(255) not NULL, " +
            " document_hyper_link  VARCHAR(255) , "+
            "tf float ,"+
            "score float ,"+
             " FOREIGN KEY (document_hyper_link) REFERENCES document(hyper_link),"+
            "PRIMARY KEY (word_name,document_hyper_link));";



    static final String imageTableCreate = "CREATE TABLE IF NOT EXISTS "+imageTableName+
            "(image_url VARCHAR(255) not NULL, " +
            "caption Text,"+
            "PRIMARY KEY (image_url));";

    static final String imageWordTableCreate = "CREATE TABLE IF NOT EXISTS  "+imageWordTableName+
            "(word_name VARCHAR(255) not NULL, " +
            " image_url  VARCHAR(255) , "+
            "tf float ,"+
            "score float ,"+
            "FOREIGN KEY (image_url) REFERENCES image(image_url),"+
            "PRIMARY KEY (word_name,image_url));";


    static final String indexTableCreate = "CREATE TABLE IF NOT EXISTS  "+indexTableName+
            " (word_name VARCHAR(255) not NULL, " +
            " document_hyper_link VARCHAR(255) NOT NULL , "+
            "word_position INT NOT NULL,"+
            " FOREIGN KEY (document_hyper_link) REFERENCES document(hyper_link),"+
            "PRIMARY KEY (word_name,document_hyper_link,word_position));";



    static final String trendsTableCreate = "CREATE TABLE IF NOT EXISTS  "+trendsTableName+
            " (region VARCHAR(255) NOT NULL , "+
            "person_name VARCHAR(255) NOT NULL,"+
            "search_count INT NOT NULL DEFAULT 0,"+
            "PRIMARY KEY (region,person_name));";


    static final String suggestionTableCreate = "CREATE TABLE IF NOT EXISTS  "+suggestionTableName+
            "(query_id INT NOT NULL AUTO_INCREMENT, "+
            " search_query VARCHAR(255) NOT NULL ,"+
            "PRIMARY KEY (query_id));";



    public void createTables() throws SQLException {
        Statement stmt= connection.createStatement();
        stmt.executeUpdate(documentTableCreate);
        stmt.executeUpdate(documentWordTableCreate);
        stmt.executeUpdate(imageTableCreate);
        stmt.executeUpdate(imageWordTableCreate);
        stmt.executeUpdate(indexTableCreate);
        stmt.executeUpdate(trendsTableCreate);
        stmt.executeUpdate(suggestionTableCreate);
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
