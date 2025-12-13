# REST to MCP Tool 示例工程

# [中文](README.md) | [English](README_EN.md)

## 项目简介

**REST to MCP Tool** 是 AI MCP Bridge 的重要示例工程，展示了如何将现有的 Spring REST Controller 无缝适配为 **Spring AI MCP Server Tool**。
该示例通过 `ai-mcp-bridge-spring-boot-mcp-adapter-tool-starter` 自动将编译时生成的 MCP 元数据转换为标准的 Spring AI Tool 定义，让您的 REST API 能够被 Spring AI MCP Client 直接调用。

## 核心特性

### 🚀 REST 到 Spring AI Tool 的无缝转换
- **零代码侵入**：无需修改现有 REST Controller，自动注册为 Spring AI Tool
- **原生集成**：深度集成 Spring AI 生态系统，支持标准的 MCP 协议
- **类型安全**：基于 JSON Schema 提供完整的类型安全调用

### 📋 自动化工具注册
- **元数据自动发现**：自动加载编译时生成的 MCP 元数据
- **ToolCallback 自动创建**：为每个 REST 方法创建对应的 ToolCallback
- **Spring AI 原生支持**：完全兼容 Spring AI Tool 调用机制

### 🛠️ 双协议支持
- **MCP 原生协议**：通过 Spring AI MCP Server 提供标准 MCP 协议支持
- **HTTP 协议**：保持原有 REST 接口的 HTTP 调用能力

## 依赖说明

### 📦 [metadata-compile](..%2Fmetadata-compile)（演示项目共用）

**重要说明**：此依赖仅用于演示项目的代码共享，在实际项目中您不需要依赖此模块。

- **演示用途**：为了避免在多个演示项目中重复编写相同的 UserController 代码
- **实际使用**：在您自己的项目中，可以直接配置 MCP 注解处理器来生成您自己的 REST Controller 元数据
- **生成原理**：通过 Maven 编译器插件分析您的 REST Controller 自动生成元数据

### 📦 服务端核心依赖

**pom.xml**:
```xml
<dependencies>
    <dependency>
        <groupId>io.github.xiaozhug</groupId>
        <artifactId>metadata-compile-jdk17</artifactId>
    </dependency>
    
    <!-- 🔌 ai-mcp-bridge-spring-boot-mcp-adapter-tool-starter（核心依赖） -->
    <!-- 这是 AI MCP Bridge 项目提供的核心适配器依赖，必须包含 -->
    <dependency>
        <groupId>io.xiaozhug</groupId>
        <artifactId>ai-mcp-bridge-spring-boot-mcp-adapter-tool-starter</artifactId>
    </dependency>
</dependencies>
```

### 📦 客户端依赖

**标准MCP Client**:
```xml
<dependencies>
    <dependency>
        <groupId>org.springframework.ai</groupId>
        <artifactId>spring-ai-starter-model-openai</artifactId>
    </dependency>

    <!-- web 与 webflux二选一 -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-web</artifactId>
    </dependency>

    <dependency>
        <groupId>org.springframework.ai</groupId>
        <artifactId>spring-ai-starter-mcp-client</artifactId>
    </dependency>
</dependencies>
```

## 环境要求

- **服务端**：JDK 17 + Spring Boot 3.x
- **客户端**：JDK 17 + Spring Boot 3.x
- Maven 3.3+

## 自动配置机制

### 🔄 工具注册流程

1. **编译时生成**：MCP 注解处理器分析 REST Controller 生成 `mcp-metadata.json`
2. **启动时扫描**：`McpMetadataToolCallbackProvider` 自动扫描元数据文件
3. **工具创建**：为每个 REST 方法创建对应的 `ToolCallback`
4. **Spring AI 注册**：通过 `ToolCallbackProvider` 接口注册到 Spring AI 上下文

## 快速开始

### 服务端配置
**REST Controller 已包含在 [metadata-compile](..%2Fmetadata-compile) 依赖中，无需单独编写**


### 客户端配置与使用

1. **配置 application.yaml**

在客户端的 `application.yaml` 中添加核心配置：

```yaml
# Spring AI 原生配置
spring:
  ai:
    mcp:
      client:
        sse:
          connections:
            server1:
              url: http://localhost:8081
```

2. **客户端主程序示例 [RestToMcpToolServerApplication.java](rest-to-mcp-tool-server%2Fsrc%2Fmain%2Fjava%2Fio%2Fxiaozhug%2Fdemo%2Fresttomcptool%2FRestToMcpToolServerApplication.java)**

## 功能验证

### 验证方式

1. **启动服务端**
2. 使用 Spring AI MCP Client 调用转换后的 MCP Tool
3. 验证调用结果是否符合预期

### 验证内容

![img.png](img.png)
![img_1.png](img_1.png)

## 与 REST to MCP HTTP 的对比

### REST to MCP Tool 的优势
- **客户端通用性**：客户端可以使用官方的 MCP Client（支持多种编程语言）
- **Spring AI 原生集成**：深度集成 Spring AI 生态系统
- **自动 Tool 注册**：无需手动配置，自动注册为 Spring AI Tool

### REST to MCP Tool 的局限性
- **参数兼容性**：Spring Web 框架特有的参数类型（如 `HttpServletRequest`）对大模型生成调用不友好
- **环境要求较高**：仅支持 JDK 17 + Spring Boot 3.x

### REST to MCP HTTP 的优势
- **参数友好**：所有参数都能被大模型友好理解和生成
- **环境兼容性更好**：服务端支持 JDK 8 或 JDK 17

### REST to MCP HTTP 的局限性
- **客户端依赖性**：需要使用本项目提供的 `ai-mcp-bridge-spring-boot-mcp-client-starter` 依赖

## 如何选择

- 如果您的项目环境为 JDK 17 + Spring Boot 3.x，且希望使用官方 MCP Client，选择 **REST to MCP Tool**
- 如果您的项目需要支持 JDK 8，或 REST Controller 包含较多 Spring Web 特有参数，选择 **REST to MCP HTTP**

## 总结

**REST to MCP Tool** 示例工程展示了 AI MCP Bridge 的强大功能：将 REST Controller 无缝转换为 Spring AI MCP Server Tool。通过该功能，开发者可以在无需代码修改的情况下，快速将现有的 Spring Web 项目升级为支持 AI 调用的 MCP 服务。
