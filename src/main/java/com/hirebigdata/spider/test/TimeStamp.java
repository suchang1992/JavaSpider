package com.hirebigdata.spider.test;

import com.mongodb.BasicDBObject;
import com.mongodb.MongoClient;
import com.mongodb.ServerAddress;

import java.net.UnknownHostException;
import java.text.Format;
import java.text.SimpleDateFormat;

/**
 * Created by Administrator on 2015/3/23.
 */
public class TimeStamp {
    public static void main(String[] args) {
        long l = System.currentTimeMillis();
        System.out.println(l);
        Format f = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        System.out.println(f.format(l));
        MongoClient mongoClient = null;
        try {
            mongoClient = new MongoClient(new ServerAddress("127.0.0.1", 27017));
            BasicDBObject obj = new BasicDBObject("user_data_id","123")
                    .append("crawled_successfully", false).append("fetched", false)
                    .append("crawled_count", 410).append("last_crawled_time",l);
            mongoClient.getDB("scrapy2").getCollection("zhihu_user_data_ids").save(obj);
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }



    }
}
