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


        }
        catch(Exception ex){
            ex.printStackTrace();
            myMap.put("status","-2");
            myMap.put("error","SQL Constraint Exception");


            /*
            String rval = '"' + "-2" + '"';
            rval += ",";
            rval += '"' + "error" + '"';
            rval += ":";
            rval += '"' + ex.getMessage() + '"';
            rval += "}";
             */
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
            queryString = "SELECT * from Identity where Identity.handle =";
            queryString += '"' + handle + '"';
            queryString += " AND Identity.password = ";
            queryString += '"' + password +'"';
            System.out.println(queryString);
        }
        catch(Exception ex){

        }
        return true;
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
