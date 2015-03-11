package com.hirebigdata.spider.test;

import com.hirebigdata.spider.pojo.Question_Cache;
import com.hirebigdata.spider.utils.Mongo;

/**
 * Created by Administrator on 2015/3/9.
 */
public class TestQuestion_Cache {
    public static void main(String[] args) {
        Question_Cache question_cache = new Question_Cache();
        question_cache.setId("2");
        new Mongo().upsertQuestion_Cache(question_cache);
    }
}
