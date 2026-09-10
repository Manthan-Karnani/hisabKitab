package com.hisabkitab.repository;

import com.hisabkitab.model.Expense;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.time.LocalDate;
import java.util.List;

public interface ExpenseRepository extends JpaRepository<Expense, Long> {

    List<Expense> findByUserIdAndExpenseDateBetweenOrderByExpenseDateDesc(Long userId, LocalDate start, LocalDate end);

    @Query("SELECT COALESCE(SUM(e.amount), 0) FROM Expense e WHERE e.user.id = :userId AND e.expenseDate BETWEEN :start AND :end")
    java.math.BigDecimal sumByUserAndMonth(@Param("userId") Long userId,
                                           @Param("start") LocalDate start,
                                           @Param("end") LocalDate end);

    @Query("SELECT e.category.name, SUM(e.amount) FROM Expense e WHERE e.user.id = :userId AND e.expenseDate BETWEEN :start AND :end GROUP BY e.category.name ORDER BY SUM(e.amount) DESC")
    List<Object[]> sumByCategory(@Param("userId") Long userId,
                                 @Param("start") LocalDate start,
                                 @Param("end") LocalDate end);
}
