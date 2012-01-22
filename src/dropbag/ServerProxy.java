package dropbag;

import org.apache.http.client.*;
import org.apache.http.impl.client.*;
import org.apache.http.client.methods.*;
import org.apache.http.*;
import java.io.*;
import org.apache.http.entity.*;
import com.google.gson.*;
import com.google.gson.stream.*;

public class ServerProxy {
    
    private ServerThread parent;

    public ServerProxy(ServerThread parent) {
        this.parent = parent;
    }
        
    /**
     * POST
     * @param url
     * @param data
     * @param contentType
     * @return
     * @throws Exception 
     */
    public <T> T post(String url, String data, String contentType, Class<T> objectType) throws Exception{
        // make request
        HttpClient client = new DefaultHttpClient();
        HttpPost request = new HttpPost( url );
        StringEntity _data = new StringEntity(data);
        _data.setContentType(contentType);
        request.setEntity(_data);
        // execute
        HttpResponse response = client.execute(request);
        // check response code
        int code = response.getStatusLine().getStatusCode();
        if( code != 200 ){
            throw new Exception("Bad HTTP response code: " + response.getStatusLine().toString());
        }
        // check response
        HttpEntity response_entity = response.getEntity();
        if( response_entity == null){
            throw new Exception("No HTTP response");
        }
        // get response content
        InputStream in = response_entity.getContent();
        String content = "";
        int l = 0;
        byte[] tmp = new byte[2048];
        while(l != -1){
            l = in.read(tmp);
            if( l != -1 ) content += new String(tmp).trim();
        }
        in.close();
        // check response content length
        if(content.length() < 1) throw new Exception("HTTP request returned no content");
        // check for valid JSON returned
        Gson gson = new Gson();
        return gson.fromJson(content, objectType);
    }
    
    public <T> T post(String url, String data, Class<T> objectType) throws Exception{
        return this.post(url, data, "application/json", objectType);
    }
    
    public <T> T post(String url, InputStream data, String contentType, Class<T> objectType) throws Exception{
        // make request
        HttpClient client = new DefaultHttpClient();
        HttpPost request = new HttpPost( url );
        InputStreamEntity _data = new InputStreamEntity(data, -1);
        _data.setContentType(contentType);
        request.setEntity(_data);
        // execute
        HttpResponse response = client.execute(request);
        // check response code
        int code = response.getStatusLine().getStatusCode();
        if( code != 200 ){
            throw new Exception("Bad HTTP response code: " + response.getStatusLine().toString());
        }
        // check response
        HttpEntity response_entity = response.getEntity();
        if( response_entity == null){
            throw new Exception("No HTTP response");
        }
        // get response content
        InputStream in = response_entity.getContent();
        String content = "";
        int l = 0;
        byte[] tmp = new byte[2048];
        while(l != -1){
            l = in.read(tmp);
            if( l != -1 ) content += new String(tmp).trim();
        }
        in.close();
        // check response content length
        if(content.length() < 1) throw new Exception("HTTP request returned no content");
        // check for valid JSON returned
        Gson gson = new Gson();
        return gson.fromJson(content, objectType);
    }
    
    public <T> T post(String url, InputStream data, Class<T> objectType) throws Exception{
        return this.post(url, data, "application/json", objectType);
    }
 
        /**
     * Generic method for POSTing data
     * @param url
     * @param data
     * @return
     * @throws Exception 
     */
    public JsonReader postStreamed(String url, String data, String contentType) throws Exception{
        // make request
        HttpClient client = new DefaultHttpClient();
        HttpPost request = new HttpPost( url );
        StringEntity _data = new StringEntity(data);
        _data.setContentType(contentType);
        request.setEntity(_data);
        // execute
        HttpResponse response = client.execute(request);
        // check response code
        int code = response.getStatusLine().getStatusCode();
        if( code != 200 ){
            throw new Exception("Bad HTTP response code: " + response.getStatusLine().toString());
        }
        // check response
        HttpEntity response_entity = response.getEntity();
        if( response_entity == null){
            throw new Exception("No HTTP response");
        }
        // get response content
        JsonReader out = new JsonReader( new InputStreamReader(response_entity.getContent(), "UTF-8") );
        out.setLenient(true);
        return out;
    }
    
    public JsonReader postStreamed(String url, String data) throws Exception{
        return this.postStreamed(url, data, "application/json");
    }
    
    /**
     * GET
     * @param url
     * @param data
     * @param contentType
     * @return
     * @throws Exception 
     */
    public <T> T get(String url, Class<T> objectType) throws Exception{
        // make request
        HttpClient client = new DefaultHttpClient();
        HttpPost request = new HttpPost( url );
        // execute
        HttpResponse response = client.execute(request);
        // check response code
        int code = response.getStatusLine().getStatusCode();
        if( code != 200 ){
            throw new Exception("Bad HTTP response code: " + response.getStatusLine().toString());
        }
        // check response
        HttpEntity response_entity = response.getEntity();
        if( response_entity == null){
            throw new Exception("No HTTP response");
        }
        // get response content
        InputStream in = response_entity.getContent();
        String content = "";
        int l = 0;
        byte[] tmp = new byte[2048];
        while(l != -1){
            l = in.read(tmp);
            if( l != -1 ) content += new String(tmp).trim();
        }
        in.close();
        // check response content length
        if(content.length() < 1) throw new Exception("HTTP request returned no content");
        // check for valid JSON returned
        Gson gson = new Gson();
        return gson.fromJson(content, objectType);
    }
    
    /**
     * Generic method for GETing data
     * @param url
     * @param data
     * @return
     * @throws Exception 
     */
    public JsonReader getStreamed(String url) throws Exception{
        // make request
        HttpClient client = new DefaultHttpClient();
        HttpGet request = new HttpGet( url );
        // execute
        HttpResponse response = client.execute(request);
        // check response code
        int code = response.getStatusLine().getStatusCode();
        if( code != 200 ){
            throw new Exception("Bad HTTP response code: " + response.getStatusLine().toString());
        }
        // check response
        HttpEntity response_entity = response.getEntity();
        if( response_entity == null){
            throw new Exception("No HTTP response");
        }
        // get response content
        return new JsonReader( new InputStreamReader(response_entity.getContent(), "UTF-8") );
    }
}
