package com.hirebigdata.spider.lagou.company;

import com.hirebigdata.spider.lagou.config.MongoConfig;
import com.hirebigdata.spider.lagou.utils.Helper;
import com.hirebigdata.spider.lagou.utils.MyMongoClient;
import com.mongodb.MongoClient;
import com.mongodb.ReflectionDBObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

/**
 * User: shellbye.com@gmail.com
 * Date: 2015/4/2
 * need more than one minute
 * crawled
 */
public class CompanyInList  extends ReflectionDBObject {
    static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(CompanyInList.class);
    String category = "";
    String name = "";
    String url = "";

    public static void main(String[] args) throws Exception {
        CompanyInList companyInList = new CompanyInList();
        companyInList.begin();
    }

    public void begin(){
        String siteMapUrl = "http://www.lagou.com/sitemap";
        String result = Helper.doGet(siteMapUrl);

//        Helper.storeToDisc(result, "c://siteMap.html");
//        String result = Helper.readFromFile("c://siteMap.html");

        Document doc = Jsoup.parse(result);

        Elements firstClass = doc.select(".companyListPage dl");
        try {
            for (Element f : firstClass) {
                String category = f.select("dt a").attr("title");
                Elements companies = f.select("dd");
                for (Element company : companies) {
                    CompanyInList companyInList = new CompanyInList();
                    companyInList.setCategory(category);
                    companyInList.setName(company.select("a").attr("title"));
                    companyInList.setUrl(company.select("a").attr("href"));
                    if (!Helper.isExistInMongoDB(MyMongoClient.getMongoClient(), MongoConfig.dbName,
                            MongoConfig.collectionLagouCompanyInList, "Url", companyInList.url)){
                        Helper.saveToMongoDB(MyMongoClient.getMongoClient(), MongoConfig.dbName,
                                MongoConfig.collectionLagouCompanyInList, companyInList);
                    }
                }
            }
        }catch (Exception e){
            log.error(e.getMessage());
            e.printStackTrace();
        }
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }
}
