package com.example.kharcha.controller;

import com.example.kharcha.entity.ExpenseEntry;
import com.example.kharcha.entity.SplitExpense;
import com.example.kharcha.entity.User;
import com.example.kharcha.exception.ExpenseNotFoundException;
import com.example.kharcha.exception.InvalidSplitException;
import com.example.kharcha.exception.UserNotFoundException;
import com.example.kharcha.service.ExpenseEntryService;
import com.example.kharcha.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

// To access these endpoints, authentication of user is needed.
@RestController
@RequestMapping("/expense")
@Slf4j
public class ExpenseEntryController {

    @Autowired
    private ExpenseEntryService expenseEntryService;

    @Autowired
    private UserService userService;

    // CRUD operations for expense entries.

    // To create new expense
    @PostMapping
    public ResponseEntity<ExpenseEntry> createExpenseEntry(@RequestBody ExpenseEntry myEntry) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String userName = authentication.getName();
            expenseEntryService.saveNewExpenseEntry(myEntry, userName);
            log.info("New expense entry created for user: {}",userName);
            return new ResponseEntity<>(myEntry, HttpStatus.CREATED);
        } catch (Exception e) {
            log.error("Error while creating new expense entry:",e);
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }

    // To get all expenses of particular user (authenticated)
    @GetMapping
    public ResponseEntity<?> getAllExpenseEntriesOfUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String userName = authentication.getName();
        User user = userService.findByUserName(userName);
        List<ExpenseEntry> all = user.getExpenseEntries();
        if (all != null && !all.isEmpty()) {
            log.info("Listed all expenses for user: {}",userName);
            return new ResponseEntity<>(all, HttpStatus.OK);
        }
        log.error("Error while listing all expenses for user: {}",userName);
        return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }

    // Extracting expense by expenseId
    @GetMapping("id/{expenseId}")
    public ResponseEntity<ExpenseEntry> getExpenseEntryById(@PathVariable ObjectId expenseId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String userName = authentication.getName();
        User user = userService.findByUserName(userName);
        List<ExpenseEntry> collect = user.getExpenseEntries().stream().filter(x -> x.getExpenseId().equals(expenseId)).toList();
        if (!collect.isEmpty()) {
            Optional<ExpenseEntry> expenseEntry = expenseEntryService.findById(expenseId);
            if (expenseEntry.isPresent()) {
                log.info("Expense entry found: {}", expenseId);
                return new ResponseEntity<>(expenseEntry.get(), HttpStatus.OK);
            }
        }
        log.error("Expense not found");
        return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }

    // Update an expense
    @PutMapping("id/{expenseId}")
    public ResponseEntity<?> updateExpenseEntryById(
            @PathVariable ObjectId expenseId,
            @RequestBody ExpenseEntry newExpenseEntry
    ) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String userName = authentication.getName();
            User user = userService.findByUserName(userName);

            if (user == null) {
                log.warn("User not found");
                return new ResponseEntity<>("User not found", HttpStatus.NOT_FOUND);
            }

            Optional<ExpenseEntry> expenseEntryOpt = expenseEntryService.findById(expenseId);
            if (expenseEntryOpt.isEmpty()) {
                log.warn("Expense entry not found");
                return new ResponseEntity<>("Expense entry not found", HttpStatus.NOT_FOUND);
            }

            ExpenseEntry oldEntry = expenseEntryOpt.get();

            // Check if the expense entry belongs to the authenticated user
            if (!user.getExpenseEntries().contains(oldEntry)) {
                log.warn("Unauthorized access");
                return new ResponseEntity<>("Unauthorized access to expense entry", HttpStatus.FORBIDDEN);
            }

            // Update fields
            if (!newExpenseEntry.getTitle().isEmpty()) {
                oldEntry.setTitle(newExpenseEntry.getTitle());
            }
            if (newExpenseEntry.getUser() != null) {
                oldEntry.setUser(newExpenseEntry.getUser());
            }
            if (newExpenseEntry.getAmount() != 0) {  // Assuming 0 is not a valid amount
                oldEntry.setAmount(newExpenseEntry.getAmount());
            }
            oldEntry.setDate(LocalDateTime.now());
            // Always update isBorrowed as it's a boolean
            oldEntry.setBorrowed(newExpenseEntry.isBorrowed());

            ExpenseEntry updatedEntry = expenseEntryService.saveExpenseEntry(oldEntry);
            log.info("Expense entry updated: {}",expenseId);
            return new ResponseEntity<>(updatedEntry, HttpStatus.OK);
        } catch (Exception e) {
            log.error("Error occurred while updating expense entry:",e);
            return new ResponseEntity<>("An error occurred while updating the expense entry", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // Delete an expense
    @DeleteMapping("id/{expenseId}")
    public ResponseEntity<?> deleteExpenseEntryById(@PathVariable ObjectId expenseId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String userName = authentication.getName();
        boolean removed =expenseEntryService.deleteById(expenseId, userName);
        if (removed) {
            log.info("Expense deleted");
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        } else {
            log.error("Error in expense entry deletion");
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }
}
