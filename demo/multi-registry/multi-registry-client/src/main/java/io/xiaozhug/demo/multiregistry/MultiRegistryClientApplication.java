package io.xiaozhug.demo.multiregistry;

import io.xiaozhug.ai.mcp.client.tool.autoconfigure.discovery.transport.creator.RequestHeaderContextHolder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Map;
import java.util.Set;

@Slf4j
@SpringBootApplication
public class MultiRegistryClientApplication {

    public static void main(String[] args) {
        ConfigurableApplicationContext applicationContext = SpringApplication.run(MultiRegistryClientApplication.class, args);
        System.out.println("=== AI助手控制台 ===");
        System.out.println("输入 'exit' 或 'quit' 退出程序");
        System.out.println("输入 'help' 查看命令帮助");
        System.out.println("====================");

        DiscoveryClient discoveryClient = applicationContext.getBean(DiscoveryClient.class);
        discoveryClient.getServices().forEach(serviceId -> {
            discoveryClient.getInstances(serviceId).forEach(instance -> {
                Map<String, String> metadata = instance.getMetadata();
                Set<String> key = metadata.keySet();
                log.info("发现服务 - 服务ID: {}, 地址: {}, 端口: {}, 元数据: {}", serviceId, instance.getHost(), instance.getPort(), key);
            });
        });

        ChatClient chatClient = applicationContext.getBean(ChatClient.class);

        // 控制台交互
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(System.in))) {
            String input;
            while (true) {
                System.out.print("\n请输入指令: ");
                input = reader.readLine();

                if (input == null || input.trim().isEmpty()) {
                    continue;
                }

                input = input.trim();

                // 退出命令
                if (input.equalsIgnoreCase("exit") || input.equalsIgnoreCase("quit")) {
                    System.out.println("程序已退出");
                    break;
                }

                // 帮助命令
                if (input.equalsIgnoreCase("help")) {
                    printHelp();
                    continue;
                }

                // 处理用户输入
                processCommand(input, chatClient);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void processCommand(String command, ChatClient chatClient) {
        try {
            System.out.println("正在处理指令...");


            MultiValueMap<String, String> headers = new LinkedMultiValueMap<>();
            headers.add("Authorization", "token-xxxxxx");
            RequestHeaderContextHolder.setHeaders(headers);

            String content = chatClient.prompt(command).call().content();
            System.out.println("\n=== AI响应 ===");
            System.out.println(content);
            System.out.println("==============");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void printHelp() {
        System.out.println("\n=== 命令帮助 ===");
        System.out.println("添加用户 [用户名] - 添加新用户并生成随机密码");
        System.out.println("生成密码 - 生成一个强密码");
        System.out.println("查询用户 [条件] - 生成查询用户的SQL语句");
        System.out.println("exit/quit - 退出程序");
        System.out.println("help - 显示帮助信息");
        System.out.println("其他任意输入将直接发送给AI");
        System.out.println("================");
    }

    @Bean
    public ChatClient syncMcpToolCallbackProvider(ChatClient.Builder chatClientBuilder, ObjectProvider<ToolCallbackProvider> httpToolCallbackProviderProvider) {
        ToolCallbackProvider[] toolCallbackProviders = httpToolCallbackProviderProvider.stream().toList().toArray(new ToolCallbackProvider[0]);
        chatClientBuilder.defaultToolCallbacks(toolCallbackProviders);
        return chatClientBuilder.build();
    }

}
