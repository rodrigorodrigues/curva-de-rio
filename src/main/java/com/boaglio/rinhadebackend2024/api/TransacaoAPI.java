package com.boaglio.rinhadebackend2024.api;

import com.boaglio.rinhadebackend2024.domain.Cliente;
import com.boaglio.rinhadebackend2024.domain.Transacao;
import com.boaglio.rinhadebackend2024.dto.TransacaoRequest;
import com.boaglio.rinhadebackend2024.dto.TransacaoResponse;
import com.boaglio.rinhadebackend2024.exception.SemSaldoException;
import com.boaglio.rinhadebackend2024.repository.TransacaoRepository;
import com.boaglio.rinhadebackend2024.service.TransacaoService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Objects;

@RestController
public class TransacaoAPI {

    private static long contador = 0L;
    private final TransacaoRepository transacaoRepository;
    private final TransacaoService transacaoService;
    private static final ObjectMapper mapper = new ObjectMapper();

    Logger log = LoggerFactory.getLogger(TransacaoAPI.class.getSimpleName());

    public TransacaoAPI(TransacaoRepository transacaoRepository, TransacaoService transacaoService) {
        this.transacaoRepository = transacaoRepository;
        this.transacaoService = transacaoService;
    }

    @PostMapping("/clientes/{id}/transacoes")
    public ResponseEntity<Object> transacoes(@PathVariable Long id, @RequestBody String  bodyStr ) {
        // log.info("transacao: "+bodyStr);
        TransacaoRequest body;
        try {
            body = mapper.readValue(bodyStr, TransacaoRequest.class);
            // tudo isso porque o Spring faz o cast de "1.2" (double) para "1" e não lança erro
            String  valor = bodyStr.split(",")[0];
            if (valor.contains("."))  {
                log.info(++contador + " Invalid request ! - valor decimal");
                return ResponseEntity.unprocessableEntity().build();
            }
        } catch (JsonProcessingException e) {
            return ResponseEntity.unprocessableEntity().build();
        }
        if (Objects.isNull(body)||
            Cliente.invalidCustomer(id) || // ID cliente
            Objects.isNull(body.tipo()) || body.tipo().isEmpty() ||  !Transacao.validTipoTransacao(body.tipo())  ||  // tipo
            Objects.isNull(body.descricao())|| body.descricao().isEmpty() ||  body.descricao().length()>10  ||    // descricao
            body.valor() <= 0 // somente valores positivos
         ) {
            log.info(++contador+" Invalid request ! ");
            return ResponseEntity.unprocessableEntity().build();
        }
        var valorDaTransacao = body.valor();
        TransacaoResponse transacaoResponse;
        if (Transacao.TipoTransacao.c.name().equals(body.tipo())) {
            // credito
            transacaoResponse = transacaoService.efetuaCredito(id,valorDaTransacao, body.descricao());
        } else {
            // debito
            try {
                transacaoResponse = transacaoService.efetuaDebito(id,valorDaTransacao, body.descricao());
            } catch (SemSaldoException e) {
                return ResponseEntity.unprocessableEntity().build();
            }
        }
       return ResponseEntity.ok(transacaoResponse);
    }

    @GetMapping("/transacoes")
    public Long getTransacoesTotal() {
        var total = transacaoRepository.count();
        log.info(++contador + " total de transacoes: " + total);
        return total;
    }

    @GetMapping("/transacoes/{id}")
    public ResponseEntity<Transacao> getTransacoes(@PathVariable String id) {
        var transacao =  transacaoRepository.findById(id);
        log.info(++contador + " buscando transacao: "+id);
        return transacao.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping("/transacoes/cliente/{id}")
    public ResponseEntity<List<Transacao>> getTransacoesPorCliente(@PathVariable Long id) {
        var transacaoList =  transacaoRepository.findFirst10ByClienteIdOrderByRealizadaEmDesc(id);
        log.info(++contador + " buscando transacao do cliente "+id);
        return ResponseEntity.ok(transacaoList);
    }
}