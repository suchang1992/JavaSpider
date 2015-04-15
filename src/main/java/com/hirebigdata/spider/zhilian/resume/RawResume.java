package com.hirebigdata.spider.zhilian.resume;

import com.mongodb.ReflectionDBObject;

/**
 * User: shellbye.com@gmail.com
 * Date: 2015/4/15
 */
public class RawResume extends ReflectionDBObject {
    String cvId = "";
    String rawHtml = "";

    public String getCvId() {
        return cvId;
    }

    public void setCvId(String cvId) {
        this.cvId = cvId;
    }

    public String getRawHtml() {
        return rawHtml;
    }

    public void setRawHtml(String rawHtml) {
        this.rawHtml = rawHtml;
    }
}
