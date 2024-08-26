package com.example.kharcha.controller;

import com.example.kharcha.entity.ExpenseEntry;
import com.example.kharcha.entity.SplitExpense;
import com.example.kharcha.entity.User;
import com.example.kharcha.service.SplitExpenseService;
import com.example.kharcha.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/split")
@Slf4j
public class SplitExpenseController {

    @Autowired
    private SplitExpenseService splitExpenseService;

    @Autowired
    private UserService userService;

    @PostMapping
    public ResponseEntity<SplitExpense> createSplitExpense(@RequestBody SplitExpense split) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String userName = authentication.getName();
            List<Double> splitAmounts = split.getSplitAmounts();
            double total = 0;
            for(double splitAmount : splitAmounts) {
                total += splitAmount;
            }
            if(total != split.getTotalAmount()) {
                log.info("Total amount didn't match split total.");
                return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
            }
            splitExpenseService.saveNewSplitExpense(split);
            log.info("New expense entry created for user: {}",userName);
            return new ResponseEntity<>(split, HttpStatus.CREATED);
        } catch (Exception e) {
            log.error("Error while creating new expense entry:",e);
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }

    @GetMapping
    public ResponseEntity<?> getAllSplitsOfUser() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String userName = authentication.getName();
            User user = userService.findByUserName(userName);
            List<SplitExpense> allSplits = user.getSplitExpenses();
            if (allSplits != null && !allSplits.isEmpty()) {
                log.info("Listed all expenses for user: {}",userName);
                return new ResponseEntity<>(allSplits, HttpStatus.OK);
            }
            log.error("Something went wrong");
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        } catch (Exception e) {
            log.error("Error while listing all expenses",e);
        }
        return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
    }
}
