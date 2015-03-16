package com.hirebigdata.spider.Callable;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.hirebigdata.spider.main.Spider;
import com.hirebigdata.spider.pojo.Answer;
import com.hirebigdata.spider.pojo.Question_Cache;
import com.hirebigdata.spider.pojo.ZhihuUserAnswer;
import com.hirebigdata.spider.utils.HttpUtil;
import com.hirebigdata.spider.utils.Mongo;
import com.mongodb.DBObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.concurrent.Callable;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by Administrator on 2015/1/13.
 */
public class CallableUserAnswerV2 implements Callable {
    ZhihuUserAnswer zhihuUserAnswer = new ZhihuUserAnswer();
    int pageNum = 1;
    String Url_name;
    String User_data_id;

    public CallableUserAnswerV2(String user_data_id, String url_name, int pageNum) {
        User_data_id = user_data_id;
        Url_name = url_name;
        this.pageNum = pageNum;
    }

    public ZhihuUserAnswer call() throws Exception {
        String html = new HttpUtil().get("http://www.zhihu.com/people/" + Url_name
                + "/answers?page=" + pageNum, Spider.getHeader());
        if (html.equals("404")) {
            return zhihuUserAnswer;
        }
        Document page = Jsoup.parse(html);
        Elements allAnswersElements = page
                .getElementsByAttributeValue("id", "zh-profile-answer-list")
                .first().getElementsByAttributeValue("class", "zm-item");
        for (Element e : allAnswersElements) {
            Answer answer = new Answer();
            answer.setAnswer_id(e.getElementsByAttributeValue("class", "question_link").attr("href"));
            answer.setAnswer_title(e.getElementsByAttributeValue("class", "question_link").text());
            answer.setAnswer_vote_up(getIntVoteFromString(e.getElementsByAttributeValue("class", "zm-item-vote-count").attr("data-votecount")));
            answer.setAnswer_content(e.getElementsByAttributeValue("class", "content hidden").text());
            answer.setAnswer_time(getTime(answer.getAnswer_content()));
            int flag = getAnswerDetil(answer);
            int i = 0;
            while( flag == -1 && i++ < 5){
                Thread.sleep(500);
                flag = getAnswerDetil(answer);
            }
//            new Mongo().pushUserAnswer(User_data_id, answer);//单个返回
            zhihuUserAnswer.getAnswers().add(answer);
        }
        return zhihuUserAnswer;
    }

    private int getAnswerDetil(Answer answer) throws IOException {
        String question_id =answer.getAnswer_id().split("/answer")[0];
        DBObject object = new Mongo().FindInQuestion(question_id);
        if (object != null){
            JSONArray tags = JSON.parseArray(object.get("tags").toString());
            for (int i = 0; i<tags.size();i++) {
                answer.getAnswer_tags().add(tags.getString(i).trim());
            }
            return 0;
        }
        try{
            String html = new HttpUtil().get("http://www.zhihu.com" + answer.getAnswer_id(), Spider.getHeader());
            Document page = Jsoup.parse(html);
            Question_Cache question_cache = new Question_Cache();
            question_cache.setId(question_id);
            Element el = page.getElementsByAttributeValue("class", "zm-tag-editor-labels zg-clear").first();
            if (el != null) {
                for (Element element : el.getElementsByTag("a")) {
                    answer.getAnswer_tags().add(element.text());
                    question_cache.getTags().add(element.text());
                }
            }else{
//            System.out.println(answer.getAnswer_id()+" "+html);
                answer.getAnswer_tags().clear();
                return -1;
            }
            el = page.getElementsByAttributeValue("class", "zm-item-title zm-editable-content").first().getElementsByTag("a").first();
            if (el != null)
                question_cache.setTitle(el.text());
            el = page.getElementsByAttributeValue("class","zg-link-litblue").first();
            if (el != null)
                question_cache.setAnswer_count(el.text().replace("查看全部 ",""));
            el = page.getElementsByAttributeValue("href",question_id+"/followers").first();
            if (el != null)
                question_cache.setFollower_count(el.text());
            new Mongo().upsertQuestion_Cache(question_cache);
            return 0;
        }catch (NullPointerException exception){
            exception.printStackTrace();
            System.out.println("->"+answer.getAnswer_id());
            return -1;
        }
    }

    private String getIntVoteFromString(String vote) {
        int v = 0;
        try {
            v = Integer.parseInt(vote);
        } catch (NumberFormatException n) {
            if (vote.contains("K")) {
                v = Integer.parseInt(vote.replace("K", "").trim());
                v = v * 1000;
            }
            if (vote.contains("W")) {
                v = Integer.parseInt(vote.replace("W", "").trim());
                v = v * 10000;
            }
            if (vote.contains("M")) {
                v = (int)Double.parseDouble(vote.replace("M", "").trim());
                v = v * 100000;
            }
        }
        return ""+v;
    }
    public String getTime(String time) {
        if (time.equals(""))
            return "";
        try {
            String dataPattern3 = "(\\d{4}|[\\u7f16][\\u8f91][\\u4e8e]|[\\u53d1][\\u5e03][\\u4e8e])( |-)(\\d{2}|[\\u6628][\\u5929])(:|-| )(\\d{2})(:|)(\\d{2}|)";
            Pattern pattern = Pattern.compile(dataPattern3);
            Matcher match = pattern.matcher(time);
            match.find();
            String t = match.group();
            while (match.find()) {
                t = match.group();
            }
            Format f = new SimpleDateFormat("yyyy-MM-dd");
            if(t.indexOf(":") > 0)
                t = f.format(new Date());
//        System.out.println(t);
            return t;
        }catch (IllegalStateException e){
            e.printStackTrace();
            System.out.println("time:"+time);
            return "";
        }
    }
}