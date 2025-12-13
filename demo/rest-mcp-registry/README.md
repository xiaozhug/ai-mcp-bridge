# REST MCP Registry 示例

# [中文](README.md) | [English](README_EN.md)

## 项目简介

这是一个演示如何将 REST 转换的 MCP 接口自动注册到**服务注册中心**的完整示例项目。项目通过 `ai-mcp-bridge-spring-boot-mcp-registry-starter` 实现 MCP 工具的自动服务注册，为 MCP 工具提供标准的服务治理能力。

## 核心特性

### 🚀 自动服务注册
- **零配置注册**：无需额外配置，自动完成 MCP 工具的服务注册
- **多注册中心支持**：支持 Eureka（本示例默认使用）、Nacos、Consul、Zookeeper 等主流注册中心，通过 enabled 开关控制
- **标准协议**：完全兼容 Spring Cloud 服务注册标准

### 📋 完整的服务治理
- **服务发现**：MCP 客户端自动发现可用的工具服务
- **健康检查**：集成健康检查机制，支持故障自动隔离
- **元数据发布**：在服务实例中发布完整的 MCP 工具信息

### 🛠️ 开箱即用
- **单注册中心**：业务服务和 MCP 工具注册到同一注册中心
- **统一治理**：统一的监控、管理和维护
- **简化部署**：降低运维复杂度，快速上线

### 核心组件
- **spring-mcp-bridge-registry-starter**：MCP 服务注册启动器（核心依赖）
- **spring-mcp-bridge-metadata-file-expose-starter**：元数据暴露支持（核心依赖）
- **metadata-compile**：元数据生成模块（演示项目共用）
- **spring-mcp-bridge-client-starter**：MCP 客户端支持（客户端核心依赖）
- **注册中心客户端**：Nacos/Eureka/Consul/Zookeeper（任选其一）

## 环境要求

### 服务端要求
- JDK 8 或 JDK 17
- Maven 3.3+
- Spring Boot 2.x 或 3.x
- 服务注册中心（本示例支持 Eureka、Nacos、Consul、Zookeeper）

### 客户端要求
- JDK 17+
- Maven 3.3+
- Spring Boot 3.x
- 服务注册中心（与服务端配置一致）

## 依赖说明

### 📦 [metadata-compile](..%2Fmetadata-compile)（演示项目共用）

**重要说明**：此依赖仅用于演示项目的代码共享，在实际项目中您不需要依赖此模块。

- **演示用途**：为了避免在多个演示项目中重复编写相同的 UserController 代码
- **实际使用**：在您自己的项目中，可以直接配置 MCP 注解处理器来生成您自己的 REST Controller 元数据
- **生成原理**：通过 Maven 编译器插件分析您的 REST Controller 自动生成元数据

### 服务端依赖
```xml
<dependencies>
    <dependency>
        <groupId>io.github.xiaozhug</groupId>
        <artifactId>metadata-compile-jdk8</artifactId>
    </dependency>
    
    <!-- 🔌 spring-mcp-bridge-registry-starter（核心依赖） -->
    <!-- 这是 AI MCP Bridge 项目提供的核心注册中心依赖，必须包含 -->
    <dependency>
        <groupId>io.github.xiaozhug</groupId>
        <artifactId>ai-mcp-bridge-spring-boot-mcp-registry-starter</artifactId>
    </dependency>

    <!-- 🔧 spring-mcp-bridge-metadata-file-expose-starter（核心依赖） -->
    <!-- 用于自动暴露 MCP 元数据，是实现服务注册的关键 -->
    <dependency>
        <groupId>io.github.xiaozhug</groupId>
        <artifactId>ai-mcp-bridge-spring-boot-mcp-file-expose-starter</artifactId>
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

### 服务端配置

1. **配置 application.yaml**

在服务端的 `application.yaml` 中添加配置：

```yaml
spring:
  application:
    name: rest-mcp-registry-server

eureka:
  client:
    service-url:
      defaultZone: http://127.0.0.1:12345/eureka/
  instance:
    instance-id: ${spring.application.name}:${random.int}
```

### 客户端配置与使用

1. **配置 application.yaml**

在客户端的 `application.yaml` 中添加核心配置：

```yaml
spring:
  application:
    name: rest-mcp-registry-client
  ai:
    openai:
      api-key: your-api-key
      chat:
        options:
          model: your-model-name
      base-url: your-base-url

  cloud:
    # spring-mcp-bridge-client-starter中的discovery方式（核心配置）
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

2. **使用注册中心发现的 MCP 客户端 [RestMcpRegistryClientApplication.java](rest-mcp-registry-client%2Fsrc%2Fmain%2Fjava%2Fio%2Fxiaozhug%2Fdemo%2Frestmcpregistry%2FRestMcpRegistryClientApplication.java)**

## 自动注册机制

### 🔄 注册流程

1. **元数据生成**：编译时生成 MCP 工具元数据
2. **服务启动**：应用启动时自动检测注册中心
3. **工具注册**：将 MCP 工具信息注册到服务实例元数据中

## 验证结果

### ✅ 服务注册验证

成功启动后，在 Eureka 控制台可以看到：

- **服务名称**：REST-MCP-REGISTRY-SERVER
- **实例状态**：UP
- **元数据中包含 MCP 相关信息**

### ✅ MCP 工具元数据验证

您可以通过访问 `http://[eureka-server-address]:[port]/eureka/apps` 查看详细的服务实例元数据：

**关键元数据字段**：

```xml
<mcp-file-0>eyJ0eXBlIjoiTUNQX0ZJTEUiLCJzZXJ2aWNlTmFtZSI6bnVsbCwiaW5zdGFuY2VJZCI6bnVsbCwidXJsIjpudWxsLCJ0b29scyI6bnVsbCwiaXRlbXMiOlt7...（完整的Base64编码MCP元数据）</mcp-file-0>
<mcp-file-size>1</mcp-file-size>
```

这些元数据包含：

- 完整的 MCP 工具定义（Base64 编码）
- 所有工具的方法描述和参数定义
- 输入参数的 JSON Schema
- HTTP 请求模板信息

## 总结

**REST MCP Registry** 示例展示了如何将传统的 REST API 无缝升级为具备服务治理能力的 MCP 工具。通过自动化的服务注册机制，开发者可以：

### 🚀 快速集成
- **引入 starter 依赖**即可获得完整的服务注册能力
- **零配置启动**，自动完成 MCP 工具的注册和发现
- **标准化的依赖管理**，与 Spring Boot 生态完美融合

### 📋 标准治理
- **基于 Spring Cloud 标准**实现，与现有微服务体系完美融合
- **兼容主流注册中心**，保持技术栈的一致性
- **符合企业架构规范**，降低学习和迁移成本

### 🔧 灵活部署
- **支持多种注册中心**（Eureka、Nacos、Consul、Zookeeper），适应不同技术架构
- **云原生友好**，支持容器化和动态扩缩容

---

### 🎯 核心价值

该示例为企业在 AI 转型过程中提供了一条**平滑的升级路径**，让现有 REST API 在**不做任何代码修改**的情况下，即可成为 AI 生态系统中的重要组成部分，实现传统应用与 AI 能力的**深度融合**。