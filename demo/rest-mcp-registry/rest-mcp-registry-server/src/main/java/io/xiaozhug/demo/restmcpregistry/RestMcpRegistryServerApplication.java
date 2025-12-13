package io.xiaozhug.demo.restmcpregistry;

import io.xiaozhug.demo.metadata.compile.UserController;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackageClasses = UserController.class)
public class RestMcpRegistryServerApplication {

    public static void main(String[] args) {
        SpringApplication.run(RestMcpRegistryServerApplication.class, args);
    }

}
