package Indexer;
import data_base.DataBase;
import Stemmer.Stemmer;
import java.util.ArrayList;
import org.apache.commons.lang3.StringUtils;

import java.util.HashMap;
import java.io.File;  // Import the File class
import java.io.FileNotFoundException;  // Import this class to handle errors
import java.util.Scanner; // Import the Scanner class to read text files

public class Indexer {
    private Stemmer S;

    private ArrayList<String> files;

    private HashMap<String, Integer> DocumentMap;
//    private HashMap<String, Integer> PublicMap;

//    private HashMap<String, Integer> WordsInFiles;

    private int DocumentCount;
//    private int PublicCount;

    private int Loop;

    public Indexer(ArrayList<String> files){
        S = new Stemmer();
        DocumentMap = new HashMap<>();
//        PublicMap = new HashMap<>();
//        WordsInFiles = new HashMap<>();
        this.files = new ArrayList<String>();

        this.files = files;
        DocumentCount = 0;
//        PublicCount = 0;
        Loop = 0;

        ConnectDataBase();

    }

    // Connect to database and create it;
    private void ConnectDataBase(){
        DataBase db = new DataBase();
        db.CreateDataBase();
    }

    // iterate the array list of files and pass it to indexer to work on it
    public void Start(){
        for (int i = 0 ; i < files.size() ; i++){
            Indexing(files.get(i));
//            WordsInFiles.put(files.get(i),DocumentCount);
            PrintMap(DocumentMap);
            DocumentCount = 0;
            DocumentMap.clear();
            Loop++;
        }
//        PrintTotalMap();
    }

    // take the name of the file and read line by line and steam this line and fill database for this line
    public void Indexing(String s){
        try {
            File file = new File(s);
            Scanner myReader = new Scanner(file);
            while (myReader.hasNextLine()) {
                String data = myReader.nextLine();
//                System.out.println(S.stem(data));
                FillDocumentMap(S.stem(data));
            }
            myReader.close();
        } catch (FileNotFoundException e) {
            System.out.println("An error occurred.");
            e.printStackTrace();
        }
    }

    // Take stemmed line and put it in the database
    public void FillDocumentMap(String s){
        for (String word : s.split(" "))
        {
//            PublicCount++;
            DocumentCount++;
            if (DocumentMap.containsKey(word))
                DocumentMap.put(word,DocumentMap.get(word)+1);
            else
                DocumentMap.put(word, 1);;


//            if (PublicMap.containsKey(word))
//                PublicMap.put(word,PublicMap.get(word)+1);
//            else
//                PublicMap.put(word, 1);;
        }
    }

    public void PrintMap(HashMap<String, Integer> DocumentMap){
        System.out.println("=============== " + this.files.get(Loop) + " =================");

        for (String key : DocumentMap.keySet()){
            System.out.println(key + " => " + DocumentMap.get(key));
        }
        System.out.println("The total words in this document is: "+ DocumentCount);
    }

    public void PrintTotalMap(){
//        System.out.println("=============== Total =================");
//        for (String key : PublicMap.keySet()){
//            System.out.println(key + " => " + PublicMap.get(key));
//        }

//        System.out.println("================= Some Information ================");
//        for(int i = 0 ; i < this.files.size(); i++){
//            System.out.println("The total words in this document is: "+WordsInFiles.get(this.files.get(i)));
//        }
//        System.out.println("The total words in all documents is: "+PublicCount);
    }


    public static void main(String[] args){
        ArrayList<String> files= new ArrayList<>();
        files.add("test.txt");
        files.add("test2.txt");
        Indexer indexer = new Indexer(files);
        indexer.Start();
    }

}
