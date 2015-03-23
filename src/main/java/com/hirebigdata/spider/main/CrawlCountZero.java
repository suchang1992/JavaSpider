package com.hirebigdata.spider.main;

import com.hirebigdata.spider.utils.Mongo;

import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by Administrator on 2015/3/20.
 */
public class CrawlCountZero {
    static String DBname;
    static int count = 1;
    static int restart_count = 0;

    public static void fun(){
        DBname = "scrapy2";
        Format f = new SimpleDateFormat("yyyy-MM-dd:HH:mm:ss");
        String uid = "";
        String ret = "";
        System.out.println("start");

        try {
            while (true) {
                uid = new Mongo().getUseridOnlyZero(DBname, "zhihu_user_data_ids");
                new Mongo().startCrawl(DBname, "zhihu_user_data_ids", uid);//开始爬取
                System.out.print(count + ":" + uid);
                ret = new Spider().spiderContent(uid);
                if (ret.equals("success")) {
                    new Mongo().finishCrawl(DBname, "zhihu_user_data_ids", uid);//完成爬取
                } else {
                    new Mongo().errorCrawl(DBname, "zhihu_user_data_ids", uid);//错误爬取
                }
                System.out.println("->finish " + ret + " " + f.format(new Date()));
                count++;
            }
        } catch (NullPointerException e){
            try {
                Thread.sleep(1000*60*60);
                System.out.println("sleep 1小时 后继续");
            } catch (InterruptedException e1) {
                fun();
            }
        } catch (Exception e){
            e.printStackTrace();
            restart_count++;
            if (restart_count<50)
                fun();
            return;
        }
    }

    public static void main(String[] args) {
        CrawlCountZero.fun();
    }
}