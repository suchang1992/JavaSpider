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
    String _xsrf;
    int offset;

    public CallableUserFollower(String user_data_id, String user_Url_name, String _xsrf, int offset) {
        User_data_id = user_data_id;
        User_Url_name = user_Url_name;
        this._xsrf = _xsrf;
        this.offset = offset;
    }

    public ZhihuUserFollower call() throws Exception {
        if (offset == 0){
            try {
                String html = new HttpUtil().get("http://www.zhihu.com/people/"
                        + User_Url_name + "/followers", Spider.getHeader());
                Document page = Jsoup.parse(html);
                Elements els = page.getElementById("zh-profile-follows-list").getElementsByAttributeValue("data-follow", "m:button");
                for (Element e : els) {
                    zhihuUserFollower.getFollowers().add(e.attr("data-id"));
                }
            }catch (NullPointerException exc){
                //
            }
        }else{
            try {
                HashMap<String, String> params = new HashMap<String, String>();
                params.put("method", "next");
                params.put("params", "{\"offset\":" + offset + ",\"order_by\":\"created\",\"hash_id\":\"" + User_data_id + "\"}");
                params.put("_xsrf", _xsrf);
                String moreUser = new HttpUtil().post("http://www.zhihu.com/node/ProfileFollowersListV2",
                        params, Spider.getHeader());
                //429 Too Many Requests
                int maxTryCount = 10;
                while ((moreUser.length() < 5) && ((maxTryCount--) > 0)) {
                    Thread.sleep((10 - maxTryCount) * 1000);
                    moreUser = new HttpUtil().post("http://www.zhihu.com/node/ProfileFollowersListV2",
                            params, Spider.getHeader());
                }
                JSONObject jsonObj = new JSONObject(moreUser);
                JSONArray msg = jsonObj.getJSONArray("msg");
                for (int i = 0; i < msg.length(); i++) {
                    String f = (String) msg.get(i);
                    Document followee = Jsoup.parse(f);
                    String dataId = followee.getElementsByAttributeValue("data-follow", "m:button").attr("data-id");
                    zhihuUserFollower.getFollowers().add(dataId);
                }
            } catch (Exception ee) {
                System.out.println(offset);
                ee.printStackTrace();
            }
        }
        return zhihuUserFollower;

//        Map<String, String> data = new HashMap<String, String>();
//        data.put("method", "next");
//        data.put("params", "{\"offset\":0,\"order_by\":\"created\",\"hash_id\":\""+User_data_id+"\"}");
//        data.put("_xsrf",User__xsrf_value);
//        return zhihuUserFollower;
    }


}
