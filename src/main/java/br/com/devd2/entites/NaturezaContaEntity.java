package br.com.devd2.entites;

import java.time.LocalDateTime;
import io.quarkus.mongodb.panache.common.MongoEntity;
import org.bson.codecs.pojo.annotations.BsonId;


import jakarta.json.bind.annotation.JsonbDateFormat;

@MongoEntity(collection = "tb_natureza_conta")
public class NaturezaContaEntity extends DocumentoBase {

    @BsonId
    public org.bson.types.ObjectId id;

    public String idUsuario;

    public int codigoTipoLancamentoConta;

    public String nomeNaturezaConta;

    public String descricaoNaturezaConta;

    public int codigoTipoSituacaoNaturezaConta;

    @JsonbDateFormat(value = "yyyy-MM-dd HH:mm:ss")
    public LocalDateTime dataHoraRegistroNaturezaConta = LocalDateTime.now();
}
