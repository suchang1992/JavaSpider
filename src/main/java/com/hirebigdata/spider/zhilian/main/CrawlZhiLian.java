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
import com.hirebigdata.spider.zhilian.utils.HttpUtils;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.message.BasicNameValuePair;

import com.ImagePreProcess3;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

/**
 * User: shellbye.com@gmail.com
 * Date: 2015/4/15
 */
public class CrawlZhiLian {
    HttpClient httpclient = HttpClientBuilder.create().build();
    public static void main(String[] args) throws Exception{
        new CrawlZhiLian().tryToLogin();
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
            HttpGet httpget2 = new HttpGet("http://rdsearch.zhaopin.com/Home/ResultForCustom?SF_1_1_1=java&orderBy=DATE_MODIFIED,1&SF_1_1_27=0&exclude=1");
            httpget2.setHeader("Referer", "http://rdsearch.zhaopin.com/home/SearchByCustom");
            HttpResponse getResponse3 = httpclient.execute(httpget2);
            Document doc = Jsoup.parse(HttpUtils.getHtml(getResponse3));
            Elements resumes = doc.select(".info");
            System.out.println(resumes.size());
            return;
        }
    }

    public File saveValidatePicture() {
        try {
            String a = ZhiLianConfig.VALIDATA_PICTURE + System.currentTimeMillis() / 1000L;
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
}
