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
class DespesaResourceTest {
    @Test void gerenciaDespesaFixaDoUsuario() {
        String usuario = new ObjectId().toHexString();
        String token = Jwt.issuer("gfin-backend").subject(usuario).groups(Set.of("usuario")).sign();
        String outro = Jwt.issuer("gfin-backend").subject(new ObjectId().toHexString()).groups(Set.of("usuario")).sign();
        String natureza = "{\"nomeNaturezaConta\":\"Casa\",\"descricaoNaturezaConta\":\"Moradia\",\"codigoTipoLancamentoConta\":1}";
        String idNatureza = given().auth().oauth2(token).contentType("application/json").body(natureza)
                .post("/api/v1/natureza-conta").then().statusCode(200).extract().path("idNaturezaConta");
        String forma = "{\"nomeFormaLancamento\":\"Dinheiro\",\"codigoLancamentoConta\":1,\"codigoSituacaoFormaLancamento\":1}";
        String idForma = given().auth().oauth2(token).contentType("application/json").body(forma)
                .post("/api/v1/formas-lancamento").then().statusCode(200).extract().path("idFormaLancamento");
        String despesa = "{\"descricaoDespesaFixa\":\"Aluguel\",\"diaVencimentoDespesaFixa\":5,\"valorDespesaFixa\":123.91,\"idNaturezaContaDespesaFixa\":\"" + idNatureza + "\",\"idFormaLancamento\":\"" + idForma + "\"}";
        String id = given().auth().oauth2(token).contentType("application/json").body(despesa)
                .post("/api/v1/despesas/fixas").then().statusCode(200).extract().path("idDespesaFixa");
        given().auth().oauth2(token).get("/api/v1/despesas/fixas/" + id).then().statusCode(200).body("descricaoDespesaFixa", is("Aluguel"));
        given().auth().oauth2(outro).get("/api/v1/despesas/fixas/" + id).then().statusCode(400);
        given().auth().oauth2(token).contentType("application/json").body("{\"descricaoDespesaFixa\":\"Condominio\"}")
                .put("/api/v1/despesas/fixas/" + id).then().statusCode(200).body("descricaoDespesaFixa", is("Condominio"));
        given().auth().oauth2(token).delete("/api/v1/despesas/fixas/" + id).then().statusCode(200);
        given().auth().oauth2(token).get("/api/v1/despesas/fixas").then().statusCode(200).body("size()", is(0));
    }
}
