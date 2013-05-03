package export;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.Connection;
import java.sql.Date;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Calendar;
/**
 * Exports nodes with Time data. Derivative of hashgather.
 * @author Baohuy Ung
 * @version 1.0
 */
public class TimeGraph {

	Statement stmt;
	String[] tempTags;
	boolean isNotEnd = true;
	int tweetsRead = 0;
	int wordCount = 0;
	int wordTotal = 0;
	int listCount = 0;
	
	ArrayList<HashNode> points = new ArrayList<HashNode>();
	
	ArrayList<HashNode>[] hashList = (ArrayList<HashNode>[])new ArrayList[100];
	
	/**
	 * Creates the a graph with times on the nodes.
	 * @param conn connection to the DB
	 * @throws SQLException
	 */
	public TimeGraph(Connection conn) throws SQLException{
		
		System.out.println("Creating statement...");
		stmt = conn.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE,
				ResultSet.CONCUR_READ_ONLY);
		String sql;
		sql = "SELECT CREATED FROM TWEETS";
		ResultSet rs = stmt.executeQuery(sql);
		
		listInit();
		
		rs.first();
		Timestamp tempDate; //insert time
		Calendar temp =  Calendar.getInstance();
		tempDate = rs.getTimestamp("CREATED");
		tweetsRead++;
		temp.setTimeInMillis(tempDate.getTime());
		listAdd(temp);
		
		while(isNotEnd){
			rs.next();
			tweetsRead++;
			tempDate = rs.getTimestamp("CREATED");
			//convert the times
			temp.setTimeInMillis(tempDate.getTime());
			listAdd(temp);
			//very last run
			if(rs.isLast()) isNotEnd = false;
		}
		
		rs.close();
		stmt.close();
		conn.close();
	}
	/**
	 * Helper method to initialize the array lists
	 */
	public void listInit(){
		for(int i = 0; i <100; i++){
			hashList[i] = new ArrayList<HashNode>();
		}
	}
	
	/**
	 * Adds an item to the list
	 * @param a calendar instance of the date
	 */
	public void listAdd(Calendar temp){
			int hashValue = hashInt(temp.hashCode());
			HashNode tempNode = new HashNode(temp);
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
	}
	
	/**
	 * Helper method that converts a hash int into a range of 0-99
	 * @param a hash int to be converted
	 * @return the new hashint for the array list
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
	 * Helper print method
	 */
	public void print(){
		for(HashNode a : points){
			System.out.println("Tag: "+ a.getItem() + " Count: "+ a.getCount());
			listCount += a.getCount();
		}
	}
	
	/**
	 * Finds an a given item within the hashtable.
	 * 
	 * @param hl the hashtable containing the nodes
	 * @param calendar the given date to search for
	 * @return if the item exists in the table return true/false
	 */
	public boolean hashFind(ArrayList<HashNode> hl, Calendar calendar){
		boolean findFlag = true;
		boolean is_Found = false;
		int index = 0;
		while(findFlag){
			HashNode tempNode = hl.get(index);
			if(tempNode.getItem().equals(calendar)){
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
	 * Helper method to add the items in the hastable into a single node list.
	 */
	public void pointList(){
		for(ArrayList<HashNode> a : hashList){
			for(HashNode b : a){
				points.add(b);
			}
		}
		
	}
	
	/**
	 * Method to write out the list to a file.
	 * 
	 * @param fileName path of the file to write to
	 * @throws IOException
	 */
	public void export(String fileName) throws IOException{
		File file = new File(fileName);
		FileWriter fw = new FileWriter(file);
		
		for(HashNode a: points){
			String temp = a.getItem() + "," + a.getCount()+"\n";
			System.out.println("Appending: "+temp);
			fw.append(temp);
		}
		fw.close();
		
	}
	
	/**
	 * This is a private class that represents a specific hash object.
	 * 
	 * @author Baohuy Ung
	 * @version 1.0
	 */
	private class HashNode{
		private Calendar item;
		private int count;
		
		private HashNode(Calendar temp){
			item = temp;
			count = 1;
		}
		
		private Calendar getItem(){
			return item;
		}
		
		private int getCount(){
			return count;
		}
		
		private void setItem(Calendar name){
			item = name;
		}
		
		private void setCount(int a){
			count = a;
		}
		private void increment(){
			count++;
		}
		
	}

}