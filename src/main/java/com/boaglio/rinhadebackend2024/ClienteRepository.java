package com.boaglio.rinhadebackend2024;

import org.springframework.data.mongodb.repository.MongoRepository;

public interface ClienteRepository extends MongoRepository<Cliente,Long> {

}
