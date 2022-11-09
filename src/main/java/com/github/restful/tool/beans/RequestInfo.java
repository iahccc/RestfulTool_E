package com.github.restful.tool.beans;

import java.io.Serializable;
import java.util.Objects;

/**
 * 请求信息
 *
 * @author iahc
 * @since 2022/11/3
 */
public class RequestInfo implements Serializable {

	private static final long serialVersionUID = 7110362049806062664L;
	private HttpMethod httpMethod;
	private String url;
	private String head;
	private String requestBody;
	private String script;

	public RequestInfo() {
	}

	public HttpMethod getHttpMethod() {
		return httpMethod;
	}

	public void setHttpMethod(HttpMethod httpMethod) {
		this.httpMethod = httpMethod;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public String getHead() {
		return head;
	}

	public void setHead(String head) {
		this.head = head;
	}

	public String getRequestBody() {
		return requestBody;
	}

	public void setRequestBody(String requestBody) {
		this.requestBody = requestBody;
	}

	public String getScript() {
		return script;
	}

	public void setScript(String script) {
		this.script = script;
	}

	@Override
	public String toString() {
		return "RequestInfo{" +
				"httpMethod=" + httpMethod +
				", url='" + url + '\'' +
				", head='" + head + '\'' +
				", requestBody='" + requestBody + '\'' +
				", script='" + script + '\'' +
				'}';
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		RequestInfo that = (RequestInfo) o;
		return httpMethod == that.httpMethod && Objects.equals(url, that.url) && Objects.equals(head, that.head) && Objects.equals(requestBody, that.requestBody) && Objects.equals(script, that.script);
	}

	@Override
	public int hashCode() {
		return Objects.hash(httpMethod, url, head, requestBody, script);
	}
}
