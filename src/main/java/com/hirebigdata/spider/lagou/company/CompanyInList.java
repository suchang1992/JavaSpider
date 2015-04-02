package com.hirebigdata.spider.lagou.company;

import com.mongodb.ReflectionDBObject;

/**
 * User: shellbye.com@gmail.com
 * Date: 2015/4/2
 */
public class CompanyInList  extends ReflectionDBObject {
    String category = "";
    String name = "";
    String url = "";

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }
}
