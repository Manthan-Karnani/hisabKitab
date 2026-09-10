package com.hisabkitab.controller;

import com.hisabkitab.model.Category;
import com.hisabkitab.model.Expense;
import com.hisabkitab.model.User;
import com.hisabkitab.repository.CategoryRepository;
import com.hisabkitab.repository.ExpenseRepository;
import com.hisabkitab.repository.UserRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/expenses")
public class ExpenseController {

    private final ExpenseRepository expenses;
    private final UserRepository users;
    private final CategoryRepository categories;

    public ExpenseController(ExpenseRepository expenses, UserRepository users, CategoryRepository categories) {
        this.expenses = expenses;
        this.users = users;
        this.categories = categories;
    }

    @GetMapping
    public ResponseEntity<?> list(@RequestParam Long userId, @RequestParam String month) {
        if (!month.matches("\\d{4}-(0[1-9]|1[0-2])")) {
            return ResponseEntity.badRequest().body(Map.of("error", "month must be YYYY-MM"));
        }
        YearMonth ym = YearMonth.parse(month);
        LocalDate start = ym.atDay(1);
        LocalDate end = ym.atEndOfMonth();
        List<Map<String, Object>> out = expenses
                .findByUserIdAndExpenseDateBetweenOrderByExpenseDateDesc(userId, start, end)
                .stream()
                .map(e -> Map.<String, Object>of(
                        "id", e.getId(),
                        "categoryId", e.getCategory().getId(),
                        "category", e.getCategory().getName(),
                        "amount", e.getAmount(),
                        "note", e.getNote() == null ? "" : e.getNote(),
                        "date", e.getExpenseDate().toString()))
                .toList();
        return ResponseEntity.ok(out);
    }

    @PostMapping
    public ResponseEntity<?> create(@RequestBody Map<String, Object> body) {
        try {
            Long userId = Long.valueOf(String.valueOf(body.get("userId")));
            Long categoryId = Long.valueOf(String.valueOf(body.get("categoryId")));
            BigDecimal amount = new BigDecimal(String.valueOf(body.get("amount")));
            LocalDate date = LocalDate.parse(String.valueOf(body.get("date")));
            String note = String.valueOf(body.getOrDefault("note", ""));

            if (amount.signum() <= 0) return ResponseEntity.badRequest().body(Map.of("error", "Amount must be positive"));

            User user = users.findById(userId).orElse(null);
            if (user == null) return ResponseEntity.badRequest().body(Map.of("error", "Invalid user"));
            Category cat = categories.findByIdAndUserId(categoryId, userId).orElse(null);
            if (cat == null) return ResponseEntity.badRequest().body(Map.of("error", "Invalid category"));

            Expense e = new Expense();
            e.setUser(user);
            e.setCategory(cat);
            e.setAmount(amount);
            e.setExpenseDate(date);
            e.setNote(note == null || note.equals("null") ? "" : note);
            Expense saved = expenses.save(e);
            return ResponseEntity.ok(Map.of(
                    "id", saved.getId(),
                    "category", cat.getName(),
                    "amount", saved.getAmount(),
                    "date", saved.getExpenseDate().toString()));
        } catch (Exception ex) {
            return ResponseEntity.badRequest().body(Map.of("error", "Invalid expense data. Required: userId, categoryId, amount, date (YYYY-MM-DD)"));
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable Long id, @RequestParam Long userId) {
        Expense e = expenses.findById(id).orElse(null);
        if (e == null || !e.getUser().getId().equals(userId)) {
            return ResponseEntity.badRequest().body(Map.of("error", "Expense not found"));
        }
        expenses.delete(e);
        return ResponseEntity.ok(Map.of("deleted", true));
    }
}
