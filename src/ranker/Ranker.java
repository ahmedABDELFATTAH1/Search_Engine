package ranker;

import data_base.DataBase;
import data_base.WordDocument;
import data_base.WordDocumentLabels;
import data_base.WordLabels;

import java.awt.*;
import java.sql.*;
import java.util.ArrayList;

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
            Float score=rs.getFloat(WordDocumentLabels.SCORE)+ tf*IDF;
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

    void clearScores()
    {
        String sql_request = "UPDATE "+DataBase.documentWordTableName+" SET '"+WordDocumentLabels.SCORE+
                "' = 0";
        try {
            db.updatedb(sql_request);
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
    }

    //private
    public void makeRank(ArrayList<String> search_list) throws SQLException {
        //Something a simple as UPDATE table1 SET column1=1; should do it.
        //reset scores
        clearScores();
        for(int i=0;i<search_list.size();i++)
        {
            String word = search_list.get(i);
            if(!wordexists(word))
                System.out.println("word doesnt exist");
            else{
                System.out.println("word does exist");
            }
            Float IDF=getIDF(word);
            System.out.println(IDF);
            relevanceWordDocument(word,IDF);
        }


    }
    public  static void main(String ar[]) throws SQLException {
        ArrayList<String> str = new ArrayList<>();
        str.add("football");
        Ranker ranker= new Ranker();
        ranker.makeRank(str);


    }

}
