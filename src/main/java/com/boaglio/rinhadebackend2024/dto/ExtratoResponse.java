package com.boaglio.rinhadebackend2024.dto;

import com.boaglio.rinhadebackend2024.domain.Transacao;

import java.util.List;

public record ExtratoResponse(Saldo saldo, List<Transacao> ultimas_transacoes) {
}
