package com.rudik.dal;
import org.springframework.data.mongodb.repository.MongoRepository;

import com.rudik.model.Users;

public interface UsersRepository extends MongoRepository<Users, String> {
  Users findByUsername(String username);
}