package com.boaglio.rinhadebackend2024.domain;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "cliente")
public class Cliente {
    @Id
    private Integer id;
    private long limite;
    private long saldo;
    public static boolean invalidCustomer(long idCustomer) {
        return idCustomer <= 0 || idCustomer >= 6;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public long getLimite() {
        return limite;
    }

    public void setLimite(long limite) {
        this.limite = limite;
    }

    public long getSaldo() {
        return saldo;
    }

    public void setSaldo(long saldo) {
        this.saldo = saldo;
    }

    @Override
    public String toString() {
        return "Cliente{" +
                "id=" + id +
                ", limite=" + limite +
                ", saldo=" + saldo +
                '}';
    }
}