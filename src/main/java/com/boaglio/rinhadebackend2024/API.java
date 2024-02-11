package com.boaglio.rinhadebackend2024;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.catalina.mapper.Mapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@RestController
public class API {

    private final ClienteRepository clienteRepository;
    private final TransacaoRepository transacaoRepository;

    private static ObjectMapper mapper = new ObjectMapper();

    Logger log = LoggerFactory.getLogger(API.class.getSimpleName());

    public API(ClienteRepository clienteRepository, TransacaoRepository transacaoRepository) {
        this.clienteRepository = clienteRepository;
        this.transacaoRepository = transacaoRepository;
    }

    @PostMapping("/clientes/{id}/transacoes")
    public ResponseEntity<Object> transacoes(@PathVariable Long id, @RequestBody String  bodyStr ) {
        TransacaoRequest body;
        try {
            body = mapper.readValue(bodyStr, TransacaoRequest.class);

            // tudo isso pq o Spring faz o cast de "1.2" (double) para "1" e não lança erro
            String  valor = bodyStr.split(",")[0];
            if (valor.contains("."))  {
                log.info("Invalid request ! - valor decimal");
                return ResponseEntity.unprocessableEntity().build();
            }
        } catch (JsonProcessingException e) {
            return ResponseEntity.unprocessableEntity().build();
        }

        if (Objects.isNull(body)||
            Cliente.invalidCustomer(id) || // ID cliente
            Objects.isNull(body.tipo()) || !Transacao.validTipoTransacao(body.tipo())  ||  // tipo
            Objects.isNull(body.descricao())|| body.descricao().isEmpty() ||  body.descricao().length()>10   // descricao
         ) {
            log.info("Invalid request ! ");
            return ResponseEntity.unprocessableEntity().build();
        }
        Optional<Cliente> cliente = clienteRepository.findById(id);
        // credito
        long saldoAtual = cliente.get().getSaldo();
        long limiteDoCliente = Cliente.LIMITES[id.intValue()];
        long valorLong = body.valor();
        Transacao transacao;
        if (Transacao.TipoTransacao.c.name().equals(body.tipo())) {
            saldoAtual+=valorLong;
            transacao = new Transacao(id,valorLong, Transacao.TipoTransacao.c, body.descricao());
        } else {
            // debito
            if ((saldoAtual + limiteDoCliente)>=valorLong) {
                log.info("Credito - cliente: "+id);
                saldoAtual-=valorLong;
                transacao = new Transacao(id,valorLong, Transacao.TipoTransacao.d, body.descricao());
            } else {
                log.info("Sem saldo - cliente: "+id);
                return ResponseEntity.unprocessableEntity().build();
            }
        }
        cliente.get().setSaldo(saldoAtual);
        // salva transacao
        transacaoRepository.save(transacao);
        // atualiza cliente (saldo)
        clienteRepository.save(cliente.get());

        return ResponseEntity.ok(new TransacaoResponse(limiteDoCliente, saldoAtual));
    }

    @GetMapping("/clientes/{id}/extrato")
    public ResponseEntity<Object> getExtrato(@PathVariable Long id) {
        log.info("extrato do cliente "+id);
        if (Cliente.invalidCustomer(id)) {
            log.info("Invalid request ! ");
            return ResponseEntity.notFound().build();
        }
        List<Transacao> transacoes = transacaoRepository.findFirst10ByClienteIdOrderByRealizadaEmDesc(id);
        Optional<Cliente> cliente = clienteRepository.findById(id);
        var saldoAtual = cliente.get().getSaldo();
        long limiteDoCliente = Cliente.LIMITES[id.intValue()];
        var dataExtrato = ZonedDateTime.now().toString();
        return ResponseEntity.ok(new ExtratoResponse( new Saldo(saldoAtual,dataExtrato , limiteDoCliente), transacoes));
    }

    @GetMapping("/clientes")
    public List<Cliente> getClientes() {
        return (ArrayList<Cliente>) clienteRepository.findAll();
    }

    @GetMapping("/clientes-total")
    public Long getClientesTotal() {
        var total =  clienteRepository.count();
        log.info("total de clientes: "+total);
        return total;
    }

    @GetMapping("/transacoes")
    public Long getTransacoesTotal() {
        var total = transacaoRepository.count();
        log.info("total de transacoes: " + total);
        return total;
    }

    @GetMapping("/clientes/{id}")
    public ResponseEntity<Cliente> getCliente(@PathVariable Long id) {
        Optional<Cliente> cliente =  clienteRepository.findById(id);
        log.info("buscando cliente: "+id);
        return cliente.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping("/transacoes/{id}")
    public ResponseEntity<Transacao> getTransacoes(@PathVariable String id) {
        Optional<Transacao> transacao =  transacaoRepository.findById(id);
        log.info("buscando transacao: "+id);
        return transacao.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping("/transacoes/cliente/{id}")
    public ResponseEntity<List<Transacao>> getTransacoesPorCliente(@PathVariable Long id) {
        List<Transacao> transacaoList =  transacaoRepository.findFirst10ByClienteIdOrderByRealizadaEmDesc(id);
        log.info("buscando transacao do cliente "+id);
        return ResponseEntity.ok(transacaoList);
    }
}