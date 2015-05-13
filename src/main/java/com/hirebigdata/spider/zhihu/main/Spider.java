package com.hirebigdata.spider.zhihu.main;

import com.hirebigdata.spider.zhihu.Callable.*;
import com.hirebigdata.spider.zhihu.pojo.*;
import com.hirebigdata.spider.zhihu.utils.*;
import com.mongodb.BasicDBObject;
import com.mongodb.MongoException;
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

    public Spider(String cookie) {
        header.put("Origin", "http://www.zhihu.com");
        header.put("Host", "www.zhihu.com");
        header.put("Connection", "keep-alive");
        header.put("Accept-Encoding", "gzip,deflate,sdch");
        header.put("Referer", "http://www.zhihu.com/people/fenng/followees");
        // header.put("Content-Type",
        // "application/x-www-form-urlencoded; charset=UTF-8");
        header.put("Accept", "gzip, deflate");
        header.put("Accept-Language", "	zh-cn,zh;q=0.8,en-us;q=0.5,en;q=0.3");
        header.put("User-Agent",
                "Mozilla/5.0 (Windows NT 6.1; WOW64; rv:32.0) Gecko/20100101 Firefox/32.0");
        // header.put("X-Requested-With", "XMLHttpRequest");
        header.put("cookie", cookie);
    }

    public String spiderContent(String UID) throws MongoException{
        ZhihuUser user = new ZhihuUser();
        //第一步 访问了 www.zhihu.com/people/uid
        String[] IDandName = getUserUrlName(UID);//返回 uid + name 并为id赋值 [2]为html
        if (IDandName[0] != null && !IDandName[2].equals("404")) {
            try {
                user = getManyCount(user, IDandName);//先简单分析得到数量 准备线程所需数据
            }catch (NullPointerException e){
                System.out.print("->error " + UID);
                return "error";
            }
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
            int f_count = Integer.parseInt(user.getFollower_count());
            int c = f_count/20 + 1;//followers的页数
            c = c >= 3000 ? 3000 : c;
//            System.out.println(c);
            FutureTask<ZhihuUserFollower>[] followersTasks = new FutureTask[c];
            if(array[4]>=1){
                for (int i=0; i<c; i++){
                    int offset = i*20 > f_count ? f_count : i*20;
//                    System.out.println(offset);
                    followersTasks[i] = new FutureTask<ZhihuUserFollower>(new CallableUserFollower(
                            user.getUser_data_id(),
                            user.getUrl_name(),
                            user.getUser__xsrf_value(),
                            offset
                    ));
                    pool.submit(followersTasks[i]);
                }
            }
            user.getFollowee_count();
            if(array[5]>=1)
                pool.submit(new CallableUserFollowee(user.getUser_data_id(),user.getUrl_name(),user.getUser__xsrf_value()));
//            FutureTask<ZhihuUserQuestion>[] quesionTasks = new FutureTask[array[0]+1];
//            if(array[0]>=1){
//                for(int i=1 ;i<=array[0];i++){
//                    quesionTasks[i] = new FutureTask<ZhihuUserQuestion>(new CallableUserQuestion(user.getUser_data_id(),user.getUrl_name(),i));
//                    pool.submit(quesionTasks[i]);
//                }
//            }
            FutureTask<ZhihuUserAnswer>[] answerTasks = new FutureTask[array[1]+1];
            if(array[1]>=1){
                for(int i=1 ;i<=array[1];i++){
                    answerTasks[i] = new FutureTask<ZhihuUserAnswer>(new CallableUserAnswerV2(user.getUser_data_id(),user.getUrl_name(),i));
                    pool.submit(answerTasks[i]);
                }
            }
            pool.shutdown();
            try {
//                while (!pool.isTerminated()) {
//                    pool.awaitTermination(1, TimeUnit.SECONDS);
//                }
//                ZhihuUserQuestion zhihuUserQuestion = new ZhihuUserQuestion();
//                if(array[0]>=1) {
//                    for (int i = 1; i <= array[0]; i++) {
//                        zhihuUserQuestion.getQuestions().addAll(quesionTasks[i].get().getQuestions());
//                    }
//                    new Mongo().upsertUserQuestion(user.getUser_data_id(), zhihuUserQuestion);
//                }
                if(array[1]>=1) {
                    ZhihuUserAnswer zhihuUserAnswer = new ZhihuUserAnswer();
                    for (int i = 1 ; i <= array[1] ; i++){
                        try {
                            zhihuUserAnswer.getAnswers().addAll(answerTasks[i].get().getAnswers());
                        } catch (InterruptedException e) {
                            continue;
                        }
                    }
                    new Mongo().upsertUserAnswer(user.getUser_data_id(), zhihuUserAnswer);
                }
                if(array[4]>=1){
                    ZhihuUserFollower zhihuUserFollower = new ZhihuUserFollower();
                    for (int i=0; i<c; i++){
                        try {
                            zhihuUserFollower.getFollowers().addAll(followersTasks[i].get(1,TimeUnit.MINUTES).getFollowers());
                        } catch (InterruptedException e) {
                            break;
                        } catch (TimeoutException e) {
                            break;
                        }
                    }
                    new Mongo().upsertUserFollower(user.getUser_data_id(), zhihuUserFollower);
                }
                return "success";
            }
            catch (ExecutionException e){
                e.printStackTrace();
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
        zhihuUser.setUser__xsrf_value(page.getElementsByAttributeValue("name", "_xsrf").first().attr("value"));
        e = page.getElementsByAttributeValue("class","zm-profile-header-img zg-avatar-big zm-avatar-editor-preview").first();
        if(e != null)
            zhihuUser.setAvatar(e.attr("src"));
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
            System.out.println("->error "+UID);
            IDandName[0] = null;
            IDandName[1] = null;
            IDandName[2] = "404";
            return -1;
        } catch (NullPointerException e){
            e.printStackTrace();
            System.out.println("->error "+UID);
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
        return header;
    }



}
