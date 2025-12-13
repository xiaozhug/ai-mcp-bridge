package io.xiaozhug.ai.mcp.apt.util;

import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.xiaozhug.ai.mcp.common.util.JsonUtils;
import io.xiaozhug.ai.mcp.common.metadata.RequestTemplateInfo;

import javax.lang.model.element.*;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import static io.xiaozhug.ai.mcp.apt.util.LogUtils.debug;
import static io.xiaozhug.ai.mcp.apt.util.LogUtils.log;

/**
 * 请求模板信息构建器
 * 用于从Spring MVC注解的方法中提取请求模板信息
 *
 * @author xiaozhug
 */
public class RequestTemplateInfoBuilder {

    public static RequestTemplateInfo createRequestTemplateInfo(TypeElement enclosingClass, ExecutableElement methodElement, AnnotationMirror requestMappingAnnotation){
        try {
            // 获取方法的URL和HTTP方法
            String methodUrl = getMethodUrl(methodElement);
            String baseUrl = getBaseUrl(enclosingClass);
            String fullUrl = combineUrls(baseUrl, methodUrl);
            String httpMethod = getHttpMethod(methodElement);

            // 分析方法参数
            Map<String, String> paramPositions = analyzeParameters(methodElement);

            // 创建RequestTemplateInfo
            return createRequestTemplateInfo(fullUrl, httpMethod, paramPositions);

        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    /**
     * 获取类上的基础URL
     */
    private static String getBaseUrl(TypeElement classElement) {
        // 检查@RequestMapping注解
        AnnotationMirror requestMapping = getAnnotationMirror(classElement, "org.springframework.web.bind.annotation.RequestMapping");
        if (requestMapping != null) {
            return getAnnotationValue(requestMapping, "value", "");
        }

        // 检查@RestController注解（虽然它本身不包含URL信息）
        AnnotationMirror restController = getAnnotationMirror(classElement, "org.springframework.web.bind.annotation.RestController");
        if (restController != null) {
            return "";
        }

        return "";
    }

    /**
     * 获取方法的URL
     */
    private static String getMethodUrl(ExecutableElement method) {
        // 检查各种HTTP方法注解
        String url = getAnnotationValue(method, "org.springframework.web.bind.annotation.GetMapping", "value", "");
        if (!url.isEmpty()) return url;

        url = getAnnotationValue(method, "org.springframework.web.bind.annotation.PostMapping", "value", "");
        if (!url.isEmpty()) return url;

        url = getAnnotationValue(method, "org.springframework.web.bind.annotation.PutMapping", "value", "");
        if (!url.isEmpty()) return url;

        url = getAnnotationValue(method, "org.springframework.web.bind.annotation.DeleteMapping", "value", "");
        if (!url.isEmpty()) return url;

        url = getAnnotationValue(method, "org.springframework.web.bind.annotation.PatchMapping", "value", "");
        if (!url.isEmpty()) return url;

        url = getAnnotationValue(method, "org.springframework.web.bind.annotation.RequestMapping", "value", "");
        return url;
    }

    /**
     * 获取HTTP方法
     */
    private static String getHttpMethod(ExecutableElement method) {
        if (hasAnnotation(method, "org.springframework.web.bind.annotation.GetMapping")) {
            return "GET";
        } else if (hasAnnotation(method, "org.springframework.web.bind.annotation.PostMapping")) {
            return "POST";
        } else if (hasAnnotation(method, "org.springframework.web.bind.annotation.PutMapping")) {
            return "PUT";
        } else if (hasAnnotation(method, "org.springframework.web.bind.annotation.DeleteMapping")) {
            return "DELETE";
        } else if (hasAnnotation(method, "org.springframework.web.bind.annotation.PatchMapping")) {
            return "PATCH";
        } else if (hasAnnotation(method, "org.springframework.web.bind.annotation.RequestMapping")) {
            // 处理@RequestMapping注解的method属性
            AnnotationMirror requestMapping = getAnnotationMirror(method, "org.springframework.web.bind.annotation.RequestMapping");
            if (requestMapping != null) {
                String methodValue = getAnnotationValue(requestMapping, "method", "");
                if (!methodValue.isEmpty()) {
                    // 解析类似"RequestMethod.GET"的值
                    if (methodValue.contains("GET")) return "GET";
                    if (methodValue.contains("POST")) return "POST";
                    if (methodValue.contains("PUT")) return "PUT";
                    if (methodValue.contains("DELETE")) return "DELETE";
                    if (methodValue.contains("PATCH")) return "PATCH";
                }
            }
            return "GET"; // 默认值
        } else {
            return "GET"; // 默认值
        }
    }

    /**
     * 分析方法参数，确定每个参数的位置
     */
    private static Map<String, String> analyzeParameters(ExecutableElement method) {
        Map<String, String> paramPositions = new HashMap<>();

        for (VariableElement param : method.getParameters()) {
            String paramName = param.getSimpleName().toString();

            // 检查参数注解
            if (hasAnnotation(param, "org.springframework.web.bind.annotation.PathVariable")) {
                paramPositions.put(paramName, "path");
            } else if (hasAnnotation(param, "org.springframework.web.bind.annotation.RequestParam")) {
                // 检查是否有@RequestParam注解且指定了value
                String paramValue = getAnnotationValue(param, "org.springframework.web.bind.annotation.RequestParam", "value", paramName);
                if (!paramValue.equals(paramName)) {
                    paramPositions.put(paramName, "query");
                } else {
                    // 如果没有指定value，默认为请求参数
                    paramPositions.put(paramName, "query");
                }
            } else if (hasAnnotation(param, "org.springframework.web.bind.annotation.RequestHeader")) {
                paramPositions.put(paramName, "header");
            } else if (hasAnnotation(param, "org.springframework.web.bind.annotation.CookieValue")) {
                paramPositions.put(paramName, "cookie");
            } else if (hasAnnotation(param, "org.springframework.web.bind.annotation.RequestBody")) {
                paramPositions.put(paramName, "body");
            } else if (hasAnnotation(param, "org.springframework.web.bind.annotation.ModelAttribute")) {
                // ModelAttribute通常用于表单提交
                paramPositions.put(paramName, "form");
            } else if (hasAnnotation(param, "org.springframework.web.bind.annotation.RequestPart")) {
                // RequestPart用于文件上传
                paramPositions.put(paramName, "form");
            } else {
                // 默认视为请求参数
                paramPositions.put(paramName, "query");
            }
        }

        return paramPositions;
    }

    /**
     * 创建RequestTemplateInfo对象
     */
    private static RequestTemplateInfo createRequestTemplateInfo(String url, String method, Map<String, String> paramPositions) {
        // 验证参数
        validateParameters(url, method, paramPositions);

        // 确定参数放置位置
        boolean hasPathVariable = paramPositions.containsValue("path");
        boolean hasRequestParam = paramPositions.containsValue("query");
        boolean hasRequestBody = paramPositions.containsValue("body");
        boolean hasFormParam = paramPositions.containsValue("form");
        boolean hasHeaderParam = paramPositions.containsValue("header");
        boolean hasCookieParam = paramPositions.containsValue("cookie");

        // 检查Content-Type，确定是否使用表单提交
        boolean useFormBody = determineUseFormBody(method, hasRequestBody, hasFormParam);

        // 确定参数放置策略
        boolean argsToUrlParam = determineArgsToUrlParam(hasRequestParam, hasRequestBody, useFormBody);
        boolean argsToJsonBody = determineArgsToJsonBody(hasRequestParam, hasRequestBody, useFormBody);
        boolean argsToFormBody = determineArgsToFormBody(hasRequestParam, hasRequestBody, useFormBody);

        // 验证参数放置策略的一致性
        validateParameterPlacementStrategy(argsToUrlParam, argsToJsonBody, argsToFormBody);

        // 创建JSON节点
        ArrayNode headersNode = createHeadersNode(paramPositions);
        ObjectNode bodyNode = createBodyNode(method, hasRequestBody);
        ObjectNode argsPositionNode = createArgsPositionNode(paramPositions);
//        ObjectNode rawNode = createRawNode(url, method, paramPositions);

        // 日志记录
        logRequestTemplateInfo(url, method, argsToUrlParam, argsToJsonBody, argsToFormBody, paramPositions);

        return new RequestTemplateInfo(
                url,
                method,
                argsToUrlParam,
                argsToJsonBody,
                argsToFormBody,
                headersNode,
                bodyNode,
                argsPositionNode,
                null
//                rawNode
        );
    }

    /**
     * 验证参数的有效性
     */
    private static void validateParameters(String url, String method, Map<String, String> paramPositions) {
        if (url == null || url.trim().isEmpty()) {
            throw new IllegalArgumentException("URL cannot be null or empty");
        }

        if (method == null || method.trim().isEmpty()) {
            throw new IllegalArgumentException("HTTP method cannot be null or empty");
        }

        if (!Arrays.asList("GET", "POST", "PUT", "DELETE", "PATCH", "HEAD", "OPTIONS", "TRACE").contains(method)) {
            throw new IllegalArgumentException("Invalid HTTP method: " + method);
        }

        if (paramPositions == null) {
            throw new IllegalArgumentException("paramPositions cannot be null");
        }
    }

    /**
     * 确定是否使用表单请求体
     */
    private static boolean determineUseFormBody(String method, boolean hasRequestBody, boolean hasFormParam) {
        // POST、PUT、PATCH方法可能使用表单提交
        boolean supportsFormBody = Arrays.asList("POST", "PUT", "PATCH").contains(method);

        // 如果有@RequestBody注解，通常不使用表单
        if (hasRequestBody) {
            return false;
        }

        // 如果有表单参数，使用表单
        if (hasFormParam) {
            return true;
        }

        // 默认对于POST、PUT、PATCH方法，使用表单提交
        return supportsFormBody;
    }

    /**
     * 确定是否将参数放在URL参数中
     */
    private static boolean determineArgsToUrlParam(boolean hasRequestParam, boolean hasRequestBody, boolean useFormBody) {
        // 如果有请求参数注解，且没有请求体和表单，将参数放在URL中
        return hasRequestParam && !hasRequestBody && !useFormBody;
    }

    /**
     * 确定是否将参数放在JSON请求体中
     */
    private static boolean determineArgsToJsonBody(boolean hasRequestParam, boolean hasRequestBody, boolean useFormBody) {
        // 如果有@RequestBody注解，或者没有请求参数且不使用表单，将参数放在JSON请求体中
        return hasRequestBody || (!hasRequestParam && !useFormBody);
    }

    /**
     * 确定是否将参数放在表单请求体中
     */
    private static boolean determineArgsToFormBody(boolean hasRequestParam, boolean hasRequestBody, boolean useFormBody) {
        // 如果使用表单提交，且没有请求体，将参数放在表单请求体中
        return useFormBody && !hasRequestBody;
    }

    /**
     * 验证参数放置策略的一致性
     */
    private static void validateParameterPlacementStrategy(boolean argsToUrlParam, boolean argsToJsonBody, boolean argsToFormBody) {
        int strategyCount = 0;
        if (argsToUrlParam) strategyCount++;
        if (argsToJsonBody) strategyCount++;
        if (argsToFormBody) strategyCount++;

        if (strategyCount > 1) {
            throw new IllegalStateException("Multiple parameter placement strategies selected: " +
                    "argsToUrlParam=" + argsToUrlParam + ", " +
                    "argsToJsonBody=" + argsToJsonBody + ", " +
                    "argsToFormBody=" + argsToFormBody);
        }

        if (strategyCount == 0) {
            log("No parameter placement strategy selected, defaulting to argsToUrlParam=true");
        }
    }

    /**
     * 创建请求头节点
     */
    public static ArrayNode createHeadersNode(Map<String, String> paramPositions) {
        ArrayNode headersArray = JsonUtils.createArrayNode();

        // 添加默认的Content-Type头
        ObjectNode contentTypeHeader = JsonUtils.create();
        contentTypeHeader.put("key", "Content-Type");
        contentTypeHeader.put("value", "application/json");
        headersArray.add(contentTypeHeader);

        // 从参数位置映射中提取请求头信息
        for (Map.Entry<String, String> entry : paramPositions.entrySet()) {
            String paramName = entry.getKey();
            String position = entry.getValue();

            if ("header".equals(position)) {
                // 添加请求头参数的占位符
                ObjectNode headerNode = JsonUtils.create();
                headerNode.put("key", paramName);
                headerNode.put("value", "${" + paramName + "}");
                headersArray.add(headerNode);
            }
        }

        return headersArray;
    }

    /**
     * 创建请求体节点
     */
    private static ObjectNode createBodyNode(String method, boolean hasRequestBody) {
        ObjectNode bodyNode = JsonUtils.create();

        // 对于GET、DELETE方法，通常没有请求体
        if (Arrays.asList("GET", "DELETE").contains(method)) {
            return bodyNode;
        }

        // 如果有@RequestBody注解，创建一个空的请求体模板
        if (hasRequestBody) {
            bodyNode.putObject("template");
        }

        return bodyNode;
    }

    /**
     * 创建参数位置节点
     */
    private static ObjectNode createArgsPositionNode(Map<String, String> paramPositions) {
        ObjectNode argsPositionNode = JsonUtils.create();

        // 设置参数位置
        paramPositions.forEach(argsPositionNode::put);

        return argsPositionNode;
    }

    /**
     * 创建原始节点，包含完整的请求信息
     */
    private static ObjectNode createRawNode(String url, String method, Map<String, String> paramPositions) {
        ObjectNode rawNode = JsonUtils.create();

        // 设置URL和方法
        rawNode.put("url", url);
        rawNode.put("method", method);

        // 设置参数信息
        ObjectNode paramsNode = rawNode.putObject("parameters");
        for (Map.Entry<String, String> entry : paramPositions.entrySet()) {
            String paramName = entry.getKey();
            String position = entry.getValue();

            ObjectNode paramNode = paramsNode.putObject(paramName);
            paramNode.put("position", position);
            paramNode.put("required", true); // 默认所有参数都是必填的
        }

        return rawNode;
    }

    /**
     * 记录RequestTemplateInfo的信息
     */
    private static void logRequestTemplateInfo(String url, String method, boolean argsToUrlParam,
                                        boolean argsToJsonBody, boolean argsToFormBody,
                                        Map<String, String> paramPositions) {
        debug(String.format(
                "Created RequestTemplateInfo - URL: %s, Method: %s, ArgsToUrlParam: %s, ArgsToJsonBody: %s, ArgsToFormBody: %s",
                url, method, argsToUrlParam, argsToJsonBody, argsToFormBody));

        if (!paramPositions.isEmpty()) {
            debug("Parameter positions: " + paramPositions);
        }
    }

    // 辅助方法：检查元素是否有指定注解
    private static boolean hasAnnotation(Element element, String annotationClassName) {
        return getAnnotationMirror(element, annotationClassName) != null;
    }

    // 辅助方法：获取元素上的注解
    private static AnnotationMirror getAnnotationMirror(Element element, String annotationClassName) {
        for (AnnotationMirror annotation : element.getAnnotationMirrors()) {
            TypeElement annotationType = (TypeElement) annotation.getAnnotationType().asElement();
            if (annotationType.getQualifiedName().contentEquals(annotationClassName)) {
                return annotation;
            }
        }
        return null;
    }

    // 辅助方法：获取注解的值
    private static String getAnnotationValue(Element element, String annotationClassName, String valueName, String defaultValue) {
        AnnotationMirror annotation = getAnnotationMirror(element, annotationClassName);
        if (annotation != null) {
            return getAnnotationValue(annotation, valueName, defaultValue);
        }
        return defaultValue;
    }

    // 辅助方法：获取注解的值
    private static String getAnnotationValue(AnnotationMirror annotation, String valueName, String defaultValue) {
        for (Map.Entry<? extends ExecutableElement, ? extends AnnotationValue> entry : annotation.getElementValues().entrySet()) {
            if (entry.getKey().getSimpleName().contentEquals(valueName)) {
                Object value = entry.getValue().getValue();
                if (value instanceof String[]) {
                    String[] values = (String[]) value;
                    return values.length > 0 ? stripQuotes(values[0]) : defaultValue;
                } else if (value instanceof String) {
                    return stripQuotes((String) value);
                } else {
                    return value != null ? stripQuotes(value.toString()) : defaultValue;
                }
            }
        }
        return defaultValue;
    }

    // 移除字符串两端的引号
    private static String stripQuotes(String value) {
        if (value == null) {
            return null;
        }
        // 移除双引号或单引号
        if ((value.startsWith("\"") && value.endsWith("\"")) ||
                (value.startsWith("'") && value.endsWith("'"))) {
            return value.substring(1, value.length() - 1);
        }
        return value;
    }

    // 辅助方法：合并基础URL和方法URL
    private static String combineUrls(String baseUrl, String methodUrl) {
        if (baseUrl.isEmpty()) {
            return methodUrl;
        }
        if (methodUrl.isEmpty()) {
            return baseUrl;
        }
        if (baseUrl.endsWith("/")) {
            if (methodUrl.startsWith("/")) {
                return baseUrl + methodUrl.substring(1);
            } else {
                return baseUrl + methodUrl;
            }
        } else {
            if (methodUrl.startsWith("/")) {
                return baseUrl + methodUrl;
            } else {
                return baseUrl + "/" + methodUrl;
            }
        }
    }
}
