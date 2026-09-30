package com.expensetracker.expense_tracker_api;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/expenses")
public class ExpenseController {

    @Autowired
    private ExpenseRepository expenseRepository;

    @Autowired
    private UserRepository userRepository;

    private User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();
        return userRepository.findByEmail(email).orElseThrow();
    }

    @GetMapping
    public List<Expense> getAllExpenses() {
        User currentUser = getCurrentUser();
        return expenseRepository.findByUser(currentUser);
    }

    @PostMapping
    public Expense createExpense(@RequestBody Expense expense) {
        User currentUser = getCurrentUser();
        expense.setUser(currentUser);
        return expenseRepository.save(expense);
    }

    @GetMapping("/{id}")
    public Expense getExpenseById(@PathVariable Long id) {
        Expense expense = expenseRepository.findById(id).orElseThrow();
        validateOwnership(expense);
        return expense;
    }

    @PutMapping("/{id}")
    public Expense updateExpense(@PathVariable Long id, @RequestBody Expense updatedExpense) {
        Expense expense = expenseRepository.findById(id).orElseThrow();
        validateOwnership(expense);

        expense.setAmount(updatedExpense.getAmount());
        expense.setDescription(updatedExpense.getDescription());
        expense.setDate(updatedExpense.getDate());
        return expenseRepository.save(expense);
    }

    @DeleteMapping("/{id}")
    public void deleteExpense(@PathVariable Long id) {
        Expense expense = expenseRepository.findById(id).orElseThrow();
        validateOwnership(expense);
        expenseRepository.deleteById(id);
    }

    private void validateOwnership(Expense expense) {
        User currentUser = getCurrentUser();
        if (!expense.getUser().getId().equals(currentUser.getId())) {
            throw new RuntimeException("Not authorized to access this expense");
        }
    }
}