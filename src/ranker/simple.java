package ranker;

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
/*
TODO: make ranker logic
Ranker Logic :
get an array list of search phrases
for each word  get all documents that a word appeard in them
for each word with a document get the tf value
multiply tf by idf value to get the final score
myultiply the final score by popularity which  is from 0 to one value
sort all documents from the heighst score to the lowest one
get heighst 10 score documents or
for eah one of them get loop over the document and search for the document to take the breif from them
get heyper link and the brief and resturn them as a json object
 */
