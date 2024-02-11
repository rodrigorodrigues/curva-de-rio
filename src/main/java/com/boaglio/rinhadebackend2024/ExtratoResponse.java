package com.boaglio.rinhadebackend2024;

import java.util.List;

public record ExtratoResponse(Saldo saldo, List<Transacao> transacoes) {
}
