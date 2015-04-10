package com.hirebigdata.spider.lagou.company;

import com.mongodb.BasicDBObject;
import com.mongodb.ReflectionDBObject;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * User: shellbye.com@gmail.com
 * Date: 2015/4/10
 */
public class KeyWithCrawledTime extends ReflectionDBObject {
    String crawled_time = new SimpleDateFormat("yyyy-MM-dd").format(new Date());
    String key = "";

    public KeyWithCrawledTime(String key){
        this.key = key;
    }

    public static KeyWithCrawledTime getKeyWithCrawledTimeFromBasicDBObject(BasicDBObject dbObject){
        return new KeyWithCrawledTime((String)dbObject.get("Key"));
    }

    public String getCrawled_time() {
        return crawled_time;
    }

    public void setCrawled_time(String crawled_time) {
        this.crawled_time = crawled_time;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }
}
