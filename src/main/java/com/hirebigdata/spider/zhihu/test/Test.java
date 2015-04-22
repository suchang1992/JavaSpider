package com.hirebigdata.spider.zhihu.test;

import com.hirebigdata.spider.zhihu.utils.Mongo;
import com.mongodb.BasicDBObject;
import com.mongodb.DBCursor;

/**
 * Created by Administrator on 2015/1/12.
 */
public class Test {
    public static void main(String[] args) {
//        Answer answer = new Answer("aaa","111");
////        new Mongo().pushUserAnswer("123",answer);
//        BasicDBObject query = new BasicDBObject("user_data_id","123");
//        BasicDBObject quva = new BasicDBObject("answers",new BasicDBObject("$nin",JSON.toJSON(answer)));
//        BasicDBObject quset = new BasicDBObject("$push",JSON.toJSON(answer));
//        DBObject one = new Mongo().getColl("scrapy2", "user_profile").findOne(quva);
//        System.out.println(one.get("answers"));

        BasicDBObject cond = new BasicDBObject("fetched",false).append("crawled_count",new BasicDBObject("$gt",2));
        DBCursor cursor = new Mongo().getColl("scrapy2", "zhihu_user_data_ids").find(cond);
//        System.out.println(cursor.next().get("crawled_count"));
//        System.out.println(cursor.next().get("crawled_count"));
//        System.out.println(cursor.next().get("crawled_count"));
//        System.out.println(cursor.next().get("crawled_count"));
//        System.out.println(cursor.next().get("crawled_count"));

        System.out.println(cursor.next().get("user_data_id"));
        System.out.println(cursor.next().get("user_data_id"));
        System.out.println(cursor.next().get("user_data_id"));
        System.out.println(cursor.next().get("user_data_id"));
        System.out.println(cursor.next().get("user_data_id"));
    }
}
