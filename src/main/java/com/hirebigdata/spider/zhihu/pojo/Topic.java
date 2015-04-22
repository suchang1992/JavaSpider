package com.hirebigdata.spider.zhihu.pojo;

/**
 * Created by Administrator on 2015/1/9.
 */
public class Topic {
    String url = "";
    String content = "";
    String answers_count = ""; //???
    String name = "";

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getAnswers_count() {
        return answers_count;
    }

    public void setAnswers_count(String answers_count) {
        this.answers_count = answers_count;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
