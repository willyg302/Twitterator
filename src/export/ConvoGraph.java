package export;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;

/**
 *
 * @author Baohuy Ung
 * @version 1.0
 *
 * Export class for conversations. These include retweets and replies to users.
 * There are two sets of methods that create either a retweet graph or a reply graph.
 */
public class ConvoGraph {
	
	Connection conn;
	ArrayList<String> nodeList = new ArrayList<String>();
	ArrayList<Edge> edgeList = new ArrayList<Edge>();
	String fileName;
	String path;
	//delimiter for CSV files
	String d = ",";
	
	/**
	 * Constructor that takes in the file name and a Connection to the database.
	 * The paths are all hard coded as to only require the name of the file becuase I was lazy
	 * This should be changed to the full path.
	 * 
	 * @param conn connection to the DB.
	 * @param fileName path of the file you want to make
	 * @param p path to the file or location to be written to
	 */
	public ConvoGraph(Connection conn, String fileName, String p){
		this.fileName = fileName;
		this.conn = conn;	
		this. path = p;
	}
	
	/**
	 * Creates the nodes for retweets.
	 * @throws SQLException thrown if there is a problem with the DB
	 * @throws IOException File not found
	 */
	public void createNodesRT() throws SQLException, IOException{
		//certain columns that have long values in the DB will show -1 for null values
		String sql = "SELECT DISTINCT(USER_SN)FROM TWEETS WHERE RETWEETED_STATUS > '-1'";
		Statement stmt = conn.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
		
		ResultSet rs = stmt.executeQuery(sql);
		
		int nodeCount=1;
		rs.first();
		while(!rs.isAfterLast()){
			//System.out.println("Making node "+ nodeCount++);
			nodeList.add(rs.getString(1));
			rs.next();
		}
	}
	
	/**
	 * Creates the nodes for replies.
	 * @throws SQLException thrown if there is a problem with the DB
	 * @throws IOException File not found
	 */
	public void createNodesRP() throws SQLException, IOException{
		String sql = "SELECT DISTINCT(USER_SN) FROM TWEETS WHERE REPLY_TO > '-1'";
		Statement stmt = conn.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
		
		ResultSet rs = stmt.executeQuery(sql);
		
		int nodeCount=1;
		rs.first();
		while(!rs.isAfterLast()){
			//System.out.println("Making node "+ nodeCount++);
			nodeList.add(rs.getString(1));
			rs.next();
		}
	}
	
	
	/**
	 * Creates the edges for all retweets.
	 * @throws SQLException thrown if there is a problem with the DB
	 * @throws IOException File not found
	 */
	public void createRT() throws SQLException, IOException{
		String sql = "SELECT * FROM TWEETS WHERE RETWEETED_STATUS > '-1'";
		Statement stmt = conn.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
		
		ResultSet rs = stmt.executeQuery(sql);
		
		int edgeCount=1;
		rs.first();
		writeLine(path+fileName+"_RTedges.csv","source,target,weight,Label\n");
		while(!rs.isAfterLast()){
			
			String retweeter, originator;
			//tweets with replies start with a user mention RT@user_sn
			retweeter = rs.getString("User_SN");
			originator = getFirstMention(rs.getString("User_Mentions"));
			//System.out.println("Source: "+ source + " Destination: "+destination);
			if(!nodeList.contains(originator)){
				nodeList.add(originator);
				System.out.print("Making new node: "+originator);
			}
			//the IDs are the index of the userSN from the list of nodes
			int retweeterID, originatorID;
			retweeterID = nodeList.indexOf(retweeter)+1;  //+1 because list starts at 0, but node labes start at 1
			originatorID = nodeList.indexOf(originator)+1;
			String text = textConvert(rs.getString("Text"));
			
			Edge tempEdge = new Edge(originatorID, retweeterID);
			// the endges will also contain the text of the reply
			if(containsEdge(tempEdge)){
				Edge foundEdge = edgeList.get(indexOfEdge(tempEdge));
				foundEdge.increment();
				foundEdge.append(text);
				//System.out.println("Edge Found Source: "+ foundEdge.getSource()+" taget: "+ foundEdge.getTarget() + " weight: "+ foundEdge.getCount() + " Text: "+ foundEdge.getText());
			}
			else{
				tempEdge.append(text);
				edgeList.add(tempEdge);
			}
			
			
			//System.out.println("Making edge "+ edge);
			edgeCount++;
			rs.next();
		}
		for(Edge e: edgeList){
			String edge = e.getSource()+d+e.getTarget()+d+e.getCount()+d+e.getText()+"\n";
			//writes all the eudges from the list to file
			writeLine(path+fileName+"_RTedges.csv",edge);
		}
	}
	
	/**
	 * Creates the edges for the retweet graph.
	 * @throws SQLException thrown if there is a problem with the DB
	 * @throws IOException File not found
	 */
	public void createReply() throws SQLException, IOException{
		String sql = "SELECT * FROM TWEETS WHERE REPLY_TO > '-1'";
		Statement stmt = conn.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
		
		ResultSet rs = stmt.executeQuery(sql);
		
		int edgeCount=1;
		rs.first();
		//paths are hardcoded because I'm lazy, should change to require full path in constructor
		writeLine(path+fileName+"_RPedges.csv","source,target,weight,Label\n");
		while(!rs.isAfterLast()){
			
			String replyer, replyedTo;
			
			//a reply starts with a user metion @userSn
			replyer = rs.getString("User_SN");
			replyedTo = rs.getString("REPLY_USER_SN");
			//System.out.println("Source: "+ source + " Destination: "+destination);
			if(!nodeList.contains(replyedTo)){
				nodeList.add(replyedTo);
				System.out.print("Making new node: "+replyedTo);
			}
			//reply ID is the index node of the userSN 
			int replyerID, ReplyToID;
			replyerID = nodeList.indexOf(replyer)+1; //+1 because list starts at 0, but node labes start at 1
			ReplyToID = nodeList.indexOf(replyedTo)+1;
			String text = textConvert(rs.getString("Text"));
			
			Edge tempEdge = new Edge(replyerID, ReplyToID);
			if(containsEdge(tempEdge)){
				Edge foundEdge = edgeList.get(indexOfEdge(tempEdge));
				foundEdge.increment();
				foundEdge.append(text);
				//System.out.println("Edge Found Source: "+ foundEdge.getSource()+" target: "+ foundEdge.getTarget() + " weight: "+ foundEdge.getCount() + " Text: "+ foundEdge.getText());
			}
			else{
				tempEdge.append(text);
				edgeList.add(tempEdge);
			}
			
			
			//System.out.println("Making edge "+ edge);
			edgeCount++;
			rs.next();
		}
		for(Edge e: edgeList){
			String edge = e.getSource()+d+e.getTarget()+d+e.getCount()+d+e.getText()+"\n";
			writeLine(path+fileName+"_RPedges.csv",edge);
		}
	}
	
	/**
	 * Helper method to write a single line to the specified file.
	 * @param fileName file to write to
	 * @param a the string to be written
	 * @throws IOException if the path is not found
	 */
	public void writeLine(String fileName, String a ) throws IOException{
		File file = new File(fileName);
		file.setWritable(true);
		FileWriter fw = new FileWriter(file, true);
		fw.append(a);
		fw.close();
	}
	
	/**
	 * Writes all the nodes to file from the node list.
	 * @param RR (either RP or RT) specifies what type of node file it represents
	 * @throws IOException path not found
	 */
	public void nodeWrite(String RR) throws IOException{
		int nodeCount = 1;	
		writeLine(path+fileName+"_nodes"+RR+".csv","id,label\n");
		for(String a: nodeList){
			String node = nodeCount++ +d+ a+"\n";
			writeLine(path+fileName+"_nodes"+RR+".csv",node);
		} 
		
	}
	
	/**
	 * Helper method to get the first user mentioned in the text.
	 * @param mention the string of users mentioned column
	 * @return the first user mention often the reply-to person or the originator of a retweet
	 */
	public String getFirstMention(String mention){
		
		String[] temp = mention.split("\\s+");
		return temp[0].substring(1);
		
	}
	
	/**
	 * Replaces any line breaks and commas in the text to be CSV friendly.
	 * @param t text from the text column
	 * @return CSV friend text
	 */
	public String textConvert(String t){
		String temp = t;
		if (temp.contains("\n")){
			System.out.println("Return characters found! Replacing with space....");
			temp = temp.replace("\n", " ");
					
		}
		if (temp.contains("|")){
			temp = temp.replace("|", "--");
		}
		return temp;
	}
	
	/**
	 * Private edge class to create an edge with the source and target IDs alone with text of an edge.
	 * @author Baohuy Ung
	 * @verion 1.0
	 */
	private class Edge{
		private int source, target;
		private int count = 1;
		private String text = "";
		private Edge(int source, int target){
			
			this.source = source;
			this.target = target;
			
		}
		
		private int getSource(){
			return source;
		}
		
		private int getTarget(){
			return target;
		}
		private int getCount(){
			return count;
		}
		private String getText(){
			return text;
		}
		
		private boolean equals(Edge e){
			if((this.source == e.getSource()) && (this.target == e.getTarget())){
				return true;
			}
			else return false;
		}
		
		private void append(String a){
			text += "["+count+"]"+a;
		}
		
		private void increment(){
			count++;
		}
	}
	
	public boolean containsEdge(Edge e){
		for(Edge t: edgeList){
			if(t.equals(e)){
				return true; 
			}
		}
		return false;
	}
	
	public int indexOfEdge(Edge e){
		int index = 0;
		for(Edge t: edgeList){
			if(t.equals(e)){
				return index;
			}
			index ++;
		}
		return -1;
	}
	
}
