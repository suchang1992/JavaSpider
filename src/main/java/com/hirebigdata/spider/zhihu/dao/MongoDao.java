package com.hirebigdata.spider.zhihu.dao;

import com.alibaba.fastjson.JSONObject;
import com.mongodb.*;

import java.net.UnknownHostException;
import java.util.*;
import java.util.Map.Entry;

public class MongoDao {

	private MongoClient mongoClient = new MongoClient("localhost", 27017);

	private String dbName;

	public MongoDao(String host, int port, String dbName)
			throws UnknownHostException {
		this.dbName = dbName;
		mongoClient = new MongoClient(host, port);
		mongoClient.setWriteConcern(WriteConcern.SAFE);
	}

	public MongoDao(String host, int port, String dbName, String username,
					String password) throws UnknownHostException {
		this.dbName = dbName;
		mongoClient = new MongoClient(host, port);
		mongoClient.getDB(dbName).authenticate(username, password.toCharArray());
		mongoClient.setWriteConcern(WriteConcern.SAFE);
	}

	public void save(String tableName, Object obj) {
		this.save(tableName, (JSONObject) JSONObject.toJSON(obj));
	}

	public void insertDBObjects(String tableName, List<DBObject> list) {
		mongoClient.getDB(dbName).getCollection(tableName).insert(list);
	}

	public void save(String tableName, JSONObject job) {
		DBObject dbo = new BasicDBObject(job);
		this.save(tableName, dbo);
	}

	public void save(String tableName, DBObject dbo) {
		mongoClient.getDB(dbName).getCollection(tableName).save(dbo);
	}

	public void update(String tableName, DBObject condition, DBObject dbo) {
		mongoClient.getDB(dbName).getCollection(tableName)
				.update(condition, dbo, true, true);
	}

	public DBObject findById(String tableName, Object id) {
		DBObject dbo = new BasicDBObject();
		dbo.put("_id", id);
		return mongoClient.getDB(dbName).getCollection(tableName).findOne(dbo);
	}

	public MongoClient getClient() {
		return mongoClient;
	}

	public DBCollection getCollection(String tableName) {
		return mongoClient.getDB(dbName).getCollection(tableName);
	}

	public void remove(String tableName, DBObject o) {
		// TODO Auto-generated method stub
		mongoClient.getDB(dbName).getCollection(tableName).remove(o);
	}

	public DBObject find(String tableName, Map<String, ?> map) {
		// TODO Auto-generated method stub
		DBObject o = new BasicDBObject();
		o.putAll(map);
		DBObject object = mongoClient.getDB(dbName).getCollection(tableName)
				.findOne(o);
		return object;
	}

	public Map<String, DBObject> find(String tableName, String column) {
		Map<String, DBObject> result = new HashMap<String, DBObject>();
		DBCursor sort = mongoClient.getDB(dbName).getCollection(tableName)
				.find();
		// ZhihuMongoService mongo = (ZhihuMongoService) SpringBeanFactory
		// .getBean("zhihuMongoService");
		// int count = 0;
		while (sort.hasNext()) {
			DBObject object = sort.next();
			// if (object.containsField("doc")) {
			// String doc = (String) object.get("doc");
			// JSONObject userdoc = new JSONObject();
			// userdoc.put("doc", doc);
			// userdoc.put("name", object.get("name"));
			// if (mongo.findZhihuDoc((String) object.get("name")) == null)
			// mongo.insertZhihuUserDoc(userdoc);
			// mongo.updateZhihuUserDocData((String) object.get("name"));
			// System.out.println((count++) + ":"
			// + (String) object.get("name"));
			// }
			result.put((String) object.get(column), null);
		}

		return result;
	}

	public DBCursor findCursor(String tableName, String column) {
		DBCursor sort = mongoClient.getDB(dbName).getCollection(tableName)
				.find();
		return sort;
	}

	public List<String> findIds(String tableName, String column) {
		List<String> result = new ArrayList<String>();
		DBCursor sort = mongoClient.getDB(dbName).getCollection(tableName)
				.find();
		while (sort.hasNext()) {
			DBObject object = sort.next();
			result.add((String) object.get(column));
		}
		return result;
	}

	public void remove(String tableName, Map<String, ?> map) {
		DBObject o = new BasicDBObject();
		o.putAll(map);
		mongoClient.getDB(dbName).getCollection(tableName).remove(o);
	}

	public static void fixJsonType(JSONObject job) {
		Set<Entry<String, Object>> entrySet = job.entrySet();
		for (Entry<String, Object> entry : entrySet) {
			if (entry.getValue() instanceof java.math.BigDecimal) {
				job.put(entry.getKey(), job.getDouble(entry.getKey()));
			} else if (entry.getValue() instanceof java.math.BigInteger) {
				job.put(entry.getKey(), job.getLong(entry.getKey()));
			}
		}
	}

	public DBObject findMaxValue(String tableName, String column) {
		DBCursor sort = null;
		try {
			sort = mongoClient.getDB(dbName).getCollection(tableName).find()
					.sort(new BasicDBObject(column, -1)).limit(1);
			if (sort.hasNext()) {
				return sort.next();
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			if (sort != null)
				sort.close();
		}
		return null;
	}
}


