package com.hisabkitab.service;

import com.hisabkitab.model.Category;
import com.hisabkitab.model.User;
import com.hisabkitab.repository.CategoryRepository;
import com.hisabkitab.repository.UserRepository;
import com.hisabkitab.util.PasswordUtil;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
public class AuthService {

    private final UserRepository users;
    private final CategoryRepository categories;

    private static final List<String> DEFAULT_CATEGORIES = List.of(
            "Food", "Travel", "Shopping", "Rent", "Utilities", "Health", "Entertainment", "Others");

    public AuthService(UserRepository users, CategoryRepository categories) {
        this.users = users;
        this.categories = categories;
    }

    @Transactional
    public User register(String username, String password) {
        username = username == null ? "" : username.trim();
        if (username.length() < 3) throw new IllegalArgumentException("Username must be at least 3 characters");
        if (password == null || password.length() < 4) throw new IllegalArgumentException("Password must be at least 4 characters");
        if (users.existsByUsername(username)) throw new IllegalArgumentException("Username already taken");

        User user = new User();
        user.setUsername(username);
        user.setPasswordHash(PasswordUtil.hash(password));
        User saved = users.save(user);

        for (String name : DEFAULT_CATEGORIES) {
            Category c = new Category();
            c.setUser(saved);
            c.setName(name);
            categories.save(c);
        }
        return saved;
    }

    public User login(String username, String password) {
        User user = users.findByUsername(username == null ? "" : username.trim())
                .orElseThrow(() -> new IllegalArgumentException("Invalid username or password"));
        if (!PasswordUtil.matches(password, user.getPasswordHash())) {
            throw new IllegalArgumentException("Invalid username or password");
        }
        return user;
    }
}
