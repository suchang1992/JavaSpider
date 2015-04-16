package com.hirebigdata.spider.zhilian.resume;

import com.mongodb.ReflectionDBObject;

/**
 * User: shellbye.com@gmail.com
 * Date: 2015/4/15
 */
public class RawResume extends ReflectionDBObject {
    String cvId = "";
    String link = "";
    String rawHtml = "";

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
