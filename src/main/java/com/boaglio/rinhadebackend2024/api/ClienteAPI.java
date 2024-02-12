package com.boaglio.rinhadebackend2024.api;

import com.boaglio.rinhadebackend2024.dto.ExtratoResponse;
import com.boaglio.rinhadebackend2024.dto.Saldo;
import com.boaglio.rinhadebackend2024.domain.Cliente;
import com.boaglio.rinhadebackend2024.repository.ClienteRepository;
import com.boaglio.rinhadebackend2024.repository.TransacaoRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@RestController
public class ClienteAPI {

    static long contador = 0L;

    private final ClienteRepository clienteRepository;
    private final TransacaoRepository transacaoRepository;

    Logger log = LoggerFactory.getLogger(ClienteAPI.class.getSimpleName());

    public ClienteAPI(ClienteRepository clienteRepository, TransacaoRepository transacaoRepository) {
        this.clienteRepository = clienteRepository;
        this.transacaoRepository = transacaoRepository;
    }

    @GetMapping("/clientes/{id}/extrato")
    public ResponseEntity<Object> getExtrato(@PathVariable Long id) {
        if (Cliente.invalidCustomer(id)) {
            log.info(++contador + " Invalid request ! ");
            return ResponseEntity.notFound().build();
        }
        var transacoes = transacaoRepository.findFirst10ByClienteIdOrderByRealizadaEmDesc(id);
        Optional<Cliente> clienteOpt = clienteRepository.findById(id);
        var saldoAtual = clienteOpt.get().getSaldo();
        var limiteDoCliente = clienteOpt.get().getLimite();
        var dataExtrato = ZonedDateTime.now().toString();
        log.info(++contador + " extrato do cliente - saldo: "+ saldoAtual + " limite: " +limiteDoCliente +  "  transacoes: "+ (long) transacoes.size() + " - cliente: "+id);
        return ResponseEntity.ok(new ExtratoResponse( new Saldo(saldoAtual,dataExtrato , limiteDoCliente), transacoes));
    }

    @GetMapping("/clientes")
    public List<Cliente> getClientes() {
        return (ArrayList<Cliente>) clienteRepository.findAll();
    }

    @GetMapping("/clientes-total")
    public Long getClientesTotal() {
        var total =  clienteRepository.count();
        log.info(++contador + " total de clientes: "+total);
        return total;
    }

    @GetMapping("/clientes/{id}")
    public ResponseEntity<Cliente> getCliente(@PathVariable Long id) {
        Optional<Cliente> cliente =  clienteRepository.findById(id);
        log.info(++contador + " buscando cliente: "+id);
        return cliente.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

}