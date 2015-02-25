package com.hirebigdata.spider.pojo;

import java.util.ArrayList;

/**
 * Created by Administrator on 2015/1/9.
 */
public class Question {
    String question_title = "";
    String question_answer_count = "0";
    String question_view_count = "0";
    String question_follower_count = "0";
    String question_id = "";
    ArrayList<String> tags = new ArrayList<String>();

    public String getQuestion_title() {
        return question_title;
    }

    public void setQuestion_title(String question_title) {
        this.question_title = question_title;
    }

    public String getQuestion_answer_count() {
        return question_answer_count;
    }

    public void setQuestion_answer_count(String question_answer_count) {
        this.question_answer_count = question_answer_count;
    }

    public String getQuestion_view_count() {
        return question_view_count;
    }

    public void setQuestion_view_count(String question_view_count) {
        this.question_view_count = question_view_count;
    }

    public String getQuestion_follower_count() {
        return question_follower_count;
    }

    public void setQuestion_follower_count(String question_follower_count) {
        this.question_follower_count = question_follower_count;
    }

    public String getQuestion_id() {
        return question_id;
    }

    public void setQuestion_id(String question_id) {
        this.question_id = question_id;
    }

    public ArrayList<String> getTags() {
        return tags;
    }

    public void setTags(ArrayList<String> tags) {
        this.tags = tags;
    }
}
