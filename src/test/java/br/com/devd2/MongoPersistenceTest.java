package br.com.devd2;

import static org.assertj.core.api.Assertions.assertThat;
import java.util.Map;
import java.util.UUID;
import com.mongodb.client.MongoClients;
import de.bwaldvogel.mongo.MongoServer;
import de.bwaldvogel.mongo.backend.memory.MemoryBackend;
import io.smallrye.jwt.build.Jwt;
import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;
import java.util.Set;
import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.common.QuarkusTestResourceLifecycleManager;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;
import br.com.devd2.entites.NaturezaContaEntity;
import br.com.devd2.entites.DespesaFixaEntity;
import br.com.devd2.entites.DespesaMensalEntity;
import br.com.devd2.entites.ReceitaFixaEntity;
import br.com.devd2.services.ProcessamentoRecorrenteService;
import java.time.YearMonth;
import java.math.BigDecimal;
import br.com.devd2.entites.FormaLancamentoEntity;
import br.com.devd2.entites.ReceitaMensalEntity;
import br.com.devd2.models.ReceitaMensalEdicaoModel;
import br.com.devd2.models.LiquidacaoReceitaModel;
import br.com.devd2.services.ReceitaService;

@QuarkusTest
@QuarkusTestResource(value = MongoPersistenceTest.IsolatedMongo.class, restrictToAnnotatedClass = true)
class MongoPersistenceTest {
    @Inject ReceitaService receitas;
    @Inject ProcessamentoRecorrenteService recorrentes;

    @Test void persisteConsultaAtualizaELiquida() throws Exception {
        var natureza = new NaturezaContaEntity();
        natureza.idUsuario = "507f1f77bcf86cd799439011";
        natureza.nomeNaturezaConta = "Teste";
        natureza.codigoTipoLancamentoConta = 2;
        natureza.codigoTipoSituacaoNaturezaConta = 1;
        natureza.salvar();
        assertThat(natureza.id).isNotNull();
        natureza.descricaoNaturezaConta = "Atualizada";
        natureza.salvar();
        NaturezaContaEntity carregada = NaturezaContaEntity.findById(natureza.id);
        assertThat(carregada.descricaoNaturezaConta).isEqualTo("Atualizada");
        assertThat(carregada.dataHoraRegistroNaturezaConta).isNotNull();

        var forma = new FormaLancamentoEntity(); forma.idUsuario = natureza.idUsuario; forma.codigoLancamentoConta=2; forma.codigoSituacaoFormaLancamento=1; forma.nomeFormaLancamento="Transferencia"; forma.salvar();
        var entrada = new ReceitaMensalEdicaoModel();
        entrada.idNaturezaContaReceita = natureza.id.toHexString();
        entrada.textoDescricaoReceita = "Sal?rio";
        entrada.dataRecebimentoReceita = "2026-11-12";
        entrada.valorReceita = 123.45;
        entrada.idFormaLancamento = forma.id.toHexString();
        var criada = receitas.registrarReceitaMensal(entrada, "507f1f77bcf86cd799439011");
        assertThat(receitas.listarReceitasMensais(11, 2026, "507f1f77bcf86cd799439011")).hasSize(1);
        assertThat(receitas.listarReceitasMensais(11, 2026, "507f1f77bcf86cd799439012")).isEmpty();
        var liquidacao = new LiquidacaoReceitaModel();
        liquidacao.idFormaLancamento = forma.id.toHexString();
        receitas.liquidarReceitaMensal(criada.idReceitaMensal, liquidacao, "507f1f77bcf86cd799439011");
        ReceitaMensalEntity salva = ReceitaMensalEntity.findById(new org.bson.types.ObjectId(criada.idReceitaMensal));
        assertThat(salva.isReceitaLiquidada).isTrue();
        assertThat(salva.valorReceita.toPlainString()).isEqualTo("123.45");
    }

    @Test void isolaDespesasENaturezasEntreUsuarios() {
        String a = Jwt.issuer("gfin-backend").subject("507f1f77bcf86cd799439011").groups(Set.of("usuario")).sign();
        String b = Jwt.issuer("gfin-backend").subject("507f1f77bcf86cd799439012").groups(Set.of("usuario")).sign();
        var natureza = new NaturezaContaEntity();
        natureza.idUsuario = "507f1f77bcf86cd799439011";
        natureza.nomeNaturezaConta = "Aluguel";
        natureza.codigoTipoLancamentoConta = 1;
        natureza.codigoTipoSituacaoNaturezaConta = 1;
        natureza.salvar();
        var forma = new FormaLancamentoEntity(); forma.idUsuario=natureza.idUsuario; forma.codigoLancamentoConta=1; forma.codigoSituacaoFormaLancamento=1; forma.nomeFormaLancamento="Dinheiro"; forma.salvar();
        String despesa = "{\"descricaoDespesaFixa\":\"Casa\",\"diaVencimentoDespesaFixa\":10,\"valorDespesaFixa\":100,\"idNaturezaContaDespesaFixa\":\"" + natureza.id.toHexString() + "\",\"idFormaLancamento\":\"" + forma.id.toHexString() + "\"}";
        String id = given().auth().oauth2(a).contentType("application/json").body(despesa)
            .post("/api/v1/despesas/fixas").then().statusCode(200).extract().path("idDespesaFixa");
        given().auth().oauth2(a).get("/api/v1/despesas/fixas/" + id).then().statusCode(200);
        assertThat(DespesaMensalEntity.count("idDespesaFixa = ?1 and idUsuario = ?2", id, natureza.idUsuario)).isEqualTo(1);
        given().auth().oauth2(b).get("/api/v1/despesas/fixas").then().statusCode(200).body("size()", is(0));
        given().auth().oauth2(b).get("/api/v1/despesas/fixas/" + id).then().statusCode(400);
        given().auth().oauth2(b).contentType("application/json").body(despesa).put("/api/v1/despesas/fixas/" + id).then().statusCode(400);
        given().auth().oauth2(b).delete("/api/v1/despesas/fixas/" + id).then().statusCode(400);
        given().auth().oauth2(b).contentType("application/json").body(despesa).post("/api/v1/despesas/fixas").then().statusCode(400);
        given().auth().oauth2(b).get("/api/v1/natureza-conta/" + natureza.id).then().statusCode(400);
        given().auth().oauth2(b).get("/api/v1/usuarios/507f1f77bcf86cd799439011").then().statusCode(400);
        given().auth().oauth2(a).get("/api/v1/despesas/fixas/" + id).then().statusCode(200);
    }

    @Test void mantemFormasPorUsuario() {
        String a = Jwt.issuer("gfin-backend").subject("507f1f77bcf86cd799439021").groups(Set.of("usuario")).sign();
        String b = Jwt.issuer("gfin-backend").subject("507f1f77bcf86cd799439022").groups(Set.of("usuario")).sign();
        String corpo = "{\"nomeFormaLancamento\":\"Pix\",\"codigoLancamentoConta\":1,\"codigoSituacaoFormaLancamento\":1}";
        String id = given().auth().oauth2(a).contentType("application/json").body(corpo).post("/api/v1/formas-lancamento").then().statusCode(200).extract().path("idFormaLancamento");
        given().auth().oauth2(a).get("/api/v1/formas-lancamento/" + id).then().statusCode(200).body("nomeFormaLancamento", is("Pix"));
        given().auth().oauth2(b).get("/api/v1/formas-lancamento/" + id).then().statusCode(400);
        given().auth().oauth2(b).delete("/api/v1/formas-lancamento/" + id).then().statusCode(400);
        given().auth().oauth2(a).contentType("application/json").body(corpo).put("/api/v1/formas-lancamento/" + id).then().statusCode(200);
        given().auth().oauth2(a).delete("/api/v1/formas-lancamento/" + id).then().statusCode(200);
        given().auth().oauth2(a).get("/api/v1/formas-lancamento/" + id).then().statusCode(400);
    }

    @Test void cadastroCriaDadosIniciaisIndependentes() {
        String email = "teste" + UUID.randomUUID().toString().replace("-", "") + "@exemplo.com";
        String corpo = "{\"nomeUsuario\":\"Pessoa\",\"emailUsuario\":\"" + email + "\",\"senhaUsuario\":\"Senha@123\"}";
        String id = given().contentType("application/json").body(corpo).post("/api/v1/usuarios").then().statusCode(200).extract().path("idUsuario");
        String token = Jwt.issuer("gfin-backend").subject(id).groups(Set.of("usuario")).sign();
        given().auth().oauth2(token).get("/api/v1/formas-lancamento").then().statusCode(200).body("size()", is(10));
        given().auth().oauth2(token).get("/api/v1/natureza-conta").then().statusCode(200).body("size()", is(13));
        assertThat(FormaLancamentoEntity.count("idUsuario = ?1 and codigoLancamentoConta = ?2", id, 1)).isEqualTo(5);
        assertThat(FormaLancamentoEntity.count("idUsuario = ?1 and codigoLancamentoConta = ?2", id, 2)).isEqualTo(5);
        assertThat(NaturezaContaEntity.count("idUsuario = ?1 and nomeNaturezaConta = ?2", id, "Outros")).isEqualTo(2);
        String outro = new org.bson.types.ObjectId().toHexString();
        assertThat(FormaLancamentoEntity.count("idUsuario = ?1", outro)).isZero();
    }

    @Test void geraRecorrentesUmaVezPorMesEUsuario() throws Exception {
        String usuario = new org.bson.types.ObjectId().toHexString();
        var despesa = new DespesaFixaEntity();
        despesa.idUsuario = usuario; despesa.codigoTipoSituacaoDespesaFixa = 1;
        despesa.descricaoDespesaFixa = "Aluguel"; despesa.diaVencimentoDespesaFixa = 31;
        despesa.valorDespesaFixa = new BigDecimal("125.50"); despesa.salvar();
        var mes = YearMonth.of(2028, 2);
        assertThat(recorrentes.gerarDespesa(despesa, mes)).isTrue();
        assertThat(recorrentes.gerarDespesa(despesa, mes)).isFalse();
        DespesaMensalEntity mensal = DespesaMensalEntity.find("idDespesaFixa = ?1", despesa.id.toHexString()).firstResult();
        assertThat(mensal.idUsuario).isEqualTo(usuario);
        assertThat(mensal.dataVencimentoDespesa.toLocalDate().toString()).isEqualTo("2028-02-29");
        assertThat(mensal.valorDespesa).isEqualByComparingTo("125.50");
        assertThat(recorrentes.gerarDespesa(despesa, mes.plusMonths(1))).isTrue();
        despesa.codigoTipoSituacaoDespesaFixa = 2;
        assertThat(recorrentes.gerarDespesa(despesa, mes.plusMonths(2))).isFalse();

        var receita = new ReceitaFixaEntity();
        receita.idUsuario = usuario; receita.codigoTipoSituacaoReceitaFixa = 1;
        receita.descricaoReceitaFixa = "Salario"; receita.diaRecebimentoReceitaFixa = 31;
        receita.valorReceitaFixa = new BigDecimal("500.00"); receita.salvar();
        assertThat(recorrentes.gerarReceita(receita, mes)).isTrue();
        assertThat(recorrentes.gerarReceita(receita, mes)).isFalse();
        ReceitaMensalEntity recebimento = ReceitaMensalEntity.find("idReceitaFixa = ?1", receita.id.toHexString()).firstResult();
        assertThat(recebimento.idUsuario).isEqualTo(usuario);
        assertThat(recebimento.dataRecebimentoReceita.toLocalDate().toString()).isEqualTo("2028-02-29");
        assertThat(recebimento.isReceitaLiquidada).isFalse();
        assertThat(recebimento.valorReceita).isEqualByComparingTo("500.00");
    }

    public static class IsolatedMongo implements QuarkusTestResourceLifecycleManager {
        private final String database = "gfin_test_" + UUID.randomUUID().toString().replace("-", "");
        private MongoServer server;
        private String uri;
        @Override public Map<String, String> start() {
            uri = System.getenv("GFIN_TEST_MONGODB_URI");
            if (uri == null || uri.isBlank()) {
                server = new MongoServer(new MemoryBackend());
                var address = server.bind();
                uri = "mongodb://127.0.0.1:" + address.getPort();
            }
            if (server != null) return Map.of("quarkus.mongodb.connection-string", uri, "quarkus.mongodb.database", database, "quarkus.mongodb.credentials.username", "", "quarkus.mongodb.credentials.password", "");
            return Map.of("quarkus.mongodb.connection-string", uri, "quarkus.mongodb.database", database);
        }
        @Override public void stop() {
            if (server != null) server.shutdownNow();
            else if (uri != null) try (var client = MongoClients.create(uri)) { client.getDatabase(database).drop(); }
        }
    }
}
