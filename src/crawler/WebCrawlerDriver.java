/**
 * AUTHOR : MOHAMED-MOKHTAR
 */

package crawler;
import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Vector;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
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
    static private Map<String, Integer> visitedLinks = new HashMap<String, Integer>();
    static private Map<String, HashSet<String>> forbiddenLinks = new HashMap<String, HashSet<String>>();
    static private Map<String, Integer> hostsPopularity = new HashMap<String, Integer>();
    static private Object pendingLinksLock = new Object();  
    static private Object visitedLinksLock = new Object();  
    static private Object forbiddenLinksLock = new Object();
    static private Map<String, Object> forbiddenLinksLocksMap = new HashMap<String, Object>();
    static private Object hostsPopularityLock = new Object();
    static private Connection dbConnection ;
	static Integer pagesCount = 0;
	static boolean DEBUG = true;
    /**
	 * @param args
     * @throws ClassNotFoundException 
     * @throws IOException 
	 */
	public static void main(String[] args) throws ClassNotFoundException, IOException {
        try {
        	String dbUser = "root";
        	String dbPassword ="";
        	String dbName = "mydatabase";
        	String dbPort = "3306";
        	String dbHost = "localhost";
            Class.forName("com.mysql.cj.jdbc.Driver");
			dbConnection = DriverManager.getConnection("jdbc:mysql://"+ dbHost +":"+ dbPort +"/" + dbName + "?useUnicode=true&useJDBCCompliantTimezoneShift=true&useLegacyDatetimeCode=false&serverTimezone=UTC",dbUser,dbPassword);
			readSeeds("seed.txt");
			initCrawlingSession();
			readThreadsCount();
			for (int i = 0 ; i < threadsCount ; ++i) {
				Thread crawlerThread = new Thread(new WebCrawler(		dbConnection,
																		pendingLinks,	 pendingLinksLock,
																		visitedLinks,	 visitedLinksLock,
																		forbiddenLinks,  forbiddenLinksLock,
																		hostsPopularity, hostsPopularityLock,
																		pagesCount,DEBUG,forbiddenLinksLocksMap
																		));	
				crawlerThread.setName("#"+i);	
				crawlerThread.start();
				}

		} 
        catch (SQLException e) {
        	if(DEBUG)
			e.printStackTrace();
		}
	}
	static private String hashMaker(String hyperlinksHash) {
		if (hyperlinksHash.length() <= 30)
			return hyperlinksHash;
		int startIndex =  hyperlinksHash.length() / 2;
		for (int i=startIndex;i<startIndex+15;++i) {
			hyperlinksHash.charAt(i);
		}
		//hyperlinksHash.substring(0,piHasher);
		return "";
	}
	static private void initCrawlingSession() throws SQLException {
		Statement statment = dbConnection.createStatement();
        ResultSet  resultSet;
        
        resultSet = statment.executeQuery("select host_name, host_ref_times from  hosts_popularity");
        while (resultSet.next())
        	hostsPopularity.put(resultSet.getString("host_name"), resultSet.getInt("host_ref_times"));
        
        resultSet = statment.executeQuery("select url,check_sum from crawler_urls where is_crawled = 1 ");
        while (resultSet.next()) {
        	visitedLinks.put(resultSet.getString("url"),resultSet.getInt("check_sum"));
        	pagesCount ++ ;
        }
        resultSet = statment.executeQuery("select url_id , url from crawler_urls where is_crawled = 0 ;");
        while (resultSet.next()) {
        	pendingLinks.add(resultSet.getString("url"));
        	pagesCount ++ ;
        }
        if (pagesCount >= WebCrawler.MAX_DOCS_COUNT)
        	pagesCount = 0;
        resultSet = statment.executeQuery("select url from forbidden_urls ;");
        while (resultSet.next()) {
        	URL url;
			try {
				url = new URL(resultSet.getString("url"));
				String host = url.getHost();
	        	if (!(forbiddenLinks.containsKey(host))) {
					forbiddenLinks.put(host, new HashSet<String>());
					forbiddenLinksLocksMap.put(host,new Object());
	        	}
	        	forbiddenLinks.get(host).add(resultSet.getString("url"));
			} catch (MalformedURLException e) {
				if(DEBUG)
					e.printStackTrace();
			} catch (SQLException e) {
				if(DEBUG)
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
					if(DEBUG)
						System.out.println("Seed is already exist for this url : " + seedUrl );                
				}
            }
            bufferedReader.close();         
    	}
        catch(FileNotFoundException ex) {
    		if(DEBUG)
    			System.out.println("Unable to open file '" + seedUrlsPath + "'");                
        }
        catch(IOException ex) {
        	if(DEBUG)	
        		System.out.println("Error reading file '" + seedUrlsPath + "'");                  
        }
	}
}
