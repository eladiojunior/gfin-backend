package br.com.devd2;

import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Test;
import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

@QuarkusTest
class SegurancaApiTest {
    @Test void cadastroELoginSaoPublicos() {
        given().contentType("application/json").body("{}").when()
                .post("/api/v1/usuarios").then().statusCode(400);
        given().contentType("application/json").body("{}").when()
                .post("/api/v1/auth/login").then().statusCode(400);
    }

    @Test void demaisRecursosExigemAutenticacao() {
        for (String caminho : new String[]{"/usuarios", "/usuarios/1", "/natureza-conta", "/correntistas", "/contas-correntes", "/cartoes", "/despesas/fixas", "/receitas/fixas"}) {
            given().when().get("/api/v1" + caminho).then().statusCode(401);
        }
        given().contentType("application/json").body("{}").when()
                .put("/api/v1/usuarios/1").then().statusCode(401);
        given().when().delete("/api/v1/usuarios/1").then().statusCode(401);
    }

    @Test void openApiDistingueCadastroPublico() {
        given().accept("application/json").when().get("/q/openapi?format=json").then()
                .statusCode(200)
                .body("paths.'/api/v1/usuarios'.post.security", anyOf(nullValue(), empty()))
                .body("paths.'/api/v1/usuarios'.get.security[0].bearerAuth", notNullValue());
    }
    @Test void openApiDocumentaRespostasReais() {
        var json = given().accept("application/json").get("/q/openapi?format=json").then().statusCode(200).extract().jsonPath();
        org.assertj.core.api.Assertions.assertThat((Object) json.get("components.schemas.NaturezaContaModel.properties.nomeNaturezaConta")).isNotNull();
        for (String path : new String[]{"/api/v1/natureza-conta", "/api/v1/formas-lancamento", "/api/v1/despesas/fixas", "/api/v1/despesas/mensais", "/api/v1/receitas/fixas", "/api/v1/receitas/mensais"}) {
            org.assertj.core.api.Assertions.assertThat((Object) json.get("paths.'" + path + "'.get.responses.'200'.content.'application/json'.schema")).isNotNull();
        }
        org.assertj.core.api.Assertions.assertThat((Object) json.get("paths.'/api/v1/auth/login'.post.responses.'200'.content.'application/json'.schema")).isNotNull();
        org.assertj.core.api.Assertions.assertThat((String) json.get("paths.'/api/v1/despesas/mensais'.post.responses.'200'.content.'application/json'.schema.type")).isEqualTo("array");
        org.assertj.core.api.Assertions.assertThat((Object) json.get("paths.'/api/v1/despesas/mensais'.post.responses.'200'.content.'application/json'.schema.items")).isNotNull();
        org.assertj.core.api.Assertions.assertThat((Object) json.get("components.schemas.DespesaMensalModel.properties.hasDespesaLiquidada")).isNotNull();
        org.assertj.core.api.Assertions.assertThat((Object) json.get("components.schemas.ReceitaMensalModel.properties.isReceitaLiquidada")).isNotNull();
    }
}
