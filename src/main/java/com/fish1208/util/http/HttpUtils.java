package com.fish1208.util.http;

import cn.hutool.core.collection.CollectionUtil;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.fish1208.common.constant.Constants;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.net.ssl.*;
import java.io.*;
import java.math.BigDecimal;
import java.net.*;
import java.security.cert.X509Certificate;
import java.util.HashMap;
import java.util.Map;

/**
 * 通用http发送方法
 *
 * @author vee
 */
public class HttpUtils {
	private static final Logger log = LoggerFactory.getLogger(HttpUtils.class);
    private static final String CHARSET = "UTF-8";
    private static final String HTTP_POST = "POST";
    private static final String HTTP_GET = "GET";
    private static final String HTTP_PUT = "PUT";
	/**
	 * 向指定 URL 发送GET方法的请求
	 *
	 * @param url   发送请求的 URL
	 * @param param 请求参数，请求参数应该是 name1=value1&name2=value2 的形式。
	 * @return 所代表远程资源的响应结果
	 */
	public static String sendGet(String url, String param) {
		return sendGet(url, param, Constants.UTF8);
	}

	/**
	 * 向指定 URL 发送GET方法的请求
	 *
	 * @param url         发送请求的 URL
	 * @param param       请求参数，请求参数应该是 name1=value1&name2=value2 的形式。
	 * @param contentType 编码类型
	 * @return 所代表远程资源的响应结果
	 */
	public static String sendGet(String url, String param, String contentType) {
		StringBuilder result = new StringBuilder();
		BufferedReader in = null;
		try {
			String urlNameString = url;
			if(StringUtils.isNotBlank(param)){
				urlNameString = url + ("?" + param);
			}
			log.info("sendGet - {}", urlNameString);
			URL realUrl = new URL(urlNameString);
			URLConnection connection = realUrl.openConnection();
			connection.setConnectTimeout(30 * 1000);
			connection.setReadTimeout(180 * 1000);
			connection.setRequestProperty("accept", "*/*");
			connection.setRequestProperty("connection", "Keep-Alive");
			connection.setRequestProperty("user-agent", "Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.1;SV1)");
			connection.connect();
			in = new BufferedReader(new InputStreamReader(connection.getInputStream(), contentType));
			String line;
			while ((line = in.readLine()) != null) {
				result.append(line);
			}
//			log.info("recv - {}", result);
		} catch (ConnectException e) {
			e.printStackTrace();
			log.error("调用HttpUtils.sendGet ConnectException, url=" + url + ",param=" + param, e);
		} catch (SocketTimeoutException e) {
			e.printStackTrace();
			log.error("调用HttpUtils.sendGet SocketTimeoutException, url=" + url + ",param=" + param, e);
		} catch (IOException e) {
			e.printStackTrace();
			log.error("调用HttpUtils.sendGet IOException, url=" + url + ",param=" + param, e);
		} catch (Exception e) {
			e.printStackTrace();
			log.error("调用HttpsUtil.sendGet Exception, url=" + url + ",param=" + param, e);
		} finally {
			try {
				if (in != null) {
					in.close();
				}
			} catch (Exception ex) {
				log.error("调用in.close Exception, url=" + url + ",param=" + param, ex);
			}
		}
		return result.toString();
	}

	/**
	 * 向指定 URL 发送GET方法的请求
	 *
	 * @param url         发送请求的 URL
	 * @param param       请求参数，请求参数应该是 name1=value1&name2=value2 的形式。
	 * @return 所代表远程资源的响应结果
	 */
	public static String sendGet(String url, Map<String,Object> param, Map<String,Object> property) {
		StringBuilder result = new StringBuilder();
		JSONObject json=new JSONObject();
		json.put("msg","网络错误");
		json.put("code","500");
		BufferedReader in = null;

		try {
			StringBuffer urlStringBuffer = new StringBuffer();
			urlStringBuffer.append(url);
			if(CollectionUtil.isNotEmpty(param)){
				for (String key:param.keySet()) {
					boolean isone=url.indexOf("?")>-1;
					urlStringBuffer.append(isone?"&":"?");
					urlStringBuffer.append(key);
					urlStringBuffer.append("=");
					urlStringBuffer.append(URLEncoder.encode(param.get(key).toString(),"utf-8"));
					url=urlStringBuffer.toString();
				}
			}



			URL realUrl = new URL(url);
			URLConnection connection = realUrl.openConnection();
			connection.setRequestProperty("accept", "*/*");
			connection.setRequestProperty("connection", "Keep-Alive");
			connection.setRequestProperty("user-agent", "Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.1;SV1)");
			if(CollectionUtil.isNotEmpty(property)){
				for (String key:property.keySet()) {
					connection.setRequestProperty(key, property.get(key).toString());
				}
			}


			connection.connect();
			in = new BufferedReader(new InputStreamReader(connection.getInputStream(), Constants.UTF8));
			String line;
			while ((line = in.readLine()) != null) {
				result.append(line);
			}
//			log.info("recv - {}", result);
		} catch (ConnectException e) {
			e.printStackTrace();
			result.append(json);
			log.error("调用HttpUtils.sendGet ConnectException, url=" + url + ",param=" + param, e);
		} catch (SocketTimeoutException e) {
			result.append(json);
			e.printStackTrace();
			log.error("调用HttpUtils.sendGet SocketTimeoutException, url=" + url + ",param=" + param, e);
		} catch (IOException e) {
			result.append(json);
			e.printStackTrace();
			log.error("调用HttpUtils.sendGet IOException, url=" + url + ",param=" + param, e);
		} catch (Exception e) {
			result.append(json);
			e.printStackTrace();
			log.error("调用HttpsUtil.sendGet Exception, url=" + url + ",param=" + param, e);
		} finally {
			try {
				if (in != null) {
					in.close();
				}
			} catch (Exception ex) {
				result.append(json);
				log.error("调用in.close Exception, url=" + url + ",param=" + param, ex);
			}
		}
		return result.toString();
	}
	/**
	 * 向指定 URL 发送POST方法的请求
	 *
	 * @param url   发送请求的 URL
	 * @param param 请求参数，请求参数应该是 name1=value1&name2=value2 的形式。
	 * @return 所代表远程资源的响应结果
	 */
	public static String sendPost(String url, String param, String contentType) {
		PrintWriter out = null;
		BufferedReader in = null;
		StringBuilder result = new StringBuilder();
		try {
			String urlNameString = url;
			log.info("sendPost - {}", urlNameString);
			URL realUrl = new URL(urlNameString);
			URLConnection conn = realUrl.openConnection();
			conn.setRequestProperty("accept", "*/*");
			conn.setRequestProperty("connection", "Keep-Alive");
			conn.setRequestProperty("user-agent", "Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.1;SV1)");
			conn.setRequestProperty("Accept-Charset", "utf-8");
			conn.setRequestProperty("Content-Type", contentType);
			conn.setDoOutput(true);
			conn.setDoInput(true);
			out = new PrintWriter(conn.getOutputStream());
			out.print(param);
			out.flush();
			in = new BufferedReader(new InputStreamReader(conn.getInputStream(), "utf-8"));
			String line;
			while ((line = in.readLine()) != null) {
				result.append(line);
			}
			log.info("recv - {}", result);
		} catch (ConnectException e) {
			log.error("调用HttpUtils.sendPost ConnectException, url=" + url + ",param=" + param, e);
		} catch (SocketTimeoutException e) {
			log.error("调用HttpUtils.sendPost SocketTimeoutException, url=" + url + ",param=" + param, e);
		} catch (IOException e) {
			log.error("调用HttpUtils.sendPost IOException, url=" + url + ",param=" + param, e);
		} catch (Exception e) {
			log.error("调用HttpsUtil.sendPost Exception, url=" + url + ",param=" + param, e);
		} finally {
			try {
				if (out != null) {
					out.close();
				}
				if (in != null) {
					in.close();
				}
			} catch (IOException ex) {
				log.error("调用in.close Exception, url=" + url + ",param=" + param, ex);
			}
		}
		return result.toString();
	}

	/**
	 * 向指定 URL 发送POST方法的请求
	 *
	 * @param url   发送请求的 URL
	 * @param param 请求参数，请求参数应该是 name1=value1&name2=value2 的形式。
	 * @return 所代表远程资源的响应结果
	 */
	public static String sendPost(String url, String param) {
		PrintWriter out = null;
		BufferedReader in = null;
		StringBuilder result = new StringBuilder();
		try {
			String urlNameString = url;
			log.info("sendPost - {}", urlNameString);
			URL realUrl = new URL(urlNameString);
			URLConnection conn = realUrl.openConnection();
			conn.setRequestProperty("accept", "*/*");
			conn.setRequestProperty("connection", "Keep-Alive");
			conn.setRequestProperty("user-agent", "Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.1;SV1)");
			conn.setRequestProperty("Accept-Charset", "utf-8");
			conn.setRequestProperty("contentType", "utf-8");
			conn.setDoOutput(true);
			conn.setDoInput(true);
			out = new PrintWriter(conn.getOutputStream());
			out.print(param);
			out.flush();
			in = new BufferedReader(new InputStreamReader(conn.getInputStream(), "utf-8"));
			String line;
			while ((line = in.readLine()) != null) {
				result.append(line);
			}
			log.info("recv - {}", result);
		} catch (ConnectException e) {
			log.error("调用HttpUtils.sendPost ConnectException, url=" + url + ",param=" + param, e);
		} catch (SocketTimeoutException e) {
			log.error("调用HttpUtils.sendPost SocketTimeoutException, url=" + url + ",param=" + param, e);
		} catch (IOException e) {
			log.error("调用HttpUtils.sendPost IOException, url=" + url + ",param=" + param, e);
		} catch (Exception e) {
			log.error("调用HttpsUtil.sendPost Exception, url=" + url + ",param=" + param, e);
		} finally {
			try {
				if (out != null) {
					out.close();
				}
				if (in != null) {
					in.close();
				}
			} catch (IOException ex) {
				log.error("调用in.close Exception, url=" + url + ",param=" + param, ex);
			}
		}
		return result.toString();
	}

	public static String sendSSLPost(String url, String param) {
		StringBuilder result = new StringBuilder();
		String urlNameString = url + "?" + param;
		try {
			log.info("sendSSLPost - {}", urlNameString);
			SSLContext sc = SSLContext.getInstance("SSL");
			sc.init(null, new TrustManager[] { new TrustAnyTrustManager() }, new java.security.SecureRandom());
			URL console = new URL(urlNameString);
			HttpsURLConnection conn = (HttpsURLConnection) console.openConnection();
			conn.setRequestProperty("accept", "*/*");
			conn.setRequestProperty("connection", "Keep-Alive");
			conn.setRequestProperty("user-agent", "Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.1;SV1)");
			conn.setRequestProperty("Accept-Charset", "utf-8");
			conn.setRequestProperty("contentType", "utf-8");
			conn.setDoOutput(true);
			conn.setDoInput(true);

			conn.setSSLSocketFactory(sc.getSocketFactory());
			conn.setHostnameVerifier(new TrustAnyHostnameVerifier());
			conn.connect();
			InputStream is = conn.getInputStream();
			BufferedReader br = new BufferedReader(new InputStreamReader(is));
			String ret = "";
			while ((ret = br.readLine()) != null) {
				if (ret != null && !"".equals(ret.trim())) {
					result.append(new String(ret.getBytes("ISO-8859-1"), "utf-8"));
				}
			}
			log.info("recv - {}", result);
			conn.disconnect();
			br.close();
		} catch (ConnectException e) {
			log.error("调用HttpUtils.sendSSLPost ConnectException, url=" + url + ",param=" + param, e);
		} catch (SocketTimeoutException e) {
			log.error("调用HttpUtils.sendSSLPost SocketTimeoutException, url=" + url + ",param=" + param, e);
		} catch (IOException e) {
			log.error("调用HttpUtils.sendSSLPost IOException, url=" + url + ",param=" + param, e);
		} catch (Exception e) {
			log.error("调用HttpsUtil.sendSSLPost Exception, url=" + url + ",param=" + param, e);
		}
		return result.toString();
	}

	private static class TrustAnyTrustManager implements X509TrustManager {
		@Override
		public void checkClientTrusted(X509Certificate[] chain, String authType) {
		}

		@Override
		public void checkServerTrusted(X509Certificate[] chain, String authType) {
		}

		@Override
		public X509Certificate[] getAcceptedIssuers() {
			return new X509Certificate[] {};
		}
	}

	private static class TrustAnyHostnameVerifier implements HostnameVerifier {
		@Override
		public boolean verify(String hostname, SSLSession session) {
			return true;
		}
	}
	/**
     * Send POST request
     */
    public static String post(String url, Map<String, String> queryParas, String data, Map<String, String> headers) {

        HttpURLConnection conn = null;
        try {

            conn = getHttpConnection(buildUrlWithQueryString(url, queryParas), HTTP_POST, headers);
			conn.setConnectTimeout(30 * 1000);
			conn.setReadTimeout(180 * 1000);
			conn.connect();
            OutputStream out = conn.getOutputStream();
            out.write(data.getBytes(CHARSET));
            out.flush();
            out.close();
            return readResponseString(conn);
        }
        catch (Exception e) {
            throw new RuntimeException(e);
        }
        finally {
            if (conn != null) {
                conn.disconnect();
            }
        }
    }

    public static String post(String url, Map<String, String> queryParas, String data) {
        return post(url, queryParas, data, null);
    }

    public static String post(String url, String data, Map<String, String> headers) {
        return post(url, null, data, headers);
    }

    public static String post(String url, String data) {
        return post(url, null, data, null);
    }

    public static String jsonPost(String url,String data){
        Map<String,String> headers = new HashMap<>();
        headers.put("Content-Type","application/json");
        return post(url,null,data,headers);
    }

    public static String jsonPost(String url,Map<String,String>headers,String data){
        if(headers == null){
            headers = new HashMap<>();
        }
        headers.put("Content-Type","application/json");
        return post(url,null,data,headers);
    }

    /**
     * Send POST request
     */
    public static String put(String url, Map<String, String> queryParas, String data, Map<String, String> headers) {
        HttpURLConnection conn = null;
        try {
            conn = getHttpConnection(buildUrlWithQueryString(url, queryParas), HTTP_PUT, headers);
            conn.connect();
            OutputStream out = conn.getOutputStream();
            out.write(data.getBytes(CHARSET));
            out.flush();
            out.close();
            return readResponseString(conn);
        }
        catch (Exception e) {
            throw new RuntimeException(e);
        }
        finally {
            if (conn != null) {
                conn.disconnect();
            }
        }
    }



    public static String jsonPut(String url,String data){
        Map<String,String> headers = new HashMap<>();
        headers.put("Content-Type","application/json");
        return put(url,null,data,headers);
    }



    private static SSLSocketFactory initSSLSocketFactory() {
        try {
            TrustManager[] tm = {new TrustAnyTrustManager()};
            SSLContext sslContext = SSLContext.getInstance("TLS", "SunJSSE");
            sslContext.init(null, tm, new java.security.SecureRandom());
            return sslContext.getSocketFactory();
        }
        catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static final SSLSocketFactory sslSocketFactory = initSSLSocketFactory();
    private static final TrustAnyHostnameVerifier trustAnyHostnameVerifier = new TrustAnyHostnameVerifier();

    private static HttpURLConnection getHttpConnection(String url, String method, Map<String, String> headers) throws Exception {
        URL _url = new URL(url);
        HttpURLConnection conn = (HttpURLConnection)_url.openConnection();
        if (conn instanceof HttpsURLConnection) {
            ((HttpsURLConnection)conn).setSSLSocketFactory(sslSocketFactory);
            ((HttpsURLConnection)conn).setHostnameVerifier(trustAnyHostnameVerifier);
        }
        conn.setRequestMethod(method);
        conn.setDoOutput(true);
        conn.setDoInput(true);
        conn.setConnectTimeout(30000);
        conn.setReadTimeout(30000);
        conn.setUseCaches(false); // Post 请求不能使用缓存
        if(headers != null){
            String contentType = headers.get("Content-Type");
            if(StringUtils.isNotEmpty(contentType)){
                conn.setRequestProperty("Content-Type",contentType);
            }else{
                conn.setRequestProperty("Content-Type","application/x-www-form-urlencoded");
            }
        }
        conn.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 6.3; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/33.0.1750.146 Safari/537.36");
        if (headers != null && !headers.isEmpty())
            for (Map.Entry<String, String> entry : headers.entrySet())
                conn.setRequestProperty(entry.getKey(), entry.getValue());

        return conn;
    }

    private static String readResponseString(HttpURLConnection conn) {
        StringBuilder sb = new StringBuilder();
        InputStream inputStream = null;
        try {
            inputStream = conn.getInputStream();
            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, CHARSET));
            String line = null;
            while ((line = reader.readLine()) != null){
                sb.append(line).append("\n");
            }
            return sb.toString();
        }
        catch (Exception e) {
            throw new RuntimeException(e);
        }
        finally {
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * Build queryString of the url
     */
    public static String buildUrlWithQueryString(String url, Map<String, String> queryParas) {
        if (queryParas == null || queryParas.isEmpty())
            return url;

        StringBuilder sb = new StringBuilder(url);
        boolean isFirst;
        if (url.indexOf("?") == -1) {
            isFirst = true;
			if(StringUtils.isNotBlank(url)){
				sb.append("?");
			}

        }
        else {
            isFirst = false;
        }

        for (Map.Entry<String, String> entry : queryParas.entrySet()) {
            if (isFirst) isFirst = false;
            else sb.append("&");

            String key = entry.getKey();
            String value = entry.getValue();
            if (!StringUtils.isEmpty(value)){
                try {value = URLEncoder.encode(value, CHARSET);} catch (UnsupportedEncodingException e) {throw new RuntimeException(e);}
                sb.append(key).append("=").append(value);
            }
        }
        return sb.toString();
    }


	public static void main(String[] args) {
		String address = "0x754288c64725a03849ca636c198576d33cdda67e";
		int pageNum = 1;
		int pageSize = 20;
		String url = "https://trx.tokenview.com/api/trx/address/normal/TBk72yKTJWppCsa7XrsU7Qhfo4yNvbm8rE/1/20";
		String json = HttpUtils.sendGet(url,null);
		System.out.print("123");
		if(com.fish1208.util.StringUtils.isBlank(json)){
			System.out.print("123");
		}
		JSONObject jsonObject = JSONObject.parseObject(json);
		if(!jsonObject.containsKey("code")){
			System.out.print("123");
		}
		int code = jsonObject.getInteger("code");
		if(code!=1){//正确
			System.out.print("123");
		}
		JSONArray array = jsonObject.getJSONArray("data");
		for (Object info : array) {
			JSONObject dataJson = (JSONObject) info;
			System.out.print(dataJson);
			//金额
			BigDecimal value = dataJson.getBigDecimal("value");
			if(value.compareTo(BigDecimal.ZERO)==1){//大于0
				//转出地址
				String fromAddress = dataJson.getString("from");
				//转入地址
				String toAddress = dataJson.getString("to");
				//hash
				String txid = dataJson.getString("txid");
				//手续费
				BigDecimal fee = dataJson.getBigDecimal("fee");
				//添加时间
				Long transactionTime = dataJson.getLong("time");
			}
		}
    }
}
