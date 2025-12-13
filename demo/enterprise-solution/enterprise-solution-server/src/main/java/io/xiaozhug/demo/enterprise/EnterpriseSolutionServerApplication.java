package io.xiaozhug.demo.enterprise;

import io.xiaozhug.demo.enterprise.mcpserver.service.MianshiyaService;
import io.xiaozhug.demo.metadata.compile.UserController;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.ai.tool.method.MethodToolCallbackProvider;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication(scanBasePackageClasses = {UserController.class, EnterpriseSolutionServerApplication.class})
public class EnterpriseSolutionServerApplication {

    public static void main(String[] args) {
        SpringApplication.run(EnterpriseSolutionServerApplication.class, args);
    }

    @Bean
    public ToolCallbackProvider serverTools(MianshiyaService mianshiyaService) {
        return MethodToolCallbackProvider.builder().toolObjects(mianshiyaService).build();
    }
}
