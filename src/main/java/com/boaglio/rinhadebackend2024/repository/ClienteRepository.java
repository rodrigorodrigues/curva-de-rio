package com.boaglio.rinhadebackend2024.repository;

import com.boaglio.rinhadebackend2024.domain.Cliente;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface ClienteRepository extends MongoRepository<Cliente,Long> {
}