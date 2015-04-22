package com.hirebigdata.spider.zhihu.test;

import com.hirebigdata.spider.zhihu.pojo.Question_Cache;
import com.hirebigdata.spider.zhihu.utils.Mongo;

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
