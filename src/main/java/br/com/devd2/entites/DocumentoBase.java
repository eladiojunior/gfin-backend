package br.com.devd2.entites;
import io.quarkus.mongodb.panache.PanacheMongoEntityBase;
import org.bson.types.ObjectId;
public abstract class DocumentoBase extends PanacheMongoEntityBase {
    public void salvar() {
        try {
            var campo = getClass().getField("id");
            if (campo.get(this) == null) { campo.set(this, new ObjectId()); persist(); }
            else update();
        } catch (ReflectiveOperationException e) { throw new IllegalStateException(e); }
    }
}
