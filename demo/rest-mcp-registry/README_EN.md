# REST MCP Registry Example

# [中文](README.md) | [English](README_EN.md)

## Project Introduction

This is a complete example project demonstrating how to automatically register REST-converted MCP interfaces to a **service registry**. The project implements automatic service registration of MCP tools through `ai-mcp-bridge-spring-boot-mcp-registry-starter`, providing standard service governance capabilities for MCP tools.

## Core Features

### 🚀 Automatic Service Registration
- **Zero-Configuration Registration**: No additional configuration needed, automatically completes service registration for MCP tools
- **Multi-Registry Support**: Supports mainstream registries like Eureka (default in this example), Nacos, Consul, Zookeeper, controlled via enabled switches
- **Standard Protocol**: Fully compatible with Spring Cloud service registration standards

### 📋 Complete Service Governance
- **Service Discovery**: MCP clients automatically discover available tool services
- **Health Check**: Integrated health check mechanism supporting automatic fault isolation
- **Metadata Publishing**: Publishes complete MCP tool information in service instances

### 🛠️ Out-of-the-box
- **Single Registry**: Business services and MCP tools register to the same registry
- **Unified Governance**: Unified monitoring, management, and maintenance
- **Simplified Deployment**: Reduces operational complexity for rapid deployment

### Core Components
- **spring-mcp-bridge-registry-starter**: MCP service registration starter (core dependency)
- **spring-mcp-bridge-metadata-file-expose-starter**: Metadata exposure support (core dependency)
- **metadata-compile**: Metadata generation module (shared by demo projects)
- **spring-mcp-bridge-client-starter**: MCP client support (client core dependency)
- **Registry Client**: Nacos/Eureka/Consul/Zookeeper (choose one)

## Environment Requirements

### Server Requirements
- JDK 8 or JDK 17
- Maven 3.3+
- Spring Boot 2.x or 3.x
- Service Registry (This example supports Eureka, Nacos, Consul, Zookeeper)

### Client Requirements
- JDK 17+
- Maven 3.3+
- Spring Boot 3.x
- Service Registry (Must match server configuration)

## Dependencies Description

### 📦 [metadata-compile](..%2Fmetadata-compile) (Shared by demo projects)

**Important Note**: This dependency is only used for code sharing between demo projects. In actual projects, you do not need to depend on this module.

- **Demonstration Purpose**: To avoid duplicating the same UserController code across multiple demo projects
- **Actual Usage**: In your own projects, you can directly configure the MCP annotation processor to generate metadata for your own REST Controllers
- **Generation Principle**: Automatically generates metadata by analyzing your REST Controllers through the Maven compiler plugin

### Server Dependencies
```xml
<dependencies>
    <dependency>
        <groupId>io.github.xiaozhug</groupId>
        <artifactId>metadata-compile-jdk8</artifactId>
    </dependency>

    <!-- 🔌 spring-mcp-bridge-registry-starter (Core Dependency) -->
    <!-- This is the core registry dependency provided by AI MCP Bridge project, must be included -->
    <dependency>
        <groupId>io.github.xiaozhug</groupId>
        <artifactId>ai-mcp-bridge-spring-boot-mcp-registry-starter</artifactId>
    </dependency>

    <!-- 🔧 spring-mcp-bridge-metadata-file-expose-starter (Core Dependency) -->
    <!-- Used for automatic exposure of MCP metadata, key to implementing service registration -->
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

### Client Dependencies
```xml
<dependencies>
<!-- 🔌 ai-mcp-bridge-spring-boot-mcp-client-starter (Core Dependency) -->
<!-- This is the core client dependency provided by AI MCP Bridge project, must be included -->
    <dependency>
        <groupId>io.github.xiaozhug</groupId>
        <artifactId>ai-mcp-bridge-spring-boot-mcp-client-starter</artifactId>
    </dependency>

    <!-- 🔧 ai-mcp-bridge-spring-boot-mcp-registry-starter (Core Dependency) -->
    <!-- Used to support discovering MCP services from the registry -->
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

## Quick Start

### Server Configuration

1. **Configure application.yaml**

Add configuration in the server's `application.yaml`:

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

### Client Configuration and Usage

1. **Configure application.yaml**

Add core configuration in the client's `application.yaml`:

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
    # spring-mcp-bridge-client-starter discovery method (core configuration)
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

2. **Use Registry-Discovered MCP Client [RestMcpRegistryClientApplication.java](rest-mcp-registry-client%2Fsrc%2Fmain%2Fjava%2Fio%2Fxiaozhug%2Fdemo%2Frestmcpregistry%2FRestMcpRegistryClientApplication.java)**

## Automatic Registration Mechanism

### 🔄 Registration Process

1. **Metadata Generation**: Generate MCP tool metadata during compilation
2. **Service Startup**: Automatically detect registry when application starts
3. **Tool Registration**: Register MCP tool information to service instance metadata

## Verification Results

### ✅ Service Registration Verification

After successful startup, you can see in the Eureka console:

- **Service Name**: REST-MCP-REGISTRY-SERVER
- **Instance Status**: UP
- **Metadata contains MCP-related information**

### ✅ MCP Tool Metadata Verification

You can view detailed service instance metadata by accessing `http://[eureka-server-address]:[port]/eureka/apps`:

**Key Metadata Fields**:

```xml
<mcp-file-0>eyJ0eXBlIjoiTUNQX0ZJTEUiLCJzZXJ2aWNlTmFtZSI6bnVsbCwiaW5zdGFuY2VJZCI6bnVsbCwidXJsIjpudWxsLCJ0b29scyI6bnVsbCwiaXRlbXMiOlt7... (Complete Base64-encoded MCP metadata)</mcp-file-0>
<mcp-file-size>1</mcp-file-size>
```

This metadata includes:

- Complete MCP tool definitions (Base64 encoded)
- All tool method descriptions and parameter definitions
- Input parameter JSON Schemas
- HTTP request template information

## Summary

**REST MCP Registry** example demonstrates how to seamlessly upgrade traditional REST APIs to MCP tools with service governance capabilities. Through the automated service registration mechanism, developers can:

### 🚀 Rapid Integration
- Obtain complete service registration capability by **introducing starter dependencies**
- **Zero-configuration startup**, automatically completing MCP tool registration and discovery
- **Standardized dependency management**, perfectly integrated with Spring Boot ecosystem

### 📋 Standard Governance
- **Based on Spring Cloud standards**, perfectly integrated with existing microservice systems
- **Compatible with mainstream registries**, maintaining consistency in technology stack
- **Compliant with enterprise architecture standards**, reducing learning and migration costs

### 🔧 Flexible Deployment
- **Supports multiple registries** (Eureka, Nacos, Consul, Zookeeper), adapting to different technical architectures
- **Cloud-native friendly**, supporting containerization and dynamic scaling

---

### 🎯 Core Value

This example provides enterprises with a **smooth upgrade path** during AI transformation, allowing existing REST APIs to become important components of the AI ecosystem **without any code modifications**, achieving **deep integration** between traditional applications and AI capabilities.