package com.hirebigdata.spider.zhilian.main;

import java.io.*;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

import com.hirebigdata.spider.lagou.config.MongoConfig;
import com.hirebigdata.spider.lagou.utils.Helper;
import com.hirebigdata.spider.lagou.utils.MyMongoClient;
import com.hirebigdata.spider.zhilian.config.ZhiLianConfig;
import com.hirebigdata.spider.zhilian.resume.RawResume;
import com.hirebigdata.spider.zhilian.utils.HttpUtils;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.conn.ConnectTimeoutException;
import org.apache.http.impl.client.HttpClientBuilder;
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
    HttpClient httpClient;
    static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(CrawlZhiLian.class);

    CrawlZhiLian() {
        // 如果5秒还没有得到服务器的回复，则超时，然后重新发送
        int timeout = 10 * 1000;
        RequestConfig.Builder builder = RequestConfig.custom();
        builder.setConnectionRequestTimeout(timeout);
        builder.setConnectTimeout(timeout);
        builder.setSocketTimeout(timeout);
        RequestConfig requestConfig = builder.build();

        httpClient = HttpClientBuilder.create().setDefaultRequestConfig(requestConfig).build();
    }

    public static void main(String[] args) throws Exception {
        CrawlZhiLian zhiLian = new CrawlZhiLian();
        zhiLian.tryToLogin();
        List<String> keywords = new ArrayList<>();
//        keywords.add("java");
//        keywords.add("php");
//        keywords.add("python");
//        keywords.add("c/c++");
//        keywords.add("lisp");
//        keywords.add("ruby");
//        keywords.add("linux");
//        keywords.add("css");
//        keywords.add("html");
//        keywords.add("js");
//        keywords.add("javascript");
//        keywords.add("web");
//        keywords.add("后台");
//        keywords.add("前端");
//        keywords.add("安卓");
//        keywords.add("android");
        keywords.add("服务器");
        keywords.add("server");
        keywords.add("研发");
        keywords.add("开发");
        keywords.add("负责");
        keywords.add("认真");
        keywords.add("外向");
        keywords.add("端正");
        keywords.add("漂亮");
        keywords.add("积极");
        for (String keyword : keywords) {
            log.info("start keyword: " + keyword);
            zhiLian.getResumeWithKeyword(keyword);
        }
    }

    public void getResumeWithKeyword(String keyword) throws Exception {
        HttpGet getFirstPage = new HttpGet("http://rdsearch.zhaopin.com/Home/ResultForCustom?SF_1_1_1="
                + URLEncoder.encode(keyword, "UTF-8") + "&orderBy=DATE_MODIFIED,1&SF_1_1_27=0&exclude=1");
        getFirstPage.setHeader("Referer", "http://rdsearch.zhaopin.com/home/SearchByCustom");

        HttpResponse getResponse3 = this.getResponse(getFirstPage);
        Document doc = Jsoup.parse(HttpUtils.getHtml(getResponse3));
        getFirstPage.releaseConnection();

        String page = doc.select("#rd-resumelist-pageNum").first().text().split("/")[1];

        int pageNum = Integer.parseInt(page);
        while (true) {
            log.warn("process page 1");
            List<RawResume> rawResumeListPage1 = processHttpGet(getFirstPage, keyword);
            getFirstPage.releaseConnection();
            Helper.multiSaveToMongoDB(MyMongoClient.getMongoClient(), MongoConfig.dbNameZhilian,
                    MongoConfig.collectionZhilianResume, rawResumeListPage1);
            break;
        }
        getFirstPage.releaseConnection();
        try {
            for (int i = 2; i <= pageNum; i++) {
                List<RawResume> rawResumeList = getMoreResume(keyword, i);
                Helper.multiSaveToMongoDB(MyMongoClient.getMongoClient(), MongoConfig.dbNameZhilian,
                        MongoConfig.collectionZhilianResume, rawResumeList);
            }
        } catch (Exception e) {
            log.error("page " + page, e);
        }
    }

    public List<RawResume> getMoreResume(String keyword, int page) throws Exception {
        log.warn("process page " + page);
        HttpGet getPages = new HttpGet("http://rdsearch.zhaopin.com/Home/ResultForCustom?SF_1_1_1="
                + URLEncoder.encode(keyword, "UTF-8") + "&orderBy=DATE_MODIFIED,1&SF_1_1_27=0&exclude=1&pageIndex=" + page);
        getPages.setHeader("Referer", "http://rdsearch.zhaopin.com/Home/ResultForCustom?SF_1_1_1="
                + URLEncoder.encode(keyword, "UTF-8") + "&orderBy=DATE_MODIFIED,1&SF_1_1_27=0&exclude=1&pageIndex=" + (page - 1));
        return processHttpGet(getPages, keyword);
    }

    public List<RawResume> processHttpGet(HttpGet getPage, String keyword) {
        HttpResponse getResponse3 = this.getResponse(getPage);
        Document doc = Jsoup.parse(HttpUtils.getHtml(getResponse3));
        getPage.releaseConnection();
        Elements resumes = doc.select(".info");
        List<RawResume> rawResumeList = new ArrayList<>();
        for (Element e : resumes) {
            RawResume rawResume = new RawResume();
            rawResume.setCvId(e.attr("tag"));
            rawResume.setLink(e.select("a").attr("href"));
            rawResume.setKeyword(keyword);
            while (true) {
                Document resumeDoc;
                HttpGet getResumeHtml = new HttpGet(rawResume.getLink());
                log.warn("           get resume of " + rawResume.getLink());
                HttpResponse response = this.getResponse(getResumeHtml);
                String resumeHtml = HttpUtils.getHtml(response);
                // 不加这一句就只能获取两个简历文本
                getResumeHtml.releaseConnection();
                resumeDoc = Jsoup.parse(resumeHtml);
                if (resumeDoc.select("#resumeContentBody").first() != null) {
                    rawResume.setRawHtml(resumeDoc.select("#resumeContentBody").first().html());
                    break;
                } else {
                    if (resumeDoc.select("#resumeContentHead").first() != null) {
                        log.warn("resume deleted " + rawResume.getCvId());
                        break;
                    }
                    while (true) {
                        log.warn("!!!!!! code appears !!!!!!.");
                        String code = getValidateCode("http://rd2.zhaopin.com/s/loginmgr/" +
                                "monitorvalidatingcode.asp?t=" + System.currentTimeMillis() / 1000L);

                        HttpPost codePost = new HttpPost(ZhiLianConfig.CHECK_VALIDATING_CODE + code);
                        HttpResponse codeResponse = this.getResponse(codePost);
                        String result = Helper.getHtml(codeResponse);
                        codePost.releaseConnection();
                        if ("true".equals(result)) {
                            break;
                        }
                    }
                }
            }
            rawResumeList.add(rawResume);
        }
        return rawResumeList;
    }

    public void logout() {
        HttpGet logout = new HttpGet("http://rd2.zhaopin.com/s/loginmgr/logout.asp");
        HttpResponse response = this.getResponse(logout);
        logout.releaseConnection();
        log.warn(response.toString());
        log.warn("logout");
    }

    public HttpResponse getResponse(HttpRequestBase requestBase) {
        while (true) {
            try {
                HttpResponse response = httpClient.execute(requestBase);
                return response;
            } catch (ConnectTimeoutException e5) {
                log.error("ConnectTimeoutException in validate code");
                this.logout();
                this.tryToLogin();
            } catch (IOException ioe) {
                log.error(ioe);
            }
        }
    }

    public void tryToLogin() {
        while (true) {
            log.warn("prepare to login");
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
            String validate = getValidateCode(ZhiLianConfig.PICTURE_TIME_STAMP + System.currentTimeMillis() / 1000L);
            params.add(new BasicNameValuePair("Validate", validate));
            params.add(new BasicNameValuePair("Submit", ""));
            try {
                httppost.setEntity(new UrlEncodedFormEntity(params, "UTF-8"));
            } catch (UnsupportedEncodingException ue) {
                log.error(ue);
            }
            HttpResponse postResponse = this.getResponse(httppost);
            log.warn("1st " + postResponse.toString());
            httppost.releaseConnection();
            if (postResponse.getStatusLine().getStatusCode() != 302) {
                log.warn("not 302, go on");
                continue;
            }

            Header[] header = postResponse.getHeaders("Location");
            String location = header[0].getValue();
            HttpGet httpget = new HttpGet(location);
            HttpResponse getResponse2 = this.getResponse(httpget);
            httpget.releaseConnection();
            log.warn("2nd " + getResponse2.toString());
            if (getResponse2.getStatusLine().getStatusCode() != 200) {
                log.warn("not 200, go on");
                continue;
            }
            log.warn("successfully login!");
            return;
        }
    }

    public File saveValidatePicture(String codePicUrl) {
        log.warn("enter getValidateCode");
        HttpGet getNewPicture = new HttpGet(codePicUrl);
        HttpResponse getResponse = this.getResponse(getNewPicture);
        HttpEntity httpEntity = getResponse.getEntity();
        getNewPicture.releaseConnection();

        byte[] b = new byte[1];
        try {
            DataInputStream di = new DataInputStream(httpEntity.getContent());
            File f = new File("codeImage.gif");
            FileOutputStream fo = new FileOutputStream(f);
            while (-1 != di.read(b, 0, 1))
                fo.write(b, 0, 1);
            di.close();
            fo.close();
            return f;
        } catch (IOException e5) {
            log.error(e5);
            return null;
        }
    }

    public String getValidateCode(String codePicUrl) {
        log.warn("enter getValidateCode");
        String line = "";
        while (line.length() < 4) {
            File f = saveValidatePicture(codePicUrl);
            if (f == null)
                continue;
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
            } catch (Exception e) {
                log.error(e);
                f.delete();
                return null;
            }
        }
        return line;
    }
}
