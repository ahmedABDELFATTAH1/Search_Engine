package ranker;



import data_base.*;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Comparator;

public class Ranker {
    DataBase db = null;
    public Ranker()
    {
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
    private Boolean wordExists(String word, Boolean imageSearch) throws SQLException {
        String sql_request= null;
        if(imageSearch)
        {
            sql_request= "SELECT * FROM "+ DataBase.imageWordTableName+" WHERE "+ WordImageLabels.WORD_NAME +" = '"+ word+"'" ;
        }
        else
        {
            sql_request= "SELECT * FROM "+ DataBase.documentWordTableName+" WHERE "+ WordDocumentLabels.WORD_NAME +" = '"+ word+"'" ;
        }
        ResultSet rs = null;
        try {
            rs = db.selectQuerydb(sql_request);
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
       return rs.next();
    }

    private Double getIDF(String word,Boolean imageSearch) throws SQLException {
        //number of documents divided by number number of documents where the word is there
        String sql_request=null;
        if(imageSearch)
        {
            sql_request = "SELECT COUNT(*) FROM "+DataBase.imageTableName;
        }
        else
        {
            sql_request = "SELECT COUNT(*) FROM "+DataBase.documentTableName;
        }
        ResultSet rs = null;
        try {
            rs = db.selectQuerydb(sql_request);
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
        rs.next();
        Integer numberDocuments=rs.getInt(1);

        if(imageSearch)
        {
            sql_request = "SELECT COUNT(*) FROM "+DataBase.imageWordTableName+" where "+ WordImageLabels.WORD_NAME+
                    " ='"+word+"';";

        }
        else
        {
            sql_request = "SELECT COUNT(*) FROM "+DataBase.documentWordTableName+" where "+ WordDocumentLabels.WORD_NAME+
                    " ='"+word+"';";

        }
         rs = null;
        try {
            rs = db.selectQuerydb(sql_request);
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
        rs.next();
        Integer numberWordDocuments=rs.getInt(1);
        Double IDF=Math.log10(Float.valueOf(numberDocuments)/Float.valueOf(numberWordDocuments));
        return  IDF+1;

    }


    private void relevanceWordDocument(String word, Double IDF, Boolean imageSearch) throws SQLException {
        String sql_request=null;
        if(imageSearch)
        {
            sql_request = "SELECT * FROM "+DataBase.imageWordTableName+" where "+ WordImageLabels.WORD_NAME +
                    "= '"+word+"';";

        }
        else
        {
            sql_request = "SELECT * FROM "+DataBase.documentWordTableName+" where "+ WordDocumentLabels.WORD_NAME +
                    "= '"+word+"';";

        }
        ResultSet rs = null;
        try {
            rs = db.selectQuerydb(sql_request);
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
        if(imageSearch)
        {
            while (rs.next())
            {
                String hyper_link=rs.getString(WordImageLabels.IMAGE_HYPER_LINK);
                Float tf=rs.getFloat(WordImageLabels.TERM_FREQUENCY);
                Double score=rs.getFloat(WordImageLabels.SCORE)+ tf*IDF;
                String updateScore="UPDATE "+DataBase.imageWordTableName+
                        " SET "+WordImageLabels.SCORE +" = "+score +
                        " WHERE "+WordImageLabels.WORD_NAME +" = '"+word+"' and "+WordImageLabels.IMAGE_HYPER_LINK+
                        " = '"+hyper_link+" ';";
                try {
                    db.updatedb(updateScore);
                } catch (SQLException throwables) {
                    throwables.printStackTrace();
                }

            }

        }
        else
        {
            while (rs.next())
            {
                String hyper_link=rs.getString(WordDocumentLabels.DOCUMENT_HYPER_LINK);
                Float tf=rs.getFloat(WordDocumentLabels.TERM_FREQUENCY);
                Float popularity=getDocumentPopularity(hyper_link);
                Double score=rs.getFloat(WordDocumentLabels.SCORE)+ tf*IDF*popularity;
                String updateScore="UPDATE "+DataBase.documentWordTableName+
                        " SET "+WordDocumentLabels.SCORE +" = "+score +
                        " WHERE "+WordDocumentLabels.WORD_NAME +" = '"+word+"' and "+WordDocumentLabels.DOCUMENT_HYPER_LINK+
                        " = '"+hyper_link+" ';";
                try {
                    db.updatedb(updateScore);
                } catch (SQLException throwables) {
                    throwables.printStackTrace();
                }

            }
        }

    }

    private Float getDocumentPopularity(String hyperLink) throws SQLException {
        String sql_request = "SELECT "+ DocumentLabels.POPULARITY + " FROM "+DataBase.documentTableName+" Where "+DocumentLabels.HYPER_LINK+
                " = '"+hyperLink+"';";
        ResultSet rs = null;
        try {
            rs = db.selectQuerydb(sql_request);
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
        rs.next();
        Float popularity=rs.getFloat(DocumentLabels.POPULARITY);
        return popularity;
    }

    void clearScores(Boolean imageSearch)
    {
        String sql_request=null;
        if(imageSearch)
        {
            sql_request = "UPDATE "+DataBase.imageWordTableName+" SET "+WordImageLabels.SCORE+
                    " = 0;";
        }
        else
        {
            sql_request = "UPDATE "+DataBase.documentWordTableName+" SET "+WordDocumentLabels.SCORE+
                    " = 0;";
        }
        try {
            db.updatedb(sql_request);
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
    }

    Float getScore(String document, Boolean imageSearch) throws SQLException {
        String sql_request=null;
        if(imageSearch)
        {
            sql_request="SELECT SUM("+WordDocumentLabels.SCORE+") FROM "+DataBase.imageWordTableName+
                    " where "+WordImageLabels.IMAGE_HYPER_LINK+" = '"+document+"';";
        }
        else
        {
            sql_request="SELECT SUM("+WordDocumentLabels.SCORE+") FROM "+DataBase.documentWordTableName+
                    " where "+WordDocumentLabels.DOCUMENT_HYPER_LINK+" = '"+document+"';";

        }
        ResultSet rs = null;
        rs = db.selectQuerydb(sql_request);
        rs.next();
        return rs.getFloat(1);
    }

    //private
    public ArrayList<DocumentResult> makeRank(ArrayList<String> search_list,Boolean imageSearch) throws SQLException {
        clearScores(imageSearch);
        ArrayList<String> criticalDocuments=null;
        for(int i=0;i<search_list.size();i++)
        {
            String word = search_list.get(i);
            String phrase[] = word.split(" ");
            if(phrase.length>1)//phrase searching logic
            {
                criticalDocuments=phraseSearching(phrase,imageSearch);//handle multiple phrase searching
                continue;
            }
            if(!wordExists(word,imageSearch)) {
                System.out.println("word doesnt exist");
                continue;
            }
            else{
                System.out.println("word does exist");
            }
            Double IDF=getIDF(word,imageSearch);
            relevanceWordDocument(word,IDF,imageSearch);
        }
        ArrayList<sortDocuments> sortedDocuments=null;
        if(criticalDocuments!=null)
        {
            sortedDocuments=new ArrayList<>();
            //TODO: get the score of all of them with each word and accumelate the results
            for(String document :criticalDocuments)
            {
                Float score=getScore(document,imageSearch);
                sortedDocuments.add(new sortDocuments(document,score));
            }
            sortedDocuments.sort(Comparator.comparing(sortDocuments::getScore));
        }
        else
        {
            sortedDocuments = getHighestScoresDocuments(100,imageSearch);
        }
        ArrayList<DocumentResult> documentResult = new ArrayList<>();
        for(sortDocuments doc : sortedDocuments)
        {
            String hyper_link=doc.hyper_link;
            String brief = getBrief(hyper_link,imageSearch);
            String title = getTitle(hyper_link,imageSearch);
            documentResult.add(new DocumentResult(hyper_link,brief,title));
        }
        return documentResult;
    }

    private String getTitle(String hyper_link, Boolean imageSearch) throws SQLException {
        String sql_request=null;
        if(imageSearch)
        {
            return "garbage";

        }
        else
        {
            sql_request =" SELECT "+DocumentLabels.TITLE+" FROM "+DataBase.documentTableName+" WHERE "+DocumentLabels.HYPER_LINK+
                    " ='"+hyper_link+"';";

        }
        ResultSet rs = null;
        rs = db.selectQuerydb(sql_request);
        rs.next();
        if(imageSearch)
            return rs.getString(ImageLabels.CAPTION);
        else
            return rs.getString(DocumentLabels.TITLE);


    }

    private String getBrief(String hyper_link, Boolean imageSearch) throws SQLException {
        String sql_request=null;
        if(imageSearch)
        {
            sql_request =" SELECT "+ImageLabels.CAPTION+" FROM "+DataBase.imageTableName+" WHERE "+ImageLabels.IMAGE_URL+
                    " ='"+hyper_link+"';";
        }
        else
        {
            sql_request =" SELECT "+DocumentLabels.STREAM_WORDS+" FROM "+DataBase.documentTableName+" WHERE "+DocumentLabels.HYPER_LINK+
                    " ='"+hyper_link+"';";
        }
        ResultSet rs = null;
        rs = db.selectQuerydb(sql_request);
        rs.next();
        if(imageSearch)
        {
            String caption = rs.getString(ImageLabels.CAPTION);
            return caption;
        }
        else {
            String stream_words = rs.getString(DocumentLabels.STREAM_WORDS);
            String brief = null;
            if (stream_words.length() > 1000) {
                brief = stream_words.substring(0, 1000);
            } else {
                brief = stream_words;
            }
            return brief;
        }
    }

    private ArrayList<String> phraseSearching(String[] phrase, Boolean imageSearch) throws SQLException {
        /*
    //for every returned web page url search for the index of the word
    //increment every one of them by one and go to the next word
    //for that url see the positions of the next word and if it matches one of the numbers in that array
    //save that place
    //if not delete that place
    //if that array becomes empty then that document is bad so don't take it
    //if number of words reach it's end so that Document is good so take it please
         */
        ArrayList<String> criticalDocuments = new ArrayList<>();
        String word=phrase[0];
        ArrayList<String> Documents= getDocuments(word);
        for (String document : Documents)
        {
            word=phrase[0];
            ArrayList<Integer> positions=getPositions(document,word);
            for(int i=1 ; i<phrase.length;i++)
            {
                for(int itr=0;itr<positions.size();itr++)
                {
                    positions.set(itr, positions.get(itr)+1);
                }
                word=phrase[i];
                ArrayList<Integer> newPositions=getPositions(document,word);
                for(int second=0;second<positions.size();second++) {
                    Boolean concatinate=false;
                    for (int first = 0; first < newPositions.size(); first++) {
                        if (newPositions.get(first) == positions.get(second)) {
                            concatinate=true;
                            continue;
                        }
                    }
                    if(!concatinate)
                    {
                        positions.remove(second);
                    }
                }

                }
            if(positions.size()>0)
            {
                criticalDocuments.add(document);
            }
            }
        return  criticalDocuments;
        }


    private ArrayList<Integer> getPositions(String document,String word) throws SQLException {
        ArrayList<Integer> pos=new ArrayList<>();
        String sql_request="SELECT "+ WordIndexLabels.POSITION+" FROM "+DataBase.indexTableName+" WHERE "+
                WordIndexLabels.DOCUMENT_HYPER_LINK+" = '"+document +"' AND "+WordIndexLabels.WORD_NAME+
                " = '"+word+"';";
        ResultSet rs = null;
        rs = db.selectQuerydb(sql_request);
        while (rs.next())
        {
            pos.add(rs.getInt(WordIndexLabels.POSITION));
        }
        return pos;
    }

    private ArrayList<String> getDocuments(String word) throws SQLException {
        ArrayList<String> Documents= new ArrayList<>();
        String sql_request="SELECT "+WordDocumentLabels.DOCUMENT_HYPER_LINK+" FROM "+DataBase.documentWordTableName+
                " WHERE "+WordDocumentLabels.WORD_NAME+" = '"+word+" ';";
        ResultSet rs = null;
        rs = db.selectQuerydb(sql_request);
        while (rs.next())
        {
            Documents.add(rs.getString(WordDocumentLabels.DOCUMENT_HYPER_LINK));
        }
        return Documents;
    }

    private ArrayList<sortDocuments> getHighestScoresDocuments(Integer numberDocuments, Boolean imageSearch) throws SQLException {
        ArrayList<sortDocuments> heightsScores=new ArrayList<>();
        String sql_request=null;
        if(imageSearch)
        {
            sql_request = "SELECT "+ WordImageLabels.IMAGE_HYPER_LINK+ " FROM "+DataBase.imageWordTableName+
                    " WHERE "+WordImageLabels.SCORE+" > 0 ORDER BY "+WordImageLabels.SCORE +" DESC LIMIT "+numberDocuments+";";
        }
        else
        {
            sql_request = "SELECT "+ WordDocumentLabels.DOCUMENT_HYPER_LINK+ " FROM "+DataBase.documentWordTableName+
                    " WHERE "+WordDocumentLabels.SCORE+" > 0 ORDER BY "+WordDocumentLabels.SCORE +" DESC LIMIT "+numberDocuments+";";
        }
        ResultSet rs = null;
        rs = db.selectQuerydb(sql_request);
        while (rs.next())
        {
            if(imageSearch)
            {
                heightsScores.add(new sortDocuments(rs.getString(WordImageLabels.IMAGE_HYPER_LINK),0f));
            }
            else
            {
                heightsScores.add(new sortDocuments(rs.getString(WordDocumentLabels.DOCUMENT_HYPER_LINK),0f));
            }

        }
        return heightsScores;
    }

    public ArrayList<String> getSuggestions(String search_query) throws SQLException {
        ArrayList<String> suggestions=new ArrayList<>();
        String sql_statement = "SELECT "+SUGGESTION+" FROM "+DataBase.suggestionTableName+" where "+SUGGESTION +" REGEXP '"+search_query+"?';";
        //SELECT name FROM student_tbl WHERE name REGEXP '^sa';
        ResultSet rs = null;
        rs = db.selectQuerydb(sql_statement);
        while (rs.next()) {
            suggestions.add(rs.getString(SUGGESTION));
        }
        return suggestions;
    }
    public static final String SUGGESTION= "search_query";
    public void addSearchQuery(String search_query) {
        String sql_statement="INSERT INTO "+DataBase.suggestionTableName+"("+SUGGESTION+") values ('"+search_query+"');";
        try {
            db.updatedb(sql_statement);
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
    }


    public class DocumentResult
    {
        public String hyper_link;
        public String title;

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

        public DocumentResult(String hyper_link, String title, String brief) {
            this.hyper_link = hyper_link;
            this.title = title;
            this.brief = brief;
        }

        public String brief;
    }

    public class sortDocuments
    {
        public String getHyper_link() {
            return hyper_link;
        }

        public void setHyper_link(String hyper_link) {
            this.hyper_link = hyper_link;
        }

        public Float getScore() {
            return score;
        }

        public void setScore(Float score) {
            this.score = score;
        }

        public String hyper_link;
        public Float score;
        public sortDocuments(String hyper_link,Float score)
        {
            this.hyper_link=hyper_link;
            this.score=score;
        }

    }

}
