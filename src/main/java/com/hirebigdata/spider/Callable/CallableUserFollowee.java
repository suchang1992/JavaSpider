package com.hirebigdata.spider.Callable;

import com.hirebigdata.spider.main.Spider;
import com.hirebigdata.spider.pojo.ZhihuUserFollowee;
import com.hirebigdata.spider.utils.HttpUtil;
import com.hirebigdata.spider.utils.Mongo;
import org.json.JSONArray;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.HashMap;
import java.util.concurrent.Callable;

/**
 * Created by Administrator on 2015/1/14.
 */
public class CallableUserFollowee implements Callable {
    ZhihuUserFollowee zhihuUserFollowee = new ZhihuUserFollowee();
    int pageNum = 1;
    String User_data_id;
    String User_Url_name;
    String _xsrf;

    public CallableUserFollowee(String user_data_id, String user_Url_name, String _xsrf) {
        User_data_id = user_data_id;
        User_Url_name = user_Url_name;
        this._xsrf = _xsrf;
    }

    @Override
    public ZhihuUserFollowee call() throws Exception {
        String html = new HttpUtil().get("http://www.zhihu.com/people/"
                + User_Url_name + "/followees", Spider.getHeader());
        Document page = Jsoup.parse(html);
        Elements els = page.getElementById("zh-profile-follows-list").getElementsByAttributeValue("data-follow", "m:button");
        for (Element e : els) {
            zhihuUserFollowee.getFollowees().add(e.attr("data-id"));
        }
        Element e = page.getElementsByAttributeValue("href", "/people/" + User_Url_name + "/followees").first();
        String count = e.parent().getElementsByTag("strong").first().text();
        Integer countInt = Integer.parseInt(count);
        Integer offsetInt = 20;
        try {
            while (countInt > 20 && offsetInt < (countInt + 21)) {
                String offset = Integer.toString(offsetInt);
                HashMap<String, String> params = new HashMap<String, String>();
                params.put("method", "next");
                params.put("params", "{\"offset\":" + offset + ",\"order_by\":\"created\",\"hash_id\":\"\" + User_data_id + \"\"}");
                params.put("_xsrf", _xsrf);
                String moreUser = new HttpUtil().post("http://www.zhihu.com/node/ProfileFolloweesListV2",
                        params, Spider.getHeader());
                JSONObject jsonObj = new JSONObject(moreUser);
                JSONArray msg = jsonObj.getJSONArray("msg");
                for (int i = 0; i < msg.length(); i++) {
                    String f = (String) msg.get(i);
                    Document followee = Jsoup.parse(f);
                    String dataId = followee.getElementsByAttributeValue("data-follow", "m:button").attr("data-id");
                    System.out.println(dataId);
                    zhihuUserFollowee.getFollowees().add(dataId);
                }
                offsetInt += 20;
            }
        } catch (Exception ee) {
            //
        }
        new Mongo().upsertUserFollowee(User_data_id, zhihuUserFollowee);
        return zhihuUserFollowee;
    }
}
