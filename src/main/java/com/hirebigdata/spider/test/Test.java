package com.hirebigdata.spider.test;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.hirebigdata.spider.main.Spider;
import com.hirebigdata.spider.pojo.*;
import com.hirebigdata.spider.utils.HttpUtil;
import com.hirebigdata.spider.utils.Mongo;
import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import org.bson.types.BasicBSONList;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.io.IOException;
import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

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
