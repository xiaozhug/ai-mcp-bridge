# metadata-compile 示例工程

# [中文](README.md) | [English](README_EN.md)

## 项目简介

**metadata-compile** 是 AI MCP Bridge 的核心示例工程，展示了如何在编译时自动生成 MCP 元数据信息。该示例演示了如何通过编译期注解处理器自动分析 Spring REST Controller，生成标准的 MCP 元数据文件，为 AI 客户端提供完整的工具调用规范。

## 核心功能

- **编译时元数据生成**：在项目编译阶段自动分析 REST Controller 接口，生成 MCP 工具元数据
- **零代码侵入**：无需修改现有 Controller 代码，保持业务逻辑完全不变
- **AI 增强描述**：集成大模型自动生成自然语言工具描述
- **智能解析**：完整解析方法签名、参数结构、HTTP 请求模板
- **配置驱动**：通过编译器参数灵活配置生成选项
- **SPI 扩展机制**：支持通过 SPI 接口扩展元数据处理逻辑，实现自定义的元数据增强 [参考](metadata-apt-extension)



## 快速开始

### 环境要求

- JDK 8 或 JDK 17
- Maven 3.3+
- Spring Boot 2.x 或 3.x

### 服务端配置与启动

1. **配置 Maven 依赖**

在 `metadata-compile/pom.xml` 中添加必要依赖：

```xml
<dependencies>
    <dependency>
        <groupId>io.github.xiaozhug</groupId>
        <artifactId>ai-mcp-bridge-apt</artifactId>
        <optional>true</optional>
    </dependency>
</dependencies>
```

2. **配置编译器插件**

在 `metadata-compile/pom.xml` 中配置 Maven 编译器插件：

```xml
<plugin>
    <groupId>org.apache.maven.plugins</groupId>
    <artifactId>maven-compiler-plugin</artifactId>
    <configuration>
        <compilerArgs>
            <arg>-parameters</arg>
            <arg>-Amcp.apiUrl=您的AI服务端点</arg>
            <arg>-Amcp.apiKey=您的API密钥</arg>
            <arg>-Amcp.model=使用的AI模型</arg>
            <arg>-Amcp.output=${project.basedir}/src/main/resources</arg>
            <arg>-Amcp.targetPackages=您自己的包路径</arg>
            <arg>-Amcp.debug=true</arg>
        </compilerArgs>
    </configuration>
</plugin>
```

3. **编写 REST Controller**

创建标准的 Spring REST Controller：

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

    // 新增用户
    @PostMapping("/add")
    public ResultBody<User> addUser(@RequestBody User user, HttpServletRequest request) {
        User savedUser = userService.addUser(user);
        return ResultBody.success(savedUser);
    }

    // 删除用户
    @DeleteMapping("/delete/{id}")
    public ResultBody<String> deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
        return ResultBody.success("用户删除成功");
    }

    // 更新用户
    @PutMapping("/update")
    public ResultBody<User> updateUser(@RequestBody User user) {
        User updatedUser = userService.updateUser(user);
        return ResultBody.success(updatedUser);
    }

    // 查询所有用户
    @GetMapping("/list")
    public ResultBody<List<User>> listUsers() {
        List<User> users = userService.listUsers();
        return ResultBody.success(users);
    }

    // 根据 ID 查询用户
    @GetMapping("/get/{id}")
    public ResultBody<User> getUserById(@PathVariable Long id) {
        User user = userService.getUserById(id);
        return ResultBody.success(user);
    }
}
```

4. **编译项目**

```bash
mvn clean compile
```

5. **查看生成的元数据**

编译完成后，在 `src/main/resources/mcp-metadata.json` 查看生成的 MCP 元数据。

## 配置参数说明

| 参数名                  | 说明                       | 示例值                                     |
|----------------------|--------------------------|-----------------------------------------|
| `mcp.apiUrl`         | AI 服务端点，用于生成自然语言描述       | 您的 API URL                              |
| `mcp.apiKey`         | API 访问密钥                 | 您的 API Key                              |
| `mcp.model`          | 使用的 AI 模型                | 您的 AI 模型                                |
| `mcp.output`         | 元数据文件输出目录                | `${project.basedir}/src/main/resources` |
| `mcp.targetPackages` | 要扫描的Rest Controller所在包路径 | `io.xiaozhug.demo.metadata`            |
| `mcp.debug`          | 调试模式开关                   | `true`                                  |

## 生成的 MCP 元信息特性

### 1. 增量更新机制
- **智能检测**：只有当类名(className)、方法名(methodName)或参数名(paramName)发生变化时，才会重新生成对应的工具项
- **增量添加**：新增的方法会在原 JSON 文件中创建新的 item，不会删除已存在的工具项
- **历史保留**：原有的工具定义会保持不变，确保向后兼容（可手动删除）

### 2. 启用控制机制
- **enabled 字段**：每个工具项都包含 enabled 字段，默认为 true
- **工具可见性**：当 enabled 设置为 false 时，该工具不会暴露给大模型客户端
- **灵活管理**：支持手动编辑 JSON 文件来控制哪些工具对 AI 可见
- **SPI调整**： 通过实现 `McpMetadataPostProcessor` 接口，可以在生成后动态调整工具的 enabled 状态

### 3. 生成的用户管理工具集示例

| 工具名称          | HTTP 方法 | 路径                      | 功能描述       |
|---------------|---------|-------------------------|------------|
| `addUser`     | POST    | `/apt/user/add`         | 新增用户       |
| `deleteUser`  | DELETE  | `/apt/user/delete/{id}` | 删除用户       |
| `updateUser`  | PUT     | `/apt/user/update`      | 更新用户信息     |
| `listUsers`   | GET     | `/apt/user/list`        | 查询所有用户     |
| `getUserById` | GET     | `/apt/user/get/{id}`    | 根据ID查询用户详情 |

## 功能验证

### 验证方式

1. **编译项目**
2. 检查 `src/main/resources/mcp-metadata.json` 文件是否生成
3. 查看生成的元数据内容是否符合预期

### 验证内容

- [ ] 工具方法签名完整性：检查生成的 MCP 工具是否包含了所有 Controller 方法
- [ ] 参数类型定义准确性：验证参数名称、类型、是否必需等信息是否正确
- [ ] 返回值类型映射正确性：确认返回值类型是否正确映射到 MCP 规范
- [ ] AI 增强描述：检查是否包含了 AI 生成的方法描述、参数说明等文档信息

## 常见问题与解决方案

### 1. 元数据未生成

**问题**：编译后未在指定目录生成 mcp-metadata.json 文件

**解决方案**：
- 确认 Maven 编译器插件配置是否正确
- 检查 `mcp.targetPackages` 参数是否指向正确的包路径
- 确认 Controller 类和方法使用了正确的 Spring Web 注解
- 查看编译日志是否有相关错误信息
- **特别检查**：如果项目中 `maven-compiler-plugin` 已配置 `<annotationProcessorPaths>`，确保 `ai-mcp-bridge-apt` 依赖已正确添加到该部分，此时完全不需要在 `<dependencies>` 中配置该依赖

### 2. 部分方法未生成元数据

**问题**：只有部分 Controller 方法生成了元数据

**解决方案**：
- 确认未生成元数据的方法是否使用了标准的 Spring Web 注解
- 检查方法参数是否使用了不支持的类型
- 查看编译日志是否有相关警告信息

### 3. AI 描述未生成

**问题**：元数据中缺少 AI 生成的描述信息

**解决方案**：
- 确认已配置有效的 `mcp.apiUrl` 和 `mcp.apiKey`
- 检查网络连接是否正常
- 查看编译日志是否有 AI 服务调用相关的错误信息

## 总结

**metadata-compile** 示例工程展示了 AI MCP Bridge 的编译时 MCP 元数据生成功能。通过该功能，开发者可以在不修改现有代码的情况下，为 Spring Web 项目的 REST Controller 接口自动生成符合 MCP 规范的元数据信息，为后续的 MCP 服务化升级奠定基础。

该示例支持多种 JDK 和 Spring Boot 版本，提供了灵活的配置选项，可以满足不同项目的需求。通过编译时注解处理，实现了零代码侵入的 MCP 元数据生成，大大提升了开发效率。