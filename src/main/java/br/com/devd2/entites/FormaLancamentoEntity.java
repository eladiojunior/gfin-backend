package br.com.devd2.entites;
import java.time.LocalDateTime;

import jakarta.json.bind.annotation.JsonbDateFormat;
import org.bson.types.ObjectId;
import org.bson.codecs.pojo.annotations.BsonId;
import io.quarkus.mongodb.panache.common.MongoEntity;
@MongoEntity(collection = "tb_forma_lancamento")
public class FormaLancamentoEntity extends DocumentoBase {
    @BsonId public ObjectId id;
    public String idUsuario;
    public int codigoLancamentoConta;
    public String nomeFormaLancamento;
    public int codigoSituacaoFormaLancamento;
    @JsonbDateFormat(value = "yyyy-MM-dd HH:mm:ss")
    public LocalDateTime dataHoraRegistroFormaLancamento = LocalDateTime.now();
}
