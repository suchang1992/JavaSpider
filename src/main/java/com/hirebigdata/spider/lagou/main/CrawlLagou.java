package com.hirebigdata.spider.lagou.main;

import com.hirebigdata.spider.lagou.company.CompanyDetail;
import com.hirebigdata.spider.lagou.config.MongoConfig;
import com.hirebigdata.spider.lagou.utils.MyMongoClient;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;

import java.util.ArrayList;
import java.util.List;

/**
 * User: shellbye.com@gmail.com
 * Date: 2015/4/8
 */
public class CrawlLagou implements Runnable {
    static org.apache.log4j.Logger logger =
            org.apache.log4j.Logger.getLogger(CrawlLagou.class);

    CrawlLagou(int skip){
        this.skip = skip;
    }

    private int skip = 0;

    @Override
    public void run() {
        this.crawl();
    }

    private void crawl(){
        try{
            DBCursor cursor = MyMongoClient.getMongoClient()
                    .getDB(MongoConfig.dbNameLagou)
                    .getCollection(MongoConfig.collectionLagouCompanyInList)
                    .find().skip(skip);
            while (cursor.hasNext()){
                DBObject companyInList = cursor.next();
                CompanyDetail companyDetail =
                        new CompanyDetail(companyInList.get("Url").toString());
                companyDetail.begin();
            }
        }catch (Exception e){
            logger.error(e);
        }
    }

    public static void main(String[] args){
        int loadOnEveryThread = 5000;
        long totalCompany = MyMongoClient.getMongoClient().getDB(MongoConfig.dbNameLagou)
                .getCollection(MongoConfig.collectionLagouCompanyInList).count();
        int threadCount = (int)(totalCompany / loadOnEveryThread);
        List<CrawlLagou> lagouList = new ArrayList<>();
        for (int i=0; i<threadCount; i++){
            if (i*loadOnEveryThread > totalCompany){
                lagouList.add(new CrawlLagou(
                        (int)totalCompany - ((i -1) * loadOnEveryThread)
                ));
                break;
            }
            lagouList.add(new CrawlLagou(i * loadOnEveryThread));
            logger.info("new thread start from : " + (i * loadOnEveryThread));
        }
        for (int i=0; i<threadCount; i++){
            if (i*loadOnEveryThread > totalCompany){
                Thread thread = new Thread(lagouList.get(i + 1));
                thread.start();
                break;
            }
            Thread thread = new Thread(lagouList.get(i));
            thread.start();
        }
    }
}
