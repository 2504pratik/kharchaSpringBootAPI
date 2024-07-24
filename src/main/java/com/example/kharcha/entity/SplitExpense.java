package com.example.kharcha.entity;

import lombok.Data;
import lombok.NonNull;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Document(collection = "split_expenses")
@Data
public class SplitExpense {

    @Id
    private ObjectId splitExpenseId;

    @NonNull
    private String title;

    private LocalDateTime date;

    @NonNull
    private List<String> userNames;

    @NonNull
    private double totalAmount;

    private List<Double> splitAmounts;
}
