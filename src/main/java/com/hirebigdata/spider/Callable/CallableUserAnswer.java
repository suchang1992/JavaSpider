package com.hirebigdata.spider.Callable;

import com.hirebigdata.spider.main.Spider;
import com.hirebigdata.spider.pojo.Answer;
import com.hirebigdata.spider.pojo.ZhihuUser;
import com.hirebigdata.spider.pojo.ZhihuUserAnswer;
import com.hirebigdata.spider.utils.HttpUtil;
import com.hirebigdata.spider.utils.Mongo;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.Callable;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by Administrator on 2015/1/13.
 */
public class CallableUserAnswer implements Callable {
    ZhihuUserAnswer zhihuUserAnswer = new ZhihuUserAnswer();
    int pageNum = 1;
    String Url_name;
    String User_data_id;

    public CallableUserAnswer(String user_data_id, String url_name, int pageNum) {
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
            int flag = getAnswerDetil(answer);
            int i = 0;
            while( flag == -1 && i++ < 5){
                flag = getAnswerDetil(answer);
            }
//            new Mongo().pushUserAnswer(User_data_id, answer);//单个返回
            zhihuUserAnswer.getAnswers().add(answer);
        }
        return zhihuUserAnswer;
    }

    private int getAnswerDetil(Answer answer) throws IOException {
        try{
            String html = new HttpUtil().get("http://www.zhihu.com" + answer.getAnswer_id(), Spider.getHeader());
            Document page = Jsoup.parse(html);
            Element el = page.getElementsByAttributeValue("class", "zm-tag-editor-labels zg-clear").first();
            if (el != null) {
                for (Element element : el.getElementsByTag("a")) {
                    answer.getAnswer_tags().add(element.text());
                }
            }else{
//            System.out.println(answer.getAnswer_id()+" "+html);
                answer.getAnswer_tags().clear();
                return -1;
            }
            Element an = page.getElementById("zh-question-answer-wrap");
            el = an.getElementsByAttributeValue("class", "answer-date-link meta-item").first();
            if (el != null) {
                answer.setAnswer_time(getTime(el.text()));
            } else {
                el = an.getElementsByAttributeValue("class", "answer-date-link last_updated meta-item").first();
                answer.setAnswer_time(getTime(el.text()));
            }//获得时间
            el = an.getElementsByAttributeValue("class", "zu-question-my-bio").first();
            if (el != null)
                answer.setAnswer_bio(el.text());//获得特殊的简介
            el = an.getElementsByAttributeValue("data-action", "/answer/content").first();
            if(el !=null)
                answer.setAnswer_content(el.html());//获得回答全文
            else
                answer.setAnswer_content(an.getElementById("answer-status").text());
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
        Format f = new SimpleDateFormat("yyyy-MM-dd");
        if(time.indexOf(":") > 0)
            time = f.format(new Date());
        return time;
    }
}