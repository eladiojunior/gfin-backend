package br.com.devd2.entites;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import io.quarkus.mongodb.panache.common.MongoEntity;
import org.bson.codecs.pojo.annotations.BsonId;


import jakarta.json.bind.annotation.JsonbDateFormat;

@MongoEntity(collection = "tb_despesa_fixa")
public class DespesaFixaEntity extends DocumentoBase {

    @BsonId
    public org.bson.types.ObjectId id;

    public String idUsuario;

    public String idNaturezaContaDespesaFixa;

    public String descricaoDespesaFixa;

    public int diaVencimentoDespesaFixa;

    public BigDecimal valorDespesaFixa;

    public int codigoTipoSituacaoDespesaFixa;

    public String idFormaLancamento;

    @JsonbDateFormat(value = "yyyy-MM-dd HH:mm:ss")
    public LocalDateTime dataHoraRegistroDespesaFixa = LocalDateTime.now();

}
