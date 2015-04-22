package com.hirebigdata.spider.zhihu.test;

import com.hirebigdata.spider.zhihu.utils.HttpUtil;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by Administrator on 2015/3/17.
 */
public class getIP {
    private static Map<String, String> header = new HashMap<String, String>();

    File file = new File("F:/aaa1.html");
    public void fun(){
        try {
            String html = new HttpUtil().get("http://1111.ip138.com/ic.asp", getHeader());
            Document page = Jsoup.parse(html);
            Element e = page.getElementsByTag("center").first();
            String dataPattern3 = "\\d{1,3}.\\d{1,3}.\\d{1,3}.\\d{1,3}";
            Pattern pattern = Pattern.compile(dataPattern3);
            Matcher match = pattern.matcher(e.text());
            match.find();
            String t = match.group();
            System.out.println(t);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }



    public static Map<String, String> getHeader() {
        header.put("Host", "1111.ip138.com");
        header.put("Connection", "keep-alive");
        header.put("Accept-Encoding", "gzip, deflate, sdch");
        header.put("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8");
        header.put("Accept-Language", "zh-CN,zh;q=0.8");
        header.put("Cache-Control","max-age=0");
        header.put("Referer","http://www.ip138.com/");
        header.put("User-Agent",
                "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/40.0.2214.115 Safari/537.36");
        return header;
    }

    public static void main(String[] args) {
        new getIP().fun();
    }
}
