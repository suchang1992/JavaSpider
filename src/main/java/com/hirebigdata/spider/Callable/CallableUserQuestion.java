package com.hirebigdata.spider.Callable;

import com.hirebigdata.spider.main.Spider;
import com.hirebigdata.spider.pojo.Question;
import com.hirebigdata.spider.pojo.ZhihuUserQuestion;
import com.hirebigdata.spider.utils.HttpUtil;
import com.hirebigdata.spider.utils.Mongo;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.concurrent.Callable;

/**
 * Created by Administrator on 2015/1/13.
 */
public class CallableUserQuestion implements Callable {
    ZhihuUserQuestion zhihuUserQuestion = new ZhihuUserQuestion();
    int pageNum = 1;
    String User_data_id;
    String Url_name;

    public CallableUserQuestion(String user_data_id, String url_name, int pageNum) {
        this.pageNum = pageNum;
        User_data_id = user_data_id;
        Url_name = url_name;
    }

    public ZhihuUserQuestion call() throws Exception {
        String html = new HttpUtil().get("http://www.zhihu.com/people/" + Url_name
                + "/asks?page=" + pageNum, Spider.getHeader());
        if (html.equals("404")) {
            return zhihuUserQuestion;
        }
        Document page = Jsoup.parse(html);
        Elements allQuestionElements = page
                .getElementsByAttributeValue("id", "zh-profile-ask-list")
                .first().getElementsByAttributeValue("class", "zm-profile-section-item zg-clear");
        for (Element e : allQuestionElements) {
            Question question = new Question();
            //vote 修改为进入问题后再爬取
//            question.setView_count(e.getElementsByAttributeValue("class", "zm-profile-vote-num").text().trim());
            question.setQuestion_id(e.getElementsByAttributeValue("class", "question_link").first().attr("href").trim());
            question.setTitle(e.getElementsByAttributeValue("class", "question_link").first().text().trim());
            Element element = e.getElementsByAttributeValue("class", "meta zg-gray").first();
            String[] str = element.text().split(" • ");
            question.setAnswer_count(str[1]);
            question.setFollower_count(str[2]);
            int flag = getQuestionDetil(question, User_data_id);
            int i = 0;
            while (flag == -1 && i++ < 5) {
                flag = getQuestionDetil(question, User_data_id);
            }
            zhihuUserQuestion.getQuestions().add(question);
//            new Mongo().pushUserQuestion(User_data_id,question);//单个返回
        }
        return zhihuUserQuestion;
    }

    private int getQuestionDetil(Question question, String User_data_id){
        try {
            String questionhtml = new HttpUtil().get("http://www.zhihu.com" + question.getQuestion_id(), Spider.getHeader());
            Document questionpage = Jsoup.parse(questionhtml);
            Element element = questionpage.getElementsByAttributeValue("class", "zm-tag-editor zg-section").first();
            if (element != null) {
                for (Element el : element.getElementsByAttributeValue("class", "zm-item-tag")) {
                    question.getTags().add(el.text());
                }
                element = questionpage.getElementsByAttributeValue("class", "zg-gray-normal").get(2);
                question.setView_count(element.getElementsByTag("strong").get(0).text());
                return 0;
            } else {
//            System.out.println(question.getQuestion_id()+" "+questionhtml);
                question.getTags().clear();
                return -1;
            }
        } catch (IOException e) {
            return -1;
        } catch (NullPointerException e) {
            return  -1;
        } catch (IndexOutOfBoundsException e){
            return -1;
        }
    }
}