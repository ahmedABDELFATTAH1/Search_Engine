package ranker;

/*
import data_base.DataBase;
import data_base.WordDocumentLabels;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;

public class Ranker {
    public static final String SUGGESTION = "search_query";
    DataBase db = null;
    public Ranker() {
        db = new DataBase();
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

    private Boolean wordExists(String word) throws SQLException {
        String sql_request = null;
        sql_request = "SELECT * FROM " + DataBase.documentWordTableName + " WHERE " + WordDocumentLabels.WORD_NAME + " = '" + word + "'";
        ResultSet rs = null;
        try {
            rs = db.selectQuerydb(sql_request);
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
        return rs.next();
    }

    private Double getIDF(String word) throws SQLException {
        //number of documents divided by number number of documents where the word is there
        String sql_request = null;
        sql_request = "SELECT COUNT(*) FROM " + DataBase.documentTableName;
        ResultSet rs = null;
        try {
            rs = db.selectQuerydb(sql_request);
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
        rs.next();
        Integer numberDocuments = rs.getInt(1);
        sql_request = "SELECT COUNT(*) FROM " + DataBase.documentWordTableName + " where " + WordDocumentLabels.WORD_NAME +
                    " ='" + word + "';";
        rs = null;
        try {
            rs = db.selectQuerydb(sql_request);
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
        rs.next();
        Integer numberWordDocuments = rs.getInt(1);
        Double IDF = Math.log10(Float.valueOf(numberDocuments) / Float.valueOf(numberWordDocuments));
        return IDF ;

    }

    private void relevanceWordDocument(String word, Double IDF,String region,HashMap<String, Double> documentMap) throws SQLException {
        String sql_request = null;
        sql_request = "SELECT * FROM " + DataBase.documentWordTableName + " where " + WordDocumentLabels.WORD_NAME +
                    "= '" + word + "';";
        ResultSet rs = null;
        try {
            rs = db.selectQuerydb(sql_request);
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
            while (rs.next()) {
                String hyper_link_id = rs.getString(WordDocumentLabels.DOCUMENT_HYPER_LINK_ID);
                Float tf = rs.getFloat(WordDocumentLabels.TERM_FREQUENCY);
                Float popularity = getDocumentPopularity(hyper_link_id);
                Double location=getLocationScore(region,hyper_link_id);
                Double score = rs.getFloat(WordDocumentLabels.SCORE) + location*(((tf) * (IDF)) + (.2*popularity));
                if(documentMap.containsKey(hyper_link_id))
                {
                    Double oldScore=documentMap.get(hyper_link_id);
                    documentMap.replace(hyper_link_id,oldScore+score);
                }
                else
                {
                    documentMap.put(hyper_link_id,score);
                }
        }

    }

    private Double getLocationScore(String region, String hyper_link_id) throws SQLException {
        CountryCodes countryCodes=new CountryCodes();
        String userEx=countryCodes.getCode(region);
        String query="select "+DocumentLabels.COUNTRY_CODE+" from "+DataBase.documentTableName+" where "+DocumentLabels.HYPER_LINK_ID+" = '"+hyper_link_id+"';";
        ResultSet rs = null;
        try {
            rs = db.selectQuerydb(query);
            rs.next();
        } catch (SQLException throwables) {
           System.out.println("NO REGION PROVIDED");
           return 1.0;
        }
        String countryCode = rs.getString(DocumentLabels.COUNTRY_CODE);
        if(countryCode.equals(userEx))
            return 1.1;
        else
            return 1.0;

    }


    private Float getDocumentPopularity(String hyperLink_id) throws SQLException {
        String sql_request = "SELECT " + DocumentLabels.POPULARITY + " FROM " + DataBase.documentTableName + " Where " + DocumentLabels.HYPER_LINK_ID +
                " = '" + hyperLink_id + "';";
        ResultSet rs = null;
        try {
            rs = db.selectQuerydb(sql_request);
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
        rs.next();
        Float popularity = rs.getFloat(DocumentLabels.POPULARITY);
        return popularity;
    }

    public void clearScores() {
        String sql_request = null;
        sql_request = "UPDATE " + DataBase.documentWordTableName + " SET " + WordDocumentLabels.SCORE +
                    " = 0 where score > 0;";

        try {
            db.updatedb(sql_request);
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
    }

    Float getScore(String document) throws SQLException {
        String sql_request = null;
        sql_request = "SELECT SUM(" + WordDocumentLabels.SCORE + ") FROM " + DataBase.documentWordTableName +
                    " where " + WordDocumentLabels.DOCUMENT_HYPER_LINK_ID + " = '" + document + "';";


        ResultSet rs = null;
        rs = db.selectQuerydb(sql_request);
        rs.next();
        return rs.getFloat(1);
    }


    //your code

    //private
    public ArrayList<DocumentResult> makeRank(ArrayList<String> search_list,String[] phrase,String region) throws SQLException {
        HashMap<String, Double> documentMap = new HashMap<String, Double>();
        ArrayList<String> criticalDocuments = null;
        if(phrase!=null)
        {
            criticalDocuments = phraseSearching(phrase);
        }
        for (int i = 0; i < search_list.size(); i++) {
            String word = search_list.get(i);
            if (!wordExists(word)) {
                System.out.println("word doesnt exist");
                continue;
            } else {
                System.out.println("word does exist");
            }
            Double IDF = getIDF(word);
            relevanceWordDocument(word, IDF,region,documentMap);
        }
        ArrayList<sortDocuments> sortedDocuments = null;
        if (criticalDocuments != null) {
            sortedDocuments = new ArrayList<>();
            for (String document : criticalDocuments) {
                Double score =documentMap.get(document);
                sortedDocuments.add(new sortDocuments(document, score));
                if(sortedDocuments.size()==100)
                    break;
            }
            sortedDocuments.sort(Comparator.comparing(sortDocuments::getScore).reversed());
        } else {
            sortedDocuments = getHighestScoresDocuments(documentMap);
            sortedDocuments.sort(Comparator.comparing(sortDocuments::getScore).reversed());
        }
        ArrayList<DocumentResult> documentResult = new ArrayList<>();
        for (sortDocuments doc : sortedDocuments) {
            String hyper_link_id = doc.hyper_link_id;
            String hyper_link = getDocumentUrl(doc.hyper_link_id);
            String brief = getBrief(hyper_link_id);
            String title = getTitle(hyper_link_id);
            documentResult.add(new DocumentResult(hyper_link,title, brief));
        }
        return documentResult;
    }

    private String getTitle(String hyper_link) throws SQLException {
        String sql_request = null;
            sql_request = " SELECT " + DocumentLabels.TITLE + " FROM " + DataBase.documentTableName + " WHERE " + DocumentLabels.HYPER_LINK_ID +
                    " ='" + hyper_link + "';";
        ResultSet rs = null;
        rs = db.selectQuerydb(sql_request);
        rs.next();
        return rs.getString(DocumentLabels.TITLE);
    }

    private String getBrief(String hyper_link) throws SQLException {
        String sql_request = null;
        sql_request = " SELECT " + DocumentLabels.STREAM_WORDS + " FROM " + DataBase.documentTableName + " WHERE " + DocumentLabels.HYPER_LINK_ID +
                    " ='" + hyper_link + "';";

        ResultSet rs = null;
        rs = db.selectQuerydb(sql_request);
        rs.next();
        String stream_words="no brief avialable";
        try {
            stream_words = rs.getString(DocumentLabels.STREAM_WORDS);
        }
        catch (SQLException e)
        {

        }

        String brief = null;
        if (stream_words.length() > 1000) {
            brief = stream_words.substring(0, 1000);
        } else {
            brief = stream_words;
        }
        return brief;

    }

    private ArrayList<String> phraseSearching(String[] phrase) throws SQLException {
        ArrayList<String> criticalDocuments = new ArrayList<>();
        String word = phrase[0];
        ArrayList<String> Documents = getDocumentsId(word);
        for (String documentId : Documents) {
            word = phrase[0];
            ArrayList<Integer> positions = getPositions(documentId, word);
            if (positions.size() == 0)
                continue;
            for (int i = 1; i < phrase.length; i++) {
                for (int itr = 0; itr < positions.size(); itr++) {
                    positions.set(itr, positions.get(itr) + 1);
                }
                word = phrase[i];
                ArrayList<Integer> newPositions = getPositions(documentId, word);
                int pos_itr=0;
                int new_pos_itr=0;
                ArrayList<Integer> keepIndices=new ArrayList<>();
                while(true)
                {
                    if(pos_itr==positions.size()||new_pos_itr==newPositions.size())
                        break;
                    if(newPositions.get(new_pos_itr).equals(positions.get(pos_itr)))
                    {
                        keepIndices.add(pos_itr);
                        pos_itr++;
                        new_pos_itr++;
                    }
                    else if(newPositions.get(new_pos_itr)>positions.get(pos_itr))
                    {
                        pos_itr++;
                    }
                    else if(newPositions.get(new_pos_itr)<positions.get(pos_itr))
                    {
                        new_pos_itr++;
                    }
                }
                ArrayList<Integer> tempPositions=new ArrayList<>();
                for(Integer move_itr:keepIndices)
                {
                    tempPositions.add(positions.get(move_itr));
                }
                positions=tempPositions;

            }
            if(positions.size()>0)
            {
                criticalDocuments.add(documentId);
            }
        }
        return criticalDocuments;
    }
   private String getDocumentUrl(String document_id) throws SQLException {
       String sql_query= "SELECT "+DocumentLabels.HYPER_LINK+" FROM "+DataBase.documentTableName+" WHERE "+DocumentLabels.HYPER_LINK_ID+
               " = "+document_id+";";

       ResultSet rs = null;
       rs = db.selectQuerydb(sql_query);
       rs.next();
       return rs.getString(1);
   }
    private Integer getWordDocumentId(String document_id, String word) throws SQLException {
        String sql_request = "SELECT " + WordDocumentLabels.WORD_DOCUMENT_ID + " FROM " + DataBase.documentWordTableName
                + " WHERE " + WordDocumentLabels.DOCUMENT_HYPER_LINK_ID + " = " + document_id + " AND " + WordDocumentLabels.WORD_NAME +
                " = '" + word + "' ;";
        ResultSet rs = null;
        rs = db.selectQuerydb(sql_request);
        if(rs.next())
        return rs.getInt(WordDocumentLabels.WORD_DOCUMENT_ID);
        else return -1;

    }
    private ArrayList<Integer> getPositions(String document_id, String word) throws SQLException {
        ArrayList<Integer> pos = new ArrayList<>();
        Integer word_document_id=getWordDocumentId(document_id,word);
        if(word_document_id==-1)
            return new ArrayList<>();
        String sql_request = "SELECT " + WordIndexLabels.POSITION + " FROM " + DataBase.indexTableName + " WHERE " +
                WordIndexLabels.WORD + " = '" + word +"' and "+WordIndexLabels.DOCUMENT_ID+" = "+document_id;
        ResultSet rs = null;
        rs = db.selectQuerydb(sql_request);
        while (rs.next()) {
            pos.add(rs.getInt(WordIndexLabels.POSITION));
        }
        return pos;
    }

    private ArrayList<String> getDocumentsId(String word) throws SQLException {
        ArrayList<String> Documents = new ArrayList<>();
       String sql_request = "SELECT " + DataBase.documentTableName +
                "." +DocumentLabels.HYPER_LINK_ID+" From "+ DataBase.documentTableName+" inner join "+
                DataBase.documentWordTableName+" on "+
                DataBase.documentTableName+"."+DocumentLabels.HYPER_LINK_ID+" = "+
                DataBase.documentWordTableName+"."+WordDocumentLabels.DOCUMENT_HYPER_LINK_ID+
                " WHERE " + DataBase.documentWordTableName+"."+WordDocumentLabels.WORD_NAME + " = '" + word + " ';";
        ResultSet rs = null;
        rs = db.selectQuerydb(sql_request);
        while (rs.next()) {
            Documents.add(rs.getString(DocumentLabels.HYPER_LINK_ID));
        }
        return Documents;
    }

    private ArrayList<sortDocuments> getHighestScoresDocuments(HashMap<String, Double> documentMap) throws SQLException {
        ArrayList<sortDocuments> heightsScores = new ArrayList<>();
        for (String link_id : documentMap.keySet()) {
            heightsScores.add(new sortDocuments(link_id,documentMap.get(link_id)));
        }
        return heightsScores;
    }

    public ArrayList<String> getSuggestions(String search_query) throws SQLException {
        ArrayList<String> suggestions = new ArrayList<>();
        String sql_statement = "SELECT " + SUGGESTION + " FROM " + DataBase.suggestionTableName + " where " + SUGGESTION + " REGEXP '" + search_query + "?';";
        ResultSet rs = null;
        rs = db.selectQuerydb(sql_statement);
        while (rs.next()) {
            suggestions.add(rs.getString(SUGGESTION));
        }
        return suggestions;
    }

    public void addSearchQuery(String search_query) throws SQLException {
        if(!searchExist(search_query)) {
            String sql_statement = "INSERT INTO " + DataBase.suggestionTableName + "(" + SUGGESTION + ") values ('" + search_query + "');";
            try {
                db.updatedb(sql_statement);
            } catch (SQLException throwables) {
                throwables.printStackTrace();
            }
        }
    }

    private boolean searchExist(String search) throws SQLException {
        String sql_query="select * from "+DataBase.suggestionTableName+" where "+SUGGESTION+" = '"+search+"';";
        ResultSet rs = null;
        rs = db.selectQuerydb(sql_query);
        return rs.next();
    }

    public ArrayList<ImageResult> imageSearch(String words) throws SQLException {
        ArrayList<ImageResult> result =new ArrayList<>();
        String sql_statement ="SELECT Distinct "+ImageLabels.IMAGE_URL+","+ImageLabels.CAPTION+" From "+DataBase.imageTableName+
                " where " + ImageLabels.STEMMED + " LIKE '"+words+"'";
        ResultSet rs = null;
        rs = db.selectQuerydb(sql_statement);

        while (rs.next()) {
            String imageUrl=rs.getString(ImageLabels.IMAGE_URL);

            result.add(new ImageResult(imageUrl,rs.getString(ImageLabels.CAPTION)));


        }

        return result;
    }


    public class DocumentResult {
        public String hyper_link;
        public String title;
        public String brief;

        public DocumentResult(String hyper_link, String title, String brief) {
            this.hyper_link = hyper_link;
            this.title = title;
            this.brief = brief;
        }

        public String getHyper_link() {
            return hyper_link;
        }

        public void setHyper_link(String hyper_link) {
            this.hyper_link = hyper_link;
        }

        public String getTitle() {
            return title;
        }

        public void setTitle(String title) {
            this.title = title;
        }

        public String getBrief() {
            return brief;
        }

        public void setBrief(String brief) {
            this.brief = brief;
        }
    }

    public class ImageResult{
        public ImageResult(String image_url, String caption) {
            this.image_url = image_url;
            this.caption = caption;
        }

        public String getImage_url() {
            return image_url;
        }

        public void setImage_url(String image_url) {
            this.image_url = image_url;
        }

        public String getCaption() {
            return caption;
        }

        public void setCaption(String caption) {
            this.caption = caption;
        }

        public String image_url;
        public String caption;


    }
    public class sortDocuments {
        public String hyper_link_id;
        public Double score;

        public sortDocuments(String hyper_link, Double score) {
            this.hyper_link_id = hyper_link;
            this.score = score;
        }

        public String getHyper_link() {
            return hyper_link_id;
        }

        public void setHyper_link(String hyper_link) {
            this.hyper_link_id = hyper_link;
        }

        public Double getScore() {
            return score;
        }

        public void setScore(Double score) {
            this.score = score;
        }

    }

}
*/