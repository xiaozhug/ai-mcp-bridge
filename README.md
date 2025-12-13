# AI MCP Bridge 🌉

# [中文](README.md) | [English](README_EN.md)

## 项目简介

AI MCP Bridge 是一个革命性的 MCP 服务治理平台，提供**一站式 Spring 应用 MCP 化解决方案**。它不仅能够将 Spring Web 项目中的 REST Controller 接口无缝升级为 MCP 服务，更提供**完整的 MCP Tool 注册发现生态**，让传统应用快速具备 AI 工具调用能力。

> 🎯 **核心价值**：零代码改造，让您的 Spring 应用瞬间拥抱 AI 时代！

## 核心功能

### 📄 编译时 MCP 元信息生成

- **提前生成**：在编译阶段自动分析 Spring Web 项目的 REST Controller 接口，生成对应的 MCP 元信息
- **工具定义**：自动生成 MCP 工具的方法签名、参数说明、返回值类型等元数据信息
- **调用规范**：为 AI 客户端提供标准的工具调用接口描述，确保调用的准确性和可靠性
- **性能优化**：减少运行时反射开销，提供更好的运行时性能

### 🛠️ REST Controller 无缝升级为 MCP 接口

- **多版本 JDK 支持**
  - 🟢 JDK 1.8：将基于 JDK 1.8 的 Spring Web 项目 REST 接口转换为 MCP 调用接口
  - 🟢 JDK 17：将基于 JDK 17 的 Spring Web 项目 REST 接口转换为 MCP 调用接口

- **完整生命周期支持**
  - 完全走原来 Spring Web 项目的调用生命周期
  - 支持拦截器、过滤器等

- **Spring Web 原生参数处理**
  - **自动参数绑定**：完整支持 Spring MVC 参数解析机制
  - **注解支持**：@RequestParam、@PathVariable、@RequestBody 等注解完整兼容
  - **类型转换**：Spring 类型转换器自动应用
  - **数据绑定**：数据绑定和验证机制无缝继承
  - **示例**：支持 HttpServletRequest 等原生 Web 对象参数自动处理

- **安全体系兼容**
  - 完全兼容原有 Spring Web 项目的认证授权体系
  - 支持自定义安全拦截器和权限校验
  - MCP 调用与原有 REST 接口共享同一套安全配置

- **协议兼容**
  - 基于 HTTP 协议
  - 符合 MCP 客户端调用规范

### 🤖 Spring AI MCP Server Tool 深度适配（仅限 JDK 17+）

- **环境要求**：专为 JDK 17 及以上版本的 Spring Web 项目设计
- **协议转换**：将 Spring Web 项目中的 REST Controller 接口转换为 MCP Server Tool
- **MCP 原生协议**：MCP Client 可直接使用 MCP 协议进行原生调用
- **生态优化**：专为 Spring AI 生态系统优化，支持 Spring AI MCP Client 直接集成，同时兼容多语言 MCP Client
- **版本适配**：完美支持 Spring Boot 3.x 与 JDK 17+ 的现代技术栈组合

### 🔍 动态 MCP Tool 注册与发现

- **自动注册**
  - 支持 REST Controller 暴露的 MCP 调用接口自动注册
  - 支持 Spring AI MCP Server Tool 的自动注册

- **动态发现**
  - MCP 客户端自动发现可用 Tools
  - 支持实时更新
  - 可发现通过 REST Controller 升级的 MCP 接口
  - 可发现专门的 Spring AI MCP Server Tools

- **负载均衡**
  - 多实例环境下负载均衡
  - 对 REST Controller 转化的 MCP 接口进行负载均衡
  - 对 Spring AI MCP Tools 进行负载均衡

### 🏗️ 多注册中心支持

- **灵活部署**
  - **单注册中心**：MCP Tools 与业务服务共享同一注册中心
  - **双注册架构**：MCP Tool 可注册到独立的注册中心，完全不影响原有业务注册中心

- **服务隔离**：实现业务服务与 MCP Tools 的流量分离和治理隔离
- **客户端双源**：MCP 客户端可同时从不同注册中心拉取服务

- **灵活组合**
  - 支持 Eureka、Nacos、Consul、Zookeeper 的笛卡尔积组合方式
  - **组合示例**：业务服务注册到 Nacos，MCP Tools 注册到 Eureka
  - **同类型双中心**：支持使用两个相同类型的注册中心
  - **单中心示例**：所有服务统一注册到单个 Nacos 或 Eureka 集群

### 🔌 智能 MCP 客户端调用封装

- **注册中心发现调用**
  - **自动服务发现**：从注册中心动态拉取 MCP 服务元信息
  - **协议自适应**：根据服务端暴露形式自动选择 HTTP 协议或 MCP 协议进行调用
  - **HTTP 协议兼容性**：智能 MCP 客户端支持通过 HTTP 协议进行 MCP 兼容性调用
  - **负载均衡**：支持多实例环境下的故障转移

- **直连 Controller 调用**
  - **元信息自动获取**：直接连接服务端暴露的 Controller 接口获取 MCP 元信息
  - **HTTP 协议调用**：底层采用 HTTP 协议构建 MCP 调用，兼容性更好
  - **零配置接入**：无需额外配置，直接连接即可使用

- **MCP Server 直连调用**
  - **标准 MCP 客户端**：直接连接服务端通过 Controller 接口转换的 MCP Server
  - **HTTP 传输层**：底层使用 HTTP 协议进行数据传输，兼顾标准性和性能
  - **工具自动发现**：通过 MCP Client 自动获取服务端工具信息和方法签名

- **统一调用接口**
  - **多协议统一**：封装不同调用方式的差异，提供统一的调用接口
  - **异常统一处理**：统一的错误处理和重试机制

## 核心优势

### 🚀 零改造升级
- 现有 REST Controller 无需任何代码修改
- 保持原有业务逻辑完全不变
- 支持渐进式迁移，降低升级风险

### 🌐 企业级服务治理
- 多注册中心并行支持
- 服务发现与负载均衡

### 🔌 灵活调用方式
- 支持多种客户端调用模式，适应不同场景需求
- 协议自适应，最大化兼容性和性能
- 统一的调用接口，简化客户端集成复杂度

## 示例项目

### MAVEN
```xml
<dependencyManagement>
    <dependencies>
        <dependency>
            <groupId>io.github.xiaozhug</groupId>
            <artifactId>ai-mcp-bridge-dependencies</artifactId>
            <version>1.0.0</version>
            <type>pom</type>
            <scope>import</scope>
        </dependency>
    </dependencies>
</dependencyManagement>
```

我们为每个核心功能提供了完整的示例项目，每个示例都有详细的独立文档：

| 示例项目                    | 功能说明                                                          | 技术栈                                                            | 核心特性                                     | 文档位置                                           |
|-------------------------|---------------------------------------------------------------|----------------------------------------------------------------|------------------------------------------|------------------------------------------------|
| **metadata-compile**    | 编译时元数据生成                                                      | (Spring Boot 2.x + JDK 8)/(Spring Boot 3.x + JDK 17)           | 编译阶段自动生成MCP Tools元数据和调用规范                | [查看文档](demo%2Fmetadata-compile%2FREADME.md)    |
| **rest-to-mcp-http**    | Controller MCP 升级                                             | (Spring Boot 2.x + JDK 8)/(Spring Boot 3.x + JDK 17)           | REST → MCP 协议转换                          | [查看文档](demo%2Frest-to-mcp-http%2FREADME.md)    |
| **rest-to-mcp-tool**    | REST转Spring AI MCP Tool                                       | Spring Boot 3.x + JDK 17                                       | REST Controller 无缝适配为 Spring AI MCP Tool | [查看文档](demo%2Frest-to-mcp-tool%2FREADME.md)    |
| **rest-mcp-registry**   | REST 转换 MCP 接口注册发现                                            | (Spring Boot 2.x + JDK 8)/(Spring Boot 3.x + JDK 17) + 注册中心支持  | REST 升级接口的服务治理                           | [查看文档](demo%2Frest-mcp-registry%2FREADME.md)   |
| **native-mcp-registry** | 原生 MCP Tool 注册发现                                              | Spring Boot 3.x + JDK 17 + 注册中心支持                              | 原生 MCP Tools 服务治理                        | [查看文档](demo%2Fnative-mcp-registry%2FREADME.md) |
| **multi-registry**      | 多注册中心架构                                                       | (Spring Boot 2.x + JDK 8)/(Spring Boot 3.x + JDK 17) + 双注册中心组合 | 业务与 MCP Tools 隔离                         | [查看文档](demo%2Fmulti-registry%2FREADME.md)      |
| **enterprise-solution** | 企业级完整方案(rest-to-mcp-http + rest-to-mcp-tool + multi-registry) | Spring Boot 3.x + JDK 17                                       | 端到端解决方案                                  | [查看文档](demo%2Fenterprise-solution%2FREADME.md) |



详细配置和使用说明请参考各个示例项目的文档。



---

<div align="center">

## 🚀 立即开始，让您的 Spring 应用拥抱 AI 时代！

**⭐ 如果这个项目对您有帮助，请给一个 Star！**

</div>