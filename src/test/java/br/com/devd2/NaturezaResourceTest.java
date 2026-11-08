package br.com.devd2;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;
import java.util.Set;
import org.bson.types.ObjectId;
import org.junit.jupiter.api.Test;
import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.QuarkusTest;
import io.smallrye.jwt.build.Jwt;

@QuarkusTest
@QuarkusTestResource(value = MongoPersistenceTest.IsolatedMongo.class, restrictToAnnotatedClass = true)
class NaturezaResourceTest {
    @Test void gerenciaNaturezaDoUsuario() {
        String usuario = new ObjectId().toHexString();
        String outro = new ObjectId().toHexString();
        String token = Jwt.issuer("gfin-backend").subject(usuario).groups(Set.of("usuario")).sign();
        String tokenOutro = Jwt.issuer("gfin-backend").subject(outro).groups(Set.of("usuario")).sign();
        String body = "{\"nomeNaturezaConta\":\"Mercado\",\"descricaoNaturezaConta\":\"Alimentacao\",\"codigoTipoLancamentoConta\":1}";
        String id = given().auth().oauth2(token).contentType("application/json").body(body)
                .post("/api/v1/natureza-conta").then().statusCode(200).extract().path("idNaturezaConta");
        given().auth().oauth2(token).get("/api/v1/natureza-conta/" + id).then().statusCode(200).body("descricaoNaturezaConta", is("Alimentacao"));
        given().auth().oauth2(tokenOutro).get("/api/v1/natureza-conta/" + id).then().statusCode(400);
        given().auth().oauth2(token).contentType("application/json").body("{\"descricaoNaturezaConta\":\"Compras\"}")
                .put("/api/v1/natureza-conta/" + id).then().statusCode(200).body("descricaoNaturezaConta", is("Compras"));
        given().auth().oauth2(token).delete("/api/v1/natureza-conta/" + id).then().statusCode(200);
        given().auth().oauth2(token).get("/api/v1/natureza-conta/" + id).then().statusCode(400);
    }
}
