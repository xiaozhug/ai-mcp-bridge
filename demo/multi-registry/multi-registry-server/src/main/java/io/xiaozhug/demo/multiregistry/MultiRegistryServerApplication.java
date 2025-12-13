package io.xiaozhug.demo.multiregistry;

import io.xiaozhug.demo.metadata.compile.UserController;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackageClasses = UserController.class)
public class MultiRegistryServerApplication {

    public static void main(String[] args) {
        SpringApplication.run(MultiRegistryServerApplication.class, args);
    }

}
