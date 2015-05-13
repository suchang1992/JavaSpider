package com.hirebigdata.spider.zhihu.utils;

import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.CookieStore;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.cookie.Cookie;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.cookie.BasicClientCookie;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HTTP;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by Administrator on 2015/3/30.
 */
public class GetCookies {

    public CookieStore cs = new BasicCookieStore();

    public static void main(String[] args) {
        GetCookies zhihu = new GetCookies();
        String _xsrf = zhihu.getXsrf("http://www.zhihu.com");
        HashMap<String, String> zhihuFormData = new HashMap<String, String>();
//        zhihuFormData.put("email", "jianguo.bai@hirebigdata.cn");
//        zhihuFormData.put("password", "wsc111111");
        zhihuFormData.put("email", "524471505@qq.com");
        zhihuFormData.put("password", "a12345678");
        zhihuFormData.put("_xsrf", _xsrf);
        zhihu.doPost(
                "http://www.zhihu.com/login",
                zhihuFormData
        );
        String result2 = zhihu.doGet("http://www.zhihu.com/");
        Document doc = Jsoup.parse(result2);
        System.out.println(doc.select(".zu-top-nav-userinfo ").get(0).select(".name").text());
        System.out.println(zhihu.getLoginCookie());
        zhihu.doGet("http://www.zhihu.com/logout");
    }

    public String login() {
        String _xsrf = getXsrf("http://www.zhihu.com");
        HashMap<String, String> zhihuFormData = new HashMap<String, String>();
        zhihuFormData.put("email", "jianguo.bai@hirebigdata.cn");
        zhihuFormData.put("password", "wsc111111");
        zhihuFormData.put("_xsrf", _xsrf);
        doPost("http://www.zhihu.com/login", zhihuFormData);
        return getLoginCookie();
    }

    public String getXsrf(String url) {
        HttpClient client = HttpClientBuilder.create().build();
        HttpGet request = new HttpGet(url);
        try {
            HttpResponse response = client.execute(request);
            Document doc = Jsoup.parse(getHtml(response));
            return doc.getElementsByAttributeValue("name", "_xsrf").get(0).attr("value").toString();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public String doGet(String url) {
        HttpClient client = HttpClientBuilder.create().build();
        HttpGet request = new HttpGet(url);
        try {
            installCookie(request);
            HttpResponse response = client.execute(request);
            updateCookie(response);

            return getHtml(response);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }

    }

    public static String getHtml(HttpResponse response) {
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

    public void installCookie(HttpRequestBase request) {
        String cookieStr = "";
        List<Cookie> list = cs.getCookies();
        for (Cookie cookie : list) {
            cookieStr += cookie.getName() + "=" + cookie.getValue() + ";";
        }
        if (cookieStr.length() > 1) {
            request.addHeader("Cookie", cookieStr);
        }
    }

    public void updateCookie(HttpResponse response) {
        Header[] headers = response.getHeaders("Set-Cookie");
        for (Header h : headers) {
            String name = h.getName();
            String value = h.getValue();

            if ("Set-Cookie".equalsIgnoreCase(name)) {
                String[] tempStr = value.split(";");
                for (String str : tempStr) {
                    String[] cookies = str.split("=", 2);
                    if (cookies.length == 1)
                        cs.addCookie(new BasicClientCookie(cookies[0], ""));
                    else
                        cs.addCookie(new BasicClientCookie(cookies[0], cookies[1]));
                }
            }
        }
    }

    public String getLoginCookie() {
        String cookieStr = "";
        List<Cookie> list = cs.getCookies();
        for (Cookie cookie : list) {
            cookieStr += cookie.getName() + "=" + cookie.getValue() + ";";
        }
        return cookieStr;
    }

    public static void installFormData(HashMap<String, String> parameter, HttpPost request) {
        List<NameValuePair> formData = new ArrayList<NameValuePair>();
        for (String key : parameter.keySet()) {
            formData.add(new BasicNameValuePair(key, parameter.get(key)));
        }
        request.setEntity(new UrlEncodedFormEntity(formData, HTTP.DEF_CONTENT_CHARSET));
    }

    public String doPost(String url, HashMap<String, String> formData) {
        HttpClient client = HttpClientBuilder.create().build();
        HttpPost request = new HttpPost(url);

        installFormData(formData, request);

        installCookie(request);
        try {
            HttpResponse response = client.execute(request);
            updateCookie(response);

            return getHtml(response);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}

