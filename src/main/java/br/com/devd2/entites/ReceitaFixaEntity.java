package br.com.devd2.entites;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import io.quarkus.mongodb.panache.common.MongoEntity;
import org.bson.codecs.pojo.annotations.BsonId;


import jakarta.json.bind.annotation.JsonbDateFormat;

@MongoEntity(collection = "tb_receita_fixa")
public class ReceitaFixaEntity extends DocumentoBase {

    @BsonId
    public org.bson.types.ObjectId id;

    public String idUsuario;

    public String idNaturezaContaReceitaFixa;

    public String descricaoReceitaFixa;

    public int diaRecebimentoReceitaFixa;

    public BigDecimal valorReceitaFixa;

    public int codigoTipoSituacaoReceitaFixa;

    @JsonbDateFormat(value = "yyyy-MM-dd HH:mm:ss")
    public LocalDateTime dataHoraRegistroReceitaFixa = LocalDateTime.now();

}
