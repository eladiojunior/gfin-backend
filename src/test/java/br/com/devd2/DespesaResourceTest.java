package br.com.devd2;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;
import java.util.Set;
import org.bson.types.ObjectId;
import br.com.devd2.entites.DespesaMensalEntity;
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
    @Test void divideTotalEmCentavosSemAlterarRegistrosExistentes() {
        String usuario = new ObjectId().toHexString();
        String token = Jwt.issuer("gfin-backend").subject(usuario).groups(Set.of("usuario")).sign();
        String natureza = "{\"nomeNaturezaConta\":\"Compras\",\"descricaoNaturezaConta\":\"Compras\",\"codigoTipoLancamentoConta\":1}";
        String idNatureza = given().auth().oauth2(token).contentType("application/json").body(natureza).post("/api/v1/natureza-conta").then().statusCode(200).extract().path("idNaturezaConta");
        String forma = "{\"nomeFormaLancamento\":\"Dinheiro\",\"codigoLancamentoConta\":1,\"codigoSituacaoFormaLancamento\":1}";
        String idForma = given().auth().oauth2(token).contentType("application/json").body(forma).post("/api/v1/formas-lancamento").then().statusCode(200).extract().path("idFormaLancamento");
        String base = "{\"descricaoDespesa\":\"Compra\",\"dataVencimentoDespesa\":\"2026-01-31\",\"idNaturezaContaDespesa\":\"" + idNatureza + "\",\"idFormaLancamento\":\"" + idForma + "\",";
        var existente = new br.com.devd2.entites.DespesaMensalEntity();
        existente.idUsuario = usuario; existente.descricaoDespesa = "Antiga"; existente.valorDespesa = new java.math.BigDecimal("100.00"); existente.salvar();
        var valores = given().auth().oauth2(token).contentType("application/json").body(base + "\"valorDespesa\":100.00,\"numeroParcelas\":3}")
            .post("/api/v1/despesas/mensais").then().statusCode(200).extract().jsonPath().getList("valorDespesa", Double.class);
        org.assertj.core.api.Assertions.assertThat(valores).containsExactly(33.33, 33.33, 33.34);
        org.assertj.core.api.Assertions.assertThat(valores.stream().map(java.math.BigDecimal::valueOf).reduce(java.math.BigDecimal.ZERO, java.math.BigDecimal::add)).isEqualByComparingTo("100.00");
        var exatas = given().auth().oauth2(token).contentType("application/json").body(base + "\"valorDespesa\":90.00,\"numeroParcelas\":3}")
            .post("/api/v1/despesas/mensais").then().statusCode(200).extract().jsonPath().getList("valorDespesa", Double.class);
        org.assertj.core.api.Assertions.assertThat(exatas).containsExactly(30.0, 30.0, 30.0);
        DespesaMensalEntity antiga = DespesaMensalEntity.findById(existente.id);
        org.assertj.core.api.Assertions.assertThat(antiga.valorDespesa).isEqualByComparingTo("100.00");
        given().auth().oauth2(token).contentType("application/json").body(base + "\"valorDespesa\":1.001,\"numeroParcelas\":3}").post("/api/v1/despesas/mensais").then().statusCode(400);
    }
}
