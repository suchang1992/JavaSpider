package com.hirebigdata.spider.zhihu.test;

import com.hirebigdata.spider.zhihu.main.Spider;

/**
 * Created by Administrator on 2015/1/9.
 */
public class Test1 {

    public static void main(String[] args) {
        new Spider().spiderContent("6bec872206d9884cd9535841b6a1f510");
//        new Spider().spiderContent("7240b2ae38836f4837c2d7355b2ee72d");
//        new Spider().spiderContent("2f14cd0e84b90882b53c5ab626674357");//专栏
//        new Spider().spiderContent("0b5786ea81cea06b261773dff4e3dcd5");//回答 提问
//        new Mongo().insertUserID("scrapy2", "zhihu_user_data_ids", "7240b2ae38836f4837c2d7355b2ee72d");
    }

}
