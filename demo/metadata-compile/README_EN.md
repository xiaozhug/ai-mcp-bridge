# metadata-compile Example Project

# [中文](README.md) | [English](README_EN.md)

## Project Introduction

**metadata-compile** is the core example project of AI MCP Bridge, demonstrating how to automatically generate MCP metadata information during compilation. This example shows how to automatically analyze Spring REST Controllers through compile-time annotation processors, generate standard MCP metadata files, and provide complete tool invocation specifications for AI clients.

## Core Features

- **Compile-time Metadata Generation**: Automatically analyzes REST Controller interfaces during project compilation to generate MCP tool metadata
- **Zero Code Intrusion**: No modification needed to existing Controller code, keeping business logic completely unchanged
- **AI-Enhanced Descriptions**: Integrates with large language models to automatically generate natural language tool descriptions
- **Intelligent Parsing**: Complete parsing of method signatures, parameter structures, HTTP request templates
- **Configuration-Driven**: Flexible configuration of generation options through compiler parameters
- **SPI Extension Mechanism**: Supports extending metadata processing logic through SPI interfaces, enabling custom metadata enhancement [Reference](metadata-apt-extension)

## Quick Start

### Environment Requirements

- JDK 8 or JDK 17
- Maven 3.3+
- Spring Boot 2.x or 3.x

### Server Configuration and Startup

1. **Configure Maven Dependencies**

Add necessary dependencies in `metadata-compile/pom.xml`:

```xml
<dependencies>
    <dependency>
        <groupId>io.github.xiaozhug</groupId>
        <artifactId>ai-mcp-bridge-apt</artifactId>
        <optional>true</optional>
    </dependency>
</dependencies>
```

2. **Configure Compiler Plugin**

Configure the Maven compiler plugin in `metadata-compile/pom.xml`:

```xml
<plugin>
    <groupId>org.apache.maven.plugins</groupId>
    <artifactId>maven-compiler-plugin</artifactId>
    <configuration>
        <compilerArgs>
            <arg>-parameters</arg>
            <arg>-Amcp.apiUrl=Your AI Service Endpoint</arg>
            <arg>-Amcp.apiKey=Your API Key</arg>
            <arg>-Amcp.model=AI Model to Use</arg>
            <arg>-Amcp.output=${project.basedir}/src/main/resources</arg>
            <arg>-Amcp.targetPackages=Your Own Package Path</arg>
            <arg>-Amcp.debug=true</arg>
        </compilerArgs>
    </configuration>
</plugin>
```

3. **Write REST Controller**

Create a standard Spring REST Controller:

```java
package io.xiaozhug.demo.metadata.compile;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

@Slf4j
@RestController
@RequestMapping(value = "/apt/user")
public class UserController {

    @Autowired
    private UserService userService;

    // Add user
    @PostMapping("/add")
    public ResultBody<User> addUser(@RequestBody User user, HttpServletRequest request) {
        User savedUser = userService.addUser(user);
        return ResultBody.success(savedUser);
    }

    // Delete user
    @DeleteMapping("/delete/{id}")
    public ResultBody<String> deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
        return ResultBody.success("User deleted successfully");
    }

    // Update user
    @PutMapping("/update")
    public ResultBody<User> updateUser(@RequestBody User user) {
        User updatedUser = userService.updateUser(user);
        return ResultBody.success(updatedUser);
    }

    // Query all users
    @GetMapping("/list")
    public ResultBody<List<User>> listUsers() {
        List<User> users = userService.listUsers();
        return ResultBody.success(users);
    }

    // Query user by ID
    @GetMapping("/get/{id}")
    public ResultBody<User> getUserById(@PathVariable Long id) {
        User user = userService.getUserById(id);
        return ResultBody.success(user);
    }
}
```

4. **Compile Project**

```bash
mvn clean compile
```

5. **View Generated Metadata**

After compilation, view the generated MCP metadata in `src/main/resources/mcp-metadata.json`.

## Configuration Parameters Explanation

| Parameter Name       | Description                                                      | Example Value                           |
|----------------------|------------------------------------------------------------------|-----------------------------------------|
| `mcp.apiUrl`         | AI service endpoint for generating natural language descriptions | Your API URL                            |
| `mcp.apiKey`         | API access key                                                   | Your API Key                            |
| `mcp.model`          | AI model to use                                                  | Your AI Model                           |
| `mcp.output`         | Metadata file output directory                                   | `${project.basedir}/src/main/resources` |
| `mcp.targetPackages` | Package path for Rest Controllers to scan                        | `io.xiaozhug.demo.metadata`             |
| `mcp.debug`          | Debug mode switch                                                | `true`                                  |

## Generated MCP Metadata Features

### 1. Incremental Update Mechanism
- **Intelligent Detection**: Only when class name (className), method name (methodName), or parameter name (paramName) changes, will the corresponding tool item be regenerated
- **Incremental Addition**: New methods will create new items in the original JSON file, existing tool items will not be deleted
- **History Retention**: Original tool definitions remain unchanged, ensuring backward compatibility (can be manually deleted)

### 2. Enable Control Mechanism
- **enabled field**: Each tool item contains an enabled field, default is true
- **Tool Visibility**: When enabled is set to false, the tool will not be exposed to large model clients
- **Flexible Management**: Supports manual editing of JSON files to control which tools are visible to AI
- **SPI Adjustment**: By implementing the `McpMetadataPostProcessor` interface, you can dynamically adjust the enabled status of tools after generation

### 3. Example Generated User Management Tool Set

| Tool Name      | HTTP Method | Path                      | Function Description     |
|----------------|-------------|---------------------------|--------------------------|
| `addUser`      | POST        | `/apt/user/add`           | Add new user             |
| `deleteUser`   | DELETE      | `/apt/user/delete/{id}`   | Delete user              |
| `updateUser`   | PUT         | `/apt/user/update`        | Update user information  |
| `listUsers`    | GET         | `/apt/user/list`          | Query all users          |
| `getUserById`  | GET         | `/apt/user/get/{id}`      | Query user details by ID |

## Function Verification

### Verification Methods

1. **Compile project**
2. Check if `src/main/resources/mcp-metadata.json` file is generated
3. Verify if the generated metadata content meets expectations

### Verification Content

- [ ] Tool method signature completeness: Check if generated MCP tools include all Controller methods
- [ ] Parameter type definition accuracy: Verify parameter names, types, required status, etc. are correct
- [ ] Return value type mapping correctness: Confirm if return value types are correctly mapped to MCP specification
- [ ] AI-enhanced descriptions: Check if includes AI-generated method descriptions, parameter explanations and other documentation information

## Common Issues and Solutions

### 1. Metadata Not Generated

**Issue**: No mcp-metadata.json file generated in the specified directory after compilation

**Solutions**:
- Confirm if Maven compiler plugin configuration is correct
- Check if `mcp.targetPackages` parameter points to the correct package path
- Verify if Controller classes and methods use correct Spring Web annotations
- Check compilation logs for related error messages
- **Special Check**: If `maven-compiler-plugin` in the project already has `<annotationProcessorPaths>` configured, ensure `ai-mcp-bridge-apt` dependency is correctly added to that section; in this case, it's completely unnecessary to configure this dependency in `<dependencies>`

### 2. Some Methods Not Generating Metadata

**Issue**: Only some Controller methods generated metadata

**Solutions**:
- Confirm if methods not generating metadata use standard Spring Web annotations
- Check if method parameters use unsupported types
- View compilation logs for related warning messages

### 3. AI Descriptions Not Generated

**Issue**: Metadata lacks AI-generated description information

**Solutions**:
- Confirm if valid `mcp.apiUrl` and `mcp.apiKey` are configured
- Check if network connection is normal
- View compilation logs for AI service call-related error messages

## Summary

**metadata-compile** example project demonstrates the compile-time MCP metadata generation function of AI MCP Bridge. Through this function, developers can automatically generate MCP-compliant metadata information for REST Controller interfaces of Spring Web projects without modifying existing code, laying the foundation for subsequent MCP service upgrade.

This example supports multiple JDK and Spring Boot versions, provides flexible configuration options, and can meet the needs of different projects. Through compile-time annotation processing, it achieves zero-code-intrusion MCP metadata generation, greatly improving development efficiency.