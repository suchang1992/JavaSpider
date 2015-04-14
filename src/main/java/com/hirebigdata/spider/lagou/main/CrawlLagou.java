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
        int count = 0;
        try{
            DBCursor cursor = MyMongoClient.getMongoClient()
                    .getDB(MongoConfig.dbName)
                    .getCollection(MongoConfig.collectionLagouCompanyInList)
                    .find().skip(skip);
            System.out.println("skip: " + skip);
            while (cursor.hasNext()){
                DBObject companyInList = cursor.next();
                CompanyDetail companyDetail =
                        new CompanyDetail(companyInList.get("Url").toString());
                companyDetail.begin();

                count++;
            }
        }catch (Exception e){
            logger.error(e.getMessage() + ", the count is " + String.valueOf(count));
            e.printStackTrace();
            throw e;
        }
    }

    public static void main(String[] args){
        int loadOnEveryThread = 1000;
        long totalCompany = MyMongoClient.getMongoClient().getDB(MongoConfig.dbName)
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
            System.out.println("new skip: " + (i * loadOnEveryThread));
        }
        for (int i=0; i<threadCount; i++){
            if (i*loadOnEveryThread > totalCompany){
                Thread thread = new Thread(lagouList.get(i + 1));
                break;
            }
            Thread thread = new Thread(lagouList.get(i));
            thread.start();
        }
    }
}
