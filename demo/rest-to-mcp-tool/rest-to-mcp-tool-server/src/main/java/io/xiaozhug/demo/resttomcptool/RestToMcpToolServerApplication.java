package io.xiaozhug.demo.resttomcptool;

import io.xiaozhug.demo.metadata.compile.UserController;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackageClasses = UserController.class)
public class RestToMcpToolServerApplication {

    public static void main(String[] args) {
        SpringApplication.run(RestToMcpToolServerApplication.class, args);
    }

}
