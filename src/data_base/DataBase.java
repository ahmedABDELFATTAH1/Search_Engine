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
    static final String PASS = "password";


    public static final String documentTableName="document";
    public static final String documentWordTableName="word_document";
    public static final String imageTableName="image";
    public static final String indexTableName="word_index";
    public static final String trendsTableName="trends";
    public static final String suggestionTableName="suggestion";




    static final String documentTableCreate = "CREATE TABLE IF NOT EXISTS "+documentTableName+
            "(id int auto_increment, " +
            "hyper_link VARCHAR(255) not NULL, " +
            "CountryCode VARCHAR(255), " +
            "data_modified  DATE ,"+
            "stream_words TEXT ,"+
            "popularity FLOAT ,"+
            "Title VARCHAR(255),"+
            "PRIMARY KEY (id));";

    static final String documentWordTableCreate = "CREATE TABLE IF NOT EXISTS  "+documentWordTableName+
            "(id int auto_increment," +
            "word_name VARCHAR(255) not NULL, " +
            "document_hyper_link_id  int not null, "+
            "tf float ,"+
            "score float ,"+
            "FOREIGN KEY (document_hyper_link_id) REFERENCES document(id),"+
            "primary key (id),"+
            "unique(word_name,document_hyper_link_id));";



    static final String imageTableCreate = "CREATE TABLE IF NOT EXISTS "+imageTableName+
            "(id int auto_increment, " +
            "image_url varchar(512),"+
            "caption Text,"+
            "stemmed Text,"+
            "PRIMARY KEY (id));";




    static final String indexTableCreate = "CREATE TABLE IF NOT EXISTS  "+indexTableName+
            "(id int auto_increment, " +
            "word_document_id int not null, "+
            "word_position INT NOT NULL,"+
            "FOREIGN KEY (word_document_id) REFERENCES word_document(id),"+
            "PRIMARY KEY (id));";




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
//        stmt.executeUpdate(imageWordTableCreate);
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
//        Statement stmt= connection.createStatement();
//        int rs = stmt.executeUpdate(sqlStatement, Statement.RETURN_GENERATED_KEYS);
//        //stmt.close();
//        return rs;

        PreparedStatement ps = connection.prepareStatement(sqlStatement, Statement.RETURN_GENERATED_KEYS);
        ps.execute();

        ResultSet rs = ps.getGeneratedKeys();
        int generatedKey = 0;
        if (rs.next()) {
            generatedKey = rs.getInt(1);
        }
        return generatedKey;

    }
    public int deletedb(String sqlStatement) throws SQLException {
        Statement stmt= connection.createStatement();
        int rs = stmt.executeUpdate(sqlStatement);
        return rs;
    }
    public int updatedb(String sqlStatement) throws SQLException {
        Statement stmt= connection.createStatement();
        int rs = stmt.executeUpdate(sqlStatement);
        return rs;
    }

    public void CreateDataBase(){
        DataBase db = new DataBase();
        try {
            db.createConnection();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
        try {
            db.createTables();
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
    }
    
}
