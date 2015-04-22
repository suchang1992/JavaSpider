package com.hirebigdata.spider.zhihu.Callable;

import com.hirebigdata.spider.zhihu.main.Spider;
import com.hirebigdata.spider.zhihu.pojo.Question;
import com.hirebigdata.spider.zhihu.utils.HttpUtil;
import com.hirebigdata.spider.zhihu.utils.Mongo;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.io.IOException;
import java.util.concurrent.Callable;

/**
 * Created by Administrator on 2015/1/19.
 */
public class CallableQuestionDetil implements Callable{
    Element e;
    String User_data_id;

    public CallableQuestionDetil(Element e, String user_data_id) {
        this.e = e;
        User_data_id = user_data_id;
    }

    @Override
    public Integer call() throws Exception {
        Question question = new Question();
        question.setQuestion_id(e.getElementsByAttributeValue("class", "question_link").first().attr("href").trim());
        question.setQuestion_title(e.getElementsByAttributeValue("class", "question_link").first().text().trim());
        Element element = e.getElementsByAttributeValue("class", "meta zg-gray").first();
        String[] str = element.text().split(" • ");
        question.setQuestion_answer_count(str[1]);
        question.setQuestion_follower_count(str[2]);
        int flag = getQuestionDetil(question, User_data_id);
        int i = 0;
        while (flag == -1 && i++ < 5) {
            flag = getQuestionDetil(question, User_data_id);
        }
        new Mongo().pushUserQuestion(User_data_id,question);//单个返回

        return 0;
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
                question.setQuestion_view_count(element.getElementsByTag("strong").get(0).text());
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
