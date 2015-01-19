package com.hirebigdata.spider.pojo;

import com.alibaba.fastjson.JSONObject;

import java.util.ArrayList;

/**
 * Created by Administrator on 2015/1/13.
 */
public class ZhihuUserQuestion {
    ArrayList<Question> questions = new ArrayList<Question>();

    public ArrayList<Question> getQuestions() {
        return questions;
    }

    public void setQuestions(ArrayList<Question> questions) {
        this.questions = questions;
    }

}
