package Indexer;
import data_base.DataBase;
import Stemmer.Stemmer;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.sql.SQLException;
import java.util.ArrayList;
import org.apache.commons.lang3.StringUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.Date;
import java.util.HashMap;
import java.io.File;  // Import the File class
import java.io.FileNotFoundException;  // Import this class to handle errors
import java.util.Scanner; // Import the Scanner class to read text files
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Indexer {
    private Stemmer S;

    private ArrayList<String> links;

    private HashMap<String, Integer> DocumentMap;

    private int DocumentCount;

    private int Loop;

    // Data for Document
    private String Path;
    private String Link;
    private String Title;

    // DataBase
    private DataBase db;

    // Document
    private Document document;
    java.sql.Date sqlDate;
    String Brief;


    public Indexer(ArrayList<String> files){
        S = new Stemmer();
        DocumentMap = new HashMap<>();
        this.links = new ArrayList<String>();

        this.links = files;
        DocumentCount = 0;
        Loop = 0;

        ConnectDataBase();

    }

    // Connect to database and create it;
    private void ConnectDataBase(){
        db = new DataBase();
        db.CreateDataBase();
    }

    // iterate the array list of files and pass it to indexer to work on it
    private void Start(){
        for (int i = 0 ; i < links.size() ; i++){
            Indexing(links.get(i));

//            FillDocument();
//            FillWord_Document();

            PrintMap(DocumentMap);

            // Clear every thing to start again
            DocumentCount = 0;
            DocumentMap.clear();

            // increment the loop
            Loop++;
        }
    }

    // take the name of the file and read line by line and steam this line and fill database for this line
    private void Indexing(String url) {
        try {
            this.document = Jsoup.connect(url).get();
        } catch (IOException e){
            System.out.println("Error in loading the page");
        }
        GetDocumentInformation();

        boolean Flag = true;
        Elements elements = document.body().select("*");
        for (Element element : elements) {
            String Stemmed = S.stem(element.ownText());
            if(StringUtils.isNotEmpty(Stemmed)){
                FillDocumentMap(Stemmed);
                System.out.println(element.nodeName() + " => " + element.ownText());
                if(Flag && element.nodeName() == "p" && element.ownText().length() > 100){
                    int index = element.ownText().indexOf(" ", 255);
                    if(index > 0)
                        Brief = element.ownText().substring(0,index).trim();
                    else
                        Brief = element.ownText();
                    Flag = !Flag;
                }
            }
        }
    }

    // Take stemmed line and put it in the database
    private void FillDocumentMap(String s){
        for (String word : s.split(" "))
        {
            DocumentCount++;
            if (DocumentMap.containsKey(word))
                DocumentMap.put(word,DocumentMap.get(word)+1);
            else
                DocumentMap.put(word, 1);;
        }
    }

    private void PrintMap(HashMap<String, Integer> DocumentMap){
        System.out.println("=============== " + Title + " =================");

        for (String key : DocumentMap.keySet()){
            System.out.println(key + " => " + DocumentMap.get(key));
        }
        System.out.println("The total words in this document is: "+ DocumentCount);
        System.out.println("The Title of this document is : "+ Title);
    }

    private void GetDocumentInformation(){
        Title = document.title();
        Link = links.get(Loop);

        try{
            URLConnection uc = new URL(Link).openConnection();
            Date d = new Date(uc.getIfModifiedSince());
            sqlDate = new java.sql.Date(d.getTime());
        } catch (IOException e) {
            e.printStackTrace();
            sqlDate = new java.sql.Date(new Date(0).getTime());
        }

        Brief = document.select("body>em").text();


    }

    private String GetTagData(String s, String line){
        Pattern p = Pattern.compile("<"+s+">(.+?)</"+s+">");
        Matcher m = p.matcher(line);
        if (m.find())
            return (m.group(1));
        else
            return null;
    }

    public void FillDocument() {
        String Query = "insert into document(hyper_link ," +
                                            "data_modified ," +
                                            "stream_words ," +
                                            "popularity ," +
                                            "Title" +
                                            ") " +
                                            "values('" +
                                            Link + "' ,'" +
                                            sqlDate + "' ,'" +
                                            Brief + "' ," +
                                            0 + " ,'" +
                                            Title +
                                            "');";
        try{
            db.insertdb(Query);
        }catch(SQLException throwables){
            throwables.printStackTrace();
        }

    }

    public void FillWord_Document(){
        for (String key : DocumentMap.keySet()){

            float tf = (float)DocumentMap.get(key)/DocumentCount;
            String Query = "insert into word_document(word_name ," +
                                                "document_hyper_link ," +
                                                "tf ," +
                                                "score" +
                                                ") " +
                                                "values('" +
                                                key + "' ,'" +
                                                Link + "' ," +
                                                tf+"," +
                                                0 +
                                                ");";
            try{
                db.insertdb(Query);
            }catch(SQLException throwables){
                throwables.printStackTrace();
            }
        }
    }


    public static void main(String[] args){

        ArrayList<String> links= new ArrayList<>();
//        links.add("https://www.tor.com/2016/09/28/the-city-born-great/");
        links.add("https://www.youtube.com/");
//        links.add("test2.txt");


        // =======================================
        // Those two line which mokhtar will call
        Indexer indexer = new Indexer(links);
        indexer.Start();
    }

}
