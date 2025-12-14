# Native MCP Registry 示例

# [中文](README.md) | [English](README_EN.md)

## 项目简介

这是一个演示如何将**原生 MCP 工具**自动注册到**服务注册中心**的完整示例项目。项目通过 Spring AI 原生 `@Tool` 注解开发 MCP 工具，并通过 `ai-mcp-bridge-spring-boot-mcp-registry-starter` 实现自动服务注册，为原生 MCP 工具提供标准的服务治理能力。

## 核心特性

### 🚀 原生 MCP 开发
- **注解驱动**：使用 Spring AI 原生 `@Tool` 注解定义 MCP 工具
- **类型安全**：完整的参数类型检查和 JSON Schema 生成
- **开发体验**：纯 Java 开发，无需额外配置转换

### 📋 完整的服务治理
- **服务发现**：MCP 客户端自动发现可用的工具服务
- **元数据发布**：在服务实例中发布完整的 MCP 工具信息

### 🛠️ 开箱即用
- **多注册中心**：支持 Eureka（本示例默认使用）、Nacos、Consul、Zookeeper
- **统一治理**：统一的监控、管理和维护
- **简化部署**：降低运维复杂度，快速上线

## 环境要求

### 服务端要求
- JDK 17+
- Maven 3.3+
- Spring Boot 3.x
- 服务注册中心（本示例支持 Eureka、Nacos、Consul、Zookeeper）

### 客户端要求
- JDK 17+
- Maven 3.3+
- Spring Boot 3.x
- 服务注册中心（与服务端配置一致）

## Maven 配置

### 服务端依赖

```xml
<dependencies>
    <!-- 🔌 ai-mcp-bridge-spring-boot-mcp-registry-starter（核心依赖） -->
    <!-- 这是 AI MCP Bridge 项目提供的核心注册中心依赖，必须包含 -->
    <dependency>
        <groupId>io.github.xiaozhug</groupId>
        <artifactId>ai-mcp-bridge-spring-boot-mcp-registry-starter</artifactId>
    </dependency>

    <!-- 🔧 ai-mcp-bridge-spring-boot-mcp-server-expose-starter（核心依赖） -->
    <!-- 用于自动暴露 MCP 服务器，是实现服务注册的关键 -->
    <dependency>
        <groupId>io.github.xiaozhug</groupId>
        <artifactId>ai-mcp-bridge-spring-boot-mcp-server-expose-starter</artifactId>
    </dependency>

    <dependency>
        <groupId>org.springframework.cloud</groupId>
        <artifactId>spring-cloud-starter-netflix-eureka-client</artifactId>
    </dependency>

</dependencies>
```

### 客户端依赖
```xml
<dependencies>
    <!-- 🔌 ai-mcp-bridge-spring-boot-mcp-client-starter（核心依赖） -->
    <!-- 这是 AI MCP Bridge 项目提供的核心客户端依赖，必须包含 -->
    <dependency>
        <groupId>io.github.xiaozhug</groupId>
        <artifactId>ai-mcp-bridge-spring-boot-mcp-client-starter</artifactId>
    </dependency>

    <!-- 🔧 ai-mcp-bridge-spring-boot-mcp-registry-starter（核心依赖） -->
    <!-- 用于支持从注册中心发现 MCP 服务 -->
    <dependency>
        <groupId>io.github.xiaozhug</groupId>
        <artifactId>ai-mcp-bridge-spring-boot-mcp-registry-starter</artifactId>
    </dependency>

    <dependency>
        <groupId>org.springframework.cloud</groupId>
        <artifactId>spring-cloud-starter-netflix-eureka-client</artifactId>
    </dependency>

</dependencies>
```

## 快速开始

### 服务端配置与启动

1. **配置 application.yaml**

在服务端的 `application.yaml` 中添加配置：

```yaml
server:
  port: 8081

spring:
  application:
    name: native-mcp-registry-server

eureka:
  client:
    service-url:
      defaultZone: http://127.0.0.1:12345/eureka/
  instance:
    instance-id: ${spring.application.name}:${random.int}


# MianshiyaService MCP工具使用的API端点配置
endpoint:
  mianshiya:
    searchQuestion: https://api.mianshiya.com/api/question/mcp/search
    resultLink: https://www.mianshiya.com/question/%s
```

2. **定义原生 MCP 工具**

使用 Spring AI 原生 `@Tool` 注解创建 MCP 工具： [MianshiyaService.java](native-mcp-registry-server%2Fsrc%2Fmain%2Fjava%2Fio%2Fxiaozhug%2Fdemo%2Fnativeregistry%2Fmcpserver%2Fservice%2FMianshiyaService.java)

3. **启动服务**

### 客户端配置与使用

1. **配置 application.yaml**

在客户端的 `application.yaml` 中添加核心配置：

```yaml
spring:
  application:
    name: native-mcp-registry-client
  ai:
    openai:
      api-key: your-api-key
      chat:
        options:
          model: your-model-name
      base-url: your-openai-base-url

  cloud:
    # ai-mcp-bridge-spring-boot-mcp-client-starter中的discovery方式（核心配置）
    discovery:
      fetch:
        enabled: true

eureka:
  client:
    service-url:
      defaultZone: http://127.0.0.1:12345/eureka/
  instance:
    instance-id: ${spring.application.name}:${random.int}
```

2. **使用注册中心发现的 MCP 客户端 [NativeMcpRegistryClientApplication.java](native-mcp-registry-client%2Fsrc%2Fmain%2Fjava%2Fio%2Fxiaozhug%2Fdemo%2Fnativeregistry%2FNativeMcpRegistryClientApplication.java)**

## 自动注册机制

### 🔄 注册流程

1. **工具定义**：使用 `@Tool` 注解定义原生 MCP 工具
2. **服务启动**：应用启动时自动检测注册中心
3. **工具注册**：将 MCP 工具信息注册到服务实例元数据中

## 验证结果

### ✅ 服务注册验证

成功启动后，在 Eureka 控制台可以看到：

- **服务名称**：NATIVE-MCP-REGISTRY-SERVER
- **实例状态**：UP
- **元数据中包含 MCP 相关信息**

### ✅ MCP 工具元数据验证

您可以通过访问 `http://[eureka-server-address]:[port]/eureka/apps` 查看详细的服务实例元数据：

**关键元数据字段**：

```xml
<mcp-server-0>eyJ0eXBlIjoiTUNQX0ZJTEUiLCJzZXJ2aWNlTmFtZSI6bnVsbCwiaW5zdGFuY2VJZCI6bnVsbCwidXJsIjpudWxsLCJ0b29scyI6bnVsbCwiaXRlbXMiOlt7...（完整的Base64编码MCP元数据）</mcp-server-0>
<mcp-server-size>1</mcp-server-size>
```

这些元数据包含：

- 完整的 MCP 工具定义（Base64 编码）
- 所有工具的方法描述和参数定义
- 输入参数的 JSON Schema
- HTTP 请求模板信息

### ✅ 客户端发现验证
![img.png](img.png)

## 总结

**native-mcp-registry** 示例工程展示了 AI MCP Bridge 的原生 MCP Server Tool 支持和服务注册与发现功能。通过该功能，开发者可以直接定义符合 MCP 规范的服务接口，并自动注册到服务注册中心，支持客户端动态发现和调用。

该示例支持多种注册中心和负载均衡策略，提供了灵活的配置选项和完整的健康检查机制，可以满足企业级应用的需求。同时，它还支持与 Spring AI 生态系统的深度集成，为 AI 应用提供了强大的工具调用能力。