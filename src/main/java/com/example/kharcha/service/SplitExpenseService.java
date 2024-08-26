package com.example.kharcha.service;

import com.example.kharcha.entity.SplitExpense;
import com.example.kharcha.entity.User;
import com.example.kharcha.exception.InvalidSplitException;
import com.example.kharcha.exception.UserNotFoundException;
import com.example.kharcha.repository.SplitExpensesRepo;
import com.example.kharcha.repository.UserRepo;
import lombok.extern.slf4j.Slf4j;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;

@Component
@Slf4j
public class SplitExpenseService {

    @Autowired
    private SplitExpensesRepo splitExpensesRepo;

    @Autowired
    private UserRepo userRepo;

    @Autowired
    private UserService userService;

    @Transactional
    public void saveNewSplitExpense(SplitExpense splitExpense) {
        try {
            splitExpense.setDate(LocalDateTime.now());
            SplitExpense saved =saveSplitExpense(splitExpense);
            for(String userName : splitExpense.getUserNames()) {
                User user = userRepo.findByUserName(userName);
                user.getSplitExpenses().add(saved);
                userService.saveUser(user);
            }
            log.info("Split expense created");
        } catch (InvalidSplitException e) {
            log.error("Invalid split :", e);
        }
    }

    public SplitExpense saveSplitExpense(SplitExpense splitExpense) {
        splitExpensesRepo.save(splitExpense);
        return splitExpense;
    }

    public Optional<SplitExpense> findById(ObjectId splitExpenseId) {
        return splitExpensesRepo.findById(splitExpenseId);
    }

    @Transactional
    public boolean deleteExpense(SplitExpense splitExpense, String userName) {
        boolean removed = false;
        try {
            User user = userService.findByUserName(userName);
            removed = user.getSplitExpenses().remove(splitExpense);
            if (removed) {
                userService.saveUser(user);
                splitExpensesRepo.deleteById(splitExpense.getSplitExpenseId());
                log.info("Expense deleted for user {}",userName);
            }
        } catch (Exception e) {
            log.error("Error while deleting the entry: {} for user {}", splitExpense.getSplitExpenseId(),userName, e);
        }
        return removed;
    }
}
