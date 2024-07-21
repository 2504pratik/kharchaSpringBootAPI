package com.example.kharcha.entity;

import lombok.Data;
import lombok.NonNull;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.ArrayList;
import java.util.List;

// POJO file for User
@Document(collection = "users")
@Data
public class User {
    @Id
    private ObjectId userId;

    @Indexed(unique = true)
    @NonNull
    private String userName;
    @NonNull
    private String password;

    @DBRef // creates a link between collections users and journal_entries
    private List<ExpenseEntry> expenseEntries = new ArrayList<>();
}
