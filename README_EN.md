# AI MCP Bridge 🌉

# [中文](README.md) | [English](README_EN.md)

## Project Introduction

AI MCP Bridge is a revolutionary MCP service governance platform, providing a **one-stop Spring application MCP-ification solution**. It not only seamlessly upgrades REST Controller interfaces in Spring Web projects into MCP services but also provides a **complete MCP Tool registration and discovery ecosystem**, enabling traditional applications to rapidly gain AI tool invocation capabilities.

> 🎯 **Core Value**: Zero-code refactoring, instantly empowering your Spring application to embrace the AI era!

## Core Features

### 📄 Compile-time MCP Metadata Generation

- **Early Generation**: Automatically analyzes REST Controller interfaces of Spring Web projects at compile time to generate corresponding MCP metadata.
- **Tool Definition**: Automatically generates MCP tool method signatures, parameter descriptions, return value types, and other metadata.
- **Call Specification**: Provides AI clients with standard tool invocation interface descriptions, ensuring accuracy and reliability.
- **Performance Optimization**: Reduces runtime reflection overhead for better runtime performance.

### 🛠️ Seamless Upgrade of REST Controllers to MCP Interfaces

- **Multi-Version JDK Support**
  - 🟢 JDK 1.8: Converts REST interfaces of Spring Web projects based on JDK 1.8 into MCP call interfaces.
  - 🟢 JDK 17: Converts REST interfaces of Spring Web projects based on JDK 17 into MCP call interfaces.

- **Full Lifecycle Support**
  - Fully follows the original Spring Web project's invocation lifecycle.
  - Supports interceptors, filters, etc.

- **Native Spring Web Parameter Handling**
  - **Automatic Parameter Binding**: Complete support for Spring MVC parameter resolution mechanisms.
  - **Annotation Support**: Full compatibility with annotations like @RequestParam, @PathVariable, @RequestBody.
  - **Type Conversion**: Automatic application of Spring type converters.
  - **Data Binding**: Seamless inheritance of data binding and validation mechanisms.
  - **Example**: Supports automatic handling of parameters like HttpServletRequest and other native Web objects.

- **Security System Compatibility**
  - Fully compatible with the original Spring Web project's authentication and authorization system.
  - Supports custom security interceptors and permission checks.
  - MCP calls share the same security configuration as the original REST interfaces.

- **Protocol Compatibility**
  - Based on HTTP protocol.
  - Compliant with MCP client call specifications.

### 🤖 Deep Adaptation for Spring AI MCP Server Tool (JDK 17+ Only)

- **Environment Requirement**: Specifically designed for Spring Web projects on JDK 17 and above.
- **Protocol Conversion**: Converts REST Controller interfaces in Spring Web projects into MCP Server Tools.
- **MCP Native Protocol**: MCP Clients can make native calls using the MCP protocol directly.
- **Ecosystem Optimization**: Optimized for the Spring AI ecosystem, supporting direct integration with Spring AI MCP Clients, while also compatible with multi-language MCP Clients.
- **Version Adaptation**: Perfectly supports modern tech stack combinations like Spring Boot 3.x and JDK 17+.

### 🔍 Dynamic MCP Tool Registration & Discovery

- **Automatic Registration**
  - Supports automatic registration of MCP call interfaces exposed by REST Controllers.
  - Supports automatic registration of Spring AI MCP Server Tools.

- **Dynamic Discovery**
  - MCP clients automatically discover available Tools.
  - Supports real-time updates.
  - Can discover MCP interfaces upgraded from REST Controllers.
  - Can discover dedicated Spring AI MCP Server Tools.

- **Load Balancing**
  - Load balancing in multi-instance environments.
  - Load balancing for MCP interfaces converted from REST Controllers.
  - Load balancing for Spring AI MCP Tools.

### 🏗️ Multi-Registry Support

- **Flexible Deployment**
  - **Single Registry**: MCP Tools and business services share the same registry.
  - **Dual-Registry Architecture**: MCP Tools can register to an independent registry, completely isolated from the original business registry.

- **Service Isolation**: Achieves traffic separation and governance isolation between business services and MCP Tools.
- **Client Dual-Source**: MCP clients can pull services from different registries simultaneously.

- **Flexible Combinations**
  - Supports Cartesian product combinations of Eureka, Nacos, Consul, Zookeeper.
  - **Combination Example**: Business services register to Nacos, MCP Tools register to Eureka.
  - **Dual Centers of Same Type**: Supports using two registries of the same type.
  - **Single Center Example**: All services unified in a single Nacos or Eureka cluster.

### 🔌 Intelligent MCP Client Call Encapsulation

- **Registry Discovery & Call**
  - **Automatic Service Discovery**: Dynamically pulls MCP service metadata from the registry.
  - **Protocol Adaptation**: Automatically selects HTTP protocol or MCP protocol for calls based on server exposure type.
  - **HTTP Protocol Compatibility**: Intelligent MCP client supports MCP-compatible calls via HTTP protocol.
  - **Load Balancing**: Supports failover in multi-instance environments.

- **Direct Controller Call**
  - **Automatic Metadata Acquisition**: Directly connects to the Controller interface exposed by the server to obtain MCP metadata.
  - **HTTP Protocol Call**: Uses HTTP protocol at the underlying layer for MCP calls, offering better compatibility.
  - **Zero-Configuration Access**: No extra configuration needed, connect and use directly.

- **MCP Server Direct Call**
  - **Standard MCP Client**: Directly connects to the MCP Server converted via the Controller interface.
  - **HTTP Transport Layer**: Uses HTTP protocol for data transmission at the underlying layer, balancing standardization and performance.
  - **Tool Auto-Discovery**: Automatically obtains server tool information and method signatures via the MCP Client.

- **Unified Call Interface**
  - **Multi-Protocol Unification**: Encapsulates differences between call methods, providing a unified call interface.
  - **Unified Exception Handling**: Unified error handling and retry mechanisms.

## Core Advantages

### 🚀 Zero-Refactoring Upgrade
- No code modification needed for existing REST Controllers.
- Keeps original business logic completely unchanged.
- Supports progressive migration, reducing upgrade risk.

### 🌐 Enterprise-Grade Service Governance
- Parallel support for multiple registries.
- Service discovery and load balancing.

### 🔌 Flexible Call Methods
- Supports multiple client call modes to adapt to different scenarios.
- Protocol adaptation maximizes compatibility and performance.
- Unified call interface simplifies client integration complexity.

## Example Projects

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

We provide complete example projects for each core feature, each with detailed independent documentation:

| Example Project         | Feature Description                                                                 | Tech Stack                                                                 | Core Characteristics                                                       | Documentation Location                                 |
|-------------------------|-------------------------------------------------------------------------------------|----------------------------------------------------------------------------|----------------------------------------------------------------------------|--------------------------------------------------------|
| **metadata-compile**    | Compile-time Metadata Generation                                                    | (Spring Boot 2.x + JDK 8)/(Spring Boot 3.x + JDK 17)                       | Automatically generates MCP Tools metadata and call specs at compile stage | [View Docs](demo%2Fmetadata-compile%2FREADME_EN.md)    |
| **rest-to-mcp-http**    | Controller MCP Upgrade                                                              | (Spring Boot 2.x + JDK 8)/(Spring Boot 3.x + JDK 17)                       | REST → MCP Protocol Conversion                                             | [View Docs](demo%2Frest-to-mcp-http%2FREADME_EN.md)    |
| **rest-to-mcp-tool**    | REST to Spring AI MCP Tool Conversion                                               | Spring Boot 3.x + JDK 17                                                   | Seamless adaptation of REST Controller to Spring AI MCP Tool               | [View Docs](demo%2Frest-to-mcp-tool%2FREADME_EN.md)    |
| **rest-mcp-registry**   | REST Converted MCP Interface Registration & Discovery                               | (Spring Boot 2.x + JDK 8)/(Spring Boot 3.x + JDK 17) + Registry Support    | Service governance for REST upgraded interfaces                            | [View Docs](demo%2Frest-mcp-registry%2FREADME_EN.md)   |
| **native-mcp-registry** | Native MCP Tool Registration & Discovery                                            | Spring Boot 3.x + JDK 17 + Registry Support                                | Service governance for native MCP Tools                                    | [View Docs](demo%2Fnative-mcp-registry%2FREADME_EN.md) |
| **multi-registry**      | Multi-Registry Architecture                                                         | (Spring Boot 2.x + JDK 8)/(Spring Boot 3.x + JDK 17) + Dual Registry Combo | Isolation between business and MCP Tools                                   | [View Docs](demo%2Fmulti-registry%2FREADME_EN.md)      |
| **enterprise-solution** | Enterprise Complete Solution (rest-to-mcp-http + rest-to-mcp-tool + multi-registry) | Spring Boot 3.x + JDK 17                                                   | End-to-end solution                                                        | [View Docs](demo%2Fenterprise-solution%2FREADME_EN.md) |

Detailed configuration and usage instructions please refer to the documentation of each example project.

---

<div align="center">

## 🚀 Get Started Now, Empower Your Spring Application for the AI Era!

**⭐ If this project is helpful to you, please give it a Star!**

</div>