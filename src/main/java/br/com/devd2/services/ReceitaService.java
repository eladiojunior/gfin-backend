package br.com.devd2.services;

import static br.com.devd2.helper.Identificadores.oid;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import br.com.devd2.entites.HistoricoReceitaFixaEntity;
import br.com.devd2.entites.NaturezaContaEntity;
import br.com.devd2.entites.ReceitaFixaEntity;
import br.com.devd2.entites.ReceitaMensalEntity;
import br.com.devd2.enums.TipoSituacaoEnum;
import br.com.devd2.helper.MapperHelper;
import br.com.devd2.infra.ServiceException;
import br.com.devd2.models.LiquidacaoReceitaModel;
import br.com.devd2.models.ReceitaFixaEdicaoModel;
import br.com.devd2.models.ReceitaFixaModel;
import br.com.devd2.models.ReceitaMensalEdicaoModel;
import br.com.devd2.models.ReceitaMensalModel;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class ReceitaService {
    @jakarta.inject.Inject FormaLancamentoService formas;
    @jakarta.inject.Inject EstornoService estornos;
    @jakarta.inject.Inject ProcessamentoRecorrenteService processamentoRecorrente;

    // -------------------------------------------------------------------------
    // Receita Fixa
    // -------------------------------------------------------------------------

    public List<ReceitaFixaModel> listarReceitasFixas(String idUsuario) {
        return ReceitaFixaEntity.<ReceitaFixaEntity>find(
            "codigoTipoSituacaoReceitaFixa = ?1 AND idUsuario = ?2",
            TipoSituacaoEnum.Ativo.getCodigo(), idUsuario)
            .stream()
            .map(MapperHelper.INSTANCE::toReceitaFixaModel)
            .collect(Collectors.toList());
    }

    public ReceitaFixaModel obterReceitaFixa(String id, String idUsuario) throws ServiceException {
        ReceitaFixaEntity entity = ReceitaFixaEntity.find("{ \'_id\': ?1, \'idUsuario\': ?2 }", oid(id), idUsuario).firstResult();
        if (entity == null) {
            throw new ServiceException("Receita fixa não identificada pelo ID %s.".formatted(id));
        }
        return MapperHelper.INSTANCE.toReceitaFixaModel(entity);
    }

    public ReceitaFixaModel registrarReceitaFixa(ReceitaFixaEdicaoModel model, String idUsuario) throws ServiceException {
        verificarRegras_ReceitaFixa(model, idUsuario);

        var entity = MapperHelper.INSTANCE.toReceitaFixaEntity(model);
        entity.idUsuario = idUsuario;
        entity.codigoTipoSituacaoReceitaFixa = TipoSituacaoEnum.Ativo.getCodigo();
        entity.salvar();

        var historico = new HistoricoReceitaFixaEntity();
        historico.idUsuario = idUsuario;
            historico.idReceitaFixa = entity.id.toHexString();
        historico.valorHistoricoReceitaFixa = entity.valorReceitaFixa;
        historico.salvar();
        processamentoRecorrente.processarNovaReceita(entity);

        return MapperHelper.INSTANCE.toReceitaFixaModel(entity);
    }

    public ReceitaFixaModel alterarReceitaFixa(String id, ReceitaFixaEdicaoModel model, boolean hasGravarHistorico, String idUsuario) throws ServiceException {
        ReceitaFixaEntity entity = ReceitaFixaEntity.find("{ \'_id\': ?1, \'idUsuario\': ?2 }", oid(id), idUsuario).firstResult();
        if (entity == null) {
            throw new ServiceException("Receita fixa não identificada pelo ID %s.".formatted(id));
        }

        if (model.descricaoReceitaFixa != null && !model.descricaoReceitaFixa.trim().isEmpty()) {
            entity.descricaoReceitaFixa = model.descricaoReceitaFixa;
        }
        if (model.diaRecebimentoReceitaFixa > 0) {
            if (model.diaRecebimentoReceitaFixa < 1 || model.diaRecebimentoReceitaFixa > 31) {
                throw new ServiceException("Dia de recebimento (%s) inválido.".formatted(model.diaRecebimentoReceitaFixa));
            }
            entity.diaRecebimentoReceitaFixa = model.diaRecebimentoReceitaFixa;
        }
        if (model.idNaturezaContaReceitaFixa != null) {
            verificarNaturezaConta(model.idNaturezaContaReceitaFixa, idUsuario);
            entity.idNaturezaContaReceitaFixa = model.idNaturezaContaReceitaFixa;
        }
        if (model.valorReceitaFixa > 0) {
            entity.valorReceitaFixa = BigDecimal.valueOf(model.valorReceitaFixa);
        }

        if (hasGravarHistorico) {
            var historico = new HistoricoReceitaFixaEntity();
            historico.idUsuario = idUsuario;
            historico.idReceitaFixa = entity.id.toHexString();
            historico.valorHistoricoReceitaFixa = entity.valorReceitaFixa;
            historico.salvar();
        }

        entity.salvar();
        return MapperHelper.INSTANCE.toReceitaFixaModel(entity);
    }

    public void excluirReceitaFixa(String id, String idUsuario) throws ServiceException {
        ReceitaFixaEntity entity = ReceitaFixaEntity.find("{ \'_id\': ?1, \'idUsuario\': ?2 }", oid(id), idUsuario).firstResult();
        if (entity == null) {
            throw new ServiceException("Receita fixa não identificada pelo ID %s.".formatted(id));
        }

        long vinculosMensais = ReceitaMensalEntity.count("idReceitaFixa = ?1 and idUsuario = ?2", id, idUsuario);
        if (vinculosMensais > 0) {
            entity.codigoTipoSituacaoReceitaFixa = TipoSituacaoEnum.Inativo.getCodigo();
            entity.salvar();
        } else {
            HistoricoReceitaFixaEntity.delete("idReceitaFixa = ?1 and idUsuario = ?2", id, idUsuario);
            entity.delete();
        }
    }

    // -------------------------------------------------------------------------
    // Receita Mensal
    // -------------------------------------------------------------------------

    public List<ReceitaMensalModel> listarReceitasMensais(int mes, int ano, String idUsuario) {
        LocalDateTime inicio = LocalDateTime.of(ano, mes, 1, 0, 0, 0);
        LocalDateTime fim = inicio.plusMonths(1);

        return ReceitaMensalEntity.<ReceitaMensalEntity>find(
            "{ 'dataRecebimentoReceita': { '$gte': ?1, '$lt': ?2 }, 'idUsuario': ?3 }",
            inicio, fim, idUsuario)
            .stream()
            .map(MapperHelper.INSTANCE::toReceitaMensalModel)
            .collect(Collectors.toList());
    }

    public ReceitaMensalModel obterReceitaMensal(String id, String idUsuario) throws ServiceException {
        ReceitaMensalEntity entity = ReceitaMensalEntity.find("{ \'_id\': ?1, \'idUsuario\': ?2 }", oid(id), idUsuario).firstResult();
        if (entity == null) {
            throw new ServiceException("Receita mensal não identificada pelo ID %s.".formatted(id));
        }
        return MapperHelper.INSTANCE.toReceitaMensalModel(entity);
    }

    public ReceitaMensalModel registrarReceitaMensal(ReceitaMensalEdicaoModel model, String idUsuario) throws ServiceException {
        verificarRegras_ReceitaMensal(model, idUsuario);

        var entity = MapperHelper.INSTANCE.toReceitaMensalEntity(model);
        entity.idUsuario = idUsuario;
        entity.dataRecebimentoReceita = converterData(model.dataRecebimentoReceita);
        entity.valorReceita = BigDecimal.valueOf(model.valorReceita);
        entity.isReceitaLiquidada = false;
        entity.salvar();

        return MapperHelper.INSTANCE.toReceitaMensalModel(entity);
    }

    public ReceitaMensalModel alterarReceitaMensal(String id, ReceitaMensalEdicaoModel model, String idUsuario) throws ServiceException {
        ReceitaMensalEntity entity = ReceitaMensalEntity.find("{ \'_id\': ?1, \'idUsuario\': ?2 }", oid(id), idUsuario).firstResult();
        if (entity == null) {
            throw new ServiceException("Receita mensal não identificada pelo ID %s.".formatted(id));
        }
        if (entity.isReceitaLiquidada) {
            throw new ServiceException("Receita mensal (ID: %s) já liquidada não pode ser alterada.".formatted(id));
        }

        if (model.textoDescricaoReceita != null && !model.textoDescricaoReceita.trim().isEmpty()) {
            entity.textoDescricaoReceita = model.textoDescricaoReceita;
        }
        if (model.dataRecebimentoReceita != null && !model.dataRecebimentoReceita.trim().isEmpty()) {
            entity.dataRecebimentoReceita = converterData(model.dataRecebimentoReceita);
        }
        if (model.valorReceita > 0) {
            entity.valorReceita = BigDecimal.valueOf(model.valorReceita);
        }
        if (model.idFormaLancamento != null) {
            formas.validar(model.idFormaLancamento, idUsuario, 2);
            entity.idFormaLancamento = model.idFormaLancamento;
        }

        estornos.atualizarReceita(entity);
        return MapperHelper.INSTANCE.toReceitaMensalModel(entity);
    }

    public ReceitaMensalModel liquidarReceitaMensal(String id, LiquidacaoReceitaModel liquidacao, String idUsuario) throws ServiceException {
        ReceitaMensalEntity entity = ReceitaMensalEntity.find("{ \'_id\': ?1, \'idUsuario\': ?2 }", oid(id), idUsuario).firstResult();
        if (entity == null) {
            throw new ServiceException("Receita mensal não identificada pelo ID %s.".formatted(id));
        }
        if (entity.isReceitaLiquidada) {
            throw new ServiceException("Receita mensal (ID: %s) já está liquidada.".formatted(id));
        }
        formas.validar(liquidacao.idFormaLancamento, idUsuario, 2);

        BigDecimal total = liquidacao.valorTotal > 0 ? BigDecimal.valueOf(liquidacao.valorTotal) : entity.valorReceita;
        estornos.liquidarReceita(id, idUsuario, liquidacao.idFormaLancamento, total);
        return obterReceitaMensal(id, idUsuario);
    }

    public void excluirReceitaMensal(String id, String idUsuario) throws ServiceException {
        ReceitaMensalEntity entity = ReceitaMensalEntity.find("{ \'_id\': ?1, \'idUsuario\': ?2 }", oid(id), idUsuario).firstResult();
        if (entity == null) {
            throw new ServiceException("Receita mensal não identificada pelo ID %s.".formatted(id));
        }
        if (entity.isReceitaLiquidada) {
            throw new ServiceException("Receita mensal (ID: %s) já liquidada não pode ser excluída.".formatted(id));
        }
        estornos.excluirPendente(id, idUsuario, false);
    }

    // -------------------------------------------------------------------------
    // Validações internas
    // -------------------------------------------------------------------------

    private void verificarRegras_ReceitaFixa(ReceitaFixaEdicaoModel model, String idUsuario) throws ServiceException {
        if (model == null) {
            throw new ServiceException("Receita fixa informada nula.");
        }
        if (model.descricaoReceitaFixa == null || model.descricaoReceitaFixa.trim().isEmpty()) {
            throw new ServiceException("Descrição da receita fixa não informada.");
        }
        if (model.diaRecebimentoReceitaFixa < 1 || model.diaRecebimentoReceitaFixa > 31) {
            throw new ServiceException("Dia de recebimento (%s) inválido.".formatted(model.diaRecebimentoReceitaFixa));
        }
        if (model.valorReceitaFixa <= 0) {
            throw new ServiceException("Valor da receita fixa não informado ou negativo.");
        }
        verificarNaturezaConta(model.idNaturezaContaReceitaFixa, idUsuario);
    }

    private void verificarRegras_ReceitaMensal(ReceitaMensalEdicaoModel model, String idUsuario) throws ServiceException {
        if (model == null) {
            throw new ServiceException("Receita mensal informada nula.");
        }
        if (model.textoDescricaoReceita == null || model.textoDescricaoReceita.trim().isEmpty()) {
            throw new ServiceException("Descrição da receita não informada.");
        }
        if (model.valorReceita <= 0) {
            throw new ServiceException("Valor da receita não informado ou negativo.");
        }
        if (model.dataRecebimentoReceita == null || model.dataRecebimentoReceita.trim().isEmpty()) {
            throw new ServiceException("Data de recebimento da receita não informada.");
        }
        formas.validar(model.idFormaLancamento, idUsuario, 2);
        verificarNaturezaConta(model.idNaturezaContaReceita, idUsuario);

        if (model.idReceitaFixa != null) {
            ReceitaFixaEntity rf = ReceitaFixaEntity.find("{ '_id': ?1, 'idUsuario': ?2 }", oid(model.idReceitaFixa), idUsuario).firstResult();
            if (rf == null) {
                throw new ServiceException("Receita fixa vinculada (ID: %s) não encontrada.".formatted(model.idReceitaFixa));
            }
        }
    }

    private void verificarNaturezaConta(String idNaturezaConta, String idUsuario) throws ServiceException {
        NaturezaContaEntity natureza = NaturezaContaEntity.find("{ '_id': ?1, 'idUsuario': ?2 }", oid(idNaturezaConta), idUsuario).firstResult();
        if (natureza == null) {
            throw new ServiceException("Natureza de conta (ID: %s) não existe.".formatted(idNaturezaConta));
        }
        if (natureza.codigoTipoSituacaoNaturezaConta == TipoSituacaoEnum.Inativo.getCodigo()) {
            throw new ServiceException("Natureza de conta (ID: %s) está inativa.".formatted(idNaturezaConta));
        }
    }

    private LocalDateTime converterData(String data) throws ServiceException {
        try {
            return LocalDateTime.parse(data + "T00:00:00");
        } catch (Exception e) {
            throw new ServiceException("Data '%s' inválida. Use o formato yyyy-MM-dd.".formatted(data));
        }
    }
}
