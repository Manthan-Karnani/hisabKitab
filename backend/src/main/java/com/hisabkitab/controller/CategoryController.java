package com.hisabkitab.controller;

import com.hisabkitab.model.Category;
import com.hisabkitab.model.User;
import com.hisabkitab.repository.CategoryRepository;
import com.hisabkitab.repository.UserRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/categories")
public class CategoryController {

    private final CategoryRepository categories;
    private final UserRepository users;

    public CategoryController(CategoryRepository categories, UserRepository users) {
        this.categories = categories;
        this.users = users;
    }

    @GetMapping
    public ResponseEntity<?> list(@RequestParam Long userId) {
        if (!users.existsById(userId)) return ResponseEntity.badRequest().body(Map.of("error", "Invalid user"));
        List<Map<String, Object>> out = categories.findByUserIdOrderByNameAsc(userId).stream()
                .map(c -> Map.<String, Object>of("id", c.getId(), "name", c.getName()))
                .toList();
        return ResponseEntity.ok(out);
    }

    @PostMapping
    public ResponseEntity<?> create(@RequestParam Long userId, @RequestBody Map<String, String> body) {
        User user = users.findById(userId).orElse(null);
        if (user == null) return ResponseEntity.badRequest().body(Map.of("error", "Invalid user"));
        String name = body.getOrDefault("name", "").trim();
        if (name.isEmpty()) return ResponseEntity.badRequest().body(Map.of("error", "Category name required"));
        if (categories.existsByUserIdAndNameIgnoreCase(userId, name)) {
            return ResponseEntity.badRequest().body(Map.of("error", "Category already exists"));
        }
        Category c = new Category();
        c.setUser(user);
        c.setName(name);
        Category saved = categories.save(c);
        return ResponseEntity.ok(Map.of("id", saved.getId(), "name", saved.getName()));
    }
}
