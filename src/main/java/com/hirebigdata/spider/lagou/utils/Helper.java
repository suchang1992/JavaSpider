package com.hirebigdata.spider.lagou.utils;

import com.hirebigdata.spider.lagou.config.MongoConfig;
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

    static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(Helper.class);
    static final int SLEEP_SECOND_WHEN_COUNTER_500 = 2;
    static final int MAX_TRY_COUNT_WHEN_COUNTER_500 = 5;

    public static void main(String[] args) {
//        isExistInMongoDB(MyMongoClient.getMongoClient(), MongoConfig.dbName, MongoConfig.collectionLagouCompanyDetail,
//                "Url", "123");
    }

    public static BasicDBObject getDocumentFromMongo(MongoClient mongoClient, String dbName,
                                                String collectionName, String field, String value){
        try {
            DB db = mongoClient.getDB(dbName);
            DBCollection collection = db.getCollection(collectionName);

            return (BasicDBObject)collection.findOne(new BasicDBObject().append(field, value));
        } catch (Exception e) {
            log.error(e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    public static boolean isExistInMongoDB(MongoClient mongoClient, String dbName,
                                           String collectionName, String field, String value) {
        try {
            DB db = mongoClient.getDB(dbName);
            DBCollection collection = db.getCollection(collectionName);

            if (field == "Url" && (value.indexOf("http://www.lagou.com/gongsi/") != 0))
                value = value.replaceAll("gongsi", "c");

            DBCursor cursor = collection.find(new BasicDBObject().append(field, value));

            return cursor.hasNext();
        } catch (Exception e) {
            log.error(e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    public static boolean updateMongoDB(MongoClient mongoClient, String dbName,
                                         String collectionName, ReflectionDBObject object,
                                         String field, String value){
        try {
            DB db = mongoClient.getDB(dbName);
            DBCollection collection = db.getCollection(collectionName);


            collection.update(
                    getDocumentFromMongo(mongoClient, dbName, collectionName, field, value),
                    object);

            return true;
        } catch (Exception e) {
            log.error(e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    public static boolean saveToMongoDB(MongoClient mongoClient, String dbName,
                                        String collectionName, ReflectionDBObject object) {
        try {
            DB db = mongoClient.getDB(dbName);
            DBCollection collection = db.getCollection(collectionName);

            collection.insert(object);

            return true;
        } catch (Exception e) {
            log.error(e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    public static String doGet(String url) {
        HttpClient client = HttpClientBuilder.create().build();
        HttpGet request = new HttpGet(url);
        try {
//            HttpResponse response = client.execute(request);
            int status_code = -1;
            int count = 0;
            while (status_code != 200 && count++ < MAX_TRY_COUNT_WHEN_COUNTER_500) {
                HttpResponse response = client.execute(request);
                status_code = response.getStatusLine().getStatusCode();
                if (status_code >= 500) {
                    // server side error, try again after some sleep
                    Thread.sleep(SLEEP_SECOND_WHEN_COUNTER_500 * 1000);
                    log.error("try " + url + " the " + count + " time");
                    continue;
                } else if (status_code == 404) {
                    log.error("counter 404 when process " + url);
                    return null;
                }
                return getHtml(response);
            }
            log.error("try too much with " + url + ", final status code is " + status_code);
            return null;
        } catch (Exception e) {
            log.error(e.getMessage());
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
            log.error(e.getMessage());
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
            log.error(e.getMessage());
            e.printStackTrace();
        } finally {
            try {
                if (fis != null)
                    fis.close();
            } catch (IOException ex) {
                log.error(ex.getMessage());
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
            log.error(e.getMessage());
            e.printStackTrace();
        } finally {
            try {
                if (fop != null) {
                    fop.close();
                }
            } catch (IOException e) {
                log.error(e.getMessage());
                e.printStackTrace();
            }
        }
    }
}
