package com.hirebigdata.spider.zhihu.pojo;

import java.util.ArrayList;

/**
 * Created by Administrator on 2015/1/13.
 */
public class ZhihuUserTopic {
    ArrayList<Topic> topics = new ArrayList<Topic>();

    public ZhihuUserTopic() {
    }

    public ZhihuUserTopic(ArrayList<Topic> topics) {
        this.topics = topics;
    }

    public ArrayList<Topic> getTopics() {
        return topics;
    }

    public void setTopics(ArrayList<Topic> topics) {
        this.topics = topics;
    }
}
