package com.hirebigdata.spider.main;

import com.hirebigdata.spider.Callable.*;
import com.hirebigdata.spider.pojo.*;
import com.hirebigdata.spider.utils.*;
import com.mongodb.BasicDBObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.*;

/**
 * Created by Administrator on 2015/1/4.
 */
public class Spider {
    private static Map<String, String> header = new HashMap<String, String>();

    public String spiderContent(String UID) {
        ZhihuUser user = new ZhihuUser();
        //第一步 访问了 www.zhihu.com/people/uid
        String[] IDandName = getUserUrlName(UID);//返回 uid + name 并为id赋值 [2]为html
        if (IDandName[0] != null && !IDandName[2].equals("404")) {
            user = getManyCount(user, IDandName);//先简单分析得到数量 准备线程所需数据
            //分析需要开启的线程数量
            int[] array =  getThreadCount(user);//0:question 1:answer 2:话题 3:专栏 4:Follower 5:Followee
            int count = (array[0]+array[1])+3;
            ExecutorService pool = Executors.newFixedThreadPool(20);//创建线程池
            //创建线程
            pool.submit(new CallableUserDetil(user));//线程加入 线程池中
            pool.submit(new CallableUserSkill_Topic(IDandName[2],user.getUser_data_id()));
            if(array[2]>=1)
                pool.submit(new CallableUserTopic(user.getUser_data_id(), user.getUrl_name()));
            if(array[3]>=1)
                pool.submit(new CallableUserColumn(user.getUser_data_id(), user.getUrl_name()));
            if(array[4]>=1)
                pool.submit(new CallableUserFollower(user.getUser_data_id(),user.getUrl_name()));
            if(array[5]>=1)
                pool.submit(new CallableUserFollowee(user.getUser_data_id(),user.getUrl_name()));
            FutureTask<ZhihuUserQuestion>[] quesionTasks = new FutureTask[array[0]+1];
            if(array[0]>=1){
                for(int i=1 ;i<=array[0];i++){
                    quesionTasks[i] = new FutureTask<ZhihuUserQuestion>(new CallableUserQuestion(user.getUser_data_id(),user.getUrl_name(),i));
                    pool.submit(quesionTasks[i]);
                }
            }
            FutureTask<ZhihuUserAnswer>[] answerTasks = new FutureTask[array[1]+1];
            if(array[1]>=1){
                for(int i=1 ;i<=array[1];i++){
                    answerTasks[i] = new FutureTask<ZhihuUserAnswer>(new CallableUserAnswer(user.getUser_data_id(),user.getUrl_name(),i));
                    pool.submit(answerTasks[i]);
                }
            }
            try {
//                while (!pool.isTerminated()) {
//                    pool.awaitTermination(1, TimeUnit.SECONDS);
//                }
                ZhihuUserQuestion zhihuUserQuestion = new ZhihuUserQuestion();
                if(array[0]>=1) {
                    for (int i = 1; i <= array[0]; i++) {
                        zhihuUserQuestion.getQuestions().addAll(quesionTasks[i].get().getQuestions());
                    }
                    new Mongo().upsertUserQuestion(user.getUser_data_id(), zhihuUserQuestion);
                }
                ZhihuUserAnswer zhihuUserAnswer = new ZhihuUserAnswer();
                if(array[1]>=1) {
                    for (int i = 1 ; i <= array[1] ; i++){
                        zhihuUserAnswer.getAnswers().addAll(answerTasks[i].get().getAnswers());
                    }
                    new Mongo().upsertUserAnswer(user.getUser_data_id(), zhihuUserAnswer);
                }
                pool.shutdown();
                return "success";
            }
            catch (ExecutionException e){
                e.printStackTrace();
                pool.shutdown();
                return "error";
            }
            catch (InterruptedException e) {
                e.printStackTrace();
                pool.shutdown();
                return "error";
            }
        }else
            return "error";
    }

    //返回当前uid或者name 在 @collection里面的个数
    public static int findUserByUIDorName(String UID, String collection){
        BasicDBObject user = new BasicDBObject();
        user.append("uid",UID);
        int count = (int) new Mongo().getColl("user", collection).count(user);
        return count;
    }

    public static ZhihuUser getManyCount(ZhihuUser zhihuUser,String[] IDandName){
        Document page = null;
        page = Jsoup.parse(IDandName[2]);
        Elements els = page.getElementsByAttributeValue("class", "profile-navbar clearfix").first().getElementsByClass("item");
        zhihuUser.setQuestions_count(els.get(1).getElementsByTag("span").first().text());
        zhihuUser.setAnswers_count(els.get(2).getElementsByTag("span").first().text());
        zhihuUser.setPosts_count(els.get(3).getElementsByTag("span").first().text());
        zhihuUser.setCollections_count(els.get(4).getElementsByTag("span").first().text());
        zhihuUser.setLogs_count(els.get(5).getElementsByTag("span").first().text());
        zhihuUser.setUrl_name(IDandName[1]);
        zhihuUser.setUser_data_id(IDandName[0]);

        Element e = page.getElementsByAttributeValue("href", "/people/" + IDandName[1] + "/columns/followed").first();
        if (e != null)
            zhihuUser.setFollow_columns_count(e.text());
        e = page.getElementsByAttributeValue("href", "/people/" + IDandName[1] + "/topics").first();
        if(e != null)
            zhihuUser.setFollow_topics_count(e.text());
        els = page.getElementsByAttributeValue("class","zg-gray-normal");
        e = els.first();
        zhihuUser.setFollowee_count(e.parent().getElementsByTag("strong").first().text());
        e = els.get(1);
        zhihuUser.setFollower_count(e.parent().getElementsByTag("strong").first().text());
        e = els.get(2);
        zhihuUser.setPersonal_page_view_count(e.getElementsByTag("strong").first().text());
        zhihuUser.setUser__xsrf_value(page.getElementsByAttributeValue("name","_xsrf").first().attr("value"));
        new Mongo().upsertUser(zhihuUser);
        return zhihuUser;
    }
    public String[] getUserUrlName(String UID) {
        String[] IDandName = new String[3];
        int flag = getUserUrlName(UID, IDandName);
        int i = 0;
        while( flag == -1 && i++ <2){
            flag = getUserUrlName(UID, IDandName);
        }
        return IDandName;
    }
    private int getUserUrlName(String UID, String[] IDandName){
        String html = "";
        Document page = null;
        try {
            html = new HttpUtil().get("http://www.zhihu.com/people/" + UID, getHeader());
            if (html.equals("404")) {
                IDandName[0] = null;
                IDandName[1] = null;
                IDandName[2] = "404";
                return -1;
            }
            page = Jsoup.parse(html);
            String s = page.getElementsByTag("meta").get(5).attr("content");
            IDandName[0] = UID;
            IDandName[1] = new String(s.getBytes(), 45, s.length() - 45);
            IDandName[2] = html;
            return 0;
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("error:"+UID);
            IDandName[0] = null;
            IDandName[1] = null;
            IDandName[2] = "404";
            return -1;
        } catch (IndexOutOfBoundsException e){
            e.printStackTrace();
            IDandName[0] = null;
            IDandName[1] = null;
            IDandName[2] = "404";
            return -1;
        } catch (NullPointerException e){
            e.printStackTrace();
            IDandName[0] = null;
            IDandName[1] = null;
            IDandName[2] = "404";
            return -1;
        }
    }

    public static int[] getThreadCount(ZhihuUser user){
        int[] array =  new int[6];
        array[0] = (int)Math.ceil(Double.parseDouble(user.getQuestions_count())/20.0);
        array[1] = (int)Math.ceil(Double.parseDouble(user.getAnswers_count())/20.0);
        array[2] = (int)Math.ceil(Double.parseDouble(user.getFollow_topics_count().replace(" 个话题","")));
        array[3] = (int)Math.ceil(Double.parseDouble(user.getFollow_columns_count().replace(" 个专栏","")));
        array[4] = (int)Math.ceil(Double.parseDouble(user.getFollower_count())/20.0);
        array[5] = (int)Math.ceil(Double.parseDouble(user.getFollowee_count())/20.0);
        return array;
    }

    public static Map<String, String> getHeader() {
        header.put("Origin", "http://www.zhihu.com");
        header.put("Host", "www.zhihu.com");
        header.put("Connection", "keep-alive");
        header.put("Accept-Encoding", "gzip,deflate,sdch");
        header.put("Referer", "http://www.zhihu.com/people/fenng/followees");
        // header.put("Content-Type",
        // "application/x-www-form-urlencoded; charset=UTF-8");
        header.put("Accept", "gzip, deflate");
        header.put("Accept-Language", "	zh-cn,zh;q=0.8,en-us;q=0.5,en;q=0.3");
        header.put(
                "cookie",
                "   q_c1=ccfd92e359e54617a0ff06a03ca42b92|1421637754000|1421637754000; z_c0=\"QUFDQWdQcEdBQUFYQUFBQVlRSlZUWG9GNUZRRlRoVk9JcjVkLXFBRWVJakdqTTUydUlhYWRnPT0=|1421637754|0ba1acefabd2db982d5be8996970d005128bb7b5\"; _xsrf=f3ac2e80dd818aa786427b5b0f777b38; __utma=51854390.879657291.1421637754.1421637754.1421637754.1; __utmb=51854390.3.10.1421637754; __utmc=51854390; __utmz=51854390.1421637754.1.1.utmcsr=zhihu.com|utmccn=(referral)|utmcmd=referral|utmcct=/; __utmv=51854390.100--|2=registration_date=20150104=1^3=entry_date=20150104=1; __utmt=1");
        header.put("User-Agent",
                "Mozilla/5.0 (Windows NT 6.1; WOW64; rv:32.0) Gecko/20100101 Firefox/32.0");
        // header.put("X-Requested-With", "XMLHttpRequest");

        return header;
    }


}
