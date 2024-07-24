package com.example.kharcha.repository;

import com.example.kharcha.entity.SplitExpense;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface SplitExpensesRepo extends MongoRepository<SplitExpense, ObjectId> {
}
