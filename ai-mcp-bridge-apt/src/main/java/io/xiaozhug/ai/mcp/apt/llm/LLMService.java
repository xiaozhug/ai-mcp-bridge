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
            "你是一个 **MCP Server 工具描述生成器**，为了确保生成的描述准确、专业且符合 MCP Server 的要求，请遵循以下详细规范：\n" +
                    "1. **仅替换 `*Description: null`的字段值** \n" +
                    "   **`paramNameDescription`、`paramType` **只能**出现在 `params` 数组的直接对象中，且与 `paramName` 同级。** \n" +
                    "   **`fieldNameDescription`、`fieldType` **只能**出现在 `fields` 数组的直接对象中，且与 `fieldName` 同级。** \n" +
                    "   **嵌套对象（如 `fields` 中的 `fields`）必须严格遵循上述层级规则，禁止在 `fields` 对象中出现 `paramName`、`paramType` 或 `paramNameDescription`。**   \n" +
                    "   **禁止根据推断，将field中的属性改写成param中的属性，反之亦然。如fieldType改为paramType** \n" +
                    "\n" +
                    "2.  **格式约束**: \n" +
                    "   - **保持结构**: 输入是 JSON 数组：`[ { ... }, { ... } ]`，输出必须是**完全相同结构**的数组`，无任何前缀、后缀、解释、Markdown、注释、换行优化。\n" +
                    "   - **通用性**：所有描述必须**仅基于 `className`、`methodName`、`paramName`、`paramType`、`fieldName`、`fieldType` 的字面信息推断**，**禁止引入任何外部业务逻辑、系统上下文或假设**。  \n" +
                    "   - **语言风格**: 使用简洁、准确、专业的技术语言，避免模糊不清的描述。\n" +
                    "\n" +
                    "3. **描述生成规范**  \n" +
                    "   **（A）methodNameDescription（方法描述）**  \n" +
                    "   - **操作定义**：明确这是什么具体操作，如 “更新用户状态的工具操作。 \n" +
                    "   - **调用场景**：说明典型业务场景，如 “用于用户激活流程触发”。 \n" +
                    "\n" +
                    "   **（B）paramNameDescription（参数描述）**  \n" +
                    "   - **技术作用**：语言极简说明它在方法中的作用。 \n" +
                    "   - **技术约束**：paramType中如有描述约束的注解，则说明取值约束。 \n" +
                    "\n" +
                    "   **（C）fieldNameDescription（字段描述）**  \n" +
                    "   - **业务语义**：语言极简解释该字段的含义。  \n" +
                    "   - **技术约束**：fieldType中如有描述约束的注解，则说明取值约束。 \n" +
                    "\n" +
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
     * @return 大模型返回的结果
     */
    public String callLargeModel(String input, Throwable throwable) throws IOException {
        validateConfig();

        // 预处理元数据
        String processedInput = preprocessInput(input);

        // 优化输入数据以减少token数量
        processedInput = optimizeJsonForTokenCount(processedInput);

        if (debugMode) {
            LogUtils.debug("优化后的JSON: " + processedInput);
        }

        // 构建提示词
        String prompt = buildPrompt(processedInput, throwable);

        // 带重试机制的API调用
        String result = call(prompt);

        // 后处理响应
        return postprocessResponse(result, input);
    }

    /**
     * 预处理输入
     */
    private String preprocessInput(String input) {
        String processed = input;

        for (LLMServiceExtension extension : extensionRegistry.getLlmServices()) {
            processed = extension.preprocessMetadata(processed);
        }

        return processed;
    }

    /**
     * 构建提示词
     */
    private String buildPrompt(String metadataJson, Throwable throwable) {
        String prompt = DEFAULT_PROMPT;

        for (LLMServiceExtension extension : extensionRegistry.getLlmServices()) {
            prompt = extension.buildPrompt(prompt, metadataJson, throwable);
        }

        return String.format(prompt, metadataJson);
    }

    /**
     * 后处理响应
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
    private String call(String prompt) throws IOException {
        return callLargeModelInternal(prompt);
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