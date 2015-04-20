package com.hirebigdata.spider.zhilian.main;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintStream;
import java.net.SocketTimeoutException;
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
import org.apache.http.conn.ConnectTimeoutException;
import org.apache.http.conn.HttpHostConnectException;
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
        int timeout = 5 * 1000;
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
//        zhiLian.logout();
        List<String> keywords = new ArrayList<>();
//        keywords.add("java");
//        keywords.add("php");
//        keywords.add("python");
//        keywords.add("c/c++");
//        keywords.add("lisp");
//        keywords.add("ruby");
//        keywords.add("linux");
        keywords.add("css");
        keywords.add("html");
        keywords.add("js");
        keywords.add("javascript");
        keywords.add("web");
        keywords.add("后台");
        keywords.add("前端");
        keywords.add("安卓");
        keywords.add("android");
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

        HttpResponse getResponse3 = httpClient.execute(getFirstPage);
        Document doc = Jsoup.parse(HttpUtils.getHtml(getResponse3));

        String page = doc.select("#rd-resumelist-pageNum").first().text().split("/")[1];

        int pageNum = Integer.parseInt(page);
        List<RawResume> rawResumeListPage1 = processHttpGet(getFirstPage);
        log.warn("process page 1");
        Helper.multiSaveToMongoDB(MyMongoClient.getMongoClient(), MongoConfig.dbNameZhilian,
                MongoConfig.collectionZhilianResume, rawResumeListPage1);
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
        return processHttpGet(getPages);
    }

    public List<RawResume> processHttpGet(HttpGet getPage) throws Exception {
        HttpResponse getResponse3 = httpClient.execute(getPage);
        Document doc = Jsoup.parse(HttpUtils.getHtml(getResponse3));
        getPage.releaseConnection();
        Elements resumes = doc.select(".info");
        List<RawResume> rawResumeList = new ArrayList<>();
        for (Element e : resumes) {
            log.warn("           process resume element");
            RawResume rawResume = new RawResume();
            rawResume.setCvId(e.attr("tag"));
            rawResume.setLink(e.select("a").attr("href"));
            try {
                while (true) {
                    Document resumeDoc;
                    try {
                        HttpGet getResumeHtml = new HttpGet(rawResume.getLink());
                        log.warn("           get resume of " + rawResume.getLink());
                        log.warn("           get resume html in try");
                        // todo stop here
                        HttpResponse response = httpClient.execute(getResumeHtml);
                        String resumeHtml = HttpUtils.getHtml(response);
                        // 不加这一句就只能获取两个简历文本
                        getResumeHtml.releaseConnection();
                        resumeDoc = Jsoup.parse(resumeHtml);
                    } catch (ConnectTimeoutException e5) {
                        this.logout();
                        this.tryToLogin();
                        log.error("ConnectTimeoutException in getting resume");
                        continue;
                    } catch (SocketTimeoutException se) {
                        log.error("SocketTimeoutException in getting resume");
                        continue;
                    }
                    log.warn("                     resume html got!");
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

                            try {
                                HttpPost codePost = new HttpPost(ZhiLianConfig.CHECK_VALIDATING_CODE + code);
                                HttpResponse codeResponse = httpClient.execute(codePost);
                                String result = Helper.getHtml(codeResponse);
                                codePost.releaseConnection();
                                if ("true".equals(result)) {
                                    break;
                                }
                            } catch (ConnectTimeoutException e5) {
                                this.logout();
                                this.tryToLogin();
                                log.error("ConnectTimeoutException in validate code");
                            } catch (SocketTimeoutException se) {
                                log.error("SocketTimeoutException in validate code");
                            }
                        }
                    }
                }
                rawResumeList.add(rawResume);
            } catch (HttpHostConnectException e1) {
                log.error(e1);
            }
        }
        return rawResumeList;
    }

    public void logout() throws Exception {
        HttpGet logout = new HttpGet("http://rd2.zhaopin.com/s/loginmgr/logout.asp");
        HttpResponse response = httpClient.execute(logout);
        log.warn(response.toString());
        log.warn("logout");
        logout.releaseConnection();
    }

    public void tryToLogin() throws IOException {
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
            httppost.setEntity(new UrlEncodedFormEntity(params, "UTF-8"));
            try {
                HttpResponse postResponse = httpClient.execute(httppost);
                log.warn("1st " + postResponse.toString());
                httppost.releaseConnection();
                if (postResponse.getStatusLine().getStatusCode() != 302) {
                    log.warn("not 302, go on");
                    continue;
                }

                Header[] header = postResponse.getHeaders("Location");
                String location = header[0].getValue();
                HttpGet httpget = new HttpGet(location);
                HttpResponse getResponse2 = httpClient.execute(httpget);
                log.warn("2nd " + getResponse2.toString());
                httpget.releaseConnection();
                if (getResponse2.getStatusLine().getStatusCode() != 200) {
                    log.warn("not 200, go on");
                    continue;
                }
                log.warn("successfully login!");
                return;
            } catch (ConnectTimeoutException e5) {
                log.error("ConnectTimeoutException in tryToLogin");
            } catch (SocketTimeoutException se) {
                log.error("SocketTimeoutException in tryToLogin");
            }
        }
    }

    public File saveValidatePicture(String codePicUrl) {
        log.warn("enter getValidateCode");
        HttpGet getNewPicture = new HttpGet(codePicUrl);
        try {
            HttpResponse getResponse = httpClient.execute(getNewPicture);
            HttpEntity httpEntity = getResponse.getEntity();
            getNewPicture.releaseConnection();

            byte[] b = new byte[1];
            DataInputStream di = new DataInputStream(httpEntity.getContent());
            File f = new File("codeImage.gif");
            FileOutputStream fo = new FileOutputStream(f);
            while (-1 != di.read(b, 0, 1))
                fo.write(b, 0, 1);
            di.close();
            fo.close();
            return f;
        } catch (ConnectTimeoutException e5) {
            log.error("ConnectTimeoutException in saveValidatePicture");
            return null;
        } catch (Exception e) {
            log.error(e);
            return null;
        } finally {
            getNewPicture.releaseConnection();
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
                e.printStackTrace();
                f.delete();
                return null;
            }
        }
        return line;
    }
}
