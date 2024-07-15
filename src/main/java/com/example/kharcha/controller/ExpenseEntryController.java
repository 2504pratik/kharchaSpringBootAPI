package com.example.kharcha.controller;

import com.example.kharcha.entity.ExpenseEntry;
import com.example.kharcha.entity.User;
import com.example.kharcha.service.ExpenseEntryService;
import com.example.kharcha.service.UserService;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/expense")
public class ExpenseEntryController {

    @Autowired
    private ExpenseEntryService expenseEntryService;

    @Autowired
    private UserService userService;

    @GetMapping
    public ResponseEntity<?> getAllExpenseEntriesOfUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String userName = authentication.getName();
        User user = userService.findByUserName(userName);
        List<ExpenseEntry> all = user.getExpenseEntries();
        if (all != null && !all.isEmpty()) {
            return new ResponseEntity<>(all, HttpStatus.OK);
        }
        return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }

    @PostMapping
    public ResponseEntity<ExpenseEntry> createExpenseEntry(@RequestBody ExpenseEntry myEntry) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String userName = authentication.getName();
            User user = userService.findByUserName(userName);
            expenseEntryService.saveEntry(myEntry, userName);
            return new ResponseEntity<>(myEntry, HttpStatus.CREATED);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }

    @GetMapping("id/{myId}")
    public ResponseEntity<ExpenseEntry> getExpenseEntryById(@PathVariable ObjectId myId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String userName = authentication.getName();
        User user = userService.findByUserName(userName);
        List<ExpenseEntry> collect = user.getExpenseEntries().stream().filter(x -> x.getExpenseId().equals(myId)).toList();
        if (!collect.isEmpty()) {
            Optional<ExpenseEntry> expenseEntry = expenseEntryService.findById(myId);
            if (expenseEntry.isPresent()) {
                return new ResponseEntity<>(expenseEntry.get(), HttpStatus.OK);
            }
        }
        return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }

    @DeleteMapping("id/{myId}")
    public ResponseEntity<?> deleteExpenseEntryById(@PathVariable ObjectId myId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String userName = authentication.getName();
        boolean removed =expenseEntryService.deleteById(myId, userName);
        if (removed) {
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    @PutMapping("id/{Id}")
    public ResponseEntity<?> updateExpenseEntryById(
            @PathVariable ObjectId Id,
            @RequestBody ExpenseEntry newEntry
    ) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String userName = authentication.getName();
            User user = userService.findByUserName(userName);

            if (user == null) {
                return new ResponseEntity<>("User not found", HttpStatus.NOT_FOUND);
            }

            Optional<ExpenseEntry> expenseEntryOpt = expenseEntryService.findById(Id);
            if (expenseEntryOpt.isEmpty()) {
                return new ResponseEntity<>("Expense entry not found", HttpStatus.NOT_FOUND);
            }

            ExpenseEntry oldEntry = expenseEntryOpt.get();

            // Check if the expense entry belongs to the authenticated user
            if (!user.getExpenseEntries().contains(oldEntry)) {
                return new ResponseEntity<>("Unauthorized access to expense entry", HttpStatus.FORBIDDEN);
            }

            // Update fields
            if (newEntry.getTitle() != null && !newEntry.getTitle().isEmpty()) {
                oldEntry.setTitle(newEntry.getTitle());
            }
            if (newEntry.getPerson() != null) {
                oldEntry.setPerson(newEntry.getPerson());
            }
            if (newEntry.getAmount() != 0) {  // Assuming 0 is not a valid amount
                oldEntry.setAmount(newEntry.getAmount());
            }
            if (newEntry.getDate() != null) {
                oldEntry.setDate(newEntry.getDate());
            }
            // Always update isBorrowed as it's a boolean
            oldEntry.setBorrowed(newEntry.isBorrowed());

            ExpenseEntry updatedEntry = expenseEntryService.saveEntry(oldEntry);
            return new ResponseEntity<>(updatedEntry, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>("An error occurred while updating the expense entry", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
