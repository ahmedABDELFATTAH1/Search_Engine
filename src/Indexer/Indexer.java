package Indexer;
import data_base.DataBase;
import Stemmer.Stemmer;

import java.awt.*;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.sql.ResultSet;
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

//    private ArrayList<String> links;
    private ResultSet links;

    private HashMap<String, IndexAndFreq> DocumentMap;

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
    int LastLinkId;
    int TotalFreq;
    float Popularity;

    // Image
    ArrayList<ImageData> Images;


    public Indexer() throws SQLException {
        S = new Stemmer();
        DocumentMap = new HashMap<>();
        this.Images = new ArrayList<ImageData>();

        DocumentCount = 0;
        Loop = 0;

        ConnectDataBase();
        GetLinksFromDataBase();
        Start();
    }

    // Connect to database and create it;
    private void ConnectDataBase(){
        db = new DataBase();
        db.CreateDataBase();
    }

    private void GetLinksFromDataBase() throws SQLException {
        String Query = "Select url from crawler_urls";
        this.links = db.selectQuerydb(Query);

        Query = "Select Sum(host_ref_times) from hosts_popularity";
        ResultSet r = db.selectQuerydb(Query);
        r.next();
        TotalFreq = r.getInt(1);
    }

    // iterate the array list of files and pass it to indexer to work on it
    private void Start() throws SQLException {
        while (this.links.next()){
            Indexing(this.links.getString("url"));

            FillDocument();
            FillWord_Document();
            FillImageTable();

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
        // Connect with url
        try {
            this.document = Jsoup.connect(url).get();
//            this.document = Jsoup.parseBodyFragment(url);
        } catch (IOException e){
            System.out.println("Error in loading the page");
        }

        // Get Information of document
        try {
            GetDocumentInformation(url);
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }

        boolean Flag = true;
        Elements elements = document.body().getAllElements();
        for (Element element : elements) {

            // Image Map
            if(element.nodeName().equals("img") && StringUtils.isNotEmpty(element.attr("src")) && StringUtils.isNotEmpty(element.attr("alt"))){
                String ImageStemmed = S.stem(element.attr("alt"));
                if(StringUtils.isNotEmpty(ImageStemmed)){
                    FillImages(element,ImageStemmed);
                    System.out.println(element.attr("src"));
                    System.out.println(element.attr("alt"));
                }
                continue;
            }

            // Ordinary Map
            String Stemmed = S.stem(element.ownText());
            if(StringUtils.isNotEmpty(Stemmed)){
                FillDocumentMap(Stemmed);
                System.out.println(element.nodeName() + " => " + element.ownText());

                // Brief
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
            if (DocumentMap.containsKey(word)){
                DocumentMap.get(word).Freg++;
                DocumentMap.get(word).Index.add(DocumentCount);
            }else{
                IndexAndFreq temp = new IndexAndFreq();
                temp.Freg = 1;
                temp.Index.add(DocumentCount);
                DocumentMap.put(word, temp);
            }
            DocumentCount++;
        }
    }

    private void FillImages(Element e,String s){
        ImageData image = new ImageData();
        image.Catption = e.attr("alt");
        image.Stemmed = s;
        image.Src = e.attr("src");
        Images.add(image);
    }

    private void PrintMap(HashMap<String, IndexAndFreq> DocumentMap){
        System.out.println("=============== " + Title + " =================");

        for (String key : DocumentMap.keySet()){
            System.out.print(key + " => " + DocumentMap.get(key).Freg+" => ");
            for(int i : DocumentMap.get(key).Index){
                System.out.print(i + " ");
            }
            System.out.println();
        }
        System.out.println("The total words in this document is: "+ DocumentCount);
        System.out.println("The Title of this document is : "+ Title);
    }

    private void GetDocumentInformation(String url) throws MalformedURLException, SQLException {
        Title = document.title();
        Link = url;

        try{
            URLConnection uc = new URL(Link).openConnection();
            Date d = new Date(uc.getIfModifiedSince());
            sqlDate = new java.sql.Date(d.getTime());
        } catch (IOException e) {
            e.printStackTrace();
            sqlDate = new java.sql.Date(new Date(0).getTime());
        }

        URL U = new URL(url);
        String Query = "Select host_ref_times from hosts_popularity where host_name = '" + U.getHost() + "';";
        ResultSet r = db.selectQuerydb(Query);
        r.next();
        int temp= r.getInt(1);
        Popularity = (float)temp/TotalFreq;
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
                                            Popularity + " ,'" +
                                            Title +
                                            "');";
        try{
            LastLinkId = db.insertdb(Query);
        }catch(SQLException throwables){
            throwables.printStackTrace();
        }
    }

    public void FillWord_Document(){
        for (String key : DocumentMap.keySet()){
            int ID = 0;
            float tf = (float)DocumentMap.get(key).Freg/DocumentCount;
            String Query = "insert into word_document(word_name ," +
                                                "document_hyper_link_id ," +
                                                "tf ," +
                                                "score" +
                                                ") " +
                                                "values('" +
                                                key + "' ,'" +
                                                LastLinkId + "' ," +
                                                tf+"," +
                                                0 +
                                                ");";
            try{
                ID = db.insertdb(Query);
            }catch(SQLException throwables){
                throwables.printStackTrace();
            }

            for(int index : DocumentMap.get(key).Index){
                Query = "insert into word_index(word_document_id ," +
                                                "word_position" +
                                                ") " +
                                                "values(" +
                                                ID + " ," +
                                                index +
                                                ");";
                try{
                    db.insertdb(Query);
                }catch(SQLException throwables){
                    throwables.printStackTrace();
                }
            }

        }
    }

    private void FillImageTable(){
        for (ImageData i : Images){
                String Query = "insert into image(image_url ," +
                                                        "caption," +
                                                        "stemmed" +
                                                        ") " +
                                                        "values('" +
                                                        i.Src + "' ,'" +
                                                        i.Catption + "' ,'" +
                                                        i.Stemmed +
                                                        "');";
            try{
                db.insertdb(Query);
            }catch(SQLException throwables){
                throwables.printStackTrace();
            }
        }
    }


    public static void main(String[] args) throws SQLException {

//        Indexer indexer = new Indexer();
    }
}


class IndexAndFreq{
    int Freg;
    ArrayList<Integer> Index = new ArrayList<>();
}

class ImageData{
    String Src;
    String Stemmed;
    String Catption;
}
