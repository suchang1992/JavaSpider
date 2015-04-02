package com.hirebigdata.spider.lagou.utils;

import com.mongodb.*;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClientBuilder;

import java.io.*;
import java.nio.charset.Charset;

/**
 * User: shellbye.com@gmail.com
 * Date: 2015/4/2
 */
public class Helper {

    public static void main(String[] args){
    }

    public static boolean saveToMongoDB(MongoClient mongoClient, String dbName,
                                        String collectionName, ReflectionDBObject object){
        try{
            DB db = mongoClient.getDB(dbName);
            DBCollection collection = db.getCollection(collectionName);

            collection.insert(object);

            return true;
        }catch (Exception e){
            e.printStackTrace();
            return false;
        }
    }

    public static String doGet(String url) {
        HttpClient client = HttpClientBuilder.create().build();
        HttpGet request = new HttpGet(url);
        try {
            HttpResponse response = client.execute(request);
            return getHtml(response);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static String getHtml(HttpResponse response) {
        StringBuffer result = new StringBuffer();
        try {
            BufferedReader rd = new BufferedReader(
                    new InputStreamReader(response.getEntity().getContent()));

            String line;
            while ((line = rd.readLine()) != null) {
                result.append(line);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result.toString();
    }

    public static String readFromFile(String path) {
        File file = new File(path);
        FileInputStream fis = null;
        StringBuffer result = new StringBuffer();
        try {
            fis = new FileInputStream(file);
            InputStreamReader isr = new InputStreamReader(fis, Charset.forName("UTF-8"));
            BufferedReader br = new BufferedReader(isr);

            String content;
            while ((content = br.readLine()) != null) {
                result.append(content);
            }
            return result.toString();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (fis != null)
                    fis.close();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
        return null;
    }

    public static void storeToDisc(String result, String storePath) {
        FileOutputStream fop = null;
        try {
            File html = new File(storePath);
            fop = new FileOutputStream(html);

            if (!html.exists()) {
                html.createNewFile();
            }

            byte[] contentInBytes = result.getBytes();

            fop.write(contentInBytes);
            fop.flush();
            fop.close();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (fop != null) {
                    fop.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
