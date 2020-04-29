package edu.uky.cs405g.sample.database;

// Used with permission from Dr. Bumgardner

import edu.uky.cs405g.sample.Launcher;
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

        }
        return myMap;
    }
    public Map<String,String> suggestions(String handle){
        Map<String,String> myMap = new LinkedHashMap<>();
        PreparedStatement stmt = null;
        try{
            Connection myCon = ds.getConnection();
            String Query = "";
        }
        catch(Exception ex){

        }
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
            String query = "INSERT INTO Identity (handle,password,fullname,location,email,bdate,joined) VALUES(";
            //Very ugly code incoming
            query += '"' + handle + '"';
            query += ",";
            query += '"' + password + '"';
            query += ",";
            query += '"' + fullname + '"';
            query += ",";
            query += '"' + location + '"';
            query += ",";
            query += '"' + email + '"';
            query += ",";
            query += '"' + bdate + '"';
            query += ",";
            query += '"' + jdate + '"';
            query += ")";
            System.out.println(query);
            stmt = myCon.prepareStatement(query,Statement.RETURN_GENERATED_KEYS);
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
