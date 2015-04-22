package com.hirebigdata.spider.zhihu.Callable;

import com.hirebigdata.spider.zhihu.main.Spider;
import com.hirebigdata.spider.zhihu.pojo.Topic;
import com.hirebigdata.spider.zhihu.pojo.ZhihuUserTopic;
import com.hirebigdata.spider.zhihu.utils.HttpUtil;
import com.hirebigdata.spider.zhihu.utils.Mongo;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.concurrent.Callable;

/**
 * Created by Administrator on 2015/1/13.
 */
public class CallableUserTopic implements Callable {
    ZhihuUserTopic zhihuUserTopic = new ZhihuUserTopic();
    String User_data_id;
    String User_Url_name;

    public CallableUserTopic(String user_data_id, String user_Url_name) {
        User_data_id = user_data_id;
        User_Url_name = user_Url_name;
    }

    @Override
    public Integer call() throws Exception {
        int flag = getTopicDetil(User_Url_name, zhihuUserTopic);
        if (flag == -1)
            flag = getTopicDetil(User_Url_name, zhihuUserTopic);

        new Mongo().upsertUserTopic(User_data_id, zhihuUserTopic);
        return 0;
    }

    private int getTopicDetil(String User_Url_name, ZhihuUserTopic zhihuUserTopic){
        String html = null;
        try {
            html = new HttpUtil().get("http://www.zhihu.com/people/" + User_Url_name + "/topics", Spider.getHeader());
            if (html.equals("404")) {
                return -1;
            }
            Document page = Jsoup.parse(html);
            Elements els= page.getElementById("zh-profile-topic-list").getElementsByAttributeValue("class","zm-profile-section-main");
            for(Element e : els){
                Topic topic = new Topic();
                topic.setUrl(e.getElementsByTag("a").get(1).attr("href"));
                topic.setName(e.getElementsByTag("a").get(1).text());
                topic.setContent(e.getElementsByAttributeValue("class", "zm-editable-content").first().text());
                topic.setAnswers_count(e.getElementsByAttributeValue("class", "zg-link-gray").first().text());
                zhihuUserTopic.getTopics().add(topic);
            }
            return 0;
        } catch (IOException e) {
            e.printStackTrace();
            return -1;
        }
    }
}