package com.hirebigdata.spider.main;

import com.hirebigdata.spider.utils.Mongo;

import java.io.*;
import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Queue;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * Created by Administrator on 2015/4/7.
 */
public class MainBigUser {

    static String DBname;
    static int count = 1;
    static int restart_count = 0;
    Queue<String> queue = new LinkedBlockingQueue<String>();
    public void fun(){
        DBname = "scrapy2";
        Format f = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String uid = "";
        String ret = "";
        System.out.println("start"+restart_count);

        try {
            while (true) {
                uid = queue.poll();
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
        }catch (Exception e){
            e.printStackTrace();
            restart_count++;
            if (restart_count<100)
                fun();
            return;
        }
    }

    public void readUid(){
        File file = new File("F:/bigUser.txt");
        InputStreamReader reader = null;
        try {
            reader = new InputStreamReader(
                    new FileInputStream(file), "UTF-8");
            BufferedReader bufferedReader = new BufferedReader(reader);
            String line = "";
            while ((line = bufferedReader.readLine()) != null) {
                queue.add(line);
            }
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        MainBigUser t = new MainBigUser();
        t.readUid();
        t.fun();

    }
}
