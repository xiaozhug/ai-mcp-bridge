package io.xiaozhug.demo.metadata.compile;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class User {

    private Long id;
    private String userName;
    private String password;
    private LocalDateTime createTime;
    private LocalDateTime createTime2;
}
