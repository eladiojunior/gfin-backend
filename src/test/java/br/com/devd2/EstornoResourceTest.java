package br.com.devd2;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Set;
import java.util.concurrent.*;
import org.bson.types.ObjectId;
import org.junit.jupiter.api.Test;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.common.QuarkusTestResource;
import io.smallrye.jwt.build.Jwt;
import jakarta.inject.Inject;
import br.com.devd2.entites.DespesaMensalEntity;
import br.com.devd2.entites.ReceitaMensalEntity;
import br.com.devd2.services.EstornoService;
import static org.assertj.core.api.Assertions.assertThat;

@QuarkusTest
@QuarkusTestResource(value = MongoPersistenceTest.IsolatedMongo.class, restrictToAnnotatedClass = true)
class EstornoResourceTest {
    @Inject EstornoService estornos;
    private String token(String id) { return Jwt.issuer("gfin-backend").subject(id).groups(Set.of("usuario")).sign(); }

    @Test void estornaDespesaEPreservaHistorico() {
        String usuario = new ObjectId().toHexString(), outro = new ObjectId().toHexString();
        var d = new DespesaMensalEntity();
        d.idUsuario=usuario; d.descricaoDespesa="Teste"; d.valorDespesa=new BigDecimal("100.00");
        d.dataVencimentoDespesa=LocalDateTime.of(2026, 1, 10, 0, 0); d.idNaturezaContaDespesa="categoria";
        d.idFormaLancamento="forma"; d.codigoVinculoFormaLiquidacao="vinculo"; d.hasDespesaLiquidada=true;
        d.dataHoraLiquidacaoDespesa=LocalDateTime.of(2026,1,11,12,0); d.valorDescontoLiquidacaoDespesa=new BigDecimal("2.00");
        d.valorMultaJurosLiquidacaoDespesa=new BigDecimal("1.00"); d.valorTotalLiquidacaoDespesa=new BigDecimal("99.00"); d.salvar();
        String path="/api/v1/despesas/mensais/"+d.id.toHexString();
        given().auth().oauth2(token(outro)).post(path+"/estornar").then().statusCode(400);
        given().auth().oauth2(token(usuario)).post("/api/v1/despesas/mensais/"+new ObjectId()+"/estornar").then().statusCode(400);
        given().auth().oauth2(token(usuario)).post(path+"/estornar").then().statusCode(200).body("hasDespesaLiquidada", is(false)).body("idFormaLancamento", is("forma")).body("codigoVinculoFormaLiquidacao", is("vinculo"));
        given().auth().oauth2(token(usuario)).post(path+"/estornar").then().statusCode(400);
        given().auth().oauth2(token(usuario)).get(path+"/historico-estornos").then().statusCode(200).body("size()", is(1)).body("[0].valorTotalLiquidacao", comparesEqualTo(99.0f)).body("[0].idFormaLancamento", is("forma"));
        given().auth().oauth2(token(outro)).get(path+"/historico-estornos").then().statusCode(400);
        given().auth().oauth2(token(usuario)).contentType("application/json").body("{\"descricaoDespesa\":\"Editada\"}").put(path).then().statusCode(200);
        given().auth().oauth2(token(usuario)).get(path+"/historico-estornos").then().statusCode(200).body("size()", is(1));
        given().auth().oauth2(token(usuario)).delete(path).then().statusCode(400);
        DespesaMensalEntity salva = DespesaMensalEntity.findById(d.id);
        assertThat(salva.valorDespesa).isEqualByComparingTo("100.00"); assertThat(salva.idNaturezaContaDespesa).isEqualTo("categoria");
        assertThat(salva.dataVencimentoDespesa).isEqualTo(d.dataVencimentoDespesa);
        assertThat(salva.dataHoraLiquidacaoDespesa).isNull();
    }
    @Test void estornosConcorrentesDeReceitaNaoDuplicamHistorico() throws Exception {
        String usuario = new ObjectId().toHexString();
        var r = new ReceitaMensalEntity(); r.idUsuario=usuario; r.textoDescricaoReceita="Teste"; r.valorReceita=new BigDecimal("50.00");
        r.dataRecebimentoReceita=LocalDateTime.of(2026,2,1,0,0); r.idNaturezaContaReceita="categoria"; r.idFormaLancamento="forma";
        r.isReceitaLiquidada=true; r.dataHoraLiquidacaoReceita=LocalDateTime.of(2026,2,2,0,0); r.valorTotalLiquidacaoReceita=new BigDecimal("52.00"); r.salvar();
        String caminho = "/api/v1/receitas/mensais/" + r.id.toHexString();
        given().auth().oauth2(token(new ObjectId().toHexString())).post(caminho + "/estornar").then().statusCode(400);
        given().auth().oauth2(token(usuario)).post("/api/v1/receitas/mensais/" + new ObjectId() + "/estornar").then().statusCode(400);
        var pool = Executors.newFixedThreadPool(2); var inicio = new CountDownLatch(1);
        try {
            Callable<Boolean> tarefa = () -> { inicio.await(); try { estornos.estornar(r.id.toHexString(), usuario, false); return true; } catch (br.com.devd2.infra.ServiceException e) { return false; } };
            var a=pool.submit(tarefa); var b=pool.submit(tarefa); inicio.countDown();
            assertThat((a.get() ? 1 : 0) + (b.get() ? 1 : 0)).isEqualTo(1);
        } finally { pool.shutdownNow(); }
        String path="/api/v1/receitas/mensais/"+r.id.toHexString();
        given().auth().oauth2(token(usuario)).get(path+"/historico-estornos").then().statusCode(200).body("size()", is(1));
        given().auth().oauth2(token(usuario)).post(path+"/estornar").then().statusCode(400);
        ReceitaMensalEntity salva=ReceitaMensalEntity.findById(r.id);
        assertThat(salva.isReceitaLiquidada).isFalse(); assertThat(salva.valorReceita).isEqualByComparingTo("50.00");
        assertThat(salva.idFormaLancamento).isEqualTo("forma"); assertThat(salva.dataRecebimentoReceita).isEqualTo(r.dataRecebimentoReceita);
    }
}
