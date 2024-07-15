package com.example.kharcha.service;

import com.example.kharcha.entity.ExpenseEntry;
import com.example.kharcha.entity.User;
import com.example.kharcha.repository.ExpenseEntryRepo;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Component
public class ExpenseEntryService {

    @Autowired
    private ExpenseEntryRepo expenseEntryRepo;

    @Autowired
    private UserService userService;

    @Transactional
    public void saveEntry(ExpenseEntry expenseEntry, String userName) {
        User user =userService.findByUserName(userName);
        expenseEntry.setDate(LocalDateTime.now());
        ExpenseEntry saved = expenseEntryRepo.save(expenseEntry);
        user.getExpenseEntries().add(saved);
        userService.saveUser(user);
    }

    public ExpenseEntry saveEntry(ExpenseEntry expenseEntry) {
        expenseEntryRepo.save(expenseEntry);
        return expenseEntry;
    }

    public List<ExpenseEntry> getAll() {
        return expenseEntryRepo.findAll();
    }

    public Optional<ExpenseEntry> findById(ObjectId id) {
        return expenseEntryRepo.findById(id);
    }

    @Transactional
    public boolean deleteById(ObjectId id, String userName) {
        boolean removed = false;
        try {
            User user = userService.findByUserName(userName);
            removed = user.getExpenseEntries().removeIf(x -> x.getExpenseId().equals(id));
            if (removed) {
                userService.saveUser(user);
                expenseEntryRepo.deleteById(id);
            }
        } catch (Exception e) {
            System.out.println(e);
            throw new RuntimeException("An error occurred while deleting the entry." + e);
        }
        return removed;
    }
}
