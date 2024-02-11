package com.boaglio.rinhadebackend2024;

import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface TransacaoRepository extends MongoRepository<Transacao,String> {
  List<Transacao> findFirst10ByClienteIdOrderByRealizadaEmDesc(Long clienteId);

}