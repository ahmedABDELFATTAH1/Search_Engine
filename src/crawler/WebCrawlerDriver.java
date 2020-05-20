/**
 * AUTHOR : MOHAMED-MOKHTAR
 */

package crawler;
import java.io.*;
import java.net.*;
import java.util.Vector;
import java.util.Iterator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Scanner; 
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import org.jsoup.nodes.Element;
import com.mysql.cj.protocol.Resultset;
import java.sql.*;

@SuppressWarnings("unused")
public class WebCrawlerDriver {

    static private int threadsCount; 
    static private LinkedList<String> pendingLinks = new LinkedList<String>(); 
    static private Set<String> visitedLinks = new HashSet<String>();
    static private Map<String, HashSet<String>> forbiddenLinks = new HashMap<String, HashSet<String>>();
    static private Map<String, Integer> hostsPopularity = new HashMap<String, Integer>();
    static private Object pendingLinksLock = new Object();  
    static private Object visitedLinksLock = new Object();  
    static private Object forbiddenLinksLock = new Object();
    static private Object hostsPopularityLock = new Object();
    static private Connection dbConnection ;
    final static private boolean LOGGER = false;
    /**
	 * @param args
     * @throws ClassNotFoundException 
	 */
	public static void main(String[] args) throws ClassNotFoundException {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
			dbConnection = DriverManager.getConnection("jdbc:mysql://localhost:3306/search_engine?useUnicode=true&useJDBCCompliantTimezoneShift=true&useLegacyDatetimeCode=false&serverTimezone=UTC","root","12");
			readSeeds("seed.txt");
			initCrawlingSession();
			readThreadsCount();
			for (int i = 0 ; i < threadsCount ; ++i) {
				Thread crawlerThread = new Thread(new WebCrawler(		dbConnection,
																		pendingLinks,	 pendingLinksLock,
																		visitedLinks,	 visitedLinksLock,
																		forbiddenLinks,  forbiddenLinksLock,
																		hostsPopularity, hostsPopularityLock));	
				crawlerThread.setName("#"+i);	
				crawlerThread.start();
				}

		} 
        catch (SQLException e) {
			e.printStackTrace();
		}
	}
	static private void initCrawlingSession() throws SQLException {
		// Load pending links
		// Load visited links
		// Load forbidden links
		Statement statment = dbConnection.createStatement();
        ResultSet  resultSet;
        
        resultSet = statment.executeQuery("select host_name, host_ref_times from  hosts_popularity");
        while (resultSet.next())
        	hostsPopularity.put(resultSet.getString("host_name"), resultSet.getInt("host_ref_times"));
        
        resultSet = statment.executeQuery("select url from crawler_urls where is_crawled = 1 ");
        while (resultSet.next())
        	visitedLinks.add(resultSet.getString("url"));
        //resultSet = statment.executeQuery("select url_id , url from crawler_urls where is_crawled = 0 or revisit_priporty = 1 ;");
        resultSet = statment.executeQuery("select url_id , url from crawler_urls where is_crawled = 0 ;");
        while (resultSet.next())
        	pendingLinks.add(resultSet.getString("url"));
        

        resultSet = statment.executeQuery("select url from forbidden_urls ;");
        while (resultSet.next()) {
        	URL url;
			try {
				url = new URL(resultSet.getString("url"));
				String host = url.getHost();
	        	if (!(forbiddenLinks.containsKey(host)))
					forbiddenLinks.put(host, new HashSet<String>());
	        	forbiddenLinks.get(host).add(resultSet.getString("url"));
			} catch (MalformedURLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	static private void setThreadsCount(int count) 
	{
		if (count>0)
			threadsCount = count ;
		else
			threadsCount = 1;
	}
	static private void readThreadsCount()
	{
        Scanner scanner = new Scanner(System.in);
        System.out.print("Enter Threads Count : ");
        int count = scanner.nextInt();
        scanner.close();
        System.out.println("The threads count entered is : "+ count);
        setThreadsCount(count);
	}
	static private void readSeeds(String seedUrlsFilename)
	{
		String seedUrlsPath = "./public/crawler/" + seedUrlsFilename;
		String seedUrl = null;
		String insertSeedQuery;
        try {
            FileReader fileReader = new FileReader(seedUrlsPath);
            BufferedReader bufferedReader = new BufferedReader(fileReader);
            while((seedUrl = bufferedReader.readLine()) != null) {
            	if (! seedUrl.startsWith("http"))
            		seedUrl = "https://" + seedUrl;
            	insertSeedQuery = " INSERT INTO `crawler_urls` (`url`) VALUES ('" + seedUrl + "') ;" ;
                try {
                    Statement stmt = dbConnection.createStatement();
					stmt.executeUpdate(insertSeedQuery);
				} catch (SQLException e) {
					// TODO Auto-generated catch block
		            System.out.println("Seed is already exist for this url : " + seedUrl );                
				}
            }
            bufferedReader.close();         
    	}
        catch(FileNotFoundException ex) {
            System.out.println("Unable to open file '" + seedUrlsPath + "'");                
        }
        catch(IOException ex) {
            System.out.println("Error reading file '" + seedUrlsPath + "'");                  
        }
	}
}
