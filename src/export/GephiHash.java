package export;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collections;
/**
 * Creates a graph of hashtags that are co-occuring within tweets. 
 * 
 * @author Baohuy Ung
 * @version 1.0
 */
public class GephiHash {
	
	Statement stmt;
	String defaultFile = "";
	String[] tempTags;
	boolean isNotEnd = true;
	int tweetsRead = 0;
	int wordCount = 0;
	int wordTotal = 0;
	int listCount = 0;
	int edgeCount = 0;
	Connection conn;
	
	//uses a hashtable to sort words then trasferns them to a vertex list
	ArrayList<HashNode> vertecies = new ArrayList<HashNode>();
	ArrayList<HashNode>[] hashList = (ArrayList<HashNode>[])new ArrayList[100];
	//name of the file
	private String edgeFile;
	
	/**
	 * @param conn conection to the DB
	 */
	public GephiHash(Connection conn){
		this.conn = conn;
	}
	
	/**
	 * Creates the SQL Statement and sorts the hashtags
	 * @throws SQLException
	 */
	public void init() throws SQLException{
	
		//System.out.println("Creating statement...");
		stmt = conn.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE,
				ResultSet.CONCUR_READ_ONLY);
		String sql;
		sql = "SELECT * FROM TWEETS Where HASHTAGS IS NOT ''";
		ResultSet rs = stmt.executeQuery(sql);
		
		listInit();
		
		rs.first();
		String tempString;
		tempString = rs.getString("HASHTAGS");
		
		tempTags = tempString.split("\\s+");
		listAdd(tempTags);
		
		tweetsRead++;
		
		while(isNotEnd){
			rs.next();
			tweetsRead++;
			tempString = rs.getString("HASHTAGS");
			System.out.println(tweetsRead + ": "+ rs.getString("HASHTAGS"));
			//very last run
			if(rs.isLast()) isNotEnd = false;
			
			tempTags = tempString.split("\\s+");
			listAdd(tempTags);
		}
		
		System.out.println("Tweets Read:" + tweetsRead);
		System.out.println("Words Found: "+ wordCount);
		//hashPrint();
		System.out.println("Word Total: "+ wordTotal);
		System.out.println("List Total: "+ listCount);
		
		vertexList();
		Collections.sort(vertecies);
		rs.close();
		stmt.close();
		
	}
	
	/**
	 * Helper method to initilize the lists
	 */
	public void listInit(){
		for(int i = 0; i <100; i++){
			hashList[i] = new ArrayList<HashNode>();
		}
	}
	
	/**
	 * Adds sorted hashtags to the list of all words
	 * @param list
	 */
	public void listAdd(String[] list){
		for(String a: list){
			int hashValue = hashInt(a.toLowerCase().hashCode());
			
			HashNode tempNode = new HashNode(a);
			if(hashList[hashValue].isEmpty()){
				hashList[hashValue].add(tempNode);
				wordCount++;
				wordTotal++;
			}
			else if(!hashFind(hashList[hashValue], tempNode.getItem())){
				hashList[hashValue].add(tempNode);
				wordCount++;
				wordTotal++;
			}
			else wordTotal++;;
			
		}
		
	}
		
	/**
	 * Converts the hash values into a range of 0-99
	 * @param a hash value of a word
	 * @return new hash value from 0-99
	 */
	public int hashInt(Integer a){	
		
		if(Integer.signum(a) == -1){
			a = a*-1;
		}
		
		String temp = Integer.toString(a);
		temp = temp.substring(0, 2);
		
		return Integer.valueOf(temp);
	}
	
	/**
	 * Helper method to print out all words in the hashlist
	 */
	public void hashPrint(){
		
		for(ArrayList<HashNode> a : hashList){
			for(HashNode b : a){
				System.out.println("Tag: "+ b.getItem() + " Count: "+ b.getCount());
				listCount += b.getCount();
			}
		}
		
	}
	
	/**
	 * Finds of a word already exists in the hash table
	 * @param hl the list to search through
	 * @param key the word to search for
	 * @return true or false depending on if the word is found in the list
	 */
	public boolean hashFind(ArrayList<HashNode> hl, String key){
		boolean findFlag = true;
		boolean is_Found = false;
		int index = 0;
		while(findFlag){
			HashNode tempNode = hl.get(index);
			if(tempNode.getItem().toLowerCase().equals(key.toLowerCase())){
				tempNode.increment();
				findFlag = false;
				is_Found = true;
			}
			if(index == hl.size()-1){
				findFlag = false;
			}
			index++;
		}
		return is_Found;
	}
	
	/**
	 * helper method to add all unique items in the hash table to a vertex list
	 */
	public void vertexList(){
		
		for(ArrayList<HashNode> a : hashList){
			for(HashNode b : a){
				vertecies.add(b);
			}
		}
		
	}
	
	/**
	 * Creates the vertex file in CSV format.
	 * @param fn the path/name of the file 
	 * @throws IOException
	 */
	public void writeVertecies(String fn) throws IOException{
		int id = 1;
		String d = ",";
		String fileName = fn;
		
		File file = new File(fileName);
		file.setWritable(true);
		
		FileWriter fw = new FileWriter(file);
		
		fw.append("id" +d+ "label" +d+ "modularity\n");
				
		for(HashNode b : vertecies){
			String tempVertex;
			tempVertex = (id++) + d + b.getItem() + d + b.getCount() + "\n";
			//System.out.print(tempVertex);
			fw.append(tempVertex);
		}
		fw.close();
		
	}
	
	/**
	 * A private version of a Hash node for use with the hash table.
	 * @author Baohuy Ung
	 *
	 */
	private class HashNode implements Comparable<HashNode>{
		private String item;
		private int count;
		
		private HashNode(String name){
			item = name.substring(1);
			count = 1;
		}
		
		private String getItem(){
			return item;
		}
		
		private int getCount(){
			return count;
		}
		
		private void setItem(String name){
			item = name;
		}
		
		private void setCount(int a){
			count = a;
		}
		private void increment(){
			count++;
		}

		@Override
		public int compareTo(HashNode a) {
			if(count> a.getCount()){
				return -1;
			}
			else if(count < a.getCount()){
				return 1;
			}
			else {
				return 0;
			}
		}
		
		public boolean equals(HashNode a){
			
			if(item.toLowerCase().equals(a.getItem().toLowerCase())){
				return true;
			}
			else return false;
			
		}
		
	}
	
	/**
	 * Locates the index of the specified hash node
	 * @param a the hash node to locate
	 * @return the index of the hashNode in the vertecie table or 0 if not found
	 */
	public int Index(HashNode a){
		boolean found = false;
		int index = 0;
		while(index < vertecies.size()){
			//System.out.println("Search for vertex at index: "+index);
			if(vertecies.get(index).equals(a)){
				return index+1;
			}
			index++;
		}
		
		return 0;
		
	}
	
	/**
	 * Creates the edges using the vertex list.
	 * @param FileName path of file to write to
	 * @throws SQLException
	 * @throws IOException
	 */
	public void edgeMake(String FileName) throws SQLException, IOException{
		edgeFile = FileName;
		File tempFile = new File(FileName);
		FileWriter tempFW = new FileWriter(tempFile);
		tempFW.append("source" +","+ "target" +","+ "type" +","+ "id" +","+ "weight\n");
		tempFW.close();
		for(HashNode root: vertecies){
			edgeFind(root);
			int vIndex = Index(root);
			writeEdge(vIndex);
		}
	}
	
	/**
	 * Locates all nodes that contain the a keyword to create an edge.
	 * 
	 * @param root the keyword to look for to create the edge relationship
	 * @throws SQLException
	 */
	public void edgeFind(HashNode root) throws SQLException{
		//hashClear();
		//System.out.println("Creating statement...");
		stmt = conn.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE,
				ResultSet.CONCUR_READ_ONLY);
		String sql;
		//selects all entities from the DB that contain the root word in the hashtag entry
		sql = "SELECT * FROM TWEETS Where HASHTAGS LIKE '%"+root.getItem()+"%'";
		//System.out.println("Executing sql statement: "+sql);
		ResultSet rs = stmt.executeQuery(sql);
		
		listInit();
		
		rs.first();
		String tempString;
		tempString = rs.getString("HASHTAGS");
		//hashtags are stored as a single string, so split up all the tags
		tempTags = tempString.split("\\s+");
		listAdd(tempTags);	
		
		tweetsRead++;
		isNotEnd = true;
		//double check if there is only one entry or no entries
		if(rs.isLast()) isNotEnd = false;
		while(isNotEnd){
			rs.next();
			if(!rs.isAfterLast()){
					String hashString = rs.getString("HASHTAGS");
			tweetsRead++;
			tempString = hashString;
			}
			//System.out.println(tweetsRead + ": "+ rs.getString("HASHTAGS"));
			
			tempTags = tempString.split("\\s+");
			listAdd(tempTags);	
			
			//very last run
			if(rs.isLast()) isNotEnd = false;
		}
		
		rs.close();
		stmt.close();

	}
	
	/**
	 * clear the hash list for a new word to be used as the keyword
	 */
	public void hashClear(){
		
		for(ArrayList<HashNode> a: hashList){
			a.clear();
		}
		
	}
	
	/**
	 * Writes the edges from a root node to the file.
	 * @param source the root node
	 * @throws IOException invalid file path
	 */
	public void writeEdge(int source) throws IOException{
		
		String d = ",";	
		File file = new File(edgeFile);
		file.setWritable(true);
		FileWriter fw = new FileWriter(file,true);
		for(ArrayList<HashNode> a: hashList){	
			for(HashNode b : a){
				int dest = Index(b);
				if(dest>source){
					edgeCount++;
					String tempVertex;
					tempVertex = (source) + d + dest + d +"undirected" + d + edgeCount +d+ b.getCount()+"\n";
					//System.out.print(tempVertex);
					fw.append(tempVertex);
				}
			}
		}
		fw.close();
		
	}
	/**
	 * closes the connection to the DB.
	 * @throws SQLException
	 */
	public void close() throws SQLException{
		conn.close();
	}
}
