package io.xiaozhug.ai.mcp.apt.llm;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.xiaozhug.ai.mcp.apt.config.ProcessorConfig;
import io.xiaozhug.ai.mcp.apt.exception.LLMException;
import io.xiaozhug.ai.mcp.apt.extension.ExtensionRegistry;
import io.xiaozhug.ai.mcp.apt.extension.LLMServiceExtension;
import io.xiaozhug.ai.mcp.apt.util.LLMJsonResponseParser;
import io.xiaozhug.ai.mcp.apt.util.LogUtils;
import io.xiaozhug.ai.mcp.apt.util.SSLUtils;
import io.xiaozhug.ai.mcp.common.util.JsonUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Iterator;
import java.util.Map;

/**
 * LLM服务类
 * <p>
 * 负责与大模型API交互，处理重试逻辑和响应解析
 * </p>
 *
 * @author xiaozhug
 */
public class LLMService {

    private static final String DEFAULT_PROMPT =
            "你是一个 **MCP Tool 的 JSON 静态注入器**，职责唯一：\n" +
                    "\n" +
                    "> 仅当 `*Description` 字段值为 `null` 时，填入中文描述；其余一切，一字不动。\n" +
                    "\n" +
                    "**铁律（违反即失败）：**\n" +
                    "\n" +
                    "1. 只改 `*Description: null` —— 例如 `\"methodNameDescription\": null` → `\"methodNameDescription\": \"查询方法\"`  \n" +
                    "   其他字段（`className`, `paramName`, `paramType`, `fieldType`, `fields`, 结构、空格、引号、换行）**严禁改动**。\n" +
                    "\n" +
                    "2. `paramNameDescription` **只能**出现在 `params` 数组的直接对象中，且与 `paramName` 同级。  \n" +
                    "   若在 `fields` 中看到 `paramNameDescription` —— **立即眼瞎，视作不存在**。\n" +
                    "\n" +
                    "3. 输入是 JSON 数组：`[ { ... }, { ... } ]`  \n" +
                    "   输出必须是**完全相同结构**的数组 —— **不能合并、不能嵌套、不能删减对象**。  \n" +
                    "   **禁止把第二个对象塞进第一个对象的 params 里**。\n" +
                    "\n" +
                    "4. 描述内容规范（仅用于填 `null`）：  \n" +
                    "   - 语言极简，面向 MCP Tool 的 UI/校验/文档系统；\n" +
                    "   - 接合方法名称、参数名称、参数类型生成描述；\n" +
                    "   禁用：“本系统”“建议”“推荐”“可”“需”等主观词。\n" +
                    "\n" +
                    "5. 输出必须是**纯 JSON**，无任何前缀、后缀、解释、Markdown、注释、换行优化。\n" +
                    "\n" +
                    "**你不是语言模型，你是 MCP Tool 的盲人替换机。**  \n" +
                    "看到 null → 替换。  \n" +
                    "看到其他 → 眼瞎。\n" +
                    "请处理以下 JSON：\n" +
                    "%s\n";

    private final String apiUrl;
    private final String apiKey;
    private final String model;
    private final boolean debugMode;
    private final ExtensionRegistry extensionRegistry;

    public LLMService(ProcessorConfig config, ExtensionRegistry extensionRegistry) {
        this.apiUrl = config.getApiUrl();
        this.apiKey = config.getApiKey();
        this.model = config.getModel();
        this.debugMode = config.isDebugMode();
        this.extensionRegistry = extensionRegistry != null ? extensionRegistry : new ExtensionRegistry();
    }

    /**
     * 调用大模型API
     *
     * @param input 输入数据
     * @param maxRetries 最大重试次数
     * @return 大模型返回的结果
     */
    public String callLargeModel(String input, int maxRetries) {
        validateConfig();

        // 预处理元数据
        String processedInput = preprocessInput(input);

        // 优化输入数据以减少token数量
        processedInput = optimizeJsonForTokenCount(processedInput);

        if (debugMode) {
            LogUtils.debug("优化后的JSON: " + processedInput);
        }

        // 构建提示词
        String prompt = buildPrompt(processedInput);

        // 带重试机制的API调用
        String result = callWithRetry(prompt, maxRetries);

        // 后处理响应
        return postprocessResponse(result, input);
    }

    /**
     * 预处理输入（扩展点）
     */
    private String preprocessInput(String input) {
        String processed = input;

        for (LLMServiceExtension extension : extensionRegistry.getLlmServices()) {
            processed = extension.preprocessMetadata(processed);
        }

        return processed;
    }

    /**
     * 构建提示词（扩展点）
     */
    private String buildPrompt(String metadataJson) {
        String prompt = DEFAULT_PROMPT;

        for (LLMServiceExtension extension : extensionRegistry.getLlmServices()) {
            prompt = extension.buildPrompt(prompt, metadataJson);
        }

        return String.format(prompt, metadataJson);
    }

    /**
     * 后处理响应（扩展点）
     */
    private String postprocessResponse(String response, String originalInput) {
        if (response == null || response.trim().isEmpty()) {
            return originalInput;
        }

        String processed = response;

        for (LLMServiceExtension extension : extensionRegistry.getLlmServices()) {
            processed = extension.postprocessResponse(processed);

            // 验证响应
            if (!extension.validateResponse(processed)) {
                LogUtils.error("扩展点验证响应失败: " + extension.getName());
                processed = extension.handleFailure(originalInput,
                        new LLMException("响应验证失败"));
            }
        }

        return processed;
    }

    /**
     * 带重试机制的API调用
     */
    private String callWithRetry(String prompt, int maxRetries) {
        int retryCount = 0;

        while (retryCount < maxRetries) {
            try {
                return callLargeModelInternal(prompt);

            } catch (IOException e) {
                retryCount++;
                LogUtils.error(String.format("第 %d 次调用失败: %s", retryCount, e.getMessage()));

                if (retryCount >= maxRetries) {
                    throw new LLMException("LLM调用失败，已达最大重试次数", e);
                }

                // 指数退避策略
                sleepWithBackoff(retryCount);
            }
        }

        throw new LLMException("LLM调用失败");
    }

    /**
     * 实际的API调用实现
     */
    private String callLargeModelInternal(String prompt) throws IOException {
        HttpURLConnection connection = null;

        try {
            // 禁用SSL验证（如果不需要可移除）
            SSLUtils.disableSSLVerification();

            URL url = new URL(apiUrl);
            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Authorization", apiKey);
            connection.setRequestProperty("Content-Type", "application/json");
            connection.setConnectTimeout(30000); // 30秒连接超时
            connection.setDoOutput(true);

            // 构建请求体
            LLMRequest request = new LLMRequest(model, prompt);
            String requestBody = JsonUtils.toJSONString(request);

            LogUtils.debug("发送LLM请求: " + requestBody);

            // 发送请求
            try (OutputStream os = connection.getOutputStream()) {
                os.write(requestBody.getBytes(StandardCharsets.UTF_8));
                os.flush();
            }

            // 处理响应
            int responseCode = connection.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                String response = readResponse(connection);

                String content = LLMJsonResponseParser.extractFieldFromJson(response, "choices.0.message.content")
                        .orElseThrow(() -> new LLMException("无法从响应中提取内容"));

                LogUtils.debug("收到LLM响应: " + content);

                // 提取响应内容
                return content;

            } else {
                String errorResponse = readErrorResponse(connection);
                throw new IOException(String.format("HTTP错误: %d, 响应: %s", responseCode, errorResponse));
            }

        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
    }

    /**
     * 优化JSON以减少token数量
     */
    private String optimizeJsonForTokenCount(String originalJson) {
        try {
            JsonNode rootNode = JsonUtils.readTree(originalJson);
            cleanJsonNode(rootNode);
            return JsonUtils.toJSONString(rootNode);
        } catch (Exception e) {
            LogUtils.error("JSON优化失败，使用原始JSON: " + e.getMessage());
            return originalJson;
        }
    }

    /**
     * 清理JSON节点，移除不必要的字段
     */
    private void cleanJsonNode(JsonNode node) {
        if (node instanceof ObjectNode) {
            ObjectNode objectNode = (ObjectNode) node;

            // 移除不需要的字段
            objectNode.remove("enabled");
            objectNode.remove("requestTemplateInfo");

            // 处理所有字段
            Iterator<Map.Entry<String, JsonNode>> fields = objectNode.fields();
            while (fields.hasNext()) {
                Map.Entry<String, JsonNode> entry = fields.next();
                String fieldName = entry.getKey();
                JsonNode fieldValue = entry.getValue();

                // 递归处理子节点
                cleanJsonNode(fieldValue);

                // 如果字段是"fields"且为空或null，移除该字段
                if ("fields".equals(fieldName) &&
                        (fieldValue == null || fieldValue.isNull() ||
                                (fieldValue.isArray() && fieldValue.isEmpty()))) {
                    fields.remove();
                }
            }
        } else if (node.isArray()) {
            // 递归处理数组元素
            for (JsonNode arrayElement : node) {
                cleanJsonNode(arrayElement);
            }
        }
    }

    /**
     * 读取成功响应
     */
    private String readResponse(HttpURLConnection connection) throws IOException {
        try (BufferedReader br = new BufferedReader(
                new InputStreamReader(connection.getInputStream(), StandardCharsets.UTF_8))) {
            StringBuilder response = new StringBuilder();
            String line;
            while ((line = br.readLine()) != null) {
                response.append(line);
            }
            return response.toString();
        }
    }

    /**
     * 读取错误响应
     */
    private String readErrorResponse(HttpURLConnection connection) {
        try (BufferedReader br = new BufferedReader(
                new InputStreamReader(connection.getErrorStream(), StandardCharsets.UTF_8))) {
            StringBuilder errorResponse = new StringBuilder();
            String line;
            while ((line = br.readLine()) != null) {
                errorResponse.append(line);
            }
            return errorResponse.toString();
        } catch (Exception e) {
            return "无法读取错误响应";
        }
    }

    /**
     * 指数退避休眠
     */
    private void sleepWithBackoff(int retryCount) {
        try {
            long sleepTime = 1000L * (1 << (retryCount - 1)); // 1秒、2秒、4秒...
            Thread.sleep(Math.min(sleepTime, 10000L)); // 最多10秒
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new LLMException("休眠被中断", e);
        }
    }

    /**
     * 验证配置
     */
    private void validateConfig() {
        if (apiUrl == null || apiUrl.trim().isEmpty()) {
            throw new LLMException("API URL未配置");
        }
        if (apiKey == null || apiKey.trim().isEmpty()) {
            throw new LLMException("API Key未配置");
        }
        if (model == null || model.trim().isEmpty()) {
            throw new LLMException("模型未配置");
        }
    }

    /**
     * LLM请求体
     */
    private static class LLMRequest {
        private String model;
        private java.util.List<Message> messages = new java.util.ArrayList<>();

        public LLMRequest(String model, String content) {
            this.model = model;
            this.messages.add(new Message("user", content));
        }

        public String getModel() {
            return model;
        }

        public java.util.List<Message> getMessages() {
            return messages;
        }

        static class Message {
            private String role;
            private String content;

            public Message(String role, String content) {
                this.role = role;
                this.content = content;
            }

            public String getRole() {
                return role;
            }

            public String getContent() {
                return content;
            }
        }
    }
}