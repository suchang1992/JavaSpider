package com.hirebigdata.spider.zhilian.resume;

import org.apache.http.client.HttpClient;

/**
 * User: shellbye.com@gmail.com
 * Date: 2015/4/27
 */
public class ContactResume extends RawResume {
    String name = "";
    String contact = "";

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getContact() {
        return contact;
    }

    public void setContact(String contact) {
        this.contact = contact;
    }
}
