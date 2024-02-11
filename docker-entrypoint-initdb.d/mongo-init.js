db = db.getSiblingDB('rinha2024q1');

db.createCollection("transacao", { capped : true, size: 10240, max :100 } );

db.createCollection('cliente');

db.cliente.insert({ _id: 1, saldo: 0});

db.cliente.insert({ _id: 2, saldo: 0});

db.cliente.insert({ _id: 3, saldo: 0});

db.cliente.insert({ _id: 4, saldo: 0});

db.cliente.insert({ _id: 5, saldo: 0});
