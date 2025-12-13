package io.xiaozhug.demo.metadata.compile;

import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class UserService {

    private final List<User> userList = new ArrayList<>();

    // 新增用户
    public User addUser(User user) {
        user.setId((long) (userList.size() + 1));
        userList.add(user);
        return user;
    }

    // 删除用户
    public void deleteUser(Long id) {
        userList.removeIf(user -> user.getId().equals(id));
    }

    // 更新用户
    public User updateUser(User user) {
        for (int i = 0; i < userList.size(); i++) {
            if (userList.get(i).getId().equals(user.getId())) {
                userList.set(i, user);
                return user;
            }
        }
        throw new RuntimeException("用户未找到");
    }

    // 查询所有用户
    public List<User> listUsers() {
        return userList;
    }

    // 根据 ID 查询用户
    public User getUserById(Long id) {
        return userList.stream()
                .filter(user -> user.getId().equals(id))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("用户未找到"));
    }
}