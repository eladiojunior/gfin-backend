package br.com.devd2.entites;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import io.quarkus.mongodb.panache.common.MongoEntity;
import org.bson.codecs.pojo.annotations.BsonId;


import jakarta.json.bind.annotation.JsonbDateFormat;

@MongoEntity(collection = "tb_cartao_credito")
public class CartaoCreditoEntity extends DocumentoBase {

    @BsonId
    public org.bson.types.ObjectId id;

    public String idUsuario;

    // Nullable: cartão de alimentação/benefício pode não estar vinculado a conta corrente.

    public String idContaCorrente;

    public String numeroCartaoCredito;

    public String nomeCartaoCredito;

    @JsonbDateFormat(value = "yyyy-MM-dd HH:mm:ss")
    public LocalDateTime dataValidadeCartaoCredito;

    public BigDecimal valorLimiteCartaoCredito;

    public boolean hasCartaoCredito;

    public boolean hasCartaoPrePago;

    public boolean hasCartaoDebito;

    public int diaVencimentoCartaoCredito;

    public String nomeProprietarioCartaoCredito;

    public int situacaoCartaoCredito;

    @JsonbDateFormat(value = "yyyy-MM-dd HH:mm:ss")
    public LocalDateTime dataHoraRegistroCartaoCredito = LocalDateTime.now();

}
