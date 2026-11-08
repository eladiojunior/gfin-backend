package br.com.devd2.services;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.YearMonth;
import java.time.ZoneId;
import java.time.LocalDateTime;
import java.util.Arrays;

import org.bson.types.ObjectId;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mongodb.MongoWriteException;
import com.mongodb.ErrorCategory;
import br.com.devd2.entites.DespesaFixaEntity;
import br.com.devd2.entites.DespesaMensalEntity;
import br.com.devd2.entites.ReceitaFixaEntity;
import br.com.devd2.entites.ReceitaMensalEntity;
import io.quarkus.scheduler.Scheduled;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class ProcessamentoRecorrenteService {
    private static final Logger log = LoggerFactory.getLogger(ProcessamentoRecorrenteService.class);

    @ConfigProperty(name = "gfin.processamento.fuso", defaultValue = "America/Sao_Paulo")
    String fuso;

    public YearMonth mesCorrente() { return YearMonth.now(ZoneId.of(fuso)); }

    @Scheduled(every = "${gfin.processamento.intervalo:30m}", concurrentExecution = Scheduled.ConcurrentExecution.SKIP)
    void executarAgendado() {
        YearMonth mes = mesCorrente();
        log.info("Processamento recorrente iniciado: competencia={}", mes);
        int[] resultado = new int[3];
        try {
            DespesaFixaEntity.<DespesaFixaEntity>find("codigoTipoSituacaoDespesaFixa = ?1", 1).stream()
                .forEach(fixa -> processarDespesaComLog(fixa, mes, resultado));
            ReceitaFixaEntity.<ReceitaFixaEntity>find("codigoTipoSituacaoReceitaFixa = ?1", 1).stream()
                .forEach(fixa -> processarReceitaComLog(fixa, mes, resultado));
            log.info("Processamento recorrente concluido: competencia={}, criados={}, existentes={}, falhas={}",
                mes, resultado[0], resultado[1], resultado[2]);
        } catch (Exception erro) {
            log.error("Processamento recorrente interrompido: competencia={}", mes, erro);
        }
    }

    public void processarNovaDespesa(DespesaFixaEntity fixa) { processarDespesaComLog(fixa, mesCorrente(), new int[3]); }
    public void processarNovaReceita(ReceitaFixaEntity fixa) { processarReceitaComLog(fixa, mesCorrente(), new int[3]); }

    private void processarDespesaComLog(DespesaFixaEntity fixa, YearMonth mes, int[] resultado) {
        try {
            boolean criada = gerarDespesa(fixa, mes);
            resultado[criada ? 0 : 1]++;
            log.info("Despesa fixa processada: id={}, competencia={}, resultado={}", fixa.id, mes, criada ? "criada" : "existente");
        } catch (Exception erro) { resultado[2]++; log.error("Falha na despesa fixa: id={}, competencia={}", fixa.id, mes, erro); }
    }

    private void processarReceitaComLog(ReceitaFixaEntity fixa, YearMonth mes, int[] resultado) {
        try {
            boolean criada = gerarReceita(fixa, mes);
            resultado[criada ? 0 : 1]++;
            log.info("Receita fixa processada: id={}, competencia={}, resultado={}", fixa.id, mes, criada ? "criada" : "existente");
        } catch (Exception erro) { resultado[2]++; log.error("Falha na receita fixa: id={}, competencia={}", fixa.id, mes, erro); }
    }

    public boolean gerarDespesa(DespesaFixaEntity fixa, YearMonth mes) {
        if (fixa.codigoTipoSituacaoDespesaFixa != 1) return false;
        validar(fixa.id, fixa.idUsuario, fixa.diaVencimentoDespesaFixa);
        String idFixa = fixa.id.toHexString();
        LocalDateTime inicio = mes.atDay(1).atStartOfDay(), fim = mes.plusMonths(1).atDay(1).atStartOfDay();
        if (DespesaMensalEntity.count("{ 'idUsuario': ?1, 'idDespesaFixa': ?2, 'dataVencimentoDespesa': { '$gte': ?3, '$lt': ?4 } }", fixa.idUsuario, idFixa, inicio, fim) > 0) return false;
        var mensal = new DespesaMensalEntity();
        mensal.id = identificador("despesa", fixa.idUsuario, idFixa, mes);
        mensal.idUsuario = fixa.idUsuario;
        mensal.idDespesaFixa = idFixa;
        mensal.idNaturezaContaDespesa = fixa.idNaturezaContaDespesaFixa;
        mensal.descricaoDespesa = fixa.descricaoDespesaFixa;
        mensal.dataVencimentoDespesa = mes.atDay(Math.min(fixa.diaVencimentoDespesaFixa, mes.lengthOfMonth())).atStartOfDay();
        mensal.valorDespesa = fixa.valorDespesaFixa;
        mensal.idFormaLancamento = fixa.idFormaLancamento;
        try { mensal.persist(); return true; } catch (MongoWriteException erro) { if (erro.getError().getCategory() == ErrorCategory.DUPLICATE_KEY) return false; throw erro; }
    }

    public boolean gerarReceita(ReceitaFixaEntity fixa, YearMonth mes) {
        if (fixa.codigoTipoSituacaoReceitaFixa != 1) return false;
        validar(fixa.id, fixa.idUsuario, fixa.diaRecebimentoReceitaFixa);
        String idFixa = fixa.id.toHexString();
        LocalDateTime inicio = mes.atDay(1).atStartOfDay(), fim = mes.plusMonths(1).atDay(1).atStartOfDay();
        if (ReceitaMensalEntity.count("{ 'idUsuario': ?1, 'idReceitaFixa': ?2, 'dataRecebimentoReceita': { '$gte': ?3, '$lt': ?4 } }", fixa.idUsuario, idFixa, inicio, fim) > 0) return false;
        var mensal = new ReceitaMensalEntity();
        mensal.id = identificador("receita", fixa.idUsuario, idFixa, mes);
        mensal.idUsuario = fixa.idUsuario;
        mensal.idReceitaFixa = idFixa;
        mensal.idNaturezaContaReceita = fixa.idNaturezaContaReceitaFixa;
        mensal.textoDescricaoReceita = fixa.descricaoReceitaFixa;
        mensal.dataRecebimentoReceita = mes.atDay(Math.min(fixa.diaRecebimentoReceitaFixa, mes.lengthOfMonth())).atStartOfDay();
        mensal.valorReceita = fixa.valorReceitaFixa;
        try { mensal.persist(); return true; } catch (MongoWriteException erro) { if (erro.getError().getCategory() == ErrorCategory.DUPLICATE_KEY) return false; throw erro; }
    }

    private void validar(ObjectId id, String usuario, int dia) {
        if (id == null || usuario == null || usuario.isBlank() || dia < 1 || dia > 31) throw new IllegalArgumentException("Lancamento fixo sem identificador, usuario ou dia valido");
    }

    private ObjectId identificador(String tipo, String usuario, String fixa, YearMonth mes) {
        try {
            byte[] hash = MessageDigest.getInstance("SHA-256").digest((tipo + ":" + usuario + ":" + fixa + ":" + mes).getBytes(StandardCharsets.UTF_8));
            return new ObjectId(Arrays.copyOf(hash, 12));
        } catch (NoSuchAlgorithmException erro) { throw new IllegalStateException(erro); }
    }
}
