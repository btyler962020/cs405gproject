package edu.uky.cs405g.sample.httpcontrollers;
//
// Sample code used with permission from Dr. Bumgardner
//This file was taken from Paul Linton in CS 405g at the University Of Kentucky
//Author(s) : Bradley Tyler , Parker Buckley
//References : None
//
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import edu.uky.cs405g.sample.Launcher;
import edu.uky.cs405g.sample.database.DBEngine;
import org.eclipse.persistence.internal.sessions.DirectCollectionChangeRecord;

import javax.annotation.PostConstruct;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.*;
import java.lang.reflect.Type;
import java.time.LocalDate;
import java.util.*;

@Path("/api")
public class API {

    private Type mapType;
    private Gson gson;

    public API() {
        mapType = new TypeToken<Map<String, String>>() {
        }.getType();
        gson = new Gson();
    }

    //curl http://localhost:9990/api/status
    //{"status_code":1}
    @GET
    @Path("/status")
    @Produces(MediaType.APPLICATION_JSON)
    public Response healthcheck() {
        String responseString = "{\"status_code\":0}";
        try {
            //Here is where you would put your system test, 
			//but this is not required.
            //We just want to make sure your API is up and active/
            //status_code = 0 , API is offline
            //status_code = 1 , API is online
            responseString = "{\"status_code\":1}";
        } catch (Exception ex) {
            StringWriter sw = new StringWriter();
            ex.printStackTrace(new PrintWriter(sw));
            String exceptionAsString = sw.toString();
            ex.printStackTrace();
            return Response.status(500).entity(exceptionAsString).build();
        }
        return Response.ok(responseString)
				.header("Access-Control-Allow-Origin", "*").build();
    } // healthcheck()

    //curl http://localhost:9998/api/listusers
    //{"1":"@paul","2":"@chuck"}
    @GET
    @Path("/listusers")
    @Produces(MediaType.APPLICATION_JSON)
    public Response listUsers() {
        String responseString = "{}";
        try {
            Map<String, String> teamMap = Launcher.dbEngine.getUsers();
            responseString = Launcher.gson.toJson(teamMap);
        } catch (Exception ex) {
            StringWriter sw = new StringWriter();
            ex.printStackTrace(new PrintWriter(sw));
            String exceptionAsString = sw.toString();
            ex.printStackTrace();
            return Response.status(500).entity(exceptionAsString).build();
        }
        return Response.ok(responseString)
                .header("Access-Control-Allow-Origin", "*").build();
    } // listUsers()
    /*
    public boolean authenticateUser(String handle, String password){
        //This function exists to ensure the user has signed in properlu
        return Launcher.dbEngine.authUser(handle,password);
    }
    */
    @POST
    @Path("/poststory")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public Response poststory(InputStream inputData){
        Map<String,String> myMap = new LinkedHashMap<>();
        StringBuilder crunchifyBuilder = new StringBuilder();
        try{
            BufferedReader in = new BufferedReader(new InputStreamReader(inputData));
            String line = null;
            while((line = in.readLine()) != null){
                crunchifyBuilder.append(line);
            }
            String jsonString = crunchifyBuilder.toString();
            Map<String,String> myVal = gson.fromJson(jsonString,mapType);
            String handle = myVal.get("handle");
            String pasword = myVal.get("password");
            String chapter = myVal.get("chapter");
            String url = myVal.get("url");
            String expires = myVal.get("expires");
            if(Launcher.dbEngine.authUser(handle,pasword)){
                //User accepted
                myMap = Launcher.dbEngine.poststory(handle,chapter,url,expires);

            }
            else{
                //User declined
                myMap.put("status_code","-10");
                myMap.put("error","invalid credentials");
            }

        }
        catch(Exception ex){

        }
        String responseString = gson.toJson(myMap);
        return Response.ok(responseString)
                .header("Access-Control-Allow-Origin", "*").build();
    }
    @POST
    @Path("/timeline")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public Response timeline(InputStream inputData){
        //This function handles the timeline API call
        Map<String,String> myMap = new LinkedHashMap<>();
        StringBuilder crunchifyBuilder = new StringBuilder();
        try{
            BufferedReader in = new BufferedReader(new InputStreamReader(inputData));
            String line = null;
            while ((line = in.readLine()) != null){
                crunchifyBuilder.append(line);
            }
            String jsonString = crunchifyBuilder.toString();
            Map<String,String> myVal = gson.fromJson(jsonString,mapType);
            String handle = myVal.get("handle");
            String password = myVal.get("password");
            String newest = myVal.get("newest");
            String oldest = myVal.get("oldest");
            if(Launcher.dbEngine.authUser(handle,password)){
                //User accepted
                myMap = Launcher.dbEngine.timeline(handle,newest,oldest);

            }
            else{
                //User declined
                myMap.put("status_code","-10");
                myMap.put("error","invalid credentials");
            }
        }
        catch(Exception ex){
            ex.printStackTrace();
        }
        String responseString = gson.toJson(myMap);
        return Response.ok(responseString)
                .header("Access-Control-Allow-Origin", "*").build();
    }


    @POST
    @Path("/reprint/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public Response reprint(@PathParam("id") String id,InputStream inputData){
        Map<String,String> myMap = new LinkedHashMap<>();
        StringBuilder crunchifyBuilder = new StringBuilder();
        try{
            BufferedReader in = new BufferedReader(new InputStreamReader(inputData));
            String line = null;
            while ((line = in.readLine()) != null){
                crunchifyBuilder.append(line);
            }
            String jsonString = crunchifyBuilder.toString();
            Map<String,String> myVal = gson.fromJson(jsonString,mapType);
            String handle = myVal.get("handle");
            String password = myVal.get("password");
            String likeit = myVal.get("likeit");
            if(Launcher.dbEngine.authUser(handle,password)){
                //User accepted
                myMap = Launcher.dbEngine.reprint(handle,id,likeit);

            }
            else{
                //User declined
                myMap.put("status_code","-10");
                myMap.put("error","invalid credentials");
            }
        }
        catch (Exception ex){
            ex.printStackTrace();
        }
        String responseString = gson.toJson(myMap);
        return Response.ok(responseString)
                .header("Access-Control-Allow-Origin", "*").build();
    }
    @POST
    @Path("/block/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public Response block(@PathParam("id") String id,InputStream inputData){
        Map<String,String> myMap = new LinkedHashMap<>();
        StringBuilder crunchifyBuilder = new StringBuilder();
        try{
            BufferedReader in = new BufferedReader(new InputStreamReader(inputData));
            String line = null;
            while((line = in.readLine()) != null){
                crunchifyBuilder.append(line);
            }
            String jsonString = crunchifyBuilder.toString();
            Map<String,String> myVal = gson.fromJson(jsonString,mapType);
            String handle = myVal.get("handle");
            String pasword = myVal.get("password");
            if(Launcher.dbEngine.authUser(handle,pasword)){
                //User accepted
                myMap = Launcher.dbEngine.block(handle,id);

            }
            else{
                //User declined
                myMap.put("status_code","-10");
                myMap.put("error","invalid credentials");
            }
        }
        catch(Exception ax){
            ax.printStackTrace();
        }
        String responseString = gson.toJson(myMap);
        return Response.ok(responseString)
                .header("Access-Control-Allow-Origin", "*").build();
    }

    @POST
    @Path("/unfollow/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public Response unfollow(@PathParam("id") String id,InputStream inputData){
        //This function will unfollow someone given the idnum
        Map<String,String> myMap = new LinkedHashMap<>();
        StringBuilder crunchifyBuilder = new StringBuilder();
        try{
            BufferedReader in = new BufferedReader(new InputStreamReader(inputData));
            String line = null;
            while((line = in.readLine()) != null){
                crunchifyBuilder.append(line);
            }
            String jsonString = crunchifyBuilder.toString();
            Map<String,String> myVal = gson.fromJson(jsonString,mapType);
            String handle = myVal.get("handle");
            String pasword = myVal.get("password");
            if(Launcher.dbEngine.authUser(handle,pasword)){
                //User accepted
                myMap = Launcher.dbEngine.unfollow(handle,id);

            }
            else{
                //User declined
                myMap.put("status_code","-10");
                myMap.put("error","invalid credentials");
            }
        }
        catch(Exception ex){
            ex.printStackTrace();
        }
        String responseString = gson.toJson(myMap);
        return Response.ok(responseString)
                .header("Access-Control-Allow-Origin", "*").build();
    }


    @POST
    @Path("/follow/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public Response follow(@PathParam("id") String id, InputStream inputData){
        //This function will follow someone given the idnum
        Map<String,String> myMap = new LinkedHashMap<>();
        StringBuilder crunchifyBuilder = new StringBuilder();
        try{
            BufferedReader in = new BufferedReader(new InputStreamReader(inputData));
            String line = null;
            while((line = in.readLine()) != null){
                crunchifyBuilder.append(line);
            }
            String jsonString = crunchifyBuilder.toString();
            Map<String,String> myVal = gson.fromJson(jsonString,mapType);
            String handle = myVal.get("handle");
            String pasword = myVal.get("password");
            if(Launcher.dbEngine.authUser(handle,pasword)){
                //User accepted
                myMap = Launcher.dbEngine.follow(handle,id);

            }
            else{
                //User declined
                myMap.put("status_code","-10");
                myMap.put("error","invalid credentials");
            }
        }
        catch (Exception ex){
            ex.printStackTrace();
        }
        String responseString = gson.toJson(myMap);
        return Response.ok(responseString)
                .header("Access-Control-Allow-Origin", "*").build();
    }

    @POST
    @Path("/suggestions")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public Response suggestions(InputStream inputData){
        //Suggestions are generated by generating a list limited with no
        //more than 4 results
        //List should be queried by finding followers of people that the follow
        //who the user follows
        Map<String,String> myMap = new LinkedHashMap<>();
        StringBuilder crunchifyBuilder = new StringBuilder();
        try{
            BufferedReader in = new BufferedReader(new InputStreamReader(inputData));
            String line = null;
            while((line = in.readLine()) != null){
                crunchifyBuilder.append(line);
            }
            String jsonString = crunchifyBuilder.toString();
            Map<String,String> myVal = gson.fromJson(jsonString,mapType);
            String handle = myVal.get("handle");
            String pasword = myVal.get("password");
            if(Launcher.dbEngine.authUser(handle,pasword)){
                //User accepted
                myMap = Launcher.dbEngine.suggestions(handle);

            }
            else{
                //User declined
                myMap.put("status_code","-10");
                myMap.put("error","invalid credentials");
            }
        }
        catch(Exception ex){

        }
        String responseString = gson.toJson(myMap);
        return Response.ok(responseString)
                .header("Access-Control-Allow-Origin", "*").build();
    }


    @POST
    @Path("/seeuser/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public Response seeUser(@PathParam("id") String id ,InputStream inputData){
        //Linked Hash Map required to maintain insertion order
        Map<String,String> myMap = new LinkedHashMap<>();
        StringBuilder crunchifyBuilder = new StringBuilder();
        try{
            BufferedReader in = new BufferedReader(new InputStreamReader(inputData));
            String line = null;
            while((line = in.readLine()) != null){
                crunchifyBuilder.append(line);
            }
            String jsonString = crunchifyBuilder.toString();
            Map<String,String> myVal = gson.fromJson(jsonString,mapType);
            String handle = myVal.get("handle");
            String password = myVal.get("password");
            if(Launcher.dbEngine.authUser(handle,password)){
                //User login succeeded
                myMap = Launcher.dbEngine.seeuser(id);

            }
            else{
                //User login failed
                //Send authfailure()
                myMap.put("status_code","-10");
                myMap.put("error","invalid credentials");

            }
        }
        catch (Exception ex){
            StringWriter sw = new StringWriter();
            ex.printStackTrace(new PrintWriter(sw));
            String exceptionAsString = sw.toString();
            ex.printStackTrace();
            return Response.status(500).entity(exceptionAsString).build();
        }
        String responseString = gson.toJson(myMap);
        return Response.ok(responseString)
                .header("Access-Control-Allow-Origin", "*").build();
    }

    //Statements preceded by @ are used for extracting supplied URL
    @POST
    @Path("/createuser")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public Response createUser(InputStream inputData){
        //Create a responseString
        Map<String,String> myMap = new LinkedHashMap<>();
        StringBuilder crunchifyBuilder = new StringBuilder();
        try{
            BufferedReader in = new BufferedReader(new InputStreamReader(inputData));
            String line = null;
            //Read supplied line from URL
            while ((line=in.readLine()) != null){
                crunchifyBuilder.append(line);
            }
            //Convert read line to string
            String jsonString = crunchifyBuilder.toString();
            //Extract supplied json to Map
            Map<String,String> myVal = gson.fromJson(jsonString,mapType);
            //Extract all supplied information
            String handle = myVal.get("handle");
            String password = myVal.get("password");
            String fullname = myVal.get("fullname");
            String location = myVal.get("location");
            //According to DOCS linton will be using "xmail" instead of "email"
            String email = myVal.get("xmail");
            String bdate = myVal.get("bdate");
            String jdate = LocalDate.now().toString();


            //createUser function returns idNum that was created
            myMap = Launcher.dbEngine.createUser(handle,password,fullname,location,email,bdate,jdate);
        }
        catch (Exception ex){
            StringWriter sw = new StringWriter();
            ex.printStackTrace(new PrintWriter(sw));
            String exceptionAsString = sw.toString();
            ex.printStackTrace();
            return Response.status(500).entity(exceptionAsString).build();
        }
        String responseString = "";
        responseString = Launcher.gson.toJson(myMap);

        return Response.ok(responseString)
                .header("Access-Control-Allow-Origin", "*").build();
    }

    //curl -d '{"foo":"silly1","bar":"silly2"}' \
	//     -H "Content-Type: application/json" \
    //     -X POST  http://localhost:9990/api/exampleJSON
	//
    //{"status_code":1, "foo":silly1, "bar":silly2}
    @POST
    @Path("/exampleJSON")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public Response exampleJSON(InputStream inputData) {
        String responseString = "{\"status_code\":0}";
        StringBuilder crunchifyBuilder = new StringBuilder();
        try {
            BufferedReader in = new BufferedReader(new InputStreamReader(inputData));
            String line = null;
            while ((line=in.readLine()) != null) {
                crunchifyBuilder.append(line);
            }
            String jsonString = crunchifyBuilder.toString();

            Map<String, String> myMap = gson.fromJson(jsonString, mapType);
            String fooval = myMap.get("foo");
            String barval = myMap.get("bar");
            //Here is where you would put your system test,
            //but this is not required.
            //We just want to make sure your API is up and active/
            //status_code = 0 , API is offline
            //status_code = 1 , API is online
            responseString = "{\"status_code\":1, "
					+"\"foo\":\""+fooval+"\", "
					+"\"bar\":\""+barval+"\"}";
        } catch (Exception ex) {
            StringWriter sw = new StringWriter();
            ex.printStackTrace(new PrintWriter(sw));
            String exceptionAsString = sw.toString();
            ex.printStackTrace();
            return Response.status(500).entity(exceptionAsString).build();
        }
        return Response.ok(responseString)
                .header("Access-Control-Allow-Origin", "*").build();
    } // exampleJSON()

    //curl http://localhost:9990/api/exampleGETBDATE/2
    //{"bdate":"1968-01-26"}
    @GET
    @Path("/exampleGETBDATE/{idnum}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response exampleBDATE(@PathParam("idnum") String idnum) {
        String responseString = "{\"status_code\":0}";
        StringBuilder crunchifyBuilder = new StringBuilder();
        try {
            Map<String,String> teamMap = Launcher.dbEngine.getBDATE(idnum);
            responseString = Launcher.gson.toJson(teamMap);
        } catch (Exception ex) {
            StringWriter sw = new StringWriter();
            ex.printStackTrace(new PrintWriter(sw));
            String exceptionAsString = sw.toString();
            ex.printStackTrace();
            return Response.status(500).entity(exceptionAsString).build();
        }
        return Response.ok(responseString)
                .header("Access-Control-Allow-Origin", "*").build();
    } // exampleBDATE

} // API.java
