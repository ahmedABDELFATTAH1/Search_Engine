package ranker;

import data_base.DataBase;
import data_base.WordLabels;

import java.sql.*;
import java.util.ArrayList;

public class Ranker {
    private Connection connection=null;
    DataBase db = null;
    public Ranker(Connection conn)
    {
        connection=conn;
        db = new DataBase();
    }
    private Boolean wordexists(String word)  {
        String sql_request= "SELECT * FROM "+ DataBase.wordTableName+" WHERE "+ WordLabels.WORD_NAME +" = "+ word ;
        ResultSet rs = null;
        try {
            rs = db.selectQuerydb(sql_request);
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
        try {
            rs.first();
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
        if(rs!=null)
            return true;
        else return false;
    }
    public void makeRank(ArrayList<String> search_list)
    {
        for(int i=0;i<search_list.size();i++)
        {
            String word = search_list.get(i);
            if(!wordexists(word))
                continue;
            //String sql_request= "SELECT * FROM "+ DataBase.documentWordTableName+" WHERE "++;

        }


    }

}
