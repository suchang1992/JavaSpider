package com.hirebigdata.spider.zhihu.ThreadClass;

import com.hirebigdata.spider.zhihu.main.Spider;
import com.hirebigdata.spider.zhihu.pojo.ZhihuUser;
import com.hirebigdata.spider.zhihu.pojo.ZhihuUserDetil;
import com.hirebigdata.spider.zhihu.utils.HttpUtil;
import com.hirebigdata.spider.zhihu.utils.Mongo;
import org.apache.http.client.ClientProtocolException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;

/**
 * Created by Administrator on 2015/1/13.
 */
public class ThreadUserDetil implements Runnable {
    ZhihuUserDetil zhihuUserDetil = new ZhihuUserDetil();
    public ThreadUserDetil(ZhihuUser user) {
        this.zhihuUserDetil.setAnswers_count(user.getAnswers_count());
        this.zhihuUserDetil.setQuestions_count(user.getQuestions_count());
        this.zhihuUserDetil.setPosts_count(user.getPosts_count());
        this.zhihuUserDetil.setLogs_count(user.getLogs_count());
        this.zhihuUserDetil.setCollections_count(user.getCollections_count());
        this.zhihuUserDetil.setFollow_topics_count(user.getFollow_topics_count());
        this.zhihuUserDetil.setFollow_columns_count(user.getFollow_columns_count());
        this.zhihuUserDetil.setFollower_count(user.getFollower_count());
        this.zhihuUserDetil.setFollowee_count(user.getFollowee_count());
        this.zhihuUserDetil.setPersonal_page_view_count(user.getPersonal_page_view_count());
        this.zhihuUserDetil.setUser__xsrf_value(user.getUser__xsrf_value());
        this.zhihuUserDetil.setUser_data_id(user.getUser_data_id());
        this.zhihuUserDetil.setUrl_name(user.getUrl_name());
    }
    @Override
    public void run() {
        int flag = getUserDetil(zhihuUserDetil);
        int i = 0;
        while(flag == -1 && i++ < 3){
            flag = getUserDetil(zhihuUserDetil);
        }
        new Mongo().upsertUserDetil(zhihuUserDetil);
        return;
    }


    private int getUserDetil(ZhihuUserDetil zhihuUserDetil)  {
        try{
            String html = new HttpUtil().get("http://www.zhihu.com/people/" + zhihuUserDetil.getUrl_name()
                    + "/about", Spider.getHeader());
            if (html.equals("404")) {
                System.out.println( "->"+Thread.currentThread().getStackTrace()[1].getLineNumber());
                return -1;
            }
            Document page = Jsoup.parse(html);

            Element e = page.getElementsByAttributeValue("class","title-section ellipsis").first().getElementsByAttributeValue("class", "name").first();
            zhihuUserDetil.setName(e.text());//得到名字
            e = page.getElementsByAttributeValue("class","bio").first();//得到简介
            if(e!= null)
                zhihuUserDetil.setBio(e.text());
            e = page.getElementsByAttributeValue("class","icon icon-profile-male").first();
            if(e != null){//得到性别
                zhihuUserDetil.setGender("icon icon-profile-male");
            }else{
                e = page.getElementsByAttributeValue("class","icon icon-profile-female").first();
                if(e != null)
                    zhihuUserDetil.setGender("icon icon-profile-female");
            }
            e = page.getElementsByAttributeValue("class", "business item").first();
            if(e != null){
                zhihuUserDetil.setBusiness(e.attr("title"));
                Element el = e.getElementsByTag("a").first();
                if(el != null)
                    zhihuUserDetil.setBusiness_topic_url(el.attr("href"));
            }
            e = page.getElementsByAttributeValue("class","fold-item").first();
            if (e != null){
                zhihuUserDetil.setDescription(e.getElementsByAttributeValue("class","content").first().text());
            }
            e = page.getElementsByAttributeValue("class","zm-profile-header-user-weibo").first();
            if(e != null)
                zhihuUserDetil.setWeibo_url(e.attr("href"));
            Elements els = page.getElementsByAttributeValue("class","zm-profile-module-desc");
            Elements es = els.first().getElementsByTag("strong");
            zhihuUserDetil.setVote_count(es.first().text());
            zhihuUserDetil.setThank_count(es.get(1).text());
            zhihuUserDetil.setFav_count(es.get(2).text());
            zhihuUserDetil.setShare_count(es.get(3).text());
            e = els.get(1);
            for(Element employ : e.getElementsByTag("li")) {
                zhihuUserDetil.getEmployments().add(employ.attr("data-title")+" - "+employ.attr("data-sub-title"));
            }
            e = els.get(2);
            for(Element loction: e.getElementsByTag("li")){
                zhihuUserDetil.getLocations().add(loction.attr("data-title"));
            }
            e = els.get(3);
            for(Element edu : e.getElementsByTag("li")){
                zhihuUserDetil.getEducations().add(edu.attr("data-title")+" - "+edu.attr("data-sub-title"));
            }
            return 0;
        }catch (NullPointerException e){
            e.printStackTrace();
            return -1;
        } catch (ClientProtocolException e) {
            e.printStackTrace();
            return -1;
        } catch (IOException e) {
            e.printStackTrace();
            return -1;
        }
    }


}
