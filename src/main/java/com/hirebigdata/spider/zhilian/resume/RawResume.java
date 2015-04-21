package com.hirebigdata.spider.zhilian.resume;

import com.mongodb.ReflectionDBObject;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * User: shellbye.com@gmail.com
 * Date: 2015/4/15
 */
public class RawResume extends ReflectionDBObject {
    String cvId = "";
    String link = "";
    String rawHtml = "";
    String keyword = "";
    String crawled_time = new SimpleDateFormat("yyyy-MM-dd").format(new Date());

    public String getKeyword() {
        return keyword;
    }

    public void setKeyword(String keyword) {
        this.keyword = keyword;
    }

    public String getCrawled_time() {
        return crawled_time;
    }

    public void setCrawled_time(String crawled_time) {
        this.crawled_time = crawled_time;
    }

    public String getCvId() {
        return cvId;
    }

    public void setCvId(String cvId) {
        this.cvId = cvId;
    }

    public String getLink() {
        return link;
    }

    public void setLink(String link) {
        this.link = link;
    }

    public String getRawHtml() {
        return rawHtml;
    }

    public void setRawHtml(String rawHtml) {
        this.rawHtml = rawHtml;
    }

    public String toString(){
        return this.link;
    }
}
