package Indexer;


import Stemmer.Stemmer;
import URLInformation.URLInformation;
import com.maxmind.geoip2.exception.GeoIp2Exception;
import data_base.DataBase;
import org.apache.commons.lang3.StringUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
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
    URLInformation urlinformation;
    String CountryCode;

    // Image
    ArrayList<ImageData> Images;


    public Indexer() throws SQLException, IOException {
        S = new Stemmer();
        DocumentMap = new HashMap<>();
        this.Images = new ArrayList<ImageData>();
        this.urlinformation = new URLInformation();

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
        String Query = "Select url from crawler_urls where done = 0";
        this.links = db.selectQuerydb(Query);


        Query = "Select Sum(host_ref_times) from hosts_popularity";
        ResultSet r = db.selectQuerydb(Query);
        r.next();
        TotalFreq = r.getInt(1);
    }

    // iterate the array list of files and pass it to indexer to work on it
    private void Start() throws SQLException {
        while (this.links.next()){
            String link=this.links.getString("url");

            Indexing(link);
            FillDocument();
            FillWord_Document();
            FillImageTable();

          //   PrintMap(DocumentMap);

            // Clear every thing to start again
            DocumentCount = 0;
            DocumentMap.clear();
            setDone(link);
            // increment the loop
            Loop++;
        }
    }

    private void setDone(String link) {
        String query="update crawler_urls set done=1 where url ='"+link+"';";
        try {
            db.updatedb(query);
        } catch (SQLException e) {
            e.printStackTrace();
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
        } catch (GeoIp2Exception e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        boolean Flag = true;
        Elements elements = document.body().getAllElements();
        for (Element element : elements) {

            // Image Map
            if(element.nodeName().equals("img") && StringUtils.isNotEmpty(element.attr("src")) && StringUtils.isNotEmpty(element.attr("alt")) && IsImage(element.attr("src"))){
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
                FillDocumentMap(Stemmed, GetScore(element.nodeName()));
                 //System.out.println(element.nodeName() + " => " + element.ownText());

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
    private void FillDocumentMap(String s,int score){
        for (String word : s.split(" "))
        {
            if (DocumentMap.containsKey(word)){
                DocumentMap.get(word).Freg++;
                DocumentMap.get(word).Extra+=score;
                DocumentMap.get(word).Index.add(DocumentCount);
            }else{
                IndexAndFreq temp = new IndexAndFreq();
                temp.Freg = 1;
                temp.Index.add(DocumentCount);
                temp.Extra = score;
                DocumentMap.put(word, temp);
            }
            DocumentCount++;
        }
    }

    private int GetScore(String tag){
        switch(tag){
            case("h1"):
                return 6;

            case("h2"):
                return 5;

            case("h3"):
                return 4;

            case("h4"):
                return 3;

            case("h5"):
                return 2;

            case("h6"):
                return 1;

            case("em"):
                return 1;

            case("strong"):
                return 2;

            case("b"):
                return 2;

            case("i"):
                return 1;

            case("u"):
                return 2;

            case("title"):
                return 10;

            default:
                return 0;
        }
    }

    private void FillImages(Element e,String s){
        ImageData image = new ImageData();
        image.Catption = e.attr("alt");
        image.Stemmed = s;
        image.Src = e.attr("src");
        Images.add(image);
    }

   /* private void PrintMap(HashMap<String, IndexAndFreq> DocumentMap){
        System.out.println("=============== " + Title + " =================");

        for (String key : DocumentMap.keySet()){
            System.out.print(key + " => " + DocumentMap.get(key).Freg+" => " + DocumentMap.get(key).Extra +" => ");
            for(int i : DocumentMap.get(key).Index){
                System.out.print(i + " ");
            }
            System.out.println();
        }
        System.out.println("The total words in this document is: "+ DocumentCount);
        System.out.println("The Title of this document is : "+ Title);
    }

*/


    private void GetDocumentInformation(String url) throws IOException, SQLException, GeoIp2Exception {
        // Title and Brief
        Title = document.title();
        if(Brief == null)
             Brief = Title;


        // Country
        CountryCode = this.urlinformation.Country(url).getIsoCode();

        // Date
        FillDocumentMap(S.stem(Title),GetScore("title"));
        Link = url;

        try{
            URLConnection uc = new URL(Link).openConnection();
            Date d = new Date(uc.getIfModifiedSince());
            sqlDate = new java.sql.Date(d.getTime());
        } catch (IOException e) {
            e.printStackTrace();
            sqlDate = new java.sql.Date(new Date(0).getTime());
        }


        // Popularity
        URL U = new URL(url);
        String UString = U.getHost();

        if(UString.startsWith("www"))
            UString = UString.substring(4);


        String Query = "Select host_ref_times from hosts_popularity where host_name = '" + UString + "';";
        ResultSet r = db.selectQuerydb(Query);
        if(r.next() != false){
            int temp= r.getInt(1);
            Popularity = (float)temp/TotalFreq;
        }else{
            Popularity = 0;
        }


        System.out.println(UString);
        System.out.println(CountryCode);
        System.out.println(Title);
        System.out.println(url);
        System.out.println("===========================================================");
        System.out.println("===========================================================");


    }

    private Boolean IsImage(String s){
        Pattern p = Pattern.compile("http(s)?:\\/\\/.*\\.(png|jpg|jpeg)");
        Matcher m = p.matcher(s);
        if (m.find())
            return true;
        else
            return false;
    }

    public void FillDocument() {
        Title=Title.replace('\"',' ');
        Brief=Brief.replace('\"',' ');
        String Query = "insert into document(hyper_link ," +
                "CountryCode ," +
                "data_modified ," +
                "stream_words ," +
                "popularity ," +
                "Title" +
                ") " +
                "values('" +
                Link + "' ,'" +
                CountryCode + "' ,'" +
                sqlDate + "' ,\"" +
                Brief + "\" ," +
                Popularity + " ,\"" +
                Title +
                "\");";
        // System.out.println(Query);
        try{
            LastLinkId = db.insertdb(Query);
        }catch(SQLException throwables){
            throwables.printStackTrace();
        }
    }

    public void FillWord_Document(){
        ArrayList<String> keys=new ArrayList<>();
        ArrayList<Integer> IDs=new ArrayList<>();
        for (String key : DocumentMap.keySet()){
            key.replace('\"', ' ');
            int ID = 0;
            float tf = (float)(DocumentMap.get(key).Freg+DocumentMap.get(key).Extra)/DocumentCount;
            String Query = "insert into word_document(word_name ," +
                    "document_hyper_link_id ," +
                    "tf ," +
                    "score" +
                    ") " +
                    "values(\"" +
                    key + "\" ," +
                    LastLinkId + " ," +
                    tf+"," +
                    0 +
                    ");";
            try{
                ID = db.insertdb(Query);
            }catch(SQLException throwables){
              System.out.println("bad request ya waad");
            }

            keys.add(key);
            IDs.add(ID);

        }
        String indexQuery= "insert into word_index(word_document_id ," +
                "word_position" +
                ") " +
                "values";
        for(int i=0;i<keys.size();i++) {
            for (int index : DocumentMap.get(keys.get(i)).Index) {
                indexQuery += "(" + IDs.get(i) + " ," + index + "),";
            }
        }
        if (indexQuery.endsWith(",")) {
            indexQuery = indexQuery.substring(0, indexQuery.length() - 1);
        }
        try {
            // System.out.println(indexQuery);
            db.insertdb(indexQuery);
        } catch (SQLException throwables) {
            System.out.println("bad request ya waad");
        }
    }

    private void FillImageTable(){

        if(Images.size()==0)
            return;
        String Query = "insert into image(image_url ," +
                "caption," +
                "stemmed" +
                ") " +
                "values";
        for (ImageData i : Images){

            String src = i.Src;
            String caption = i.Catption;
            String stemmed = i.Stemmed;

            caption=caption.replace('\"',' ');
            caption=caption.replace("'","");
            stemmed=stemmed.replace('\"',' ');
            stemmed=stemmed.replace("'","");
            Query += "('" +
                    src + "' ,\"" +
                    caption + "\" ,'" +
                    stemmed +
                    "'),";
        }

        if (Query.endsWith(","))
            Query = Query.substring(0, Query.length() - 1);


        try {
            db.insertdb(Query);
        } catch (SQLException e) {
            e.printStackTrace();
        }

    }


    public static void main(String[] args) throws SQLException {

//        Indexer indexer = new Indexer();
    }
}


class IndexAndFreq{
    int Freg;
    ArrayList<Integer> Index = new ArrayList<>();
    int Extra = 0;
}

class ImageData{
    String Src;
    String Stemmed;
    String Catption;
}
