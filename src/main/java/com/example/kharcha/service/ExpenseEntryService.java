package com.example.kharcha.service;

import com.example.kharcha.entity.ExpenseEntry;
import com.example.kharcha.entity.User;
import com.example.kharcha.exception.ExpenseNotFoundException;
import com.example.kharcha.exception.InvalidSplitException;
import com.example.kharcha.exception.UserNotFoundException;
import com.example.kharcha.repository.ExpenseEntryRepo;
import com.example.kharcha.repository.UserRepo;
import lombok.extern.slf4j.Slf4j;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Component
@Slf4j
public class ExpenseEntryService {

    @Autowired
    private ExpenseEntryRepo expenseEntryRepo;

    @Autowired
    private UserService userService;

    // For expense entry creation
    @Transactional
    public void saveNewExpenseEntry(ExpenseEntry expenseEntry, String userName) {
        try {
            User user =userService.findByUserName(userName);
            expenseEntry.setDate(LocalDateTime.now());
            ExpenseEntry saved = expenseEntryRepo.save(expenseEntry);
            user.getExpenseEntries().add(saved);
            userService.saveUser(user);
            log.info("Expense entry created for user {}",userName);
        } catch (Exception e) {
            log.error("Error while creating the entry: {} for user {}", expenseEntry,userName, e);
        }
    }

    // For expense entry update
    public ExpenseEntry saveExpenseEntry(ExpenseEntry expenseEntry) {
        expenseEntryRepo.save(expenseEntry);
        return expenseEntry;
    }

    public Optional<ExpenseEntry> findById(ObjectId expenseId) throws ExpenseNotFoundException{
        ExpenseEntry expense;
        expense = expenseEntryRepo.findById(expenseId)
                .orElseThrow(() -> new ExpenseNotFoundException("Expense not found"));
        return Optional.ofNullable(expense);
    }

    @Transactional
    public boolean deleteById(ObjectId expenseId, String userName) {
        boolean removed = false;
        try {
            User user = userService.findByUserName(userName);
            removed = user.getExpenseEntries().removeIf(x -> x.getExpenseId().equals(expenseId));
            if (removed) {
                userService.saveUser(user);
                expenseEntryRepo.deleteById(expenseId);
                log.info("Expense deleted for user {}",userName);
            }
        } catch (Exception e) {
            log.error("Error while deleting the entry: {} for user {}", expenseId,userName, e);
        }
        return removed;
    }
}
