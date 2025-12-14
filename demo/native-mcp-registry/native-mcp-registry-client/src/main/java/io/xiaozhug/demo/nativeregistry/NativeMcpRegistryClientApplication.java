package io.xiaozhug.demo.nativeregistry;

import io.xiaozhug.ai.mcp.client.tool.autoconfigure.discovery.transport.creator.RequestHeaderContextHolder;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

@SpringBootApplication
public class NativeMcpRegistryClientApplication {

    public static void main(String[] args) {
        ConfigurableApplicationContext applicationContext = SpringApplication.run(NativeMcpRegistryClientApplication.class, args);
        System.out.println("=== AI助手控制台 ===");
        System.out.println("输入 'exit' 或 'quit' 退出程序");
        System.out.println("输入 'help' 查看命令帮助");
        System.out.println("====================");

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

    @Bean
    public ChatClient syncMcpToolCallbackProvider(ChatClient.Builder chatClientBuilder, ObjectProvider<ToolCallbackProvider> httpToolCallbackProviderProvider) {
        ToolCallbackProvider[] toolCallbackProviders = httpToolCallbackProviderProvider.stream().toList().toArray(new ToolCallbackProvider[0]);
        chatClientBuilder.defaultToolCallbacks(toolCallbackProviders);
        return chatClientBuilder.build();
    }

}
