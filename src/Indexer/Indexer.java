package Indexer;
import data_base.DataBase;
import Stemmer.Stemmer;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import org.apache.commons.lang3.StringUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

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
    DataBase db;

    // Document
    Document document;


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

        Elements elements = document.body().select("*");
        for (Element element : elements) {
            String Stemmed = S.stem(element.ownText());
            if(StringUtils.isNotEmpty(Stemmed)){
                FillDocumentMap(Stemmed);
//                System.out.println(element.nodeName() + " => " + element.ownText());
            }

        }

//        try {
//            File file = new File(s);
//            GetDocumentInformation();
//            Scanner myReader = new Scanner(file);
//            while (myReader.hasNextLine()) {
//                String data = myReader.nextLine();
//                if(StringUtils.isNotEmpty(data)){
//                    System.out.println(S.stem(data));
//                    Title = GetTagData("title",data);
//                    FillDocumentMap(S.stem(data));
//                }
//            }
//            myReader.close();
//        } catch (FileNotFoundException e) {
//            System.out.println("An error occurred.");
//            e.printStackTrace();
//        }
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
//                                            "data_modified ," +
                                            "doc_path_file ," +
                                            "stream_words ," +
                                            "popularity ," +
                                            "Title ," +
                                            "is_image" +
                                            ") " +
                                            "values('" +
                                            Link + "' ,'" +
//                                            null + "' ,'" +
                                            Path + "' ,'" +
                                            null + "' ," +
                                            0 + " ,'" +
                                            Title + "' ," +
                                            0 +
                                            ");";
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
        links.add("https://www.tor.com/2016/09/28/the-city-born-great/");
//        links.add("test2.txt");


        // =======================================
        // Those two line which mokhtar will call
        Indexer indexer = new Indexer(links);
        indexer.Start();
    }

}
