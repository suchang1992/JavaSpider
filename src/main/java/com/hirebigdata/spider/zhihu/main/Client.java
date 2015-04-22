package com.hirebigdata.spider.zhihu.main;

import com.hirebigdata.spider.zhihu.utils.Mongo;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Date;

/**
 * Created by Administrator on 2015/1/15.
 */
public class Client {
    static int count = 1;

    public static void main(String[] agrs) {
        try {
            Socket socket = new Socket("localhost", 4700);
            PrintWriter os = new PrintWriter(socket.getOutputStream());
            BufferedReader is = new BufferedReader(new InputStreamReader(
                    socket.getInputStream()));
            String User_data_id;
            User_data_id = is.readLine();
            while (User_data_id != null) {
                System.out.print(User_data_id);
                String ret = new Spider().spiderContent(User_data_id);
                //开始爬取
                new Mongo().startCrawl("scrapy2","zhihu_user_data_ids",User_data_id);
                if (ret.equals("success")) {;
                    //完成爬取
                    new Mongo().finishCrawl("scrapy2", "zhihu_user_data_ids", User_data_id);
                    count++;
                    os.println("continue");
                    os.flush();
                    User_data_id = is.readLine();
                } else if (ret.equals("error")) {
                    System.out.print(new Date() + " count:" + count + ", " + "error it " + User_data_id + "\n");
                    new Mongo().errorCrawl("scrapy2","zhihu_user_data_ids",User_data_id);
                    count++;
                    os.println("continue");
                    os.flush();
                    User_data_id = is.readLine();
                } else
                    User_data_id = null;
            }
            os.close();
            is.close();
            socket.close();
        } catch (IOException ioe) {
            System.out.println("io error");
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("client error");
        }
    }
}
