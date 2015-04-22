package com.hirebigdata.spider.zhihu.pojo;

import java.util.ArrayList;

/**
 * Created by Administrator on 2015/1/13.
 */
public class ZhihuUserAnswer {
    ArrayList<Answer> answers = new ArrayList<Answer>();

    public ArrayList<Answer> getAnswers() {
        return answers;
    }

    public void setAnswers(ArrayList<Answer> answers) {
        this.answers = answers;
    }
}
