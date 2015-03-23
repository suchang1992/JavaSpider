package com.hirebigdata.spider.test;

import com.mongodb.*;

/**
 * User: shellbye.com@gmail.com
 * Date: 2015/3/20
 */
public class ClearUserDataId {
    public static void main(String[] args) throws Exception {
        System.out.println(System.currentTimeMillis());
        int skip = 0;
        while (true) {
            try {
                MongoClient mongoClient = new MongoClient("218.244.136.200", 27017);
                DB db = mongoClient.getDB("scrapy2");
                DBCollection ids = db.getCollection("zhihu_user_data_ids");
                DBCollection user = db.getCollection("user_profile");
                BasicDBObject q = new BasicDBObject();
                q.put("crawled_count", new BasicDBObject("$ne", 0));
                DBCursor cursor = ids.find(q).skip(skip).limit(2000);
                skip += 2000;
                int count = 0;

                while (cursor.hasNext()) {
                    DBObject idObject = cursor.next();
                    String current_user_data_id = idObject.get("user_data_id").toString();
                    BasicDBObject query = new BasicDBObject("user_data_id", current_user_data_id);
                    DBObject userObject = user.findOne(query);
                    if (null == userObject) {
                        System.out.println(count++);
                        ids.update(
                                idObject,
                                new BasicDBObject(
                                        "$set",
                                        new BasicDBObject("crawled_count", "0")
                                                .append("fetched", false)
                                                .append("crawled_successfully", false)
                                )
                        );
                        System.out.println("update " + current_user_data_id);
                    }
                }
                System.out.println(System.currentTimeMillis());
                System.out.println("End.");
            } catch (Exception e) {
                e.printStackTrace();
                System.out.println(System.currentTimeMillis());
            }
        }
    }
}
