package com.hirebigdata.spider.pojo;

import java.util.ArrayList;

/**
 * Created by Administrator on 2015/1/14.
 */
public class ZhihuUserFollowee {
    ArrayList<String> followees= new ArrayList<String>();

    public ArrayList<String> getFollowees() {
        return followees;
    }

    public void setFollowees(ArrayList<String> followees) {
        this.followees = followees;
    }
}
