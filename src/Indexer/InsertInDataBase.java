package Indexer;

import data_base.DataBase;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;

public class InsertInDataBase {
    private DataBase db;


    public InsertInDataBase() throws SQLException {

        this.db = new DataBase();
        db.CreateDataBase();

        // Delete all rows in crawler and popularity
        String Query = "delete from crawler_urls";
        db.deletedb(Query);

        Query = "delete from hosts_popularity";
        db.deletedb(Query);

        Query = "delete from word_index";
        db.deletedb(Query);

        Query = "delete from word_document";
        db.deletedb(Query);

        Query = "Delete from document";
        db.deletedb(Query);

        Query = "Delete from image";
        db.deletedb(Query);

        ArrayList<String> links= new ArrayList<>();
        links.add("https://www.fatakat.com");
//        links.add("https://www.google.com");
//        links.add("https://en.wikipedia.org/wiki/AND_gate");
//        links.add("https://en.wikipedia.org/wiki/OR_gate");

//        links.add("https://elegant-jones-f4e94a.netlify.com/valid_doc.html");
//        links.add("https://wuzzuf.net/internship/288003-PHP-Developer---Internship-ElMnassa-Innovation-Development-Cairo-Egypt?l=cup&t=bj&a=Internships-in-Egypt&o=2");



        HashMap<String,Integer> hosts = new HashMap<String , Integer>();
        hosts.put("fatakat.com",1);
//        hosts.put("www.google.com",1);
//        hosts.put("en.wikipedia.org",2);


        for(String s :links){
            Query = "insert into crawler_urls(url) values('"+ s +"')";

            db.insertdb(Query);
        }

        for(String s :hosts.keySet()){
            Query = "insert into hosts_popularity(host_name,host_ref_times) values('"+ s +"', '" + hosts.get(s) +"')";

            db.insertdb(Query);
        }

    }

    public static void main(String[] args) throws SQLException, IOException {
        InsertInDataBase I = new InsertInDataBase();
        Indexer indexer = new Indexer();
    }
}
