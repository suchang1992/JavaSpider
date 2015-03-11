package com.hirebigdata.spider.Callable;

import com.hirebigdata.spider.main.Spider;
import com.hirebigdata.spider.pojo.ZhihuUser;
import com.hirebigdata.spider.pojo.ZhihuUserFollower;
import com.hirebigdata.spider.utils.HttpUtil;
import com.hirebigdata.spider.utils.Mongo;
import org.json.JSONArray;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;

/**
 * Created by Administrator on 2015/1/14.
 */
public class CallableUserFollower implements Callable {
    ZhihuUserFollower zhihuUserFollower = new ZhihuUserFollower();
    int pageNum = 1;
    String User_data_id;
    String User_Url_name;

    public CallableUserFollower(String user_data_id, String user_Url_name) {
        User_data_id = user_data_id;
        User_Url_name = user_Url_name;
    }

    public ZhihuUserFollower call() throws Exception {
        String html = new HttpUtil().get("http://www.zhihu.com/people/"
                + User_Url_name + "/followers", Spider.getHeader());
        Document page = Jsoup.parse(html);
        Elements els = page.getElementById("zh-profile-follows-list").getElementsByAttributeValue("data-follow","m:button");
        for(Element e: els){
            zhihuUserFollower.getFollowers().add(e.attr("data-id"));
        }
        Element e = page.getElementsByAttributeValue("href", "/people/" + User_Url_name + "/followers").first();
        String count = e.getElementsByTag("strong").first().text();
        Integer countInt = Integer.parseInt(count);
        Integer offsetInt = 20;
        while (countInt > 20 && offsetInt < (countInt+21)){
            String offset = Integer.toString(offsetInt);
            HashMap<String, String> params = new HashMap<String, String>();
            params.put("method", "next");
            params.put("params", "{\"offset\":" + offset + ",\"order_by\":\"created\",\"hash_id\":\"a4ca7a2400c67cd0e7a026c81183a8a5\"}");
            params.put("_xsrf", "3968f17621eeb31ce6ac15848765bf99");
            String moreUser = new HttpUtil().post("http://www.zhihu.com/node/ProfileFollowersListV2",
                    params, Spider.getHeader());
            JSONObject jsonObj = new JSONObject(moreUser);
            JSONArray msg = jsonObj.getJSONArray("msg");
            for (int i=0; i<msg.length(); i++){
                String f = (String)msg.get(i);
                Document followee = Jsoup.parse(f);
                String dataId = followee.getElementsByAttributeValue("data-follow","m:button").attr("data-id");
                zhihuUserFollower.getFollowers().add(dataId);
            }
            offsetInt += 20;
        }
        new Mongo().upsertUserFollower(User_data_id,zhihuUserFollower);
        return zhihuUserFollower;

//        Map<String, String> data = new HashMap<String, String>();
//        data.put("method", "next");
//        data.put("params", "{\"offset\":0,\"order_by\":\"created\",\"hash_id\":\""+User_data_id+"\"}");
//        data.put("_xsrf",User__xsrf_value);
//        return zhihuUserFollower;
    }


}