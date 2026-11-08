package br.com.devd2.entites;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import io.quarkus.mongodb.panache.common.MongoEntity;
import org.bson.codecs.pojo.annotations.BsonId;


import jakarta.json.bind.annotation.JsonbDateFormat;

@MongoEntity(collection = "tb_receita_mensal")
public class ReceitaMensalEntity extends DocumentoBase {

    @BsonId
    public org.bson.types.ObjectId id;

    public String idUsuario;

    public String idReceitaFixa;

    public String idNaturezaContaReceita;

    public String idFormaLancamento;

    public String textoDescricaoReceita;

    @JsonbDateFormat(value = "yyyy-MM-dd")
    public LocalDateTime dataRecebimentoReceita;

    public BigDecimal valorReceita;

    public boolean isReceitaLiquidada;

    @JsonbDateFormat(value = "yyyy-MM-dd HH:mm:ss")
    public LocalDateTime dataHoraLiquidacaoReceita;

    public BigDecimal valorTotalLiquidacaoReceita;

    @JsonbDateFormat(value = "yyyy-MM-dd HH:mm:ss")
    public LocalDateTime dataHoraRegistroReceita = LocalDateTime.now();
}
