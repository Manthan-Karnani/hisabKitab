package com.hisabkitab.controller;

import com.hisabkitab.model.Budget;
import com.hisabkitab.model.User;
import com.hisabkitab.repository.BudgetRepository;
import com.hisabkitab.repository.UserRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.math.BigDecimal;
import java.util.Map;

@RestController
@RequestMapping("/api/budgets")
public class BudgetController {

    private final BudgetRepository budgets;
    private final UserRepository users;

    public BudgetController(BudgetRepository budgets, UserRepository users) {
        this.budgets = budgets;
        this.users = users;
    }

    private static boolean validMonth(String m) {
        return m != null && m.matches("\\d{4}-(0[1-9]|1[0-2])");
    }

    @GetMapping
    public ResponseEntity<?> get(@RequestParam Long userId, @RequestParam String month) {
        if (!validMonth(month)) return ResponseEntity.badRequest().body(Map.of("error", "month must be YYYY-MM"));
        return ResponseEntity.ok(budgets.findByUserIdAndMonth(userId, month)
                .map(b -> Map.of("id", b.getId(), "month", b.getMonth(), "amount", b.getAmount()))
                .orElse(Map.of("month", month, "amount", BigDecimal.ZERO)));
    }

    @PostMapping
    public ResponseEntity<?> set(@RequestBody Map<String, Object> body) {
        Long userId = Long.valueOf(String.valueOf(body.get("userId")));
        String month = String.valueOf(body.getOrDefault("month", ""));
        if (!validMonth(month)) return ResponseEntity.badRequest().body(Map.of("error", "month must be YYYY-MM"));
        BigDecimal amount;
        try {
            amount = new BigDecimal(String.valueOf(body.get("amount")));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", "Invalid amount"));
        }
        if (amount.signum() <= 0) return ResponseEntity.badRequest().body(Map.of("error", "Amount must be positive"));

        User user = users.findById(userId).orElse(null);
        if (user == null) return ResponseEntity.badRequest().body(Map.of("error", "Invalid user"));

        Budget b = budgets.findByUserIdAndMonth(userId, month).orElseGet(Budget::new);
        b.setUser(user);
        b.setMonth(month);
        b.setAmount(amount);
        Budget saved = budgets.save(b);
        return ResponseEntity.ok(Map.of("id", saved.getId(), "month", saved.getMonth(), "amount", saved.getAmount()));
    }
}
