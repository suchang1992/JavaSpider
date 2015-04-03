package com.hirebigdata.spider.lagou.company;

import com.hirebigdata.spider.lagou.config.MongoConfig;
import com.hirebigdata.spider.lagou.utils.Helper;
import com.mongodb.DB;
import com.mongodb.MongoClient;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;


/**
 * User: shellbye.com@gmail.com
 * Date: 2015/4/2
 */
public class SiteMap {
    public static void main(String[] args) throws Exception {
        String siteMapUrl = "http://www.lagou.com/sitemap";
        String result = Helper.doGet(siteMapUrl);
        // Get the siteMap need need six second

//        Helper.storeToDisc(result, "c://siteMap.html");
//        String result = Helper.readFromFile("c://siteMap.html");

        Document doc = Jsoup.parse(result);

        Elements firstClass = doc.select(".companyListPage dl");
        MongoClient mongoClient = new MongoClient(MongoConfig.MongoDBUrl, MongoConfig.port);
        for (Element f : firstClass) {
            String category = f.select("dt a").attr("title");
            Elements companies = f.select("dd");
            for (Element company : companies) {
                CompanyInList companyInList = new CompanyInList();
                companyInList.setCategory(category);
                companyInList.setName(company.select("a").attr("title"));
                companyInList.setUrl(company.select("a").attr("href"));
                Helper.saveToMongoDB(mongoClient, MongoConfig.dbName, MongoConfig.collectionLagouCompanyInList, companyInList);
            }
        }
        // From new MongoClient to here need more than one minute
    }
}