package banana.core.request;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;

import org.apache.http.NameValuePair;

import banana.core.download.impl.HttpsProxy;
import banana.core.request.HttpRequest.Method;
import banana.core.request.PageRequest.PageEncoding;
import banana.core.util.URLEncodedUtils;

public final class RequestBuilder {

	private byte[] requestBody;
	private String url;
	private String download;
	private String downloadPath;
	private Method method = Method.GET;
	private Map<String, Object> headers;
	private Map<String, Object> params;
	private Map<String, Object> attribute;
	private String processor;
	private HttpsProxy httpsProxy;
	private PageEncoding pageEncoding;
	private int priority;

	public static RequestBuilder custom() {
		return new RequestBuilder();
	}

	/**
	 * 创建网页下载请求PageRequest
	 * 
	 * @param url
	 *            这个请求对应的http或者https 地址
	 * @param processorCls
	 *            下载完成后处理这个网页Page的PageProcessor的class对象
	 * @return PageRequest
	 */
	public RequestBuilder setUrl(String url) {
		this.url = url;
		return this;
	}

	public RequestBuilder setDownload(String download) {
		return setDownload(download, UUID.randomUUID().toString());
	}

	public RequestBuilder setDownload(String download, String downloadPath) {
		this.download = download;
		this.downloadPath = downloadPath;
		return this;
	}

	public RequestBuilder setProcessor(String processor) {
		this.processor = processor;
		return this;
	}

	public RequestBuilder setHeaders(Map<String, Object> headers) {
		this.headers = headers;
		return this;
	}

	public RequestBuilder setParams(Map<String, Object> params) {
		this.params = params;
		return this;
	}

	public RequestBuilder setAttribute(Map<String, Object> attribute) {
		this.attribute = attribute;
		return this;
	}

	public RequestBuilder setHttpsProxy(HttpsProxy httpsProxy) {
		this.httpsProxy = httpsProxy;
		return this;
	}

	public RequestBuilder setMethod(Method method) {
		this.method = method;
		return this;
	}

	public RequestBuilder setPriority(int priority) {
		this.priority = priority;
		return this;
	}

	public RequestBuilder setPageEncoding(PageEncoding pageEncoding) {
		this.pageEncoding = pageEncoding;
		return this;
	}

	public RequestBuilder setRequestBody(byte[] requestBody) {
		this.requestBody = requestBody;
		return this;
	}

	public byte[] getRequestBody() {
		return requestBody;
	}

	public String getUrl() {
		return url;
	}

	public String getDownload() {
		return download;
	}

	public String getDownloadPath() {
		return downloadPath;
	}

	public Method getMethod() {
		return method;
	}

	public Map<String, Object> getHeaders() {
		return headers;
	}

	public Map<String, Object> getParams() {
		return params;
	}

	public Map<String, Object> getAttribute() {
		return attribute;
	}

	public String getProcessor() {
		return processor;
	}

	public PageEncoding getPageEncoding() {
		return pageEncoding;
	}

	public int getPriority() {
		return priority;
	}

	public HttpRequest build() {
		HttpRequest ret = null;
		if (requestBody != null) {
			PageRequest req = new PageRequest();
			req.load(requestBody);
		} else {
			if (url != null) {
				ret = new PageRequest(normalUrl(url), processor);
				if (pageEncoding != null) {
					((PageRequest) ret).setPageEncoding(pageEncoding);
				}
			} else {
				ret = new BinaryRequest(normalUrl(download), downloadPath);
			}
			ret.setMethod(method);
			if (headers != null) {
				for (Entry<String, Object> entry : headers.entrySet()) {
					ret.putHeader(entry.getKey(), (String) entry.getValue());
				}
			}
			if (params != null) {
				for (Entry<String, Object> entry : params.entrySet()) {
					ret.putParams(entry.getKey(), (String) entry.getValue());
				}
			}
			if (attribute != null) {
				for (Entry<String, Object> entry : attribute.entrySet()) {
					ret.addAttribute(entry.getKey(), entry.getValue());
				}
			}
			if (priority >= 0 && priority <= 1000) {
				ret.setPriority(priority);
			} else {
				throw new IllegalArgumentException("priority的值必须在0-1000之间");
			}
		}
		return ret;
	}

	public static String normalUrl(String url) {
		if (url.startsWith("//")) {
			url = "https:" + url;
		}
		if (!url.contains("?")) {
			return url;
		}
		if (url.contains("??")) {
			url = url.replace("??", "?");
		}
		String[] urlData = url.split("\\?");
		String baseUrl = urlData[0];
		Map<String, String> pramas = new HashMap<String, String>();
		List<NameValuePair> pair = null;
		if (urlData.length > 1) {
			String querys = urlData[1];
			pair = URLEncodedUtils.parse(querys);
			for (NameValuePair nameValue : pair) {
				String value = nameValue.getValue().replaceAll(" ", "+");
				pramas.put(nameValue.getName(), value);
			}
		}
		baseUrl += "?";
		if (pair != null && !pair.isEmpty()) {
			Iterator<String> iter = pramas.keySet().iterator();
			String name = null;
			while (iter.hasNext()) {
				name = iter.next();
				baseUrl += name + "=" + pramas.get(name);
				if (iter.hasNext()) {
					baseUrl += "&";
				}
			}
		}
		return baseUrl;
	}

}
