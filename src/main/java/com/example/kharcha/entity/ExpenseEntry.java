package com.example.kharcha.entity;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Document(collection = "expense_entries")
@Data
@NoArgsConstructor
public class ExpenseEntry {
    @Id
    private ObjectId expenseId;

    @NonNull
    private String title;

    @NonNull
    private float amount;

    private String person;

    @NonNull
    private LocalDateTime date;

    private boolean isBorrowed;
}
