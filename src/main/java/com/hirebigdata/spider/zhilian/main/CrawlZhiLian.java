package com.hirebigdata.spider.zhilian.main;

import java.io.*;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.hirebigdata.spider.lagou.config.MongoConfig;
import com.hirebigdata.spider.lagou.utils.Helper;
import com.hirebigdata.spider.lagou.utils.MyMongoClient;
import com.hirebigdata.spider.zhilian.config.ZhiLianConfig;
import com.hirebigdata.spider.zhilian.resume.ContactResume;
import com.hirebigdata.spider.zhilian.resume.RawResume;
import com.hirebigdata.spider.zhilian.utils.AccessExcel;
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
    static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger("zhilian");
    String username = "";
    String password = "";

    CrawlZhiLian(String username, String password){
        this.username = username;
        this.password = password;
        // 如果50秒还没有得到服务器的回复，则超时，然后重新发送
        int timeout = 50 * 1000;
        RequestConfig.Builder builder = RequestConfig.custom();
        builder.setConnectionRequestTimeout(timeout);
        builder.setConnectTimeout(timeout);
        builder.setSocketTimeout(timeout);
        RequestConfig requestConfig = builder.build();

        httpClient = HttpClientBuilder.create().setDefaultRequestConfig(requestConfig).build();
    }

    public static void main(String[] args) throws Exception {
        CrawlZhiLian.getResumeByKeyword();
        // 已下载
//        CrawlZhiLian.getResumeByCvId("JS139767110R90250002000", "jiri59483132", "linxiaohua87860519");
        // 未下载
//        CrawlZhiLian.getResumeByCvId("", "jiri59483132", "linxiaohua87860519");
    }

    public static void getResumeByKeyword(){
        CrawlZhiLian zhiLian = new CrawlZhiLian("jiri59483132", "linxiaohua87860519");
        zhiLian.tryToLogin();
        while (true) {
            HashMap<String, String> keywordMap = AccessExcel.getRandomKeywords();
            String bigWord = keywordMap.get("bigWord");
            String smallWord = keywordMap.get("smallWord");
            log.info("start keyword: " + bigWord + "[" + smallWord + "]");
            try {
                zhiLian.getResumeWithKeyword(smallWord, bigWord);
            }catch (UnsupportedEncodingException e){
                log.error(e);
            }
        }
    }

    public static ContactResume getResumeByCvId(String CvId, String username, String password){
        if (CvId.contains("_")){
            CvId = CvId.split("_")[0];
        }
        CrawlZhiLian zhiLian = new CrawlZhiLian(username, password);
        zhiLian.tryToLogin();
        while (true){
            String url = "http://rd.zhaopin.com/resumepreview/resume/viewone/2/" +
                    CvId + "_1_1?searchresume=1";
            HttpGet resumeGet = new HttpGet(url);
            HttpResponse resumeRes = zhiLian.getResponse(resumeGet);
            String resume = HttpUtils.getHtml(resumeRes);
            resumeGet.releaseConnection();
            Document doc = Jsoup.parse(resume);
            Element userName = doc.getElementById("userName");
            ContactResume contactResume = new ContactResume();
            contactResume.setCvId(CvId);
            contactResume.setRawHtml(doc.select("#resumeContentBody").first().html());
            contactResume.setKeyword("");
            contactResume.setLink(url);
            if (userName != null){
                // 已下载
                System.out.println(userName);
                contactResume.setName(userName.text());
                contactResume.setContact(doc.select(".summary-bottom").first().text());
                return contactResume;
            }else {
                zhiLian.downloadResume(CvId, "name");
            }
        }
    }

    public void downloadResume(String CvId, String resumeName){
        HttpGet preDownloadGet = new HttpGet("http://rd.zhaopin.com/resumepreview/resume/_Download?" +
                "extID=" + CvId + "&" +
                "resumeVersion=1&" +
                "language=1");
        preDownloadGet.setHeader("Referer", "http://rd.zhaopin.com/resumepreview/resume/viewone/2/" + CvId + "_1_1?" +
                "searchresume=1");
        HttpResponse downloadRes = this.getResponse(preDownloadGet);
        String preDownloadHtml = HttpUtils.getHtml(downloadRes);
        Document doc = Jsoup.parse(preDownloadHtml);
        preDownloadGet.releaseConnection();
        String publicCollectionFolders = doc.select("#favorite option").first().attr("value");
        System.out.println(publicCollectionFolders);
        System.out.println(resumeName);
        String url = "http://rd.zhaopin.com/resumepreview/resume/DownloadResume?" +
                "r=0.24855911917984486&" +
                "extID=" + CvId + "&" +
                "versionNumber=1&" +
                "favoriteID=" + publicCollectionFolders + "&" +
                "resumeName=" + resumeName;
        System.out.println(url);
        HttpPost buyResumePost = new HttpPost(url);
        buyResumePost.setHeader("Referer", "http://rd.zhaopin.com/resumepreview/resume/viewone/2/" +
                CvId + "_1_1?searchresume=1");
        while (true){
            HttpResponse buyRes = this.getResponse(buyResumePost);
            buyResumePost.releaseConnection();
            if (buyRes.getEntity().getContentLength() == 37){
                log.error("resume bought! " + CvId);
                log.error(HttpUtils.getHtml(buyRes));
                break;
            }
        }
    }

    public void getResumeWithKeyword(String keywordToSearch, String keywordToStore)
            throws UnsupportedEncodingException {
        HttpGet getFirstPage = new HttpGet("http://rdsearch.zhaopin.com/Home/ResultForCustom?SF_1_1_1="
                + URLEncoder.encode(keywordToSearch, "UTF-8") + "&orderBy=DATE_MODIFIED,1&SF_1_1_27=0&exclude=1");
        getFirstPage.setHeader("Referer", "http://rdsearch.zhaopin.com/home/SearchByCustom");

        HttpResponse getResponse3 = this.getResponse(getFirstPage);
        Document doc = Jsoup.parse(HttpUtils.getHtml(getResponse3));
        getFirstPage.releaseConnection();

        String page = doc.select("#rd-resumelist-pageNum").first().text().split("/")[1];

        int pageNum = Integer.parseInt(page);
        while (true) {
            log.warn("process page 1");
            List<RawResume> rawResumeListPage1 = processHttpGet(getFirstPage, keywordToStore);
            getFirstPage.releaseConnection();
            Helper.multiSaveToMongoDB(MyMongoClient.getMongoClient(), MongoConfig.dbNameZhilian,
                    MongoConfig.collectionZhilianResume, rawResumeListPage1);
            break;
        }
        getFirstPage.releaseConnection();
        try {
            for (int i = 2; i <= pageNum; i++) {
                List<RawResume> rawResumeList = getMoreResume(keywordToSearch, i, keywordToStore);
                Helper.multiSaveToMongoDB(MyMongoClient.getMongoClient(), MongoConfig.dbNameZhilian,
                        MongoConfig.collectionZhilianResume, rawResumeList);
            }
        } catch (Exception e) {
            log.error("page " + page, e);
        }
    }

    public List<RawResume> getMoreResume(String keywordToSearch, int page, String keywordToStore) throws Exception {
        log.warn("process page " + page);
        HttpGet getPages = new HttpGet("http://rdsearch.zhaopin.com/Home/ResultForCustom?SF_1_1_1="
                + URLEncoder.encode(keywordToSearch, "UTF-8") + "&orderBy=DATE_MODIFIED,1&SF_1_1_27=0&exclude=1&pageIndex=" + page);
        getPages.setHeader("Referer", "http://rdsearch.zhaopin.com/Home/ResultForCustom?SF_1_1_1="
                + URLEncoder.encode(keywordToSearch, "UTF-8") + "&orderBy=DATE_MODIFIED,1&SF_1_1_27=0&exclude=1&pageIndex=" + (page - 1));
        return processHttpGet(getPages, keywordToStore);
    }

    public List<RawResume> processHttpGet(HttpGet getPage, String keywordToStore) {
        HttpResponse getResponse3 = this.getResponse(getPage);
        Document doc = Jsoup.parse(HttpUtils.getHtml(getResponse3));
        getPage.releaseConnection();
        Elements resumes = doc.select(".info");
        List<RawResume> rawResumeList = new ArrayList<>();
        for (Element e : resumes) {
            RawResume rawResume = getRawResume(e, keywordToStore);
            if (rawResume != null)
                rawResumeList.add(rawResume);
        }
        return rawResumeList;
    }

    public RawResume getRawResume(Element e, String keywordToStore){
        RawResume rawResume = new RawResume();
        rawResume.setCvId(e.attr("tag"));
        rawResume.setLink(e.select("a").attr("href"));
        rawResume.setKeyword(keywordToStore);
        if (Helper.isExistInMongoDB(MyMongoClient.getMongoClient(), MongoConfig.dbNameZhilian,
                MongoConfig.collectionZhilianResume, "CvId", e.attr("tag"))){
            log.info("resume already exist in db, " + e.attr("tag"));
            return null;
        }
        while (true) {
            Document resumeDoc;
            HttpGet getResumeHtml = new HttpGet(rawResume.getLink());
//                log.warn("           get resume of " + rawResume.getLink());
            HttpResponse response = this.getResponse(getResumeHtml);
            String resumeHtml = HttpUtils.getHtml(response);
            // 不加这一句就只能获取两个简历文本
            getResumeHtml.releaseConnection();
            resumeDoc = Jsoup.parse(resumeHtml);
            if (resumeDoc.select("#resumeContentBody").first() != null) {
                rawResume.setRawHtml(resumeDoc.select("#resumeContentBody").first().html());
                return rawResume;
//                break;
            } else {
                if (resumeDoc.select("#resumeContentHead").first() != null) {
                    log.warn("resume deleted " + rawResume.getCvId());
                    return null;
//                    break;
                }
                while (true) {
//                        log.warn("!!!!!! code appears !!!!!!.");
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
    }

    public void logout() {
        HttpGet logout = new HttpGet("http://rd2.zhaopin.com/s/loginmgr/logout.asp");
        HttpResponse response = this.getResponse(logout);
        logout.releaseConnection();
//        log.warn(response.toString());
        log.warn("logout");
    }

    public HttpResponse getResponse(HttpRequestBase requestBase) {
        while (true) {
            try {
                HttpResponse response = httpClient.execute(requestBase);
                return response;
            } catch (ConnectTimeoutException e5) {
                log.error("ConnectTimeoutException in getResponse");
                this.logout();
                this.tryToLogin();
            } catch (IOException ioe) {
                log.error(ioe);
            }
        }
    }

    public void tryToLogin() {
        while (true) {
//            log.warn("prepare to login");
            long nowTimeStamp = System.currentTimeMillis();
            long nowTime = nowTimeStamp / 1000;
            HttpPost httppost = new HttpPost(ZhiLianConfig.LOGIN_POST + "?DYWE="
                    + nowTimeStamp + "." + 438478 + "." + nowTime + "."
                    + nowTime + "." + 1);
            httppost.setHeader("Referer", "http://hr.zhaopin.com/hrclub/index.html");
            httppost.setHeader("Origin", "http://hr.zhaopin.com");
            httppost.setHeader("Host", "rd2.zhaopin.com");

            List<NameValuePair> params = new ArrayList<>(2);
            params.add(new BasicNameValuePair("username", username));
            params.add(new BasicNameValuePair("password", password));

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
//            log.warn("1st " + postResponse.toString());
            httppost.releaseConnection();
            if (postResponse.getStatusLine().getStatusCode() != 302) {
//                log.warn("not 302, go on");
                continue;
            }

            Header[] header = postResponse.getHeaders("Location");
            String location = header[0].getValue();
            HttpGet httpget = new HttpGet(location);
            HttpResponse getResponse2 = this.getResponse(httpget);
            httpget.releaseConnection();
//            log.warn("2nd " + getResponse2.toString());
            if (getResponse2.getStatusLine().getStatusCode() != 200) {
//                log.warn("not 200, go on");
                continue;
            }
            log.warn("successfully login!");
            return;
        }
    }

    public File saveValidatePicture(String codePicUrl) {
//        log.warn("enter getValidateCode");
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
//        log.warn("enter getValidateCode");
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
