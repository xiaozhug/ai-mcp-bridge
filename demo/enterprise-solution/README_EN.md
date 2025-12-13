# Enterprise Solution Example

# [中文](README.md) | [English](README_EN.md)

## Project Overview

Enterprise Solution is an **enterprise-level MCP solution** that integrates the core functionalities of all previous example projects. It provides complete capabilities for REST to MCP conversion, multi-registry support, MCP service registration, and serves as a comprehensive solution for enterprise AI application integration.

This example uses Nacos as the first registry and Consul as the second registry (target for MCP metadata publishing). It achieves seamless integration of dual registries through centralized configuration in the configuration file.

## 🚀 Core Features

### 🔄 Full-Chain MCP Conversion & Registration
- **REST to MCP Automatic Conversion**: Seamlessly converts existing REST APIs into MCP tools
- **MCP Service Automatic Registration**: Registers both converted MCP tools and MCP Servers to service registries
- **Dual Protocol Parallel Support**: Simultaneously supports MCP protocol and HTTP REST protocol calls

### 🌐 Multi-Registry Support
- **Four Registry Support**: Nacos, Eureka, Consul, Zookeeper
- **Dual Registry Parallel Operation**: Supports registering services to two different registries simultaneously
- **Centralized Configuration Management**: All registry configurations are managed centrally in the configuration file, eliminating the need for additional registry management services

### 🛠️ Enterprise-Level Features
- **Service Governance Integration**: Complete service discovery and metadata publishing
- **Compile-time Metadata Generation**: AI-enhanced natural language descriptions with incremental update mechanism
- **Type-Safe Invocation**: Complete type safety guarantee based on JSON Schema

## Dependencies Description

### 📦 [metadata-compile](..%2Fmetadata-compile) (Shared by demo projects)

**Important Note**: This dependency is only used for code sharing between demo projects. In actual projects, you do not need to depend on this module.

- **Demonstration Purpose**: To avoid duplicating the same UserController code across multiple demo projects
- **Actual Usage**: In your own projects, you can directly configure the MCP annotation processor to generate metadata for your own REST Controllers
- **Generation Principle**: Automatically generates metadata by analyzing your REST Controllers through the Maven compiler plugin

```xml
<dependencies>
    <dependency>
        <groupId>io.github.xiaozhug</groupId>
        <artifactId>metadata-compile-jdk17</artifactId>
    </dependency>

    <!-- MCP Core Registration & Discovery -->
    <dependency>
        <groupId>io.github.xiaozhug</groupId>
        <artifactId>ai-mcp-bridge-spring-boot-mcp-registry-starter</artifactId>
    </dependency>

    <!-- MCP Metadata File Exposure -->
    <dependency>
        <groupId>io.github.xiaozhug</groupId>
        <artifactId>ai-mcp-bridge-spring-boot-mcp-file-expose-starter</artifactId>
    </dependency>

    <!-- MCP Server Exposure Support -->
    <dependency>
        <groupId>io.github.xiaozhug</groupId>
        <artifactId>ai-mcp-bridge-spring-boot-mcp-server-expose-starter</artifactId>
    </dependency>

    <!-- Registry Clients (Supporting Multiple Registries) -->
    <dependency>
        <groupId>com.alibaba.cloud</groupId>
        <artifactId>spring-cloud-starter-alibaba-nacos-discovery</artifactId>
    </dependency>

    <dependency>
        <groupId>org.springframework.cloud</groupId>
        <artifactId>spring-cloud-starter-consul-discovery</artifactId>
    </dependency>

    <!-- Health Check Support -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-actuator</artifactId>
    </dependency>
</dependencies>
```

### Client Maven Dependency Configuration

```xml
<dependencies>
<!-- MCP Service Registration Starter -->
    <dependency>
        <groupId>io.github.xiaozhug</groupId>
        <artifactId>ai-mcp-bridge-spring-boot-mcp-registry-starter</artifactId>
    </dependency>

    <!-- MCP Client Starter -->
    <dependency>
        <groupId>io.github.xiaozhug</groupId>
        <artifactId>ai-mcp-bridge-spring-boot-mcp-client-starter</artifactId>
    </dependency>

    <!-- Registry Clients (Supporting Multiple Registries) -->
    <dependency>
        <groupId>com.alibaba.cloud</groupId>
        <artifactId>spring-cloud-starter-alibaba-nacos-discovery</artifactId>
    </dependency>

    <dependency>
        <groupId>org.springframework.cloud</groupId>
        <artifactId>spring-cloud-starter-consul-discovery</artifactId>
    </dependency>
</dependencies>
```

## 🔧 Function Module Details

### 1. REST to MCP Conversion Module

**Function Description**: Automatically converts existing [REST Controller](..%2Fmetadata-compile%2Fmetadata-compile-jdk17) into MCP tools without modifying existing code.

**Core Features**:
- **Zero-Code Intrusive Conversion**: No modification needed for existing REST Controllers; automatic conversion to MCP tools
- **Automatic MCP Tool Definition Generation**: Creates corresponding MCP tool definitions for each REST method
- **JSON Schema Auto-generation**: Generates complete JSON Schema based on method parameters
- **Complete Type-Safe Invocation**: Supports parameter type checking and validation to ensure call safety

### 2. Standard MCP Server Module

**Function Description**: Uses standard Spring AI \`@Tool\` annotations to directly define MCP tools [MianshiyaService.java](enterprise-solution-server%2Fsrc%2Fmain%2Fjava%2Fio%2Fxiaozhug%2Fdemo%2Fenterprise%2Fmcpserver%2Fservice%2FMianshiyaService.java).

**Core Features**:
- **Standard MCP Tool Definition**: Uses Spring AI \`@Tool\` annotations to directly define MCP tools
- **Complete Tool Description**: Supports detailed tool descriptions and parameter explanations
- **Flexible Parameter Handling**: Supports various parameter types and complex data structures
- **Coexistence with REST Conversion**: Can be used simultaneously with the REST to MCP conversion module

### 3. MCP Service Registration Module

**Function Description**: Automatically registers MCP tools and MCP Servers to configured service registries, supporting parallel registration to multiple registries.

**Core Features**:
- **Automatic Service Registration**: Automatically registers MCP tools and MCP Servers to configured service registries
- **Multi-Registry Parallel Registration**: Supports registering service instances to multiple registries simultaneously
- **Complete Service Discovery**: Provides service discovery capability, supporting automatic discovery of available tools by clients
- **Metadata Publishing**: Publishes complete MCP tool and MCP Server metadata information in service instances

### 4. Multi-Registry Configuration Module

**Function Description**: Manages connections and registration processes of multiple registries through configuration files, eliminating the need for additional intelligent management components.

**Core Features**:
- **Centralized Configuration Management**: All registry configurations are centrally managed in application.yaml, simplifying operational complexity
- **Configuration Conflict Avoidance**: Uses separated configuration design to avoid conflicts with the same registry
- **Dual Registry Parallel Operation**: Supports registering services to two different registries simultaneously, manually specified through configuration files

## Quick Start

### Server Configuration and Startup

1. **Server Configuration File (application.yaml)**

```yaml
server:
  port: 8082

spring:
  application:
    name: enterprise-solution-server

  cloud:
    nacos:
      discovery:
        # First registry (primary registry)
        server-addr: 127.0.0.1:8848
    consul:
      enabled: false
      host: 127.0.0.1
      port: 8500
      discovery:
        hostname: 127.0.0.1

# MCP Configuration
mcp:
  # Specifies the second registry (MCP metadata will be published to this registry)
  # Options: eureka, nacos, consul, zookeeper
  registry: consul

# Mianshiya API Configuration (used by MianshiyaService)
endpoint:
  mianshiya:
    searchQuestion: https://api.mianshiya.com/api/question/mcp/search
    resultLink: https://www.mianshiya.com/question/%s
```

2. **Start the Server**

### Client Configuration and Usage

1. **Client Configuration File (application.yaml)**

```yaml
server:
  port: 8081

spring:
  application:
    name: enterprise-solution-client
  ai:
    openai:
      api-key: your-api-key
      chat:
        options:
          model: your-model-name
      base-url: your-openai-base-url

  cloud:
    nacos:
      discovery:
        # First registry (primary registry)
        server-addr: 127.0.0.1:8848
    consul:
      enabled: false
      host: 127.0.0.1
      port: 8500
      discovery:
        hostname: 127.0.0.1

    # Service discovery core configuration (must be enabled)
    discovery:
      fetch:
        enabled: true

# MCP Configuration
mcp:
  # Specifies the MCP registry to connect to
  registry: consul
```

2. **Client Usage Example [EnterpriseSolutionClientApplication.java](enterprise-solution-client%2Fsrc%2Fmain%2Fjava%2Fio%2Fxiaozhug%2Fdemo%2Fenterprise%2FEnterpriseSolutionClientApplication.java)**

## How It Works

### Configuration Loading Phase
- Automatically loads all pre-configured registry settings when the application starts
- Parses the \`mcp.registry\` parameter to determine the target registry for MCP metadata publishing

### Automatic Detection Phase
- System reads the registry type specified in the \`mcp.registry\` configuration
- Verifies the completeness and validity of the corresponding registry configuration
- Loads the corresponding \`application-mcp-{registry}.yaml\` configuration file

### Service Registration Phase
- Registers basic service information to the primary registry
- Registers services to the registry specified by \`mcp.registry\` and publishes MCP metadata
- Clients automatically discover MCP tools through \`spring.cloud.discovery.fetch.enabled=true\` configuration

### MCP Tool Discovery and Invocation Phase
- Clients automatically connect to the configured registry upon startup
- Obtain available MCP tool lists through service discovery mechanism
- Build invocation requests based on tool metadata
- Send requests to corresponding service instances and process responses

## Verification Results

### ✅ Service Registration Verification
After successful startup, the following can be observed in both registry consoles:

- **Service Name**: \`enterprise-solution-server\`
- **Instance Status**: \`UP\`
- **Registration Status**: Successfully registered in both registries

![img.png](img.png)
![img_1.png](img_1.png)

### ✅ MCP Tool Metadata Verification

#### Primary Registry (No MCP Metadata)
In the first registry, **only contains standard service registration information without any MCP-specific metadata**, maintaining complete backward compatibility.

![img_2.png](img_2.png)

#### MCP Registry (Contains Complete Metadata)
In the registry specified by the \`mcp.registry\` configuration, complete MCP tool metadata can be seen:

![img_3.png](img_3.png)

**Explanation**:
- The \`<mcp-file-size>\` value indicates the number of metadata fragments, usually greater than 0
- When metadata is large, it is automatically fragmented and stored, generating multiple fragments such as \`<mcp-file-0>\`, \`<mcp-file-1>\`, etc.
- Clients automatically merge all fragments to restore complete MCP tool metadata

#### Important Notes (Nacos Metadata Length Limit)
If using Nacos as the MCP registry, note that Nacos has default length limits for metadata. When MCP tool metadata is large, Nacos server configuration may need adjustment:

```bash
# Set in Nacos server environment variables
export NACOS_NAMING_SERVICE_METADATA_LENGTH=20480  # Example: Set to 20KB

# Or set in Nacos configuration file
nacos.naming.service.metadata.length=20480
```

## Application Scenarios

### 🔄 Enterprise-Level MCP Deployment

- **Unified Service Governance**: Centralized management of all MCP tool registration and discovery
- **Multi-Protocol Support**: Simultaneously supports MCP protocol and HTTP REST protocol to meet different scenario requirements
- **Flexible Deployment Strategies**: Supports multiple registry combinations to adapt to different enterprise technology stacks

### 🧪 Hybrid MCP Tool Development

- **Progressive Migration**: Existing REST APIs can be converted to MCP tools without modification
- **Standard MCP Development**: Simultaneously supports direct MCP tool development using standard \`@Tool\` annotations
- **Unified Management**: MCP tools developed through both methods are uniformly registered and managed

### 🌐 Large-Scale Service Integration

- **Service Isolation Optimization**: Clients' MCP tools only pull from the MCP registry, avoiding pulling unnecessary services; meanwhile, the primary registry does not pull services from the MCP registry, achieving precise isolation of service discovery
- **Unified Tool Discovery**: Clients discover and invoke all MCP tools through a unified interface
- **Complete Type Safety**: Type checking based on JSON Schema to ensure invocation safety

## Advantages Summary

- **Functional Completeness**: Integrates all core functionalities, providing a one-stop MCP solution
- **Flexibility**: Supports multiple registry combinations to adapt to different technology stacks
- **Compatibility**: Ensures seamless integration between old and new systems, reducing migration costs
- **Scalability**: Easy to extend support for more registry types and tool types

## Important Dependency Configuration Notes

### ⚠️ Critical Dependency Position Requirement

**The \`ai-mcp-bridge-spring-boot-mcp-registry-starter\` dependency must be declared early in pom.xml**

```xml
<dependencies>
<!-- Must be declared early: MCP Service Registration Starter -->
    <dependency>
        <groupId>io.github.xiaozhug</groupId>
        <artifactId>ai-mcp-bridge-spring-boot-mcp-registry-starter</artifactId>
    </dependency>
<!-- Other dependencies... -->
</dependencies>
```

# 🔧 Detailed Explanation of Critical Dependency Position Requirement

## 📝 Background: Challenges with Spring Parent-Child Container Mechanism

When implementing the Spring parent-child container mechanism to support dual registries, I faced a key technical challenge:

### Problem Description
- **Requirement**: Need to ignore certain functionalities of the \`@ConditionalOnMissingBean\` annotation in the child container
- **Challenge**: The Spring Boot framework itself does not provide suitable extension points to modify this behavior
- **Scenario**: When \`@ConditionalOnMissingBean(search = SearchStrategyALL)\` executes in the child container, it searches for Beans in the parent container, causing Bean definition conflicts between parent and child containers
  This is precisely the fundamental reason why we copied and enhanced the SpringBootCondition class and require dependency order priority. Although this solution is not perfect, it is the necessary means to achieve dual registry functionality under current technical constraints.

### Technical Analysis
Spring's conditional annotation mechanism has design limitations in parent-child container environments, requiring custom extensions to solve cross-container Bean lookup issues.

### Solution
This is precisely the fundamental reason why we copied and enhanced the SpringBootCondition class and require dependency order priority.

## Summary

The **enterprise-solution** example project is AI MCP Bridge's enterprise-level comprehensive solution, integrating all core functionalities including REST to MCP conversion, standard MCP Server development, and multi-registry support.

This example achieves seamless integration of dual registries through centralized configuration in the configuration file. It supports both automatic conversion of REST Controllers and direct development using standard \`@Tool\` annotations to create MCP tools, providing complete technical support for enterprise-level AI application integration.