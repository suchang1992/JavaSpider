package com.hirebigdata.spider.lagou.company;

import com.mongodb.ReflectionDBObject;

/**
 * User: shellbye.com@gmail.com
 * Date: 2015/4/3
 */
public class Member extends ReflectionDBObject {
    String portrait = "";
    String name = "";
    String weibo = "";
    String position = "";
    String intro = "";

    public String getPortrait() {
        return portrait;
    }

    public void setPortrait(String portrait) {
        this.portrait = portrait;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getWeibo() {
        return weibo;
    }

    public void setWeibo(String weibo) {
        this.weibo = weibo;
    }

    public String getPosition() {
        return position;
    }

    public void setPosition(String position) {
        this.position = position;
    }

    public String getIntro() {
        return intro;
    }

    public void setIntro(String intro) {
        this.intro = intro;
    }
}
