package edu.uky.cs405g.sample.database;

// Used with permission from Dr. Bumgardner
//This file was taken from Paul Linton in CS 405g at the University Of Kentucky
//Author(s) : Bradley Tyler , Parker Buckley
//References : NONE

import com.google.gson.Gson;
import edu.uky.cs405g.sample.Launcher;
import org.apache.commons.collections4.QueueUtils;
import org.apache.commons.dbcp2.*;
import org.apache.commons.pool2.ObjectPool;
import org.apache.commons.pool2.impl.GenericObjectPool;

import javax.sql.DataSource;
import java.io.Console;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLIntegrityConstraintViolationException;
import java.sql.Statement;
import java.sql.PreparedStatement;
import java.util.*;


public class DBEngine {
    private DataSource ds;
    public boolean isInit = false;
    public DBEngine(String host, String database, String login, 
		String password) {

        try {
            Class.forName("com.mysql.cj.jdbc.Driver").newInstance();
            String dbConnectionString = null;
            if(database == null) {
                dbConnectionString ="jdbc:mysql://" + host + "?" 
					+"user=" + login  +"&password=" + password 
					+"&useUnicode=true&useJDBCCompliantTimezoneShift=true"
					+"&useLegacyDatetimeCode=false&serverTimezone=UTC"; 
			} else {
                dbConnectionString ="jdbc:mysql://" + host + "/" + database
				+ "?" + "user=" + login  +"&password=" + password 
				+ "&useUnicode=true&useJDBCCompliantTimezoneShift=true"
				+ "&useLegacyDatetimeCode=false&serverTimezone=UTC";
            }
            ds = setupDataSource(dbConnectionString);
            isInit = true;
        }
        catch (Exception ex) {
            ex.printStackTrace();
        }
    } // DBEngine()

    public static DataSource setupDataSource(String connectURI) {
        //
        // First, we'll create a ConnectionFactory that the
        // pool will use to create Connections.
        // We'll use the DriverManagerConnectionFactory,
        // using the connect string passed in the command line
        // arguments.
        //
        ConnectionFactory connectionFactory = null;
            connectionFactory = 
				new DriverManagerConnectionFactory(connectURI, null);
        //
        // Next we'll create the PoolableConnectionFactory, which wraps
        // the "real" Connections created by the ConnectionFactory with
        // the classes that implement the pooling functionality.
        //
        PoolableConnectionFactory poolableConnectionFactory =
                new PoolableConnectionFactory(connectionFactory, null);

        //
        // Now we'll need a ObjectPool that serves as the
        // actual pool of connections.
        //
        // We'll use a GenericObjectPool instance, although
        // any ObjectPool implementation will suffice.
        //
        ObjectPool<PoolableConnection> connectionPool =
                new GenericObjectPool<>(poolableConnectionFactory);

        // Set the factory's pool property to the owning pool
        poolableConnectionFactory.setPool(connectionPool);

        //
        // Finally, we create the PoolingDriver itself,
        // passing in the object pool we created.
        //
        PoolingDataSource<PoolableConnection> dataSource =
                new PoolingDataSource<>(connectionPool);

        return dataSource;
    } // setupDataSource()

    public Map<String,String> seeuser(String idnum){
        //Search and find results given idnum do not return password
        Map<String,String> myMap = new LinkedHashMap<>();
        String query = "SELECT handle,fullname,location,email,bdate,joined FROM Identity where idnum = ?";
        PreparedStatement stmt = null;
        try{
            Connection myCon = ds.getConnection();
            stmt = myCon.prepareStatement(query);
            stmt.setString(1,idnum);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()){
                String handle = rs.getString("handle");
                String fullname = rs.getString("fullname");
                String location = rs.getString("location");
                String email = rs.getString("email");
                String bdate = rs.getString("bdate");
                String joined = rs.getString("joined");
                myMap.put("status","1");
                myMap.put("handle",handle);
                myMap.put("fullname",fullname);
                myMap.put("location",location);
                myMap.put("email",email);
                myMap.put("bdate",bdate);
                myMap.put("joined",joined);
            }

        }
        catch (Exception ex){
            ex.printStackTrace();
        }
        return myMap;
    }
    public Map<String,String> poststory(String handle,String chapter,String url,String expiration)
    {
        Map<String,String> myMap = new LinkedHashMap<>();
        PreparedStatement stmt = null;
        try{
          Connection myCon = ds.getConnection();
          String Query = "INSERT INTO Story (idnum,chapter,url,expires) VALUES((SELECT idnum from Identity where handle = ?),?,?,?);";
          stmt = myCon.prepareStatement(Query);
          stmt.setString(1,handle);
          stmt.setString(2,chapter);
          stmt.setString(3,url);
          stmt.setString(4,expiration);
          int result = stmt.executeUpdate();
          myMap.put("status",Integer.toString(result));

        }
        catch(SQLIntegrityConstraintViolationException ex){
            myMap.put("status","-2");
            myMap.put("error","SQL Constraint Exception");

        }
        catch (Exception a){
            a.printStackTrace();
        }
        return myMap;
    }
    public boolean isBlockedSid(String handle,String sidnum){
        //This function works to check if blocked if given sidnum
        PreparedStatement stmt = null;
        try{
            Connection mycon = ds.getConnection();
            String query = "SELECT * from Block where blocked = (SELECT idnum from Identity WHERE Identity.handle = ?)AND idnum = (SELECT idnum from Story where Story.sidnum = ?)";
            stmt = mycon.prepareStatement(query);
            stmt.setString(1,handle);
            stmt.setString(2,sidnum);
            ResultSet rs = stmt.executeQuery();
            int rowCount = rs.last() ? rs.getRow() : 0;
            rs.beforeFirst();
            if (rowCount == 0)
                return false;
            return true;
        }
        catch (Exception a){
            a.printStackTrace();
        }
        return false;
    }
    public boolean isBlocked(String handle,String idnum){
        //Reports if user is Blocked by an idnum
        PreparedStatement stmt = null;
        try{
            Connection mycon = ds.getConnection();
            String query = "SELECT * from Block where blocked = (SELECT idnum from Identity WHERE Identity.handle = ?)AND idnum = ?";
            stmt = mycon.prepareStatement(query);
            stmt.setString(1,handle);
            stmt.setString(2,idnum);
            ResultSet rs = stmt.executeQuery();
            int rowCount = rs.last() ? rs.getRow() : 0;
            rs.beforeFirst();
            if (rowCount == 0)
                return false;
            return true;
        }
        catch (Exception ex){
            ex.printStackTrace();
        }
        return false;
    }
    public Map<String,String> reprint(String handle,String idnum,String likeit){
        Map<String,String> myMap = new LinkedHashMap<>();
        if(isBlockedSid(handle,idnum)){
            //User is blocked
            myMap.put("status","0");
            myMap.put("error","blocked");
            return myMap;
        }
        PreparedStatement stmt = null;
        try{
            Connection mycon = ds.getConnection();
            String query = "INSERT INTO Reprint(idnum,sidnum,likeit) VALUES " +
                    "((SELECT idnum from Identity where Identity.handle = ?),(SELECT sidnum FROM Story where Story.sidnum = ?),?)";
            stmt = mycon.prepareStatement(query);
            stmt.setString(1,handle);
            stmt.setString(2,idnum);
            stmt.setBoolean(3,Boolean.parseBoolean(likeit));
            stmt.executeUpdate();
            myMap.put("status","1");
        }
        catch(SQLIntegrityConstraintViolationException ex){
            myMap.put("status","-2");
            myMap.put("error","SQL Constraint Exception");
            return  myMap;
        }
        catch(Exception a){
            a.printStackTrace();
        }
        return myMap;
    }
    public Map<String,String> block(String handle,String id){
        //This function will block
        Map<String,String> myMap = new LinkedHashMap<>();
        PreparedStatement stmt = null;
        try {
            Connection myCon = ds.getConnection();
            String Query = "INSERT INTO Block(idnum,blocked) VALUES((SELECT idnum FROM Identity where Identity.handle = ?),?)";
            stmt = myCon.prepareStatement(Query);
            stmt.setString(1,handle);
            stmt.setString(2,id);
            stmt.executeUpdate();
            myMap.put("status","1");
        }
        catch (SQLIntegrityConstraintViolationException e){
            myMap.put("status","0");
            myMap.put("error","DNE");
        }
        catch(Exception ax){
            ax.printStackTrace();
        }
        return myMap;
    }
    public Map<String,String> unfollow(String handle,String id){
        Map<String,String> myMap = new LinkedHashMap<>();
        PreparedStatement stmt = null;
        //Checking for blocked is unnecesarry
        try{
            Connection myCon = ds.getConnection();
            String Query = "DELETE FROM Follows where follower = (SELECT idnum FROM Identity where Identity.handle = ?) AND followed = ?";
            stmt = myCon.prepareStatement(Query,Statement.RETURN_GENERATED_KEYS);
            stmt.setString(1,handle);
            stmt.setString(2,id);
            int rowsAffected = stmt.executeUpdate();
            if(rowsAffected == 0){
                //Not following to begin with
                myMap.put("status","0");
                myMap.put("error","not currently followed");

            }
            else{
                myMap.put("status","1");
            }

        }
        catch(Exception a){
            a.printStackTrace();
        }
        return myMap;
    }
    public Map<String,String> timeline(String handle,String newest,String oldest){
        Map<String,String> myMap = new LinkedHashMap<>();
        PreparedStatement stmt = null;
        try{
            Connection myCon = ds.getConnection();
            String myQuery = "SELECT \"story\",handle,sidnum,chapter,tstamp FROM" +
                    "(SELECT \"story\",idnum,sidnum,chapter,tstamp FROM Story S WHERE S.idnum IN " +
                    "(SELECT followed FROM Follows WHERE follower IN ( SELECT idnum from Identity I where I.handle LIKE ?)) " +
                    "AND S.idnum NOT IN(SELECT blocked FROM Block B WHERE B.idnum IN ( SELECT idnum from Identity I where I.handle LIKE ?)) AND S.tstamp BETWEEN ? AND ?) AS x RIGHT JOIN Identity px on (x.idnum = px.idnum) WHERE x.idnum IN (SELECT idnum FROM Story O WHERE O.idnum = x.idnum)" +
                    "UNION " +
                    "SELECT \"reprint\",handle,sidnum,chapter,tstamp " +
                    "FROM(SELECT \"reprint\",idnum, S.sidnum, S.chapter, S.tstamp " +
                    "FROM Story S WHERE S.sidnum IN (SELECT sidnum FROM Reprint R WHERE R.likeit IS FALSE  AND R.idnum IN " +
                    "(SELECT followed FROM Follows WHERE follower IN ( SELECT idnum from Identity I where I.handle LIKE ?)) " +
                    "AND R.idnum NOT IN(SELECT blocked FROM Block B WHERE B.idnum IN " +
                    "( SELECT idnum from Identity I where I.handle LIKE ?))AND S.tstamp BETWEEN ? AND ?)) " +
                    "AS x RIGHT JOIN Identity px on (x.idnum = px.idnum) WHERE x.idnum IN (SELECT idnum FROM Story O where O.idnum = x.idnum)";



            stmt = myCon.prepareStatement(myQuery);
            stmt.setString(1,handle);
            stmt.setString(2,handle);
            stmt.setString(3,oldest);
            stmt.setString(4,newest);
            stmt.setString(5,handle);
            stmt.setString(6,handle);
            stmt.setString(7,oldest);
            stmt.setString(8,newest);
            ResultSet rs = stmt.executeQuery();
            int storyCount = 0;
            while(rs.next()){
                String myType = rs.getString("story");
                String author = rs.getString("handle");
                String sidnum = Integer.toString(rs.getInt("sidnum"));
                String chapter = rs.getString("chapter");
                String mydate = rs.getString("tstamp");
                Map<String,String> tempMap = new LinkedHashMap<>();
                tempMap.put("type",myType);
                tempMap.put("author",author);
                tempMap.put("sidnum",sidnum);
                tempMap.put("chapter",chapter);
                tempMap.put("date",mydate);
                String jsonString = Launcher.gson.toJson(tempMap);
                myMap.put(Integer.toString(storyCount),jsonString);
                storyCount++;

            }
            if(storyCount == 0){
                myMap.put("status","0");
                myMap.put("error","timeline empty");
            }



        }
        catch (Exception a){
            a.printStackTrace();
        }
        return myMap;
    }
    public Map<String,String> follow(String handle,String id){
        Map<String,String> myMap = new LinkedHashMap<>();
        PreparedStatement stmt = null;
        if(isBlocked(handle,id)){
            //User is blocked
            myMap.put("status","0");
            myMap.put("error","blocked");
            return myMap;
        }
        try{
            Connection myCon = ds.getConnection();
            String Query = "INSERT INTO Follows(follower,followed) VALUES((SELECT idnum from Identity where Identity.handle= ?),?)";
            stmt = myCon.prepareStatement(Query);
            stmt.setString(1,handle);
            stmt.setString(2,id);
            stmt.executeUpdate();
            myMap.put("status","1");
        }
        catch (SQLIntegrityConstraintViolationException ax){
            myMap.put("status","0");
            myMap.put("error","DNE");
        }
        catch(Exception a){
            a.printStackTrace();
        }
        return myMap;
    }
    public Map<String,String> suggestions(String handle){
        Map<String,String> myMap = new LinkedHashMap<>();
        PreparedStatement stmt = null;
        try{
            Connection myCon = ds.getConnection();
            //String Query = "SELECT idnum,handle FROM Identity WHERE idnum IN (SELECT followed FROM Follows where follower NOT IN (SELECT idnum FROM Identity where handle = ?)) LIMIT 4";
            String Query = "SELECT idnum,handle FROM Identity WHERE idnum IN (SELECT followed from Follows where follower IN (SELECT followed FROM Follows where follower IN (SELECT idnum FROM Identity where handle = ?)))";
            stmt = myCon.prepareStatement(Query);
            stmt.setString(1,handle);
            ResultSet rs = stmt.executeQuery();
            String handleString = "";
            String idnumString = "";
            int statusNum = 0;
            while(rs.next()){
                String myhandle = rs.getString("handle");
                String myidnum = rs.getString("idnum");
                handleString +="," + myhandle;
                idnumString += "," + myidnum;
                statusNum++;

            }
            if(statusNum == 0){
                myMap.put("status","0");
                myMap.put("error","no suggestions");
                return myMap;
            }
            myMap.put("status",Integer.toString(statusNum));
            myMap.put("idnums",idnumString);
            myMap.put("handles",handleString);
            return  myMap;

        }
        catch(Exception ex){
            ex.printStackTrace();
        }
        return myMap;
    }
    public Map<String,String> createUser(String handle,String password,String fullname,String location,String email,String bdate,String jdate)
    {
        //This function creates a user and returns the idNum

        //Using maps makes converting to json much easier
        Map<String,String> myMap = new LinkedHashMap<>();
        //System.out.println(jdate);
        PreparedStatement stmt = null;
        String userId = null;
        try{
            Connection myCon = ds.getConnection();
            String query = "INSERT INTO Identity (handle,password,fullname,location,email,bdate,joined) VALUES(?,?,?,?,?,?,?)";
            //stmt = myCon.prepareStatement(query);
            stmt = myCon.prepareStatement(query,Statement.RETURN_GENERATED_KEYS);
            stmt.setString(1,handle);
            stmt.setString(2,password);
            stmt.setString(3,fullname);
            stmt.setString(4,location);
            stmt.setString(5,email);
            stmt.setString(6,bdate);
            stmt.setString(7,jdate);

            //stmt = myCon.prepareStatement(query,Statement.RETURN_GENERATED_KEYS);
            stmt.executeUpdate();
            ResultSet rs = stmt.getGeneratedKeys();
            if(rs.next()){
                //Get the number
                userId = Integer.toString(rs.getInt(1));
            }
            myMap.put("status",userId);
            //myCon.close();

        }
        catch(SQLIntegrityConstraintViolationException exs){
            //An error in SQL constraints
            myMap.put("status","-2");
            myMap.put("error","SQL Constraint Exception");
            return  myMap;
        }
        catch(Exception ex){
            ex.printStackTrace();
            return myMap;
        }

        return myMap;
    }
    public boolean authUser(String handle,String password){
        //Function to query identification table for username/password
        PreparedStatement stmt = null;
        try{
            Connection conn = ds.getConnection();
            String queryString = null;
            queryString = "SELECT * from Identity where handle = ? AND password = ?";
            stmt = conn.prepareStatement(queryString);
            stmt.setString(1,handle);
            stmt.setString(2,password);
            ResultSet rs = stmt.executeQuery();
            int rowCount = rs.last() ? rs.getRow() : 0;
            rs.beforeFirst();
            if (rowCount == 0)
                return false;
            return true;
        }
        catch(Exception ex){
            return false;
        }

    }
    public Map<String,String> getUsers() {
        Map<String,String> userIdMap = new HashMap<>();

        PreparedStatement stmt = null;
        try
        {
            Connection conn = ds.getConnection();
            String queryString = null;
            queryString = "SELECT * FROM Identity";
            stmt = conn.prepareStatement(queryString);
			// No parameters, so no binding needed.
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                String userId = Integer.toString(rs.getInt("idnum"));
                String userName = rs.getString("handle");
                userIdMap.put(userId, userName);
            }
            rs.close();
            stmt.close();
            conn.close();
        }
        catch(Exception ex)
        {
            ex.printStackTrace();
        }
        return userIdMap;
    } // getUsers()

    public Map<String,String> getBDATE(String idnum) {
        Map<String,String> userIdMap = new HashMap<>();

        PreparedStatement stmt = null;
        Integer id = Integer.parseInt(idnum);
        try
        {
            Connection conn = ds.getConnection();
            String queryString = null;
// Here is a statement, but we want a prepared statement.
//            queryString = "SELECT bdate FROM Identity WHERE idnum = "+id;
//            
            queryString = "SELECT bdate FROM Identity WHERE idnum = ?";
// ? is a parameter placeholder
            stmt = conn.prepareStatement(queryString);
			stmt.setInt(1,id);
// 1 here is to denote the first parameter.
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                String bdate = rs.getString("bdate");
                userIdMap.put("bdate", bdate);
            }
            rs.close();
            stmt.close();
            conn.close();
        }
        catch(Exception ex)
        {
            ex.printStackTrace();
        }
        return userIdMap;
    } // getBDATE()

} // class DBEngine
