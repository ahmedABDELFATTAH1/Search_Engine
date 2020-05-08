package ranker;
import com.mysql.cj.protocol.Resultset;

import java.sql.*;

public class simple {
    public  static void main(String ar[]) throws ClassNotFoundException, SQLException {
        System.out.println(("DataBase tutorial"));
        Class.forName("com.mysql.cj.jdbc.Driver");
        Connection con=DriverManager.getConnection("jdbc:mysql://localhost:3306/mydatabase","root","12");
        System.out.println("Database Tutorial");
        Statement st=con.createStatement();
        ResultSet  rs=st.executeQuery("select * from books");
        rs.next();
        System.out.println( rs.getString("author"));

    }

}
