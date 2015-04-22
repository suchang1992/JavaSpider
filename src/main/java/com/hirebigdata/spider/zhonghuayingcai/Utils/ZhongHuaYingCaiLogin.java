package com.hirebigdata.spider.zhonghuayingcai.Utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;

public class ZhongHuaYingCaiLogin {
	final static String LOGIN 			= "http://www.chinahr.com/modules/hmcompanyx/?c=login&m=chklogin";
	final static String NEW_JOB 		= "http://www.chinahr.com/modules/hmrecruit/index.php?c=managejob&m=issue&new=managejob";
	final static String POST_NEW_JOB 	= "http://www.chinahr.com/modules/hmrecruit/index.php?c=managejob&m=insert_hrm";
	final static String PRE_NEW_JOB		= "http://www.chinahr.com/modules/hmrecruit/index.php?";
	final static String LOGOUT			= "http://www.chinahr.com/modules/hmcompanyx/?c=logout";

	Header[] headers = null;
	HttpClient httpclient = null;
	String headerString = "";
	String location = "";
	
	String job_hope = "1004,1017,1238";
	String jobName = "测试工程师004";
	
	public static void main(String arg[]) throws Exception {
		ZhongHuaYingCaiLogin zhongHuaYingCai = new ZhongHuaYingCaiLogin();
		zhongHuaYingCai.login("vipcdylf","longhu123");
		zhongHuaYingCai.loginRedirect();
		String s = zhongHuaYingCai.doGet("http://www.chinahr.com/modules/hmresume/?c=searchx&new=searchx&src=index",zhongHuaYingCai.headerString);
//		System.out.println(s);
		HashMap<String, String> formData = new HashMap<>();
		formData.put("flag","1");
		formData.put("keywordSelect1","0");
		formData.put("fuzzyWishPlace", "1");
		formData.put("allKeyword", "1");
		formData.put("matchLevel", "1,2");
		formData.put("searcherCount", "0");
		formData.put("used", "0");
		formData.put("allKeyword2", "0");
		formData.put("keyword", "java ios");
		formData.put("keywordSelect", "0");
		formData.put("page", "1");
		s = zhongHuaYingCai.doPost("http://www.chinahr.com/modules/jmw/SocketAjax.php?m=hmresume&f=resume&action=myresume&list_type=search&usetoken=1",formData,zhongHuaYingCai.headerString);
		System.out.println(s);
		JSONObject jsonObject = JSON.parseObject(s);
		zhongHuaYingCai.logout();
//		System.exit(0);
	}
	
	public void logout() throws ClientProtocolException, IOException{
		HttpGet httpLogout = new HttpGet(LOGOUT);
		httpLogout.setHeader("Referer", "http://www.chinahr.com/modules/hmrecruit/index.php?c=job_list&classify=0");
		httpLogout.setHeader("Origin", "http://www.chinahr.com");
		httpLogout.setHeader("Host", "www.chinahr.com");
		httpLogout.setHeader("Cookie", headerString);
		HttpResponse response1 = httpclient.execute(httpLogout);
//		System.out.println(response1.toString());
		httpLogout.releaseConnection();
	}

	public static String doGet(String url, String pageCookie){
		HttpClient client = HttpClientBuilder.create().build();
		HttpGet request = new HttpGet(url);
		try {
			request.addHeader("Cookie", pageCookie);
			HttpResponse response = client.execute(request);
			return getHtml(response);
		}catch (Exception e){
			e.printStackTrace();
			return null;
		}
	}

	public static String doPost(String url, HashMap<String, String> formData, String pageCookie){
		HttpClient client = HttpClientBuilder.create().build();
		HttpPost request = new HttpPost(url);
		installFormData(formData, request);
		request.addHeader("Cookie", pageCookie);
		try {
			HttpResponse response = client.execute(request);
			return getHtml(response);
		}catch (Exception e){
			e.printStackTrace();
			return null;
		}
	}

	public static void installFormData(HashMap<String, String> parameter, HttpPost request) {
		List<NameValuePair> formData = new ArrayList<NameValuePair>();
		for (String key : parameter.keySet()) {
			formData.add(new BasicNameValuePair(key, parameter.get(key)));
		}
		try {
			request.setEntity(new UrlEncodedFormEntity(formData, "UTF-8"));
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
	}

	public void login(String name, String password) throws ClientProtocolException, IOException{
		httpclient = HttpClients.createDefault();
		//设置浏览器参数
		HttpPost httppost = new HttpPost(LOGIN);
		httppost.setHeader("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8");
		httppost.setHeader("Accept-Encoding", "gzip,deflate,sdch");
		httppost.setHeader("Accept-Language", "en-US,en;q=0.8,zh-CN;q=0.6,zh;q=0.4");
		httppost.setHeader("Cache-Control", "max-age=0");
		httppost.setHeader("Connection", "keep-alive");
		httppost.setHeader("Referer", "http://www.chinahr.com/modules/hmcompanyx/?c=login&http_referer=");
		httppost.setHeader("Origin", "http://www.chinahr.com");
		httppost.setHeader("Host", "www.chinahr.com");
		httppost.setHeader("User-Agent", "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/36.0.1985.125 Safari/537.36");
		//填写账号密码
		List<NameValuePair> params = new ArrayList<NameValuePair>(2);
		params.add(new BasicNameValuePair("redirect", ""));
		params.add(new BasicNameValuePair("uname", name));
		params.add(new BasicNameValuePair("pass", password));
		httppost.setEntity(new UrlEncodedFormEntity(params, "UTF-8"));
		HttpResponse postResponse = httpclient.execute(httppost);
//		System.out.println(postResponse.toString());
		httppost.releaseConnection();
		headers = postResponse.getHeaders("Set-Cookie");
		headerString += updateCookie(headers);
		if (postResponse.getStatusLine().getStatusCode() != 302){
			return;
		}
		
		Header header = postResponse.getHeaders("Location")[0];
		location = header.getValue();
	}
	
	public void loginRedirect() throws ClientProtocolException, IOException{
		HttpGet httpget = new HttpGet(location);
		httpget.setHeader("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8");
		httpget.setHeader("Accept-Encoding", "gzip,deflate,sdch");
		httpget.setHeader("Accept-Language", "en-US,en;q=0.8,zh-CN;q=0.6,zh;q=0.4");
		httpget.setHeader("Cache-Control", "max-age=0");
		httpget.setHeader("Connection", "keep-alive");
		httpget.setHeader("Cookie", headerString);
		httpget.setHeader("Host", "www.chinahr.com");
		httpget.setHeader("Referer", "http://www.chinahr.com/modules/hmcompanyx/?c=login&http_referer=");
		HttpResponse getResponse = httpclient.execute(httpget);
//		System.out.println(getResponse.toString());
		System.out.println("登录成功");
		headers = getResponse.getHeaders("Set-Cookie");
		headerString += updateCookie(headers);
		httpget.releaseConnection();

	}

	public static String getHtml(HttpResponse response) {
		StringBuffer result = new StringBuffer();
		try {
			BufferedReader rd = new BufferedReader(
					new InputStreamReader(response.getEntity().getContent(),"UTF-8"));
			String line;
			while ((line = rd.readLine()) != null) {
				result.append(line);
			}
		}catch (Exception e){
			e.printStackTrace();
		}
		return result.toString();
	}

	public static String updateCookie(Header[] headers){
		String headerString = "";
		for (Header h : headers) {
			String value = h.getValue();
			headerString += value;
		}
		return headerString;
	}
}
