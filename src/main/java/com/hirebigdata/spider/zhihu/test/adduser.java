package com.hirebigdata.spider.zhihu.test;

import com.hirebigdata.spider.zhihu.utils.Mongo;

import java.io.*;

/**
 * Created by Administrator on 2015/1/19.
 */
public class adduser {
    public static void main(String[] args) {
        File file = new File("./user_data_ids");
        try {
            InputStreamReader reader = new InputStreamReader(
                    new FileInputStream(file), "UTF-8");
            BufferedReader bufferedReader = new BufferedReader(reader);
            String line = "";
            while ((line = bufferedReader.readLine()) != null) {
                new Mongo().insertUserID("scrapy2", "zhihu_user_data_ids", line);
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }


    }
}
