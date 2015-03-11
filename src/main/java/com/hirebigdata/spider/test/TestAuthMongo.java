package com.hirebigdata.spider.test;

import com.mongodb.DB;
import com.mongodb.DBObject;
import com.mongodb.MongoClient;
import com.mongodb.ServerAddress;

import java.net.UnknownHostException;

/**
 * Created by Administrator on 2015/3/4.
 */
public class TestAuthMongo {
    public static void main(String[] args) {
        MongoClient mongoClientFrom = null;
        try {
            mongoClientFrom = new MongoClient(new ServerAddress("121.48.175.146", 27017));
            DB db = mongoClientFrom.getDB("scrapy2");
            db.authenticate("sc","123456".toCharArray());
            DBObject user_profile = db.getCollection("user_profile").findOne();
            System.out.println(user_profile);
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }

    }
}
