/**
 * AUTHOR : MOHAMED-MOKHTAR
 */

package crawler;
import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
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
    final static public int MAX_DOCS_COUNT = 5000;
    final static public int SLEEP_SCHEDULE_PERIOD_IN_HOURS = 24; 	
	private LinkedList<String> pendingLinks;
	private Map<String, Integer> visitedLinks;
	private Map<String, HashSet<String>> forbiddenLinks;
    private Map<String, Integer> hostsPopularity ;
    private java.sql.Connection dbConnection;
    private Object pendingLinksLock;  
    private Object visitedLinksLock;  
    private Object forbiddenLinksLock;
    private Object hostsPopularityLock;  
    private Integer pagesCount;
    private Map<String, Object> forbiddenLinksLocksMap;
    private Object forbiddenLinksLocksMapLock; 
    private boolean DEBUG;
	public WebCrawler(java.sql.Connection dbConnection,
			LinkedList<String> pendingLinks,Object pendingLinksLock,
			Map<String, Integer>visitedLinks, Object visitedLinksLock,
			Map<String, HashSet<String>> forbiddenLinks, Object forbiddenLinksLock,
			Map<String, Integer> hostsPopularity, Object hostsPopularityLock,
			Integer pagesCount , boolean DEBUG,Map<String, Object> forbiddenLinksLocksMap) {
		this.dbConnection = dbConnection;
		this.pendingLinks = pendingLinks;
		this.visitedLinks = visitedLinks;
		this.forbiddenLinks = forbiddenLinks;
		this.hostsPopularity = hostsPopularity;
		this.pendingLinksLock = pendingLinksLock;
		this.visitedLinksLock = visitedLinksLock;
		this.forbiddenLinksLock = forbiddenLinksLock;
		this.hostsPopularityLock = hostsPopularityLock;
		this.pagesCount = pagesCount;
		this.DEBUG = DEBUG;
		this.forbiddenLinksLocksMap = forbiddenLinksLocksMap;
	}
	private void visitLink() {
		String link = popPendingLink();
		if (link == null || link.isEmpty() )
			return ;
        try {
        	URL url = new URL(link);
			String host = url.getHost();
			validateUrlWorking(link);
			addCurrentlyVisiting(link);
			Connection connection = Jsoup.connect(link);
			Document doc = connection.userAgent("*").get();
	        Elements links = doc.getElementsByTag("a");
	        String hyperlinksHash ="";
	        int checkSum = 0;
            for (Element linksIterator : links) {
                if (!linksIterator.attr("href").startsWith("#")) {
                    String newLink = linksIterator.attr("abs:href");
                    newLink = normalizeUrl(newLink,host);
                    if (!(newLink.isEmpty())) {
                    	checkSum += (int) newLink.charAt(newLink.length() - 1) * (int) newLink.charAt(((int)(newLink.length() / 3))) ;
                    	checkSum += (int) newLink.charAt(((int)newLink.length() / 2)) * (int) newLink.charAt(((int)(newLink.length() / 4))) ;
            			URL newUrl = new URL(newLink);; 
            			String newHost = newUrl.getHost();
            			String newHostNorm = newHost.startsWith("www.") ? newHost.substring(4) : newHost;
            			parseRobots(newHost);
            			if (!(isForbiddenLink(newHost, newLink))) 
            			{
                        	addPendingLink(newLink);
                        	synchronized (pagesCount) {
                        		pagesCount++;	
							}
                        	if(!DEBUG)
                	        System.out.println(Thread.currentThread().getName() + " has crawled "+ newLink );
                            incrementHostPopularity(newHostNorm);
            			}
                	}
                }
        	}
			addVisitedLink(link,checkSum);
        }
        catch (IOException e) {
        	if (DEBUG) {
				e.printStackTrace();
			System.out.println("Faild to visit url : " + link);}
		}
	}
	private String hashMaker(String hyperlinksHash) {
		MessageDigest digest;
		byte[] encodedhash;
		try {
			digest = MessageDigest.getInstance("MD5");
			encodedhash = digest.digest(hyperlinksHash.getBytes());
			return new String(encodedhash);
		} catch (NoSuchAlgorithmException e) {
			if (DEBUG)
				e.printStackTrace();
			return "";
		}
	}
	private boolean validateUrlWorking(String link) {
    	URL url;
		try {
			url = new URL(link);
			String host = url.getHost();
			HttpURLConnection httpConnection = (HttpURLConnection) url.openConnection();
			if (httpConnection.getContentType() == null || !(httpConnection.getContentType().contains("text/html"))|| httpConnection.getResponseCode() != 200 )
				return false;
			else
				return true;
		} catch (IOException e) {
			if (DEBUG)
				e.printStackTrace();
			return false;
		}
	}
	private void addPendingLink(String link){
		synchronized (visitedLinksLock) {
			if (!(visitedLinks.containsKey(link))){
				synchronized (pendingLinksLock) {                
					if (!(pendingLinks.contains(link))) {
						pendingLinks.add(link);
						String pendingLinksQuery = " INSERT INTO `crawler_urls` (`url`) VALUES ('" + link + "') ON DUPLICATE KEY UPDATE revisit_priporty = 1;" ;
						executeNonQuery(pendingLinksQuery);
					}
				}
			}
		}
	}

	private void addVisitedLink(String url, int checkSum){
		synchronized (visitedLinksLock) {
			String oldCheckSumQuery = " SELECT `check_sum` from crawler_urls where url = '"+url+"' or `check_sum` = " + checkSum + " ;" ;
			int oldCheckSum = 0;
			boolean deleteThis = false;
			ResultSet rs = executeReader(oldCheckSumQuery);
			try {
				if (rs.next()) {
					oldCheckSum = rs.getInt("check_sum");
				}
				if (rs.next()) {
					deleteThis = true;
				}
			} catch (SQLException e) {
				if(DEBUG)
					e.printStackTrace();
			}
			if(deleteThis && checkSum != 0)
			{
				String deleteLinkQuery = " DELETE FROM `crawler_urls` WHERE url = '"+ url + "' ; " ;
				executeNonQuery(deleteLinkQuery);
				return;
			}
			int revisitPriporty =  oldCheckSum != checkSum ? 1 : 0;
			String addVisitedLinkQuery = " UPDATE `crawler_urls` SET `is_crawled`= 1 , `revisit_priporty`= "+ revisitPriporty  + ", `check_sum` = "+ checkSum +" WHERE url = '"+ url + "' ; " ;
			executeNonQuery(addVisitedLinkQuery);
			}
	}
	private void addCurrentlyVisiting (String url){
		synchronized (visitedLinksLock) {
			visitedLinks.put(url, 0);
		}
	}
	private void addForbiddenLink(String host, String url){
		url = url.toLowerCase();
		synchronized (forbiddenLinksLock) {
			if (!(forbiddenLinks.containsKey(host))) {
				forbiddenLinks.put(host, new HashSet<String>());
			}
		}
				forbiddenLinks.get(host).add(url);
				String addForbiddenLinkQuery = " INSERT INTO `forbidden_urls` (`url`) VALUES ('" + url + "') ;" ;
				executeNonQuery(addForbiddenLinkQuery);
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
		synchronized (forbiddenLinksLocksMap.get(host)) {
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
        catch(IOException e) {
        	if (DEBUG) {
				e.printStackTrace();
        	System.out.println("Error writing to file '"+ url + "'");
        	}
    	}
	}
	private String normalizeUrl(String url,String host ) {
		if ((url.contains("youtube.com"))) {
			return "";
		}
		if(url.length() >= 490)
			return "";
		url = url.toLowerCase();
		url.replace("'", "");
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
			if (forbiddenLinks.containsKey(host)) {
			synchronized (forbiddenLinksLocksMap.get(host)) {
				return;
			}
			}
			else {
				forbiddenLinks.put(host, new HashSet<String>());
				forbiddenLinksLocksMap.put(host,new Object());
			}
		}
		synchronized (forbiddenLinksLocksMap.get(host)){
		try {
		boolean start = false;
		String hostLink = "https://" + host + "/robots.txt" ;
		URL url = new URL(hostLink); 
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
				else if (start && line.contains("User-agent")) {
					start = false;
					break;
					}
				if (start && line.startsWith("Disallow:"))
				{
						tempLink = "https://"+host+line.substring(9);
						tempLink = normalizeUrl(tempLink, host);
						if(!(forbiddenLinks.get(host).contains(tempLink))) {
							forbiddenLinks.get(host).add(tempLink);
							String addForbiddenLinkQuery = " INSERT INTO `forbidden_urls` (`url`) VALUES ('" + tempLink + "') ON DUPLICATE KEY UPDATE url = '"+ tempLink+"' ;" ;
							executeNonQuery(addForbiddenLinkQuery);			
							}
				}
			}
			scanner.close();
		}
		catch (FileNotFoundException e) {
			if (DEBUG) {
				e.printStackTrace();
				System.out.println("Robots.txt file for host" + host + "could not be accessed right now or does not exist.");
			}
		}
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
        	if (DEBUG) {
        	System.out.println("Error occured due to : " +Query);
            e.printStackTrace();}
        }
        return isSuccess;
    } 
    private ResultSet executeReader(String Query) {
        try {
            Statement stmt = dbConnection.createStatement();
            ResultSet rs = stmt.executeQuery(Query);
            return rs;
        } catch (SQLException e) {
        	if (DEBUG) {
        		e.printStackTrace();}
        }
        return null;
    }	
    private void restartSesssion() {
    	pagesCount = 0;
    	String revisitQuery = "SELECT `url` from crawler_urls where revisit_priority = 1 and `is_crawled` = 1 ;";
    	ResultSet rs = executeReader(revisitQuery);
    	try {
			while(rs.next()) {
				pendingLinks.add(rs.getString("url"));
				visitedLinks.remove(rs.getString("url"));
			}
		} catch (SQLException e) {
			if(DEBUG)
				e.printStackTrace();
		}
    	return ;
    }
    private void doWork(){
		while(true) {
			visitLink();
			try {
				synchronized (pagesCount) {
					if(pagesCount >= MAX_DOCS_COUNT) {
						Thread.sleep(SLEEP_SCHEDULE_PERIOD_IN_HOURS*30*60*1000);
						if (Thread.currentThread().getName() == "#0") {
							restartSesssion();
						}
						Thread.sleep(SLEEP_SCHEDULE_PERIOD_IN_HOURS*30*60*1000);
					}	
				}
			}
			catch (InterruptedException e) {
				if (DEBUG) {
				e.printStackTrace();
				}
			}
		}
	}
	@Override 
	public void run() {
		doWork();
	}

}
