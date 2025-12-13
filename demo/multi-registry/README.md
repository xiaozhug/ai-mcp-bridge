# Multi Registry 示例

# [中文](README.md) | [English](README_EN.md)

## 项目简介

这是一个演示如何将 MCP 工具**同时注册到两个不同服务注册中心**的示例项目。本示例使用 Nacos 作为第一个注册中心，Consul 作为第二个注册中心（MCP 元数据发布目标），通过配置文件中的集中配置实现双注册中心的无缝集成。

## 核心特性

### 🌐 双注册中心支持
- **集中配置管理**：所有注册中心配置在配置文件中集中管理，简化运维
- **灵活配置选择**：通过 `mcp.registry` 配置指定第二个注册中心
- **配置冲突避免**：采用分离式配置设计，有效避免相同注册中心的配置冲突

### 🔧 配置管理
- **一站式配置**：所有注册中心配置在主配置文件中集中管理
- **条件化启用**：通过 `mcp.registry` 参数灵活指定启用的第二个注册中心
- **分离式配置**：使用独立的配置文件管理不同注册中心的详细配置

### 🚀 自动注册机制
- **双注册中心并行**：支持同时向两个不同的注册中心注册服务
- **完整元数据发布**：在服务实例中发布详尽的 MCP 工具信息
- **服务发现支持**：集成 `spring.cloud.discovery.fetch.enabled=true` 配置，实现客户端自动发现 MCP 工具

## 依赖说明

### 📦 [metadata-compile](..%2Fmetadata-compile)（演示项目共用）

**重要说明**：此依赖仅用于演示项目的代码共享，在实际项目中您不需要依赖此模块。

- **演示用途**：为了避免在多个演示项目中重复编写相同的 UserController 代码
- **实际使用**：在您自己的项目中，可以直接配置 MCP 注解处理器来生成您自己的 REST Controller 元数据
- **生成原理**：通过 Maven 编译器插件分析您的 REST Controller 自动生成元数据

### 服务端依赖

```xml
<dependencies>
    <!-- 元数据生成（演示项目共用） -->
    <dependency>
        <groupId>io.github.xiaozhug</groupId>
        <artifactId>metadata-compile-jdk8</artifactId>
    </dependency>

    <!-- MCP 服务注册启动器（核心依赖） -->
    <dependency>
        <groupId>io.xiaozhug</groupId>
        <artifactId>ai-mcp-bridge-spring-boot-mcp-registry-starter</artifactId>
    </dependency>

    <!-- MCP 元数据暴露支持（核心依赖） -->
    <dependency>
        <groupId>io.xiaozhug</groupId>
        <artifactId>ai-mcp-bridge-spring-boot-mcp-file-expose-starter</artifactId>
    </dependency>

    <!-- 注册中心客户端（支持多种注册中心） -->
    <dependency>
        <groupId>com.alibaba.cloud</groupId>
        <artifactId>spring-cloud-starter-alibaba-nacos-discovery</artifactId>
    </dependency>

    <dependency>
        <groupId>org.springframework.cloud</groupId>
        <artifactId>spring-cloud-starter-consul-discovery</artifactId>
    </dependency>

    <!-- 健康检查支持 -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-actuator</artifactId>
    </dependency>
</dependencies>
```

### 客户端依赖

```xml
<dependencies>
    <!-- MCP 服务注册启动器（核心依赖） -->
    <dependency>
        <groupId>io.github.xiaozhug</groupId>
        <artifactId>ai-mcp-bridge-spring-boot-mcp-registry-starter</artifactId>
    </dependency>

    <!-- MCP 客户端启动器（核心依赖） -->
    <dependency>
        <groupId>io.github.xiaozhug</groupId>
        <artifactId>ai-mcp-bridge-spring-boot-mcp-client-starter</artifactId>
    </dependency>

    <!-- 注册中心客户端（支持多种注册中心） -->
    <dependency>
        <groupId>com.alibaba.cloud</groupId>
        <artifactId>spring-cloud-starter-alibaba-nacos-discovery</artifactId>
    </dependency>

    <dependency>
        <groupId>org.springframework.cloud</groupId>
        <artifactId>spring-cloud-starter-consul-discovery</artifactId>
    </dependency>

    <!-- 健康检查支持 -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-actuator</artifactId>
    </dependency>
</dependencies>
```

## 快速开始

### 服务端配置与启动

1. **主配置文件 (application.yaml)**

```yaml
server:
  port: 8082

spring:
  application:
    name: multi-registry-server

  cloud:
    nacos:
      discovery:
        # 第一个注册中心（主注册中心）
        server-addr: 127.0.0.1:8848
    consul:
      enabled: false
      host: 127.0.0.1
      port: 8500
      discovery:
        hostname: 127.0.0.1

# MCP 配置
mcp:
  # 指定第二个注册中心（MCP 元数据将发布到此注册中心）
  # 本示例使用 consul 作为第二个注册中心
  registry: consul
```

2. **多注册中心配置文件**

项目提供了4个预配置的注册中心配置文件，用于指定 MCP 元数据发布的目标注册中心：

#### application-mcp-nacos.yaml
```yaml
spring:
  cloud:
    nacos:
      discovery:
        # 第二个注册中心（MCP 元数据发布目标）
        server-addr: 127.0.0.1:8848
```

#### application-mcp-eureka.yaml
```yaml
eureka:
  client:
    enabled: true
    service-url:
      defaultZone: http://127.0.0.1:12346/eureka/
```

#### application-mcp-consul.yaml
```yaml
spring:
  cloud:
    consul:
      enabled: true
      host: 127.0.0.1
      port: 8500
      discovery:
        hostname: 127.0.0.1
```

#### application-mcp-zookeeper.yaml
```yaml
spring:
  cloud:
    zookeeper:
      enabled: true
      connect-string: 127.0.0.1:2181
```

3. **启动服务端**

### 客户端配置与使用

1. **客户端配置文件 (application.yaml)**

**配置说明：**
- Nacos 作为第一个注册中心（主注册中心）
- Consul 作为第二个注册中心（MCP 元数据注册中心）
- 客户端需要同时连接到这两个注册中心

```yaml
server:
  port: 8081

spring:
  application:
    name: multi-registry-client
  ai:
    openai:
      api-key: your-api-key
      chat:
        options:
          model: your-model
      base-url: your-base-url

  cloud:
    nacos:
      discovery:
        # 第一个注册中心（主注册中心）
        server-addr: 127.0.0.1:8848
    consul:
      enabled: false
      host: 127.0.0.1
      port: 8500
      discovery:
        hostname: 127.0.0.1

    # 服务发现核心配置（必须启用）
    discovery:
      fetch:
        enabled: true

# MCP 配置
mcp:
  # 指定要连接的 MCP 注册中心（Consul 作为 MCP 元数据注册中心）
  registry: consul
```

2. **客户端使用示例 [MultiRegistryClientApplication.java](multi-registry-client%2Fsrc%2Fmain%2Fjava%2Fio%2Fxiaozhug%2Fdemo%2Fmultiregistry%2FMultiRegistryClientApplication.java)**

## 工作机制

### 配置加载阶段
- 应用启动时自动加载所有预配置的注册中心配置
- 解析 `mcp.registry` 参数确定 MCP 元数据发布的目标注册中心

### 自动检测阶段
- 系统读取 `mcp.registry` 配置指定的注册中心类型
- 验证对应注册中心的配置完整性和有效性
- 加载对应的 `application-mcp-{registry}.yaml` 配置文件

### 服务注册阶段
- 向主注册中心注册基础服务信息
- 向 `mcp.registry` 指定的注册中心注册服务并发布 MCP 元数据
- 客户端通过 `spring.cloud.discovery.fetch.enabled=true` 配置自动发现 MCP 工具

## 验证结果

### ✅ 服务注册验证
成功启动后，在两个注册中心控制台都可以观察到：

- **服务名称**：`multi-registry-server`
- **实例状态**：`UP`
- **注册状态**：双注册中心均成功注册

![img.png](img.png)
![img_1.png](img_1.png)


### ✅ MCP 工具元数据验证

#### 主注册中心（无 MCP 元数据）
在第一个注册中心（本示例中的 Nacos）中，**仅包含标准的服务注册信息，不包含任何 MCP 特定元数据**，保持完全的向后兼容性。

![img_2.png](img_2.png)

#### MCP 注册中心（包含完整元数据）
在 `mcp.registry` 配置指定的注册中心（本示例中的 Consul）中，可以看到完整的 MCP 工具元数据：

![img_3.png](img_3.png)

**说明**：
- `<mcp-file-size>` 值表示元数据分片数量，通常大于 0
- 当元数据较大时，会自动分片存储，生成 `<mcp-file-0>`、`<mcp-file-1>` 等多个分片
- 客户端会自动合并所有分片，还原完整的 MCP 工具元数据

#### 注意事项（Nacos 元数据长度限制）
如果使用 Nacos 作为 MCP 注册中心，需要注意 Nacos 默认对元数据长度有一定限制。当 MCP 工具元数据较大时，可能需要调整 Nacos 服务端配置：

```bash
# 在 Nacos 服务端环境变量中设置
export NACOS_NAMING_SERVICE_METADATA_LENGTH=20480  # 示例：设置为20KB

# 或在 Nacos 配置文件中设置
nacos.naming.service.metadata.length=20480
```

这将确保 MCP 工具的完整元数据能够成功发布到 Nacos 注册中心。

#### 传统注册中心（仅标准服务信息）
在另一个注册中心中，仅包含标准的服务注册信息，不包含任何 MCP 特定元数据，保持完全的向后兼容性。

## 应用场景

### 🔄 渐进式 MCP 部署

- **新功能发布**：在支持 MCP 的新注册中心发布完整元数据
- **向后兼容**：在传统注册中心保持基础服务注册，确保系统平滑过渡
- **风险控制**：分阶段部署，降低系统升级风险

### 🧪 MCP 功能验证

- **功能测试**：在特定注册中心验证 MCP 工具发现功能
- **兼容性保障**：确保传统服务发现机制完全不受影响
- **性能评估**：对比分析新旧注册中心的性能表现

### 🌐 混合环境支持

- **团队协作**：适应不同团队使用不同注册中心的多样化场景
- **多地域部署**：满足不同地域或业务线的特定注册中心需求

## 优势总结

- **灵活性**：支持多种注册中心组合，适应不同技术栈
- **兼容性**：确保新旧系统无缝衔接，降低迁移成本
- **可扩展性**：易于扩展支持更多注册中心类型
- **服务隔离优化**：客户端的 MCP 工具只会从 MCP 注册中心拉取，避免拉取无用服务；同时主注册中心也不会拉取 MCP 注册中心的服务，实现了服务发现的精准隔离

## 重要依赖配置说明

### ⚠️ 关键依赖位置要求

**`ai-mcp-bridge-spring-boot-mcp-registry-starter` 依赖必须在 pom.xml 中靠前声明**

```xml
<dependencies>
    <!-- 必须靠前声明：MCP 服务注册启动器 -->
    <dependency>
        <groupId>io.github.xiaozhug</groupId>
        <artifactId>ai-mcp-bridge-spring-boot-mcp-registry-starter</artifactId>
    </dependency>
    <!-- 其他依赖... -->
</dependencies>
```

# 🔧 关键依赖位置要求的原因详解

## 📝 背景：Spring 父子容器机制的挑战

在实现 Spring 父子容器机制来支持双注册中心时，我面临一个关键的技术挑战：

### 问题描述
- **需求**：在子容器中需要忽略 `@ConditionalOnMissingBean` 注解的某些功能
- **挑战**：Spring Boot 框架本身没有提供合适的扩展点来修改这个行为
- **场景**：当 `@ConditionalOnMissingBean(search = SearchStrategyALL)` 在子容器中执行时，它会搜索父容器中的 Bean，导致父子容器间的 Bean 定义冲突
  这正是我们复制并增强 SpringBootCondition 类并要求依赖顺序优先的根本原因。这种解决方案虽然不完美，但在当前技术约束下是实现双注册中心功能的必要手段。

### 技术分析
Spring 的条件注解机制在父子容器环境下存在设计限制，需要通过自定义扩展来解决跨容器 Bean 查找的问题。

### 解决方案
这正是我们复制并增强 SpringBootCondition 类并要求依赖顺序优先的根本原因。

## 总结

**multi-registry** 示例工程展示了 AI MCP Bridge 的双注册中心支持功能，通过该功能，开发者可以将 MCP 工具同时注册到两个不同的服务注册中心。本示例使用 Nacos 作为第一个注册中心，Consul 作为第二个注册中心（MCP 元数据发布目标）。

该示例通过配置文件中的集中配置实现双注册中心的无缝集成，支持灵活的注册中心组合选择，确保新旧系统无缝衔接，同时易于扩展支持更多注册中心类型。核心依赖包括 `spring-mcp-bridge-registry-starter`、`spring-mcp-bridge-metadata-file-expose-starter` 和 `spring-mcp-bridge-client-starter`，这些依赖共同实现了多注册中心的协调管理和 MCP 工具的发现调用功能。