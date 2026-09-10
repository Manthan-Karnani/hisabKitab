package com.hisabkitab.controller;

import com.hisabkitab.repository.BudgetRepository;
import com.hisabkitab.repository.ExpenseRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/summary")
public class SummaryController {

    private final ExpenseRepository expenses;
    private final BudgetRepository budgets;

    public SummaryController(ExpenseRepository expenses, BudgetRepository budgets) {
        this.expenses = expenses;
        this.budgets = budgets;
    }

    @GetMapping
    public ResponseEntity<?> summary(@RequestParam Long userId, @RequestParam String month) {
        if (!month.matches("\\d{4}-(0[1-9]|1[0-2])")) {
            return ResponseEntity.badRequest().body(Map.of("error", "month must be YYYY-MM"));
        }
        YearMonth ym = YearMonth.parse(month);
        LocalDate start = ym.atDay(1);
        LocalDate end = ym.atEndOfMonth();

        BigDecimal budget = budgets.findByUserIdAndMonth(userId, month)
                .map(com.hisabkitab.model.Budget::getAmount)
                .orElse(BigDecimal.ZERO);
        BigDecimal spent = expenses.sumByUserAndMonth(userId, start, end);
        if (spent == null) spent = BigDecimal.ZERO;
        BigDecimal remaining = budget.subtract(spent);

        List<Map<String, Object>> byCategory = expenses.sumByCategory(userId, start, end).stream()
                .map(row -> Map.<String, Object>of(
                        "category", (String) row[0],
                        "total", (BigDecimal) row[1]))
                .toList();

        return ResponseEntity.ok(Map.of(
                "month", month,
                "totalBudget", budget,
                "totalSpent", spent,
                "remaining", remaining,
                "byCategory", byCategory));
    }
}
