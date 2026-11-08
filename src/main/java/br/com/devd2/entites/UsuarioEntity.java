package br.com.devd2.entites;

import java.time.LocalDateTime;
import io.quarkus.mongodb.panache.common.MongoEntity;
import org.bson.codecs.pojo.annotations.BsonId;


import jakarta.json.bind.annotation.JsonbDateFormat;

@MongoEntity(collection = "tb_usuario_sistema")
public class UsuarioEntity extends DocumentoBase {

    @BsonId
    public org.bson.types.ObjectId id;

    public String nomeUsuario;

    public String emailUsuario;

    public String senhaUsuario;

    public String saltSenhaUsuario;

    public boolean isAlterarSenhaUsuario;

    public boolean isConfirmacaoEmailUsuario;

    public int codigoTipoSituacaoUsuario;

    @JsonbDateFormat(value = "yyyy-MM-dd HH:mm:ss")
    public LocalDateTime dataUltimoAcessoUsuario;

    @JsonbDateFormat(value = "yyyy-MM-dd HH:mm:ss")
    public LocalDateTime dataHoraRegistroUsuario = LocalDateTime.now();
}
