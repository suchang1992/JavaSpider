package com.hirebigdata.spider.lagou.utils;

/**
 * User: shellbye.com@gmail.com
 * Date: 2015/4/2
 */
public class MongoClient2 {
    private static MongoClient2 mongoClient;

    private MongoClient2(){}

    public static MongoClient2 getMongoClient(){
        if (mongoClient == null){
            synchronized (MongoClient2.class){
                if (mongoClient == null){
                    mongoClient = new MongoClient2();
                }
            }
        }
        return mongoClient;
    }
}
