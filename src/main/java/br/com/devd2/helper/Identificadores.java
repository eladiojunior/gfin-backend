package br.com.devd2.helper;
import org.bson.types.ObjectId;
import jakarta.ws.rs.BadRequestException;
public final class Identificadores {
    private Identificadores() {}
    public static ObjectId oid(String id) {
        if (id == null || !ObjectId.isValid(id)) throw new BadRequestException("Identificador invalido");
        return new ObjectId(id);
    }
}
