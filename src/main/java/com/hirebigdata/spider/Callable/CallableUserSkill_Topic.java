package com.hirebigdata.spider.Callable;

import com.hirebigdata.spider.pojo.Skilled_topic;
import com.hirebigdata.spider.pojo.ZhihuUserSkill_Topic;
import com.hirebigdata.spider.utils.Mongo;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.concurrent.Callable;

/**
 * Created by Administrator on 2015/1/15.
 */
public class CallableUserSkill_Topic implements Callable{
    ZhihuUserSkill_Topic zhihuUserSkill_topic = new ZhihuUserSkill_Topic();
    String html;
    String User_data_id;

    public CallableUserSkill_Topic(String html, String user_data_id) {
        this.html = html;
        User_data_id = user_data_id;
    }

    @Override
    public Integer call() throws Exception {
        Document page = Jsoup.parse(html);
        Element e = page.getElementsByAttributeValue("class","zm-profile-section-list zg-clear").first();
        if (e != null){
            Elements els = e.getElementsByAttributeValue("class", "item");
            for ( Element element : els){
                Skilled_topic skilled_topic = new Skilled_topic();
                skilled_topic.setName(element.getElementsByAttributeValue("class", "zg-gray-darker").text());
                skilled_topic.setUrl(element.attr("data-url-token"));
                skilled_topic.setVote(element.getElementsByAttributeValue("class","zg-icon vote").first().parent().text());
                skilled_topic.setComment(element.getElementsByAttributeValue("class","zg-icon comment").first().parent().text());
                zhihuUserSkill_topic.getSkilled_topics().add(skilled_topic);
            }
            new Mongo().upsertUserSkill(User_data_id,zhihuUserSkill_topic);
            return 0;
        }else {
            return 1;
        }
    }
}
