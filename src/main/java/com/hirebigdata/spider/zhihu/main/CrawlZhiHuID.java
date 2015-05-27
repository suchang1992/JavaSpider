package com.hirebigdata.spider.zhihu.main;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.hirebigdata.spider.zhihu.utils.GetCookies;
import com.hirebigdata.spider.zhihu.utils.Mongo;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.BasicHttpEntity;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HTTP;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * Created by Administrator on 2015/5/11.
 */
public class CrawlZhiHuID implements Runnable{

    static InputStreamReader inputStreamReader;
    String keyword = "";

    static {
        inputStreamReader = readUidInputStream();
    }

    CrawlZhiHuID(String keyword){
        this.keyword = keyword;
    }

    public static void main(String[] args) {
        String keyword = getkeyword();
        ExecutorService executorService = Executors.newFixedThreadPool(110);
        for (int i=keyword.length()-1; i>0; i--){
            System.out.print(i);
            System.out.println(keyword.charAt(i));
            executorService.execute(new CrawlZhiHuID(String.valueOf(keyword.charAt(i))));
        }
        executorService.shutdown();
    }

    public void run(){
//        CrawlZhiHuID crawlZhiHuID = new CrawlZhiHuID();
        this.crawlManager();
    }

    public void crawlManager() {
        GetCookies zhihu = new GetCookies();
        String cookie = zhihu.login();
        while (true) {

            int offset = 10;
            String url = "http://www.zhihu.com/r/search?q=" + keyword + "&type=people&offset=";
            System.out.println("access："+keyword+" offset："+offset);
            String nextUrl = getDataID(url + offset, cookie);
            while (nextUrl != null) {
                offset += 10;
                System.out.println("access："+keyword+" offset："+offset);
                nextUrl = getDataID("http://www.zhihu.com"+nextUrl, cookie);
                try {
                    System.out.println("sleep one second");
                    TimeUnit.MILLISECONDS.sleep(500);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public static String getkeyword(){
        char[] buf =new char[1024 * 3];
        try {
            inputStreamReader.read(buf);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return new String(buf);
    }
    public String getDataID(String url, String cookie) {
        String s = doGet(url, cookie);
        JSONObject jsonObject = JSON.parseObject(s);
        String string = jsonObject.getJSONObject("paging").getString("next");
        JSONArray jsonArray = jsonObject.getJSONArray("htmls");

        for (int i = 0; i < jsonArray.size(); i++) {
            Document usethtml = Jsoup.parse(jsonArray.getString(i));
            String dataID = usethtml.getElementsByAttributeValue("data-follow", "m:button").attr("data-id");
            new Mongo().insertUserID("scrapy2", "zhihu_user_data_ids", dataID);
        }
        return string;
    }

    public static InputStreamReader readUidInputStream(){
        File file = new File("key.txt");
        InputStreamReader reader = null;
        try {
            reader = new InputStreamReader(
                    new FileInputStream(file), "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return reader;
    }


    public String doPost(String url, HashMap<String, String> formData, String cookie) {
        HttpClient client = HttpClientBuilder.create().build();
        HttpPost request = new HttpPost(url);
        installFormData(formData, request);
        request.addHeader("Cookie", cookie);
        try {
            HttpResponse response = client.execute(request);
            return getHtml(response);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public String doGet(String url, String cookie) {
        HttpClient client = HttpClientBuilder.create().build();
        HttpGet request = new HttpGet(url);
        try {
            request.addHeader("Cookie", cookie);
            HttpResponse response = client.execute(request);
            return getHtml(response);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public String getHtml(HttpResponse response) {
        StringBuffer result = new StringBuffer();
        try {
            BufferedReader rd = new BufferedReader(
                    new InputStreamReader(response.getEntity().getContent()));
            String line;
            while ((line = rd.readLine()) != null) {
                result.append(line);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return result.toString();
    }

    public void installFormData(HashMap<String, String> parameter, HttpPost request) {
        List<NameValuePair> formData = new ArrayList<NameValuePair>();
        for (String key : parameter.keySet()) {
            formData.add(new BasicNameValuePair(key, parameter.get(key)));
        }
        request.setEntity(new UrlEncodedFormEntity(formData, HTTP.DEF_CONTENT_CHARSET));
    }
}
