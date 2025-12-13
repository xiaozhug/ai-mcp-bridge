# REST to MCP HTTP 示例工程

# [中文](README.md) | [English](README_EN.md)

## 项目简介

**REST to MCP HTTP** 是 AI MCP Bridge 的核心示例工程，展示了如何将现有的 Spring REST Controller 无缝升级为 MCP 服务接口。该示例演示了如何在不修改原有业务代码的情况下，将标准的 REST API 转换为符合 MCP 规范的服务接口，并通过 HTTP 协议提供给 AI 客户端调用。

## 核心特性

### 🚀 REST 到 MCP 的无缝转换
- **零代码侵入**：无需修改现有 REST Controller，自动转换为 MCP 工具
- **协议兼容**：基于 HTTP 协议，符合 MCP 客户端调用规范
- **完整生命周期**：完全复用 Spring Web 项目的调用链路，支持拦截器、过滤器等



## 依赖说明

### 📦 [metadata-compile](..%2Fmetadata-compile)（演示项目共用）

**重要说明**：此依赖仅用于演示项目的代码共享，在实际项目中您不需要依赖此模块。

- **演示用途**：为了避免在多个演示项目中重复编写相同的 UserController 代码
- **实际使用**：在您自己的项目中，可以直接配置 MCP 注解处理器来生成您自己的 REST Controller 元数据
- **生成原理**：通过 Maven 编译器插件分析您的 REST Controller 自动生成元数据

### 服务端依赖

**server/pom.xml**:
```xml
<dependencies>
    <dependency>
        <groupId>io.github.xiaozhug</groupId>
        <artifactId>metadata-compile-jdk8</artifactId>
    </dependency>

    <dependency>
        <groupId>io.github.xiaozhug</groupId>
        <artifactId>ai-mcp-bridge-spring-boot-mcp-file-expose-starter</artifactId>
    </dependency>
</dependencies>
```

### 客户端依赖

**client/pom.xml**:
```xml
<dependencies>
    <!-- 🔌 ai-mcp-bridge-spring-boot-mcp-client-starter（核心依赖） -->
    <!-- 这是 AI MCP Bridge 项目提供的核心客户端依赖，必须包含 -->
    <dependency>
        <groupId>io.github.xiaozhug</groupId>
        <artifactId>ai-mcp-bridge-spring-boot-mcp-client-starter</artifactId>
    </dependency>
</dependencies>
```

## 快速开始

### 环境要求

- **服务端**：JDK 8 或 JDK 17 + Spring Boot 2.x 或 3.x
- **客户端**：JDK 17 + Spring Boot 3.x
- Maven 3.3+

### 服务端配置
**REST Controller 已包含在 [metadata-compile](..%2Fmetadata-compile) 依赖中，无需单独编写**


### 客户端配置与使用

1. **配置 application.yml**

在客户端的 `application.yml` 中添加核心配置：

```yml
#  ai-mcp-bridge-spring-boot-mcp-client-starter中的http方式
spring:
  mcp:
    fetch:
      http:
        enabled: true
        connections:
          server1: http://localhost:8081
```

2. **客户端主程序示例 [RestToMcpHttpClientApplication.java](rest-to-mcp-http-client%2Fsrc%2Fmain%2Fjava%2Fio%2Fxiaozhug%2Fdemo%2Fresttomcphttp%2FRestToMcpHttpClientApplication.java)**


## 功能验证

### 验证方式

1. **启动服务端**
2. 访问元数据端点查看生成的 MCP 元数据
3. 使用客户端调用转换后的 MCP 接口
4. 验证调用结果是否符合预期

### 验证内容

![img.png](img.png)
![img_1.png](img_1.png)


## 总结

**REST to MCP HTTP** 示例工程展示了 AI MCP Bridge 的核心功能：将 REST Controller 无缝转换为 MCP 服务接口。通过该功能，开发者可以在无需代码修改的情况下，快速将现有的 Spring Web 项目升级为支持 AI 调用的 MCP 服务。

该示例提供了完整的服务端和客户端实现，支持通过 HTTP 协议进行 MCP 调用，并集成了 Spring AI，可以直接与 AI 模型配合使用。通过简单的注解配置，即可实现 REST API 到 MCP 服务的转换，为 AI 应用提供强大的工具调用能力。