package io.xiaozhug.demo.metadata.compile;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

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