package br.com.devd2.entites;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import io.quarkus.mongodb.panache.common.MongoEntity;
import org.bson.codecs.pojo.annotations.BsonId;


import jakarta.json.bind.annotation.JsonbDateFormat;

@MongoEntity(collection = "tb_historico_receita_fixa")
public class HistoricoReceitaFixaEntity extends DocumentoBase {

    @BsonId
    public org.bson.types.ObjectId id;

    public String idUsuario;

    public String idReceitaFixa;

    public BigDecimal valorHistoricoReceitaFixa;

    @JsonbDateFormat(value = "yyyy-MM-dd HH:mm:ss")
    public LocalDateTime dataHoraRegistroHistoricoReceitaFixa = LocalDateTime.now();
}
