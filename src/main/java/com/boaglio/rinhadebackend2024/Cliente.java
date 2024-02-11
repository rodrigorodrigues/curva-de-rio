package com.boaglio.rinhadebackend2024;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "cliente")
public class Cliente {
    public static long [] LIMITES = {0,100000,80000,1000000,10000000,500000};
    @Id
    private Integer id;
    private long saldo;
    public static boolean invalidCustomer(long idCustomer) {
        return idCustomer <= 0 || idCustomer >= 6;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public long getSaldo() {
        return saldo;
    }

    public void setSaldo(long saldo) {
        this.saldo = saldo;
    }

}