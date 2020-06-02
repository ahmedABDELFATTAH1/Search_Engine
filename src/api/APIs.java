package api;
/*
import com.example.demo.Stemmer.Stemmer;
import com.example.demo.ranker.Ranker;
import com.example.demo.ranker.Ranker.DocumentResult;
import com.example.demo.ranker.Ranker.ImageResult;
import com.example.demo.ranker.Trends;
import org.apache.commons.lang3.StringUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.web.bind.annotation.*;

import java.sql.SQLException;
import java.util.ArrayList;


@RestController
@RequestMapping("/search")
public class APIs {

    public Ranker ranker = null;
    public Trends trends = null;
    public APIs() {
        ranker = new Ranker();
        trends = new Trends();
    }

    ArrayList<String> listOfWords;
    ArrayList<String> names;

    @GetMapping(value = "document")
    public String getDocument(@RequestParam(value = "search_query", defaultValue = "google") String search_query,
                              @RequestParam(value = "region", required = true, defaultValue = "Egypt") String region) throws SQLException, JSONException {


        names = trends.getNames(search_query);
        ranker.addSearchQuery(search_query.replace("\"", ""));
        if (names.size() > 0) {
            for (String name : names) {
                trends.addFamousNames(name, region);
            }

        }
        listOfWords = queryPreProcessor(search_query);
        String[] words = listOfWords.get(0).split(" ");
        ArrayList<String> wordsArray = new ArrayList<>();
        for (String word : words) {
            wordsArray.add(word);
        }
        String[] phrase = null;
        if (listOfWords.size() > 1) {
            phrase = listOfWords.get(1).split(" ");
        }
        ArrayList<DocumentResult> results = ranker.makeRank(wordsArray, phrase,region);
        for (DocumentResult result : results) {
            System.out.println(result.hyper_link);
            System.out.println(result.brief);
            System.out.println(result.title);
        }
        JSONArray jsonArray = new JSONArray();
        for (int i = 0; i < results.size(); i++) {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("TITLE", results.get(i).title);
            jsonObject.put("URL", results.get(i).hyper_link);
            jsonObject.put("BRIEF", results.get(i).brief);
            jsonArray.put(jsonObject);
        }
        return jsonArray.toString();
    }


    private ArrayList<String> queryPreProcessor(String search_query) {
        ArrayList<String> result=new ArrayList<>();
        Stemmer S = new Stemmer();
        String[] valuesInQuotes = StringUtils.substringsBetween(search_query , "\"", "\"");
        String stemmed= S.stem(search_query);
        result.add(stemmed);
        if(valuesInQuotes!=null)
        {
            result.add(S.stem(valuesInQuotes[0]));
        }
        return result;
    }


    @GetMapping(value="image")
    public String getImage( @RequestParam(value = "search_query") String search_query,
                                 @RequestParam(value = "region", required = true, defaultValue = "honey") String region) throws SQLException, JSONException {


        Stemmer s=new Stemmer();
        String words = s.stem(search_query);
        ArrayList<Ranker.ImageResult> results= ranker.imageSearch(words);
        for(ImageResult result : results)
        {
            System.out.println(result.image_url);
            System.out.println(result.caption);
        }
        JSONArray jsonArray= new JSONArray();
        for(int i=0;i<results.size();i++)
        {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("IMAGE_CAPTION",results.get(i).caption);
            jsonObject.put("IMAGE_URL", results.get(i).image_url);
            jsonArray.put(jsonObject) ;
        }
        return  jsonArray.toString();
    }


    @GetMapping(value="trend")
    public String getTrend( @RequestParam(value = "region") String region) throws SQLException, JSONException {
        ArrayList<Trends.TrendsData> results=trends.famousPersons(region);
        JSONArray jsonArray= new JSONArray();
        for(int i=0;i<results.size();i++)
        {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("PERSON_NAME",results.get(i).personName);
            jsonObject.put("COUNT", results.get(i).count);
            jsonArray.put(jsonObject) ;
        }
        return  jsonArray.toString();
    }

    @GetMapping(value="suggest")
    public String getSuggestion( @RequestParam(value = "search_query") String search_query) throws SQLException, JSONException {
        JSONArray jsonArray= new JSONArray();
        if(search_query=="")
        {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("SUGGESTION", "none");
            jsonArray.put(jsonObject) ;
            return jsonArray.toString();
        }
        ArrayList<String> suggestions=ranker.getSuggestions(search_query);


        for(int i=0;i<suggestions.size();i++)
        {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("SUGGESTION", suggestions.get(i));
            jsonArray.put(jsonObject) ;
        }
        return jsonArray.toString();
    }



}
*/