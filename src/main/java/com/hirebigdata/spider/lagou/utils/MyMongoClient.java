package com.hirebigdata.spider.lagou.utils;

import com.hirebigdata.spider.lagou.config.MongoConfig;
import com.mongodb.MongoClient;

/**
 * User: shellbye.com@gmail.com
 * Date: 2015/4/2
 */
public class MyMongoClient {
    private static MongoClient mongoClient;

    private MyMongoClient(){}

    public static MongoClient getMongoClient(){
        if (mongoClient == null){
            synchronized (MyMongoClient.class){
                if (mongoClient == null){
                    try{
                        mongoClient = new MongoClient(MongoConfig.MongoDBUrl, MongoConfig.port);
                    }catch (Exception e){

                    }
                }
            }
        }
        return mongoClient;
    }

    public static void main(String[] args){
    }
}
