package br.com.devd2.entites;

import java.time.LocalDateTime;
import io.quarkus.mongodb.panache.common.MongoEntity;
import org.bson.codecs.pojo.annotations.BsonId;


import jakarta.json.bind.annotation.JsonbDateFormat;

@MongoEntity(collection = "tb_agenda_correntista")
public class CorrentistaEntity extends DocumentoBase {

    @BsonId
    public org.bson.types.ObjectId id;

    public String idUsuario;

    public String nomeBanco;

    public String numeroAgencia;

    public String numeroContaCorrente;

    public String nomeCorrentista;

    public String observacao;

    @JsonbDateFormat(value = "yyyy-MM-dd HH:mm:ss")
    public LocalDateTime dataHoraRegistroCorrentista = LocalDateTime.now();
}
