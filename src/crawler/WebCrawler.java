/**
 * AUTHOR : MOHAMED-MOKHTAR
 */

package crawler;
import java.io.*;
import java.net.*;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;
import java.util.Scanner;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import com.mysql.cj.protocol.Resultset;
import java.sql.*;

@SuppressWarnings("unused")
class WebCrawler implements Runnable {
    final static private int MAX_DOCS_COUNT = 5000;
    final static private int SLEEP_SCHEDULE_PERIOD_IN_MINUTES = 60; 	
	private LinkedList<String> pendingLinks;
	private Set<String> visitedLinks;
	private Map<String, HashSet<String>> forbiddenLinks;
    private Map<String, Integer> hostsPopularity ;
    private java.sql.Connection dbConnection;
    private Object pendingLinksLock;  
    private Object visitedLinksLock;  
    private Object forbiddenLinksLock;
    private Object hostsPopularityLock;  
    
	public WebCrawler(java.sql.Connection dbConnection,
			LinkedList<String> pendingLinks,Object pendingLinksLock,
			Set<String> visitedLinks, Object visitedLinksLock,
			Map<String, HashSet<String>> forbiddenLinks, Object forbiddenLinksLock,
			Map<String, Integer> hostsPopularity, Object hostsPopularityLock) {
		this.dbConnection = dbConnection;
		this.pendingLinks = pendingLinks;
		this.visitedLinks = visitedLinks;
		this.forbiddenLinks = forbiddenLinks;
		this.hostsPopularity = hostsPopularity;
		this.pendingLinksLock = pendingLinksLock;
		this.visitedLinksLock = visitedLinksLock;
		this.forbiddenLinksLock = forbiddenLinksLock;
		this.hostsPopularityLock = hostsPopularityLock;
	}
	private void visitLink() {
		String link = popPendingLink();
		if (link == null || link.isEmpty() )
			return ;
        try {
        	URL url = new URL(link);
			String host = url.getHost();
			HttpURLConnection httpConnection = (HttpURLConnection) url.openConnection();
			if (httpConnection.getContentType() == null || !(httpConnection.getContentType().contains("text/html"))|| httpConnection.getResponseCode() != 200 )
				return ;
			parseRobots(host);
			if (isForbiddenLink(host, link)) {
				return ;
			}
			addCurrentlyVisiting(link);
			Connection connection = Jsoup.connect(link);
			Document doc = connection.userAgent("*").get();
	        Elements links = doc.getElementsByTag("a");
            for (Element linksIterator : links) {
                if (!linksIterator.attr("href").startsWith("#")) {
                    String newLink = linksIterator.attr("abs:href");
                    newLink = normalizeUrl(newLink,host);
                    if (!(newLink.isEmpty()))
                    	addPendingLink(newLink);
            	}
        	}
            incrementHostPopularity(host);
			addVisitedLink(link);
	        System.out.println(Thread.currentThread().getName() + " has visited "+ link );

        }
        catch (IOException e) {
			System.out.println("Faild to visit url : " + link);
		}
	}
	private void addPendingLink(String link){
		synchronized (pendingLinksLock) {                
			if (!(pendingLinks.contains(link))) {
				synchronized (visitedLinksLock) {
					if (!(visitedLinks.contains(link))){
						pendingLinks.add(link);
						String pendingLinksQuery = " INSERT INTO `crawler_urls` (`url`) VALUES ('" + link + "') ;" ;
						executeNonQuery(pendingLinksQuery);
					}
				}
			}
		}
	}
	private void addVisitedLink(String url){
		int id ; 
		synchronized (visitedLinksLock) {
			//String addVisitedLinkQuery = " UPDATE `crawler_urls` SET `is_crawled`= 1 , `revisit_priporty`=0 , , `hyperlinks_hash`=[value-5] WHERE url = `"+ url + "`" ;
			String addVisitedLinkQuery = " UPDATE `crawler_urls` SET `is_crawled`= 1 , `revisit_priporty`= 0  WHERE url = '"+ url + "' ; " ;
			executeNonQuery(addVisitedLinkQuery);
			}
	}

	private void addCurrentlyVisiting (String url){
		synchronized (visitedLinksLock) {
			visitedLinks.add(url);
		}
	}
	private void addForbiddenLink(String host, String url){
		url = url.toLowerCase();
		synchronized (forbiddenLinksLock) {
			if (!(forbiddenLinks.containsKey(host))) {
				forbiddenLinks.put(host, new HashSet<String>());
			}
			if( !(forbiddenLinks.get(host).contains(url)) ){
				forbiddenLinks.get(host).add(url);
				String addForbiddenLinkQuery = " INSERT INTO `forbidden_urls` (`url`) VALUES ('" + url + "') ;" ;
				executeNonQuery(addForbiddenLinkQuery);
				}
			}
		}
	private String popPendingLink(){
		String url = null;
		synchronized (pendingLinksLock) {                
			if (!pendingLinks.isEmpty()) {
				url = pendingLinks.pop();
				}
		}
		return url;
	}
	private boolean isForbiddenLink(String host,String url) {
		boolean isForbidden = false;
		synchronized (forbiddenLinksLock) {
			if (forbiddenLinks.containsKey(host)) {
				Iterator<String> it = forbiddenLinks.get(host).iterator();
				while (it.hasNext()) {
					if(url.startsWith(it.next())) {
						isForbidden = true;
							break;
						}
				}
			}
		}
		return isForbidden;
	}
	private void saveHtmlDocument(String document,String url) throws IOException
	{
		String documnetPath = "./public/crawler/saved_docs/" + url +".txt";
        try {
        	FileWriter fileWriter = new FileWriter(documnetPath);
        	BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);
        	bufferedWriter.write(document);
        	bufferedWriter.close();
        }
        catch(IOException ex) {
        	System.out.println("Error writing to file '"+ url + "'");
    	}
	}
	private String normalizeUrl(String url,String host ) {
		if(!(host.contains("youtube.com")))
			url = url.toLowerCase();
		if (url.contains("onclick=") || url.contains("mailto:") || url.startsWith("#") || url.startsWith(" ")) {
			return "";
		}
		if (url.startsWith("//")) {
			url = "https:" + url;
		}
		else if (url.startsWith("/") || url.startsWith("?")) {
			url = host + url;
		}
		while (url.endsWith("?")) {
			url = url.substring(0,url.length() - 1);
		}
		if (!(url.startsWith("https://") || url.startsWith("https://"))) {
			return "";
		}
		url.replaceAll("/index.html", "");
		url.replaceAll("/index.htm", "");
		url.replaceAll("default.asp","");
		url.replaceAll(":80","");
		if (!(url.endsWith("/"))) {
			url = url + "/" ; 
		}
        return url;
    }
	private void parseRobots(String host) throws IOException
	{
		synchronized (forbiddenLinksLock) {
		if (forbiddenLinks.containsKey(host))
			return;
		}
		try {
		boolean start = false;
		String link = "https://" + host + "/robots.txt" ;
		URL url = new URL(link); 
		HttpURLConnection httpConnection = (HttpURLConnection) url.openConnection();
		httpConnection.setRequestMethod("GET");
		int responseCode = httpConnection.getResponseCode();
		if (responseCode != 200)
			return;
		String tempLink = new String("");
		String line = new String("");
		Scanner scanner = new Scanner(url.openStream());
		while (scanner.hasNext()) {
			line = scanner.nextLine();
			line = line.replace(" ", "");
			if(line.equals("User-agent:*"))
				start = true;
			else if (line.contains("User-agent"))
				start = false;
			if (start)
			{
				if(line.startsWith("Allow:"))
				{
					tempLink ="https://"+host+line.substring(6);
					tempLink = normalizeUrl(tempLink,host);
					addPendingLink(tempLink);
				}
				else if(line.startsWith("Disallow:"))
				{
					tempLink = "https://"+host+line.substring(9);
					addForbiddenLink(host, tempLink);
				}
			}
		}
		scanner.close();
		}
		catch (FileNotFoundException e) {
			System.out.println("Robots.txt file for host" + host + "could not be accessed right now or does not exist.");
			e.printStackTrace();
		}
	}
	private void incrementHostPopularity(String host) {
		synchronized(hostsPopularityLock) {
			if (!(hostsPopularity.containsKey(host))) {
				hostsPopularity.put(host, 1);
				String hostsPopularityQuery = "INSERT INTO `hosts_popularity` (`host_name`, `host_ref_times`) VALUES ( '" + host + "' , 1)" ;
				executeNonQuery(hostsPopularityQuery);

			}
			else {
				int hostPopularityCount = hostsPopularity.get(host) + 1;
				hostsPopularity.put(host,hostPopularityCount ) ;
				String hostsPopularityQuery = " UPDATE `hosts_popularity` SET `host_ref_times`= "+ hostPopularityCount +" WHERE `host_name` = '"+ host + "' ;" ;
				executeNonQuery(hostsPopularityQuery);

			}
		}
		return ;
	}
	private int executeNonQuery(String Query) {
        int isSuccess = 0;
        try {
            Statement stmt = dbConnection.createStatement();
            isSuccess = stmt.executeUpdate(Query);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return isSuccess;
    } 
    private ResultSet executeReader(String Query) {
        try {
            Statement stmt = dbConnection.createStatement();
            ResultSet rs = stmt.executeQuery(Query);
            return rs;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }	
    private void doWork(){
		while(true) {
			visitLink();
			try {
				if(visitedLinks.size() == MAX_DOCS_COUNT) {
					Thread.sleep(SLEEP_SCHEDULE_PERIOD_IN_MINUTES*60*1000);
				}	
			}
			catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}
	@Override 
	public void run() {
		doWork();
	}

}