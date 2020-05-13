package ranker;

import data_base.*;

import java.sql.*;
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
    private Boolean wordexists(String word) throws SQLException {
        String sql_request= "SELECT * FROM "+ DataBase.wordTableName+" WHERE "+ WordLabels.WORD_NAME +" = '"+ word+"'" ;
        ResultSet rs = null;
        try {
            rs = db.selectQuerydb(sql_request);
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
       return rs.next();

    }

    private float getIDF(String word) throws SQLException {
        String sql_request = "SELECT "+WordLabels.INVERSE_DOCUMENT_FREQUENCY+" FROM " + DataBase.wordTableName + " WHERE " + WordLabels.WORD_NAME + " = '" + word + "'";
        ResultSet rs = null;
        try {
            rs = db.selectQuerydb(sql_request);
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
        rs.next();
        return rs.getFloat(WordLabels.INVERSE_DOCUMENT_FREQUENCY);
    }


    private void relevanceWordDocument(String word,Float IDF) throws SQLException {
        String sql_request = "SELECT * FROM "+DataBase.documentWordTableName+" where "+ WordDocumentLabels.WORD_NAME +
                "= '"+word+"';";
        ResultSet rs = null;
        try {
            rs = db.selectQuerydb(sql_request);
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }

        while (rs.next())
        {
            String hyper_link=rs.getString(WordDocumentLabels.DOCUMENT_HYPER_LINK);
            Float tf=rs.getFloat(WordDocumentLabels.TERM_FREQUENCY);
            Float popularity=getDocumentPopularity(hyper_link);
            Float score=rs.getFloat(WordDocumentLabels.SCORE)+ tf*IDF*popularity;
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

    void clearScores()
    {
        String sql_request = "UPDATE "+DataBase.documentWordTableName+" SET "+WordDocumentLabels.SCORE+
                " = 0;";
        try {
            db.updatedb(sql_request);
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
    }

    Float getScore(String document) throws SQLException {
        String sql_request="SELECT SUM("+WordDocumentLabels.SCORE+") FROM "+DataBase.documentWordTableName+
                " where "+WordDocumentLabels.DOCUMENT_HYPER_LINK+" = '"+document+"';";
        ResultSet rs = null;
        rs = db.selectQuerydb(sql_request);
        rs.next();
        return rs.getFloat(1);
    }

    //private
    public ArrayList<DocumentResult> makeRank(ArrayList<String> search_list) throws SQLException {
        //Something a simple as UPDATE table1 SET column1=1; should do it.
        //reset scores
        clearScores();
        ArrayList<String> ciriticalDocumnets=null;
        for(int i=0;i<search_list.size();i++)
        {
            String word = search_list.get(i);
            String phrase[] = word.split(" ");
            if(phrase.length>1)//phrase searching logic
            {
                ciriticalDocumnets=phraseSearching(phrase);//handle multiple phrase searching

            }
            if(!wordexists(word)) {
                System.out.println("word doesnt exist");
                continue;
            }
            else{
                System.out.println("word does exist");
            }
            Float IDF=getIDF(word);
            System.out.println(IDF);
            relevanceWordDocument(word,IDF);
        }
        ArrayList<sortDocuments> sortedDocuments=null;
        if(ciriticalDocumnets!=null)
        {
            sortedDocuments=new ArrayList<>();
            //TODO: get the score of all of them with each word and accumelate the results
            for(String document :ciriticalDocumnets)
            {
                Float score=getScore(document);
                sortedDocuments.add(new sortDocuments(document,score));
            }
            sortedDocuments.sort(Comparator.comparing(sortDocuments::getScore));
        }
        else
        {
            sortedDocuments = getHighestScoresDocuments(100);
        }
        ArrayList<DocumentResult> documentResult = new ArrayList<>();
        for(sortDocuments doc : sortedDocuments)
        {
            String hyper_link=doc.hyper_link;
            System.out.println(hyper_link);
            String brief = getBrief(hyper_link);
            String title = getTitle(hyper_link);
            documentResult.add(new DocumentResult(hyper_link,brief,title));
        }
        return documentResult;
    }

    private String getTitle(String hyper_link) throws SQLException {
        String sql_request =" SELECT "+DocumentLabels.TITLE+" FROM "+DataBase.documentTableName+" WHERE "+DocumentLabels.HYPER_LINK+
                " ='"+hyper_link+"';";
        ResultSet rs = null;
        rs = db.selectQuerydb(sql_request);
        rs.next();
        return rs.getString(DocumentLabels.TITLE);
    }

    private String getBrief(String hyper_link) throws SQLException {

        String sql_request =" SELECT "+DocumentLabels.STREAM_WORDS+" FROM "+DataBase.documentTableName+" WHERE "+DocumentLabels.HYPER_LINK+
                " ='"+hyper_link+"';";
        ResultSet rs = null;
        rs = db.selectQuerydb(sql_request);
        rs.next();
        String stream_words=rs.getString(DocumentLabels.STREAM_WORDS);
        String brief = null;
        if(stream_words.length()>1000)
        {
            brief=stream_words.substring(0,1000);
        }
        else
        {
            brief=stream_words;
        }
        return brief;
    }

    private ArrayList<String> phraseSearching(String[] phrase) throws SQLException {
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
        String sql_request="SELECT "+WordIndexLabels.POSITION+" FROM "+DataBase.indexTableName+" WHERE "+
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

    private ArrayList<sortDocuments> getHighestScoresDocuments(Integer numberDocuments) throws SQLException {
        ArrayList<sortDocuments> heightsScores=new ArrayList<>();
        String sql_request = "SELECT "+ WordDocumentLabels.DOCUMENT_HYPER_LINK+ " FROM "+DataBase.documentWordTableName+
                " WHERE "+WordDocumentLabels.SCORE+" > 0 ORDER BY "+WordDocumentLabels.SCORE +" DESC LIMIT "+numberDocuments+";";
        ResultSet rs = null;
        rs = db.selectQuerydb(sql_request);
        while (rs.next())
        {
            heightsScores.add(new sortDocuments(rs.getString(WordDocumentLabels.DOCUMENT_HYPER_LINK),0f));
        }
        return heightsScores;
    }

    public  static void main(String ar[]) throws SQLException {
        ArrayList<String> str = new ArrayList<>();
        str.add("webb search engine");
        Ranker ranker= new Ranker();
        ArrayList<DocumentResult> results=ranker.makeRank(str);
        for(DocumentResult result : results)
        {
            System.out.println(result.hyper_link);
            System.out.println(result.brief);
            System.out.println(result.title);
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
