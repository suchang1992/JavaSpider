package com.hirebigdata.spider.utils;


import org.apache.commons.collections.map.CaseInsensitiveMap;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.ParseException;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DecompressingHttpClient;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicHeader;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.CoreConnectionPNames;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;
import org.apache.log4j.Logger;

import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

@SuppressWarnings("deprecation")
public class HttpUtil {

	private static Logger log = Logger.getLogger(IOUtil.class);

	public static String readKeywords(String bufferPath) {
		String result = "";
		try {
			@SuppressWarnings("resource")
			BufferedReader br = new BufferedReader(new FileReader(new File(
					bufferPath)));
			while (br.ready()) {
				result += (br.readLine() + "\r\n");
			}
			return result;
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return result;
	}

	public static Map<String, String> read2ShortMap(String path) {
		@SuppressWarnings("unchecked")
		Map<String, String> list = new CaseInsensitiveMap();
		try {
			@SuppressWarnings("resource")
			BufferedReader br = new BufferedReader(new FileReader(
					new File(path)));
			while (br.ready()) {
				String line = br.readLine();
				String[] lines = line.split("\t");
				list.put(lines[1], lines[0] + "\t" + lines[3] + "\t" + lines[4]
						+ "\t" + lines[5]);
			}
			return list;
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}

	public static Map<String, String> read2Map(String path) {
		@SuppressWarnings("unchecked")
		Map<String, String> list = new CaseInsensitiveMap();
		try {
			@SuppressWarnings("resource")
			BufferedReader br = new BufferedReader(new FileReader(
					new File(path)));
			while (br.ready()) {
				String line = br.readLine();
				String[] lines = line.split("\t");
				list.put(lines[2], lines[0] + "\t" + lines[3] + "\t" + lines[4]
						+ "\t" + lines[5]);
			}
			return list;
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}

	public static List<String> read2List(String path, String encoding) {
		List<String> list = new ArrayList<String>();
		try {
			@SuppressWarnings("resource")
			BufferedReader br = new BufferedReader(new InputStreamReader(
					new FileInputStream(path), encoding));
			while (br.ready()) {
				String line = br.readLine();
				list.add(line);
			}
			return list;
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}

	public static void write2File(String path, String content) {
		try {
			Writer wr = new OutputStreamWriter(new FileOutputStream(path),
					"GB2312");
			wr.write(content);
			wr.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public static String getPages(URL url) {
		String result = "";
		try {
			BufferedReader br = new BufferedReader(new InputStreamReader(
					url.openStream()));
			while (br.ready()) {
				result += br.readLine() + "\r\n";
			}
			return result;
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return result;

	}

	public static String post(String url, Map<String, String> params,
			Map<String, String> header, long sleeptime)
			throws ClientProtocolException, IOException {
		DefaultHttpClient httpclient = new DefaultHttpClient();
		String body = null;

		log.info("create httppost:" + url);
		HttpPost post = postForm(url, params);

		for (String name : header.keySet()) {
			post.setHeader(name, header.get(name));
		}

		body = invoke(httpclient, post, sleeptime);

		httpclient.getConnectionManager().shutdown();

		return body;
	}
	
	public  String post(String url, Map<String, String> params,
			Map<String, String> header)
			throws ClientProtocolException, IOException {
		DefaultHttpClient httpclient = new DefaultHttpClient();
		String body = null;
		log.info("create httppost:" + url);
		HttpPost post = postForm(url, params);

		for (String name : header.keySet()) {
			post.setHeader(name, header.get(name));
		}
		body = invoke(httpclient, post);
		httpclient.getConnectionManager().shutdown();

		return body;
	}

	public static String post(String url, String JSONparams,
			Map<String, String> header, long sleeptime)
			throws ClientProtocolException, IOException {
		DefaultHttpClient httpclient = new DefaultHttpClient();
		String body = null;

		log.info("create httppost:" + url);
		HttpPost post = postForm(url, JSONparams);

		for (String name : header.keySet()) {
			post.setHeader(name, header.get(name));
		}

		body = invoke(httpclient, post, sleeptime);

		httpclient.getConnectionManager().shutdown();

		return body;
	}

	public static String get(String url, Map<String, String> header,
			long sleeptime) throws ClientProtocolException, IOException {
		DefaultHttpClient httpclient = new DefaultHttpClient();
		String body = null;
		httpclient.getParams().setParameter(
				CoreConnectionPNames.CONNECTION_TIMEOUT, 10000);
		log.info("create httppost:" + url);
		HttpGet get = new HttpGet(url);
		if (header != null)
			for (String name : header.keySet()) {
				get.setHeader(name, header.get(name));
			}
		body = invoke(httpclient, get, sleeptime);
		httpclient.getConnectionManager().shutdown();
		return body;
	}

	public String get(String url, Map<String, String> header)
			throws ClientProtocolException, IOException {
		DefaultHttpClient httpclient = new DefaultHttpClient();
		String body = null;
		httpclient.getParams().setParameter(
				CoreConnectionPNames.CONNECTION_TIMEOUT, 10000);
		log.info("create httppost:" + url);
		try{
			HttpGet get = new HttpGet(url);
			if (header != null)
				for (String name : header.keySet()) {
					get.setHeader(name, header.get(name));
				}
			body = invoke(httpclient, get);
		} catch (Exception e){
			return "404";
		}
		httpclient.getConnectionManager().shutdown();
		return body;
	}

	static HttpResponse response;

	private static String invoke(DefaultHttpClient httpclient,
			final HttpUriRequest httpost, long sleeptime)
			throws ClientProtocolException, IOException {
		final DecompressingHttpClient httpClientNew = new DecompressingHttpClient(
				httpclient);// 避免返回compressed格式的Entity出现乱码
		Thread hth = new Thread() {
			@Override
			public void run() {
				try {
					response = sendRequest(httpClientNew, httpost);
				} catch (IllegalArgumentException ec) {
					response = null;
					interrupted();
				} catch (ClientProtocolException e) {
					response = null;
					interrupted();
				} catch (IOException e) {
					response = null;
					interrupted();
				}
			}
		};
		hth.start();
		try {
			Thread.sleep(sleeptime);
			if (response == null) {
				hth.interrupt();
				return "";
			}
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		if (response.getStatusLine().getStatusCode() == 200) {
			return paseResponse(response);
		}
		String body = paseResponse(response);

		return body;
	}

	private String invoke(DefaultHttpClient httpclient,
			HttpUriRequest httpost) throws ClientProtocolException, IOException {
		final DecompressingHttpClient httpClientNew = new DecompressingHttpClient(
				httpclient);// 避免返回compressed格式的Entity出现乱码
		response = sendRequest(httpClientNew, httpost);
		if (response.getStatusLine().getStatusCode() == 200) {
			return paseResponse(response);
		}else if(response.getStatusLine().getStatusCode() >300){
			return ""+response.getStatusLine().getStatusCode();
		}
		String body = paseResponse(response);
		return body;
	}

	private static String paseResponse(HttpResponse response)
			throws UnsupportedEncodingException, ParseException, IOException {
		log.info("get response from http server..");
		HttpEntity entity = response.getEntity();

		log.info("response status: " + response.getStatusLine());
		String charset = EntityUtils.getContentCharSet(entity);

		log.info(charset);
		String body = EntityUtils.toString(entity);
		return body;
	}

	private static HttpResponse sendRequest(DecompressingHttpClient httpclient,
			HttpUriRequest httpost) throws ClientProtocolException, IOException {
		log.info("execute post...");
		HttpResponse response = null;

		response = httpclient.execute(httpost);

		return response;
	}

	private static HttpPost postForm(String url, Map<String, String> params)
			throws UnsupportedEncodingException {

		HttpPost httpost = new HttpPost(url);
		List<NameValuePair> nvps = new ArrayList<NameValuePair>();

		Set<String> keySet = params.keySet();
		for (String key : keySet) {
			nvps.add(new BasicNameValuePair(key, params.get(key)));
		}
		log.info("set utf-8 form entity to httppost");
		httpost.setEntity(new UrlEncodedFormEntity(nvps, HTTP.UTF_8));

		return httpost;
	}

	private static final String APPLICATION_JSON = "application/json";

	private static final String CONTENT_TYPE_TEXT_JSON = "text/json";

	public static HttpPost postForm(String url, String params)
			throws UnsupportedEncodingException {

		HttpPost httpost = new HttpPost(url);
		httpost.addHeader(HTTP.CONTENT_TYPE, APPLICATION_JSON);

		StringEntity se = new StringEntity(params);
		se.setContentType(CONTENT_TYPE_TEXT_JSON);
		se.setContentEncoding(new BasicHeader(HTTP.CONTENT_TYPE,
				APPLICATION_JSON));
		httpost.setEntity(se);

		return httpost;
	}
}
