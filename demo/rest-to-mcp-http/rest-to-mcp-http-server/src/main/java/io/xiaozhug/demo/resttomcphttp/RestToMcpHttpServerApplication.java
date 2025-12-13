package io.xiaozhug.demo.resttomcphttp;

import io.xiaozhug.demo.metadata.compile.UserController;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackageClasses = UserController.class)
public class RestToMcpHttpServerApplication {

    public static void main(String[] args) {
        SpringApplication.run(RestToMcpHttpServerApplication.class, args);
    }

}
