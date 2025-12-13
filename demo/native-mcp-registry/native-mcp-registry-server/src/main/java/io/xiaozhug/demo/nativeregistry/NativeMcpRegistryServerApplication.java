package io.xiaozhug.demo.nativeregistry;

import io.xiaozhug.demo.nativeregistry.mcpserver.service.MianshiyaService;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.ai.tool.method.MethodToolCallbackProvider;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class NativeMcpRegistryServerApplication {

    public static void main(String[] args) {
        SpringApplication.run(NativeMcpRegistryServerApplication.class, args);
    }

    @Bean
    public ToolCallbackProvider serverTools(MianshiyaService mianshiyaService) {
        return MethodToolCallbackProvider.builder().toolObjects(mianshiyaService).build();
    }
}
