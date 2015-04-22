package com.hirebigdata.spider.zhihu.utils;

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
import org.apache.http.impl.client.DecompressingHttpClient;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.CoreConnectionPNames;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;
import org.apache.log4j.Logger;

import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;

@SuppressWarnings("deprecation")
public class IOUtil {

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
		Map<String, String> list = new HashMap<String, String>();
		try {
			@SuppressWarnings("resource")
			BufferedReader br = new BufferedReader(new FileReader(
					new File(path)));
			while (br.ready()) {
				String line = br.readLine();
				String[] lines = line.split("\t");
				// list.put(lines[2], lines[0] + "\t" + lines[3] + "\t" +
				// lines[4]
				// + "\t" + lines[5]);
				list.put(lines[0], lines[1]);
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
	
	public static Map<String, String> read2Map(String path, String delma) {
		Map<String, String> list = new HashMap<String, String>();
		try {
			@SuppressWarnings("resource")
			BufferedReader br = new BufferedReader(new FileReader(
					new File(path)));
			while (br.ready()) {
				String line = br.readLine();
				if (!line.startsWith("#") && line != null && !line.equals("")) {
					String[] lines = line.split(delma);
					list.put(lines[0].trim(), lines[1].trim());
				}
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
	
	public static BufferedReader read(String path) {
		try {
			@SuppressWarnings("resource")
			BufferedReader br = new BufferedReader(new InputStreamReader(
					new FileInputStream(path)));			
			return br;
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
					"utf-8");
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
	
	public static void write2File(String path, String content,String encoding) {
		try {
			Writer wr = new OutputStreamWriter(new FileOutputStream(path),
					encoding);
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
			Map<String, String> header) throws ClientProtocolException,
			IOException {
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

	public static String get(String url, Map<String, String> header)
			throws ClientProtocolException, IOException {
		DefaultHttpClient httpclient = new DefaultHttpClient();
		// httpclient.getCredentialsProvider().setCredentials(new
		// AuthScope("116.251.217.178", 18441),
		// new UsernamePasswordCredentials("seallhf@qq.com", "seallhf5228363"));
		// HttpHost proxy = new HttpHost("116.251.217.178", 18441);
		// httpclient.getParams().setParameter(ConnRoutePNames.DEFAULT_PROXY,
		// proxy);
		String body = null;
		httpclient.getParams().setParameter(
				CoreConnectionPNames.CONNECTION_TIMEOUT, 10000);
		log.info("create httppost:" + url);
		HttpGet get = new HttpGet(url);
		for (String name : header.keySet()) {
			get.setHeader(name, header.get(name));
		}
		body = invoke(httpclient, get);
		httpclient.getConnectionManager().shutdown();
		return body;
	}

	private static String invoke(DefaultHttpClient httpclient,
			HttpUriRequest httpost) throws ClientProtocolException, IOException {
		DecompressingHttpClient httpClientNew = new DecompressingHttpClient(
				httpclient);// 避免返回compressed格式的Entity出现乱码
		HttpResponse response = sendRequest(httpClientNew, httpost);
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

		String body = null;

		// String responseString = new
		// String(EntityUtils.toString(entity).getBytes("ISO-8859-1"),"utf-8");
		body = new String(EntityUtils.toString(entity).getBytes("UTF-8"));
		log.info(body);
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

}


