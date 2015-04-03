package com.hirebigdata.spider.lagou.company;

import com.mongodb.ReflectionDBObject;

/**
 * User: shellbye.com@gmail.com
 * Date: 2015/4/3
 */
public class Product extends ReflectionDBObject {
    String name = "";
    String intro = "";
    String url = "";

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getIntro() {
        return intro;
    }

    public void setIntro(String intro) {
        this.intro = intro;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }
}
