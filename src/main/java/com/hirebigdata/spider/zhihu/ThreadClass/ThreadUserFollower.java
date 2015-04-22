package com.hirebigdata.spider.zhihu.ThreadClass;

import com.hirebigdata.spider.zhihu.main.Spider;
import com.hirebigdata.spider.zhihu.pojo.ZhihuUserFollower;
import com.hirebigdata.spider.zhihu.utils.HttpUtil;
import com.hirebigdata.spider.zhihu.utils.Mongo;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;

/**
 * Created by Administrator on 2015/1/14.
 */
public class ThreadUserFollower implements Runnable {
    ZhihuUserFollower zhihuUserFollower = new ZhihuUserFollower();
    int pageNum = 1;
    String User_data_id;
    String User_Url_name;

    public ThreadUserFollower(String user_data_id, String user_Url_name) {
        User_data_id = user_data_id;
        User_Url_name = user_Url_name;
    }

    public void run() {
        String html = null;
        try {
            html = new HttpUtil().get("http://www.zhihu.com/people/"
                    + User_Url_name + "/followers", Spider.getHeader());
            Document page = Jsoup.parse(html);
            Elements els = page.getElementById("zh-profile-follows-list").getElementsByAttributeValue("data-follow","m:button");
            for(Element e: els){
                zhihuUserFollower.getFollowers().add(e.attr("data-id"));
            }
            new Mongo().upsertUserFollower(User_data_id,zhihuUserFollower);
            return ;

    //        Map<String, String> data = new HashMap<String, String>();
    //        data.put("method", "next");
    //        data.put("params", "{\"offset\":0,\"order_by\":\"created\",\"hash_id\":\""+User_data_id+"\"}");
    //        data.put("_xsrf",User__xsrf_value);
    //        return zhihuUserFollower;
        } catch (IOException e) {
            e.printStackTrace();
            return ;
        }
    }
}
