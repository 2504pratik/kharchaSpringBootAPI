package com.example.kharcha.repository;

import com.example.kharcha.entity.ExpenseEntry;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface ExpenseEntryRepo extends MongoRepository<ExpenseEntry, ObjectId> {

}
