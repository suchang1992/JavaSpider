package com.hirebigdata.spider.lagou.main;

import com.hirebigdata.spider.lagou.company.CompanyDetail;
import com.hirebigdata.spider.lagou.config.MongoConfig;
import com.hirebigdata.spider.lagou.utils.MyMongoClient;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;

/**
 * User: shellbye.com@gmail.com
 * Date: 2015/4/8
 */
public class CrawlLagou {
    static org.apache.log4j.Logger logger = org.apache.log4j.Logger.getLogger(CrawlLagou.class);
    public static void main(String[] args){
        int count = 0;
        try{
            DBCursor cursor = MyMongoClient.getMongoClient()
                    .getDB(MongoConfig.dbName)
                    .getCollection(MongoConfig.collectionLagouCompanyInList)
                    .find().skip(27687);

            while (cursor.hasNext()){
                DBObject companyInList = cursor.next();
                CompanyDetail companyDetail = new CompanyDetail(companyInList.get("Url").toString());
                companyDetail.begin();

                count++;
            }
        }catch (Exception e){
            logger.error(e.getMessage() + ", the count is " + String.valueOf(count));
            e.printStackTrace();
            throw e;
        }
    }
}
