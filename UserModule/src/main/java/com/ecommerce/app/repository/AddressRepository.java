package com.ecommerce.app.repository;

import com.ecommerce.app.model.Address;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AddressRepository extends MongoRepository<Address,String> {
    List<Address> findByUserId(String userId);

    List<Address> findByUserIdIn(List<String> userIds);
}


