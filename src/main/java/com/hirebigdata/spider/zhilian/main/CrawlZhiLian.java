package com.hirebigdata.spider.zhilian.main;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;

import com.hirebigdata.spider.zhilian.config.ZhiLianConfig;
import com.hirebigdata.spider.zhilian.resume.RawResume;
import com.hirebigdata.spider.zhilian.utils.HttpUtils;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
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

import com.ImagePreProcess3;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

/**
 * User: shellbye.com@gmail.com
 * Date: 2015/4/15
 */
public class CrawlZhiLian {
    HttpClient httpclient = HttpClientBuilder.create().build();
    CookieStore cs = new BasicCookieStore();
    public static void main(String[] args) throws Exception{
        CrawlZhiLian zhiLian = new CrawlZhiLian();
        zhiLian.tryToLogin();
        zhiLian.getResumeWithKeyword("java");
    }

    public void getResumeWithKeyword(String keyword) throws Exception {
        HttpGet httpget2 = new HttpGet("http://rdsearch.zhaopin.com/Home/ResultForCustom?SF_1_1_1="
                + keyword + "&orderBy=DATE_MODIFIED,1&SF_1_1_27=0&exclude=1");
        httpget2.setHeader("Referer", "http://rdsearch.zhaopin.com/home/SearchByCustom");

        HttpResponse getResponse3 = httpclient.execute(httpget2);
        Document doc = Jsoup.parse(HttpUtils.getHtml(getResponse3));

        String page = doc.select("#rd-resumelist-pageNum").first().text().split("/")[1];

        int pageNum = Integer.parseInt(page);
        List<RawResume> rawResumeListAll = processHttpGet(httpget2);
        for (int i=2; i<=pageNum; i++){
            rawResumeListAll.addAll(getMoreResume(keyword, i));
        }

        System.out.println(rawResumeListAll.size());

    }

    public List<RawResume> getMoreResume(String keyword, int page) throws Exception{
        System.out.println("process page " + page);
        Thread.sleep(10 * 1000);
        HttpGet httpget2 = new HttpGet("http://rdsearch.zhaopin.com/Home/ResultForCustom?SF_1_1_1="
                + keyword + "&orderBy=DATE_MODIFIED,1&SF_1_1_27=0&exclude=1&pageIndex=" + page);
        httpget2.setHeader("Referer", "http://rdsearch.zhaopin.com/Home/ResultForCustom?SF_1_1_1="
                + keyword + "&orderBy=DATE_MODIFIED,1&SF_1_1_27=0&exclude=1&pageIndex=" + (page-1));

        return processHttpGet(httpget2);
    }

    public List<RawResume> processHttpGet(HttpGet httpget2) throws Exception{
        HttpResponse getResponse3 = httpclient.execute(httpget2);
        Document doc = Jsoup.parse(HttpUtils.getHtml(getResponse3));
        Elements resumes = doc.select(".info");
        List<RawResume> rawResumeList = new ArrayList<>();
        for (Element e : resumes){
            System.out.println("           process resume element");
            RawResume rawResume = new RawResume();
            rawResume.setCvId(e.attr("tag"));
            rawResume.setLink(e.select("a").attr("href"));
            HttpGet getResumeHtml = new HttpGet(rawResume.getLink());
            HttpResponse response = httpclient.execute(getResumeHtml);

            String resumeHtml = HttpUtils.getHtml(response);
            Document resumeDoc = Jsoup.parse(resumeHtml);
            if (resumeDoc.select("#resumeContentBody").first() != null)
                rawResume.setRawHtml(resumeDoc.select("#resumeContentBody").first().text());
            else{
                // todo
                System.out.println("!!!!!! code, wait for ten second.");
                Thread.sleep(10 * 1000);
            }
            rawResumeList.add(rawResume);
            // 不加这一句就只能获取两个简历文本
            getResumeHtml.releaseConnection();
        }
        httpget2.releaseConnection();

        return rawResumeList;
    }

    public void tryToLogin() throws IOException {
        while (true) {
            long nowTimeStamp = System.currentTimeMillis();
            long nowTime = nowTimeStamp / 1000;
            HttpPost httppost = new HttpPost(ZhiLianConfig.LOGIN_POST + "?DYWE="
                    + nowTimeStamp + "." + 438478 + "." + nowTime + "."
                    + nowTime + "." + 1);
            httppost.setHeader("Referer", "http://hr.zhaopin.com/hrclub/index.html");
            httppost.setHeader("Origin", "http://hr.zhaopin.com");
            httppost.setHeader("Host", "rd2.zhaopin.com");

            List<NameValuePair> params = new ArrayList<>(2);
            params.add(new BasicNameValuePair("username", "jiri59483132"));
            params.add(new BasicNameValuePair("password", "linxiaohua87860519"));

            // validate
            String validate = getValidateCode();
            params.add(new BasicNameValuePair("Validate", validate));
            params.add(new BasicNameValuePair("Submit", ""));
            httppost.setEntity(new UrlEncodedFormEntity(params, "UTF-8"));
            HttpResponse postResponse = httpclient.execute(httppost);
            System.out.println(postResponse.toString());
            if (postResponse.getStatusLine().getStatusCode() != 302) {
                continue;
            }
            Header[] header = postResponse.getHeaders("Location");
            String location = header[0].getValue();
            httppost.releaseConnection();

            HttpGet httpget = new HttpGet(location);
            HttpResponse getResponse2 = httpclient.execute(httpget);
            System.out.println(getResponse2.toString());
            httpget.releaseConnection();
            if (getResponse2.getStatusLine().getStatusCode() != 200) {
                continue;
            }
            System.out.println("successfully login!");
            return;
        }
    }

    public File saveValidatePicture() {
        try {
            String a = ZhiLianConfig.PICTURE_TIME_STAMP + System.currentTimeMillis() / 1000L;
            HttpGet httpgetNewPicture = new HttpGet(a);
            HttpResponse getResponse = httpclient.execute(httpgetNewPicture);
            HttpEntity httpEntity = getResponse.getEntity();

            byte[] b = new byte[1];
            DataInputStream di = new DataInputStream(httpEntity.getContent());
            File f = new File("codeImage.gif");
            FileOutputStream fo = new FileOutputStream(f);
            while (-1 != di.read(b, 0, 1))
                fo.write(b, 0, 1);
            di.close();
            fo.close();
            return f;
        } catch (Exception e) {
            return null;
        }
    }

    public String getValidateCode() {
        String line = "";
        while (line.length() < 4) {
            File f = saveValidatePicture();
            try {
                // 因为ImagePreProcess3的main函数是直接将结果打印了出来，
                // 所以这里我对console的输出做了重定向，打印到文件中，
                // 然后从文件中读取结果
                String arg = f.getAbsolutePath() + " c:\\train";
                File f2 = new File("c:\\output1.txt");
                PrintStream out = new PrintStream(new FileOutputStream(f2));
                PrintStream old_out = System.out;
                System.setOut(out);
                // get the code
                ImagePreProcess3.main(arg.split(" "));
                System.setOut(old_out);
                BufferedReader br2 = new BufferedReader(new FileReader(f2));
                if ((line = br2.readLine()) != null) {
                    continue;
                } else {
                    line = "";
                }
                f2.delete();
                f.delete();
            } catch (IOException e) {
                e.printStackTrace();
                f.delete();
                return null;
            }catch (Exception e) {
                e.printStackTrace();
                f.delete();
                return null;
            }
        }
        return line;
    }

    public static void installCookie(HttpRequestBase request, CookieStore cs) {
        String cookieStr = "";
        List<Cookie> list = cs.getCookies();
        for (Cookie cookie : list) {
            cookieStr += cookie.getName() + "=" + cookie.getValue() + ";";
        }
        if (cookieStr.length() > 1) {
            request.addHeader("Cookie", cookieStr);
        }
    }

    public static void updateCookie(HttpResponse response, CookieStore cs) {
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
}
