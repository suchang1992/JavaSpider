package com.hirebigdata.spider.ThreadClass;

import com.hirebigdata.spider.main.Spider;
import com.hirebigdata.spider.pojo.Column;
import com.hirebigdata.spider.pojo.ZhihuUserColumn;
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
public class ThreadUserColumn implements Runnable{
    ZhihuUserColumn zhihuUserColumn = new ZhihuUserColumn();
    String User_data_id;
    String User_Url_name;

    public ThreadUserColumn(String user_data_id, String user_Url_name) {
        User_data_id = user_data_id;
        User_Url_name = user_Url_name;
    }
    @Override
    public void run() {
        int flag = getColumnDetil(User_Url_name, zhihuUserColumn);
        int i = 0;
        while( flag == -1 && i++ < 2){
            flag = getColumnDetil(User_Url_name, zhihuUserColumn);
        }
        new Mongo().upsertUserColumn(User_data_id, zhihuUserColumn);
        return ;
    }

    private int getColumnDetil(String User_Url_name, ZhihuUserColumn zhihuUserColumn) {
        String html = null;
        try {
            html = new HttpUtil().get("http://www.zhihu.com/people/" + User_Url_name + "/columns/followed", Spider.getHeader());
            if (html.equals("404")) {
                return -1;
            }
            Document page = Jsoup.parse(html);
            Elements els = page.getElementsByAttributeValue("class","zm-profile-section-item zg-clear");
            for ( Element e : els){
                Column column = new Column();
                column.setUrl(e.getElementsByAttributeValue("class", "zm-list-avatar-link").first().attr("href"));
                column.setName(e.getElementsByAttributeValue("class", "zm-profile-section-main").first().getElementsByAttributeValue("href", column.getUrl()).first().text());
                column.setDescription(e.getElementsByAttributeValue("class", "description").html());
                column.setMeta(e.getElementsByAttributeValue("class", "meta").text());
    //            new Mongo().pushUserColumn(User_data_id, column);
                zhihuUserColumn.getColumns().add(column);
            }
            return 0;
        } catch (IOException e) {
            e.printStackTrace();
            zhihuUserColumn.getColumns().clear();
            return -1;
        }
    }
}
