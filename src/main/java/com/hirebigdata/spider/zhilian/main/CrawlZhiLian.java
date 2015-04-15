package com.hirebigdata.spider.zhilian.main;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.PrintStream;
import java.util.HashMap;

import com.hirebigdata.spider.zhilian.config.ZhiLianConfig;
import com.hirebigdata.spider.zhilian.utils.HttpUtils;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.CookieStore;
import org.apache.http.impl.client.BasicCookieStore;

import com.ImagePreProcess3;

/**
 * User: shellbye.com@gmail.com
 * Date: 2015/4/15
 */
public class CrawlZhiLian {
    public static void main(String[] args) throws Exception {
        CookieStore zhilianCookie = new BasicCookieStore();
        System.out.println(login(zhilianCookie));
    }

    public static int login(CookieStore cookieStore) {
        while (true) {
            long nowTimeStamp = System.currentTimeMillis();
            long nowTime = nowTimeStamp / 1000;


            HashMap<String, String> param = new HashMap<>();
            param.put("username", "jiri59483132");
            param.put("password", "linxiaohua87860519");
            param.put("Validate", getCode(cookieStore));
            param.put("Submit", "");


            HttpResponse postResponse = HttpUtils.doPostResponse(
                    ZhiLianConfig.LOGIN_POST + "?DYWE="
                            + nowTimeStamp + "." + 438478 + "."
                            + nowTime + "."
                            + nowTime + "." + 1,
                    param,
                    cookieStore
            );


            if (postResponse.getStatusLine().getStatusCode() == 302) {
                Header[] header = postResponse.getHeaders("Location");
                String location = header[0].getValue();
                HttpResponse getResponse = HttpUtils.doGetResponse(location, new BasicCookieStore());
                if (getResponse.getStatusLine().getStatusCode() == 200) {
                    return 200;
                }
            }
        }
    }

    public static String getCode(CookieStore cookieStore) {
        String line = "";
        while (line.length() < 4) {
            File f = savePicture(cookieStore);
            try {
                // 因为ImagePreProcess3的main函数是直接将结果打印了出来，
                // 所以这里我对console的输出做了重定向，打印到文件中，
                // 然后从文件中读取结果
                File f2 = new File("c:\\output1.txt");
                PrintStream out = new PrintStream(new FileOutputStream(f2));
                PrintStream old_out = System.out;
                System.setOut(out);
                ImagePreProcess3.main((f.getAbsolutePath() + " c:\\train").split(" "));
                System.setOut(old_out);
                BufferedReader br2 = new BufferedReader(new FileReader(f2));
                if ((line = br2.readLine()) != null)
                    continue;

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

    public static File savePicture(CookieStore cookieStore) {
        try {
            String a = ZhiLianConfig.VALIDATA_PICTURE + System.currentTimeMillis() / 1000L;
            HttpResponse getResponse = HttpUtils.doGetResponse(a, cookieStore);
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
}
