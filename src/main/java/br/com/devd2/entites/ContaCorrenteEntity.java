package br.com.devd2.entites;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import io.quarkus.mongodb.panache.common.MongoEntity;
import org.bson.codecs.pojo.annotations.BsonId;


import jakarta.json.bind.annotation.JsonbDateFormat;

@MongoEntity(collection = "tb_conta_corrente")
public class ContaCorrenteEntity extends DocumentoBase {

    @BsonId
    public org.bson.types.ObjectId id;

    public String idUsuario;

    public int numeroBanco;

    public String nomeBanco;

    public String numeroAgencia;

    public String numeroContaCorrente;

    public String nomeTitularConta;

    public BigDecimal valorLimiteConta;

    public boolean isContaCorrenteAtiva;

    @JsonbDateFormat(value = "yyyy-MM-dd HH:mm:ss")
    public LocalDateTime dataHoraRegistroContaCorrente = LocalDateTime.now();
}
