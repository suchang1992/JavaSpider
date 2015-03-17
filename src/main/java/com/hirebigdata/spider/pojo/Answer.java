package com.hirebigdata.spider.pojo;

import java.util.ArrayList;

/**
 * Created by Administrator on 2015/1/9.
 */
public class Answer {
    String answer_title = "";
    String answer_time = "";
    String answer_vote_up = "";
    String answer_bio = "";
    String answer_content = "";
    ArrayList<String> answer_tags = new ArrayList<String>();
    String answer_id = "";

    public Answer() {
    }

    public Answer(String answer_title, String answer_id) {
        this.answer_title = answer_title;
        this.answer_id = answer_id;
    }

    public String getAnswer_title() {
        return answer_title;
    }

    public void setAnswer_title(String answer_title) {
        this.answer_title = answer_title;
    }

    public String getAnswer_time() {
        return answer_time;
    }

    public void setAnswer_time(String answer_time) {
        this.answer_time = answer_time;
    }

    public String getAnswer_vote_up() {
        return answer_vote_up;
    }

    public void setAnswer_vote_up(String answer_vote_up) {
        this.answer_vote_up = answer_vote_up;
    }

    public String getAnswer_bio() {
        return answer_bio;
    }

    public void setAnswer_bio(String answer_bio) {
        this.answer_bio = answer_bio;
    }

    public String getAnswer_content() {
        return answer_content;
    }

    public void setAnswer_content(String answer_content) {
        this.answer_content = answer_content;
    }

    public ArrayList<String> getAnswer_tags() {
        return answer_tags;
    }

    public void setAnswer_tags(ArrayList<String> answer_tags) {
        this.answer_tags = answer_tags;
    }

    public String getAnswer_id() {
        return answer_id;
    }

    public void setAnswer_id(String answer_id) {
        this.answer_id = answer_id;
    }

    @Override
    public String toString() {
        return super.toString();
    }
}
