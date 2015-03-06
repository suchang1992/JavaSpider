package com.hirebigdata.spider.utils;

import com.alibaba.fastjson.JSONObject;
import com.hirebigdata.spider.pojo.*;
import com.mongodb.*;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.net.UnknownHostException;
import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class Mongo {
	private static MongoClient mongoClient;
	private static Logger log = Logger.getLogger(Mongo.class);
	private static DB db;
	public static final String mongoDBname = "scrapy2";

	public static String getMongoDBname() {
		return mongoDBname;
	}

	static {
		try {
			// File file = new File("./config.txt");
			// FileInputStream fin = new FileInputStream(file);
			// byte[] b = new byte[1024];
			// fin.read(b);
			// String serverip = new String(b);
			// mongoClient = new MongoClient(new ServerAddress(serverip.trim(),
			// 27017));
			mongoClient = new MongoClient(new ServerAddress("218.244.136.252", 27017));
			mongoClient.setWriteConcern(WriteConcern.SAFE);
		} catch (UnknownHostException e) {
			log.info("get mongo instance failed");
		}
		// catch (FileNotFoundException e) {
		// // TODO Auto-generated catch block
		// e.printStackTrace();
		// }
		catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	public DB getDB(String DBName) {
		if (db == null) {
			db = mongoClient.getDB(DBName);
			db.authenticate("sc","123456".toCharArray());
		}
		return db;
	}
	public MongoClient getMongClient() {
		return mongoClient;
	}

	public DBCollection getColl(String DBName, String collection) {
		return this.getDB(DBName).getCollection(collection);
	}

	public void upsertUser(ZhihuUser zhihuUser){
		BasicDBObject query = new BasicDBObject("user_data_id",zhihuUser.getUser_data_id());
		BasicDBObject user = new BasicDBObject("$set",JSONObject.toJSON(zhihuUser));
		getColl(mongoDBname,"user_profile").update(query,user,true,false);
	}
	public void upsertUserDetil(ZhihuUserDetil zhihuUserDetil){
			BasicDBObject query = new BasicDBObject("user_data_id",zhihuUserDetil.getUser_data_id());
			BasicDBObject user = new BasicDBObject("$set",JSONObject.toJSON(zhihuUserDetil));
			getColl(mongoDBname,"user_profile").update(query,user,true,false);
			System.out.print("->Detile");
	}
	public void upsertUserTopic(String User_data_id,ZhihuUserTopic zhihuUserTopic){
			BasicDBObject query = new BasicDBObject("user_data_id",User_data_id);
			DBObject updateSetValue=new BasicDBObject("$set",JSONObject.toJSON(zhihuUserTopic));
			getColl(mongoDBname,"user_profile").update(query, updateSetValue, true, false);
			System.out.print("->Topic");
	}
	public void upsertUserColumn(String User_data_id,ZhihuUserColumn zhihuUserColumn){
			BasicDBObject query = new BasicDBObject("user_data_id",User_data_id);
			DBObject updateSetValue=new BasicDBObject("$set",JSONObject.toJSON(zhihuUserColumn));
			getColl(mongoDBname,"user_profile").update(query,updateSetValue,true,false);
			System.out.print("->Column");
	}
	public void pushUserColumn(String User_data_id,Column column) {
		BasicDBObject query = new BasicDBObject("user_data_id",User_data_id);
		BasicDBObject va = new BasicDBObject("columns",JSONObject.toJSON(column));
		DBObject updateSetValue=new BasicDBObject("$addToSet",va);
		getColl(mongoDBname,"user_profile").update(query,updateSetValue,true,false);
	}
	public void upsertUserSkill(String User_data_id,ZhihuUserSkill_Topic zhihuUserSkill_topic){
			BasicDBObject query = new BasicDBObject("user_data_id",User_data_id);
			DBObject updateSetValue=new BasicDBObject("$set",JSONObject.toJSON(zhihuUserSkill_topic));
			getColl(mongoDBname,"user_profile").update(query,updateSetValue,true,false);
			System.out.print("->Skill");
	}

	public void upsertUserQuestion(String User_data_id, ZhihuUserQuestion zhihuUserQuestion){
			BasicDBObject query = new BasicDBObject("user_data_id",User_data_id);
			DBObject updateSetValue=new BasicDBObject("$set",JSONObject.toJSON(zhihuUserQuestion));
			getColl(mongoDBname,"user_profile").update(query,updateSetValue,true,false);
			System.out.print("->Question");
	}

	public void upsertUserFollower(String User_data_id,ZhihuUserFollower zhihuUserFollower) {
			BasicDBObject query = new BasicDBObject("user_data_id",User_data_id);
			DBObject updateSetValue=new BasicDBObject("$set",JSONObject.toJSON(zhihuUserFollower));
			getColl(mongoDBname,"user_profile").update(query,updateSetValue,true,false);
			for(String uid :zhihuUserFollower.getFollowers()){
				insertUserID(mongoDBname, "zhihu_user_data_ids", uid);
			}
			System.out.print("->Follower");
	}
	public void upsertUserFollowee(String User_data_id,ZhihuUserFollowee zhihuUserFollowee) {
			BasicDBObject query = new BasicDBObject("user_data_id",User_data_id);
			DBObject updateSetValue=new BasicDBObject("$set",JSONObject.toJSON(zhihuUserFollowee));
			getColl(mongoDBname,"user_profile").update(query,updateSetValue,true,false);
			for(String uid :zhihuUserFollowee.getFollowees()){
				insertUserID(mongoDBname, "zhihu_user_data_ids", uid);
			}
			System.out.print("->Followee");
	}

	public boolean pushUserQuestion(String User_data_id, Question question){
		if(question != null){
			BasicDBObject query = new BasicDBObject("user_data_id",User_data_id);
			BasicDBObject va = new BasicDBObject("questions",JSONObject.toJSON(question));
			DBObject updateSetValue=new BasicDBObject("$addToSet",va);
			getColl(mongoDBname,"user_profile").update(query,updateSetValue,true,false);
		}
		return false;
	}
	public boolean pushUserAnswer(String User_data_id, Answer answer){
		if(answer != null){
			BasicDBObject query = new BasicDBObject("user_data_id",User_data_id);
			BasicDBObject va = new BasicDBObject("answers",JSONObject.toJSON(answer));
			DBObject updateSetValue=new BasicDBObject("$addToSet",va);
			getColl(mongoDBname,"user_profile").update(query,updateSetValue,true,false);
		}
		return false;
	}

	public void upsertUserAnswer(String User_data_id, ZhihuUserAnswer zhihuUserAnswer){
			BasicDBObject query = new BasicDBObject("user_data_id",User_data_id);
			DBObject updateSetValue=new BasicDBObject("$set",JSONObject.toJSON(zhihuUserAnswer));
			getColl(mongoDBname,"user_profile").update(query,updateSetValue,true,false);
			System.out.print("->Answer");
	}

	public boolean startCrawl(String DBName, String tableName,String user_data_id){
		BasicDBObject cond = new BasicDBObject("user_data_id",user_data_id);
		BasicDBObject setValue = new BasicDBObject("$set",new BasicDBObject("fetched",true))
				.append("$inc", new BasicDBObject("crawled_count", 1));
		getDB(DBName).getCollection(tableName).update(cond,setValue,true,true);
		return true;
	}
	public boolean errorCrawl(String DBName, String tableName,String user_data_id){
		BasicDBObject cond = new BasicDBObject("user_data_id",user_data_id);
		BasicDBObject setValue = new BasicDBObject("$set",new BasicDBObject("fetched",false));
		getDB(DBName).getCollection(tableName).update(cond,setValue,true,true);
		return true;
	}
	public boolean finishCrawl(String DBName, String tableName,String user_data_id){
		Format f = new SimpleDateFormat("yyyy-MM-dd:HH:mm:ss");
		BasicDBObject cond = new BasicDBObject("user_data_id",user_data_id);
		BasicDBObject value = new BasicDBObject("crawled_successfully",true)
				.append("last_crawled_time", f.format(new Date()));
		BasicDBObject setValue = new BasicDBObject("$set",value).append("$inc",new BasicDBObject("crawled_count",1));
		getDB(DBName).getCollection(tableName).update(cond,setValue,true,true);
		return true;
	}
	public void insertUserID(String DBName, String tableName,String user_data_id){
		Format f = new SimpleDateFormat("yyyy-MM-dd:HH:mm:ss");
		BasicDBObject cond = new BasicDBObject("user_data_id",user_data_id);
		DBObject object = getDB(DBName).getCollection(tableName).findOne(cond);
		if(object == null) {
			BasicDBObject obj = new BasicDBObject("user_data_id",user_data_id)
					.append("crawled_successfully", false).append("fetched", false)
					.append("last_crawled_time", f.format(new Date()))
					.append("crawled_count", 0);
			new Mongo().getColl(DBName,tableName).save(obj);
		}
	}

	public HashMap<Integer, DBObject> ServerGetUser(String DBName, String tableName,int NUM) {
		HashMap<Integer, DBObject> result = new HashMap<Integer, DBObject>();
		BasicDBObject cond = new BasicDBObject("fetched",false);
		DBCursor sort = getDB(DBName).getCollection(tableName).find(cond).skip(NUM*1000).limit(1000);
		int i = 1;
		while (sort.hasNext()) {
			DBObject object = sort.next();
			result.put(i++, object);
		}
		return result;
	}
	public String getUserid(String DBName, String tableName){
		BasicDBObject cond = new BasicDBObject("fetched",false);
		BasicDBObject co = new BasicDBObject("$lt",50);
		cond.append("crawled_count",co);
		return (String)getColl(DBName, tableName).findOne(cond).get("user_data_id");
	}

	public DBObject FindInQuestion(String answer_id){
		String question_id =answer_id.split("/answer")[0];
		BasicDBObject cond = new BasicDBObject("id",question_id);
		return getColl(mongoDBname, "questions").findOne(cond);
	}

	public static void main(String[] args) {

	}
}
