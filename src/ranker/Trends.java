package ranker;
/*

import com.example.demo.data_base.DataBase;
import com.example.demo.data_base.TrendsLabels;
import edu.stanford.nlp.pipeline.*;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Properties;


public class Trends {
    StanfordCoreNLP pipeline=null;
    DataBase db = null;
    public Trends()
    {
        db = new DataBase();
        Properties props = new Properties();
        props.setProperty("annotators", "tokenize,ssplit,pos,lemma,ner");
        // set up pipeline
        pipeline = new StanfordCoreNLP(props);
        // make an example document
    }
    public ArrayList<String> getNames(String searchPhrase)
    {
        ArrayList<String> names=new ArrayList<>();
        CoreDocument doc = new CoreDocument(searchPhrase);
        // annotate the document
        pipeline.annotate(doc);
        // view results
        System.out.println("---");
        System.out.println("entities found");

        for (CoreEntityMention em : doc.entityMentions())
        {
            if(em.entityType().equals("PERSON")) {
                names.add(em.text());
            }
        }

        return names;
    }

    public ArrayList<TrendsData> famousPersons(String region) throws SQLException {
        ArrayList<TrendsData> trendsData=new ArrayList<>();
        String sql_request = "SELECT * FROM "+DataBase.trendsTableName+
                " WHERE "+TrendsLabels.REGION+"= '"+region+"' ORDER BY "+TrendsLabels.SEARCH_COUNT +" DESC LIMIT 10;";
        ResultSet rs = null;
        rs = db.selectQuerydb(sql_request);
        while (rs.next())
        {
            String name=rs.getString(TrendsLabels.PERSON_NAME);
            Integer count=rs.getInt(TrendsLabels.SEARCH_COUNT);
            TrendsData temp=new TrendsData(name,count);
            trendsData.add(temp);
        }
        return trendsData;
    }

    public class TrendsData
    {
        public TrendsData(String personName,Integer count)
        {
            this.personName=personName;
            this.count=count;
        }
        public String personName;
        public Integer count;
    }


    public void addFamousNames(String name, String region) throws SQLException {
        Integer count=getTrendCount(name,region);
        String sql_statement="INSERT INTO "+DataBase.trendsTableName+" ("+TrendsLabels.PERSON_NAME+","+TrendsLabels.REGION+
                ") VALUES ('"+name+"','"+region+"') ON DUPLICATE KEY UPDATE "+
                TrendsLabels.SEARCH_COUNT+"="+(count+1)+";";
        try {
            db.updatedb(sql_statement);
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }

    }

    private Integer getTrendCount(String name, String region) throws SQLException {
        Integer count =null;
        String sql_statement="SELECT "+ TrendsLabels.SEARCH_COUNT +" FROM "+DataBase.trendsTableName+
                " WHERE "+TrendsLabels.PERSON_NAME +" = '"+name+"' and "+TrendsLabels.REGION+
                " ='"+region+"';";
        ResultSet rs = null;
        rs = db.selectQuerydb(sql_statement);
        if(rs.next())
        {
            count=rs.getInt(1);
        }
        else{
            count =0;
        }
        return count;
    }
}

 */
