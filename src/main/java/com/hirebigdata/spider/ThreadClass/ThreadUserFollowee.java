package com.hirebigdata.spider.ThreadClass;

import com.hirebigdata.spider.main.Spider;
import com.hirebigdata.spider.pojo.ZhihuUserFollowee;
import com.hirebigdata.spider.utils.HttpUtil;
import com.hirebigdata.spider.utils.Mongo;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.concurrent.Callable;

/**
 * Created by Administrator on 2015/1/14.
 */
public class ThreadUserFollowee implements Runnable{
    ZhihuUserFollowee zhihuUserFollowee = new ZhihuUserFollowee();
    int pageNum = 1;
    String User_data_id;
    String User_Url_name;

    public ThreadUserFollowee(String user_data_id, String user_Url_name) {
        User_data_id = user_data_id;
        User_Url_name = user_Url_name;
    }

    @Override
    public void run() {
        String html = null;
        try {
            html = new HttpUtil().get("http://www.zhihu.com/people/"
                    + User_Url_name + "/followees", Spider.getHeader());
            Document page = Jsoup.parse(html);
            Elements els = page.getElementById("zh-profile-follows-list").getElementsByAttributeValue("data-follow","m:button");
            for(Element e: els){
                zhihuUserFollowee.getFollowees().add(e.attr("data-id"));
            }
            new Mongo().upsertUserFollowee(User_data_id, zhihuUserFollowee);
            return ;
        } catch (IOException e) {
            e.printStackTrace();
            return ;
        }
    }
}
