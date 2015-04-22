package com.hirebigdata.spider.zhihu.main;

import com.hirebigdata.spider.zhihu.utils.Mongo;
import com.mongodb.DBObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;


public class MultiServer implements Runnable{
	static final String DBname = "scrapy2";
	private Socket client;
	private HashMap<Integer, DBObject> idMap ;
	private int next = 1;
	private long LEN;
	private static int ClientCount = 0;
	public MultiServer(Socket c){
		this.client = c;
		idMap = new Mongo().ServerGetUser(DBname, "zhihu_user_data_ids",ClientCount);
		LEN = idMap.size();
		ClientCount++;
	}
	public void run(){
		try{
			BufferedReader in = new BufferedReader(new InputStreamReader(client.getInputStream()));  
            PrintWriter out = new PrintWriter(client.getOutputStream()); 
            String readline;
            while(true){
                if (next>LEN) {
					idMap = new Mongo().ServerGetUser(DBname, "zhihu_user_data_ids",ClientCount);
					LEN = idMap.size();
					next = 1;
				}
                DBObject obj = idMap.get(next++);
				try {
					String str = (String) obj.get("user_data_id");
					System.out.println((next - 1) + ":" + str);
					out.println(str);
					out.flush();
					readline = in.readLine();
					if(readline.indexOf("continue")>=0)
						continue;
					else
						break;
				}catch (NullPointerException e){
					idMap = new Mongo().ServerGetUser(DBname, "zhihu_user_data_ids",ClientCount);
					LEN = idMap.size();
					next = 1;
					continue;
				}
            }
            client.close();
		}catch(Exception ex){
			ex.printStackTrace();
		}
    } 


	
	public static void main(String[] args){

		try{
			ServerSocket server=null;
			try{
				server=new ServerSocket(4700);
			}catch(Exception e){
				e.printStackTrace();
			}
			while(true){
				MultiServer mt = new MultiServer(server.accept()); 
				Thread td = new Thread(mt);
				td.start();
			}
		}catch(Exception e){
			e.printStackTrace();
		}
		
	}
}
