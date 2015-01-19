package com.hirebigdata.spider.Callable;

import com.hirebigdata.spider.main.Spider;
import com.hirebigdata.spider.pojo.ZhihuUserFollowee;
import com.hirebigdata.spider.utils.HttpUtil;
import com.hirebigdata.spider.utils.Mongo;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.concurrent.Callable;

/**
 * Created by Administrator on 2015/1/14.
 */
public class CallableUserFollowee implements Callable{
    ZhihuUserFollowee zhihuUserFollowee = new ZhihuUserFollowee();
    int pageNum = 1;
    String User_data_id;
    String User_Url_name;

    public CallableUserFollowee(String user_data_id, String user_Url_name) {
        User_data_id = user_data_id;
        User_Url_name = user_Url_name;
    }

    @Override
    public ZhihuUserFollowee call() throws Exception {
        String html = new HttpUtil().get("http://www.zhihu.com/people/"
                + User_Url_name + "/followees", Spider.getHeader());
        Document page = Jsoup.parse(html);
        Elements els = page.getElementById("zh-profile-follows-list").getElementsByAttributeValue("data-follow","m:button");
        for(Element e: els){
            zhihuUserFollowee.getFollowees().add(e.attr("data-id"));
        }
        new Mongo().upsertUserFollowee(User_data_id, zhihuUserFollowee);
        return zhihuUserFollowee;
    }
}
