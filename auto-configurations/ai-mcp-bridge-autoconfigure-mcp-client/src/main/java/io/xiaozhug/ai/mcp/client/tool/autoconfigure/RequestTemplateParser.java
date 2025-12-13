package io.xiaozhug.ai.mcp.client.tool.autoconfigure;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.xiaozhug.ai.mcp.client.tool.autoconfigure.discovery.transport.creator.RequestHeaderContextHolder;
import io.xiaozhug.ai.mcp.common.metadata.RequestTemplateInfo;
import org.slf4j.Logger;
import org.springframework.http.MediaType;
import org.springframework.util.CollectionUtils;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.util.UriBuilder;

import java.net.URI;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * @author xiaozhug
 */
public class RequestTemplateParser {

	public static final Pattern PATH_VARIABLES_PATTERN = Pattern.compile("(?<!\\{)\\{([^}]+)\\}(?!\\})");

	public static URI buildUri(UriBuilder builder, String processedUrl, RequestTemplateInfo info,
							   Map<String, Object> args) {
		// 检查URL是否包含查询参数
		if (processedUrl.contains("?")) {
			// 如果URL包含查询参数，需要分别处理路径和查询参数
			String[] urlParts = processedUrl.split("\\?", 2);
			String path = urlParts[0];
			String existingQuery = urlParts.length > 1 ? urlParts[1] : "";

			// 设置路径
			builder.path(path);

			// 解析现有的查询参数
			if (!existingQuery.isEmpty()) {
				String[] queryPairs = existingQuery.split("&");
				for (String pair : queryPairs) {
					if (!pair.isEmpty()) {
						String[] keyValue = pair.split("=", 2);
						if (keyValue.length == 2) {
							builder.queryParam(keyValue[0], keyValue[1]);
						}
						else if (keyValue.length == 1) {
							builder.queryParam(keyValue[0], "");
						}
					}
				}
			}
		}
		else {
			// 如果URL不包含查询参数，直接设置路径
			builder.path(processedUrl);
		}

		// 添加额外的查询参数
		for (Map.Entry<String, Object> entry : args.entrySet()) {
			String key = entry.getKey();
			Object value = entry.getValue();
			boolean addToQuery = info.argsToUrlParam;
			if (info.argsPosition != null && info.argsPosition.has(key)) {
				String position = info.argsPosition.path(key).asText();
				addToQuery = "query".equals(position);
			}
			if (addToQuery && value != null) {
				if (value instanceof final Collection<?> collection) {
					for (Object item : collection) {
						builder.queryParam(key, item);
					}
				}
				else if (value instanceof Map<?, ?> map) {
					for (Map.Entry<?, ?> kvEntry : map.entrySet()) {
						if (kvEntry.getKey() != null && kvEntry.getValue() != null) {
							builder.queryParam(kvEntry.getKey().toString(), kvEntry.getValue());
						}
					}
				}
				else {
					builder.queryParam(key, value);
				}
			}
		}
		return builder.build();
	}

	private static void handleCookies(MultiValueMap<String, String> headers,
									  RequestTemplateInfo info, Map<String, Object> args, BiConsumer<String, String> additionalHeaders) {

		// 从args中查找cookie相关参数
		StringBuilder cookieBuilder = new StringBuilder();

		for (Map.Entry<String, Object> entry : args.entrySet()) {
			String key = entry.getKey();
			Object value = entry.getValue();

			if (value != null) {
				// 检查是否指定了cookie位置
				if (info.argsPosition != null && info.argsPosition.has(key)) {
					String position = info.argsPosition.path(key).asText();
					if ("cookie".equals(position)) {
						if (!cookieBuilder.isEmpty()) {
							cookieBuilder.append("; ");
						}
						cookieBuilder.append(key).append("=").append(value.toString());
					}
				}
			}
		}
		// 如果有cookie，添加到请求头
		if (!cookieBuilder.isEmpty()) {
			String cookieHeader = cookieBuilder.toString();
			additionalHeaders.accept("Cookie", cookieHeader);
			headers.add("Cookie", cookieHeader);
		}
	}

	public static MultiValueMap<String, String> addHeaders(RequestTemplateInfo info, Map<String, Object> params, Map<String, Object> args,
														   BiFunction<String, Map<String, Object>, String> templateProcessor,
														   BiConsumer<String, String> additionalHeaders) {
		Map<String, List<String>> h = (Map<String, List<String>>) params.get("headers");
		MultiValueMap<String, String> headers = h == null ? new LinkedMultiValueMap<>() : new LinkedMultiValueMap<>(h);
		headers.putAll(RequestHeaderContextHolder.getHeaders());

		if (!CollectionUtils.isEmpty(headers)) {
			for (Map.Entry<String, List<String>> entry : headers.entrySet()) {
				String key = entry.getKey();
				for (String value : entry.getValue()) {
					additionalHeaders.accept(key, value);
				}
			}
		}

		JsonNode headersNode = info.headers;
		if (headersNode != null && headersNode.isArray()) {
			for (JsonNode header : headersNode) {
				String key = header.path("key").asText();
				String valueTemplate = header.path("value").asText();
				String value = templateProcessor.apply(valueTemplate, params);
				additionalHeaders.accept(key, value);
				headers.add(key, value);
			}
		}

		handleCookies(headers, info, args, additionalHeaders);
		for (Map.Entry<String, Object> entry : args.entrySet()) {
			String key = entry.getKey();
			Object value = entry.getValue();
			boolean addToHeader = false;
			if (info.argsPosition != null && info.argsPosition.has(key)) {
				String position = info.argsPosition.path(key).asText();
				addToHeader = "header".equals(position);
			}
			if (addToHeader && value != null) {
				if (value instanceof final Collection<?> collection) {
					for (Object item : collection) {
						String v = String.valueOf(item);
						additionalHeaders.accept(key, v);
						headers.add(key, v);
					}
				}
				else {
					String v = String.valueOf(value);
					additionalHeaders.accept(key, v);
					headers.add(key, v);
				}
			}
		}

		return headers;
	}

	public static String addPathVariables(String url, RequestTemplateInfo info, Map<String, Object> args) {
		if (url == null || url.isEmpty() || args == null || args.isEmpty()) {
			return url;
		}

		Matcher matcher = PATH_VARIABLES_PATTERN.matcher(url);

		StringBuilder result = new StringBuilder();
		while (matcher.find()) {
			String variableName = matcher.group(1);
			if (info.argsPosition != null && info.argsPosition.has(variableName)) {
				String position = info.argsPosition.path(variableName).asText();
				if ("path".equals(position)) {
					Object value = args.get(variableName);
					String replacement = value != null ? value.toString() : matcher.group(0);
					matcher.appendReplacement(result, Matcher.quoteReplacement(replacement));
				}
			}
		}
		matcher.appendTail(result);

		return result.toString();
	}

	public static Object addRequestBody(MultiValueMap<String, String> headers, RequestTemplateInfo info,
									  Map<String, Object> params, Map<String, Object> args,
									  BiFunction<String, Map<String, Object>, String> templateProcessor,
									  BiFunction<MediaType, Object, Object> bodySetter,
									  ObjectMapper objectMapper, Logger logger) {
		boolean hasBody = info.body != null && !info.body.asText().isEmpty();
		int optionCount = (hasBody ? 1 : 0) + (info.argsToJsonBody ? 1 : 0) + (info.argsToFormBody ? 1 : 0)
				+ (info.argsToUrlParam ? 1 : 0);
		if (optionCount > 1) {
			throw new IllegalArgumentException(
					"Only one of body, argsToJsonBody, argsToFormBody, or argsToUrlParam should be specified");
		}
		if (hasBody) {
			String bodyTemplate = info.body.asText();
			String processedBody = templateProcessor.apply(bodyTemplate, params);
			return bodySetter.apply(org.springframework.http.MediaType.APPLICATION_JSON, processedBody);
		}
		else {
			String bodyType = info.argsToFormBody ? "form" : "json";
			MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();
			Object jsonData = null;
			if (!info.argsToJsonBody && !info.argsToFormBody) {
				String contentType = headers.getFirst("Content-Type");
				if (contentType != null && contentType.contains("application/x-www-form-urlencoded")) {
					bodyType = "form";
				}
				else {
					bodyType = "json";
				}
			}
			for (Map.Entry<String, Object> entry : args.entrySet()) {
				String key = entry.getKey();
				Object value = entry.getValue();
				if (value == null) {
					continue;
				}
				boolean addToBody = info.argsToFormBody || info.argsToJsonBody;
				if (info.argsPosition != null && info.argsPosition.has(key)) {
					String position = info.argsPosition.path(key).asText();
					addToBody = "body".equals(position);
				}
				if (addToBody) {
					if ("form".equals(bodyType)) {
						formData.add(key, convertToString(value));
					} else {
						jsonData = value;
					}
				}
			}

			if ("json".equals(bodyType)) {
				try {
					String jsonBody = objectMapper.writeValueAsString(jsonData);
					return bodySetter.apply(org.springframework.http.MediaType.APPLICATION_JSON, jsonBody);
				}
				catch (com.fasterxml.jackson.core.JsonProcessingException e) {
					logger.error("Failed to create JSON request body", e);
					return null;
				}
			}
			else {
				return bodySetter.apply(org.springframework.http.MediaType.APPLICATION_FORM_URLENCODED, formData);
			}
		}
	}

	private static String convertToString(Object value) {
		if (value == null) {
			return "";
		}
		if (value instanceof Collection) {
			return String.join(",", ((Collection<?>) value).stream()
					.map(Object::toString)
					.collect(Collectors.toList()));
		}
		if (value.getClass().isArray()) {
			return String.join(",", Arrays.stream((Object[]) value)
					.map(Object::toString)
					.collect(Collectors.toList()));
		}
		return value.toString();
	}

}