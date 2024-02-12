package com.boaglio.rinhadebackend2024.service;

import com.boaglio.rinhadebackend2024.domain.Transacao;
import com.boaglio.rinhadebackend2024.dto.TransacaoResponse;
import com.boaglio.rinhadebackend2024.exception.SemSaldoException;
import com.boaglio.rinhadebackend2024.repository.ClienteRepository;
import com.boaglio.rinhadebackend2024.repository.TransacaoRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class TransacaoService {

    private static long contador = 0L;
    Logger log = LoggerFactory.getLogger(TransacaoService.class.getSimpleName());
    private final ClienteRepository clienteRepository;
    private final TransacaoRepository transacaoRepository;

    public TransacaoService(ClienteRepository clienteRepository, TransacaoRepository transacaoRepository) {
        this.clienteRepository = clienteRepository;
        this.transacaoRepository = transacaoRepository;
    }

    /**
     * O uso do "synchronized" garante a consitencia do saldo das diversas threads de saque
     */
    synchronized public TransacaoResponse efetuaDebito(long clientId, long valorDaTransacao, String descricao) throws SemSaldoException {

        var cliente = clienteRepository.findById(clientId);
        var saldoAtual = cliente.get().getSaldo();
        var limiteDoCliente = cliente.get().getLimite();
        Transacao transacao;
        if ((saldoAtual + limiteDoCliente)>=valorDaTransacao) {
            saldoAtual-=valorDaTransacao;
            log.info(++contador + " Debito: " + valorDaTransacao + " saldo: "+ saldoAtual + " limite: " +limiteDoCliente +  " - cliente: "+clientId);
            transacao = new Transacao(clientId,valorDaTransacao, Transacao.TipoTransacao.d, descricao);
        } else {
            log.info(++contador + " Sem saldo - cliente: "+clientId);
            throw new SemSaldoException();
        }

        cliente.get().setSaldo(saldoAtual);
        // salva transacao
        transacaoRepository.save(transacao);

        // atualiza cliente (saldo)
        clienteRepository.save(cliente.get());

       return new TransacaoResponse(cliente.get().getLimite(), saldoAtual);
    }

    synchronized public TransacaoResponse efetuaCredito(long clientId,long valorDaTransacao,String descricao) {

        var cliente = clienteRepository.findById(clientId);
        var saldoAtual = cliente.get().getSaldo();
        var limiteDoCliente = cliente.get().getLimite();
        saldoAtual+=valorDaTransacao;
        log.info(++contador + " Credito - cliente: " + valorDaTransacao + " saldo: "+ saldoAtual + " limite: " +limiteDoCliente +  "- cliente: "+clientId);
        var transacao = new Transacao(clientId,valorDaTransacao, Transacao.TipoTransacao.c, descricao);

        cliente.get().setSaldo(saldoAtual);

        // salva transacao
        transacaoRepository.save(transacao);

        // atualiza cliente (saldo)
        clienteRepository.save(cliente.get());

        return new TransacaoResponse(cliente.get().getLimite(), saldoAtual);
    }
}

