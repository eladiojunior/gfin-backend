package br.com.devd2.services;

import static br.com.devd2.helper.Identificadores.oid;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import br.com.devd2.entites.CartaoCreditoEntity;
import br.com.devd2.entites.ContaCorrenteEntity;
import br.com.devd2.entites.DespesaFixaEntity;
import br.com.devd2.entites.DespesaMensalEntity;
import br.com.devd2.entites.HistoricoDespesaFixaEntity;
import br.com.devd2.entites.NaturezaContaEntity;
import br.com.devd2.enums.TipoSituacaoEnum;
import br.com.devd2.helper.MapperHelper;
import br.com.devd2.infra.ServiceException;
import br.com.devd2.models.DespesaFixaEdicaoModel;
import br.com.devd2.models.DespesaFixaModel;
import br.com.devd2.models.DespesaMensalEdicaoModel;
import br.com.devd2.models.DespesaMensalModel;
import br.com.devd2.models.LiquidacaoDespesaModel;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class DespesaService {
    @jakarta.inject.Inject FormaLancamentoService formas;
    @jakarta.inject.Inject EstornoService estornos;
    @jakarta.inject.Inject ProcessamentoRecorrenteService processamentoRecorrente;

    // -------------------------------------------------------------------------
    // Despesa Fixa
    // -------------------------------------------------------------------------

    public List<DespesaFixaModel> listarDespesasFixas(String idUsuario) {
        return DespesaFixaEntity.<DespesaFixaEntity>find(
            "codigoTipoSituacaoDespesaFixa = ?1 AND idUsuario = ?2",
            TipoSituacaoEnum.Ativo.getCodigo(), idUsuario)
            .stream()
            .map(MapperHelper.INSTANCE::toDespesaFixaModel)
            .collect(Collectors.toList());
    }

    public DespesaFixaModel obterDespesaFixa(String id, String idUsuario) throws ServiceException {
        DespesaFixaEntity entity = DespesaFixaEntity.find("{ \'_id\': ?1, \'idUsuario\': ?2 }", oid(id), idUsuario).firstResult();
        if (entity == null) {
            throw new ServiceException("Despesa fixa não identificada pelo ID %s.".formatted(id));
        }
        return MapperHelper.INSTANCE.toDespesaFixaModel(entity);
    }

    public DespesaFixaModel registrarDespesaFixa(DespesaFixaEdicaoModel despesaFixa, String idUsuario) throws ServiceException {
        verificarRegras_DespesaFixa(despesaFixa, idUsuario);
        formas.validar(despesaFixa.idFormaLancamento, idUsuario, 1);

        var entity = MapperHelper.INSTANCE.toDespesaFixaEntity(despesaFixa);
        entity.idUsuario = idUsuario;
        entity.codigoTipoSituacaoDespesaFixa = TipoSituacaoEnum.Ativo.getCodigo();
        entity.salvar();

        var historico = new HistoricoDespesaFixaEntity();
        historico.idUsuario = idUsuario;
            historico.idDespesaFixa = entity.id.toHexString();
        historico.valorHistoricoDespesaFixa = entity.valorDespesaFixa;
        historico.salvar();
        processamentoRecorrente.processarNovaDespesa(entity);

        return MapperHelper.INSTANCE.toDespesaFixaModel(entity);
    }

    public DespesaFixaModel alterarDespesaFixa(String id, DespesaFixaEdicaoModel despesaFixa, boolean hasGravarHistorico, String idUsuario) throws ServiceException {
        DespesaFixaEntity entity = DespesaFixaEntity.find("{ \'_id\': ?1, \'idUsuario\': ?2 }", oid(id), idUsuario).firstResult();
        if (entity == null) {
            throw new ServiceException("Despesa fixa não identificada pelo ID %s.".formatted(id));
        }

        if (despesaFixa.idFormaLancamento != null) {
            formas.validar(despesaFixa.idFormaLancamento, idUsuario, 1);
            entity.idFormaLancamento = despesaFixa.idFormaLancamento;
        }
        if (despesaFixa.diaVencimentoDespesaFixa != 0) {
            if (despesaFixa.diaVencimentoDespesaFixa < 1 || despesaFixa.diaVencimentoDespesaFixa > 31) {
                throw new ServiceException("Dia de vencimento (%s) inválido.".formatted(despesaFixa.diaVencimentoDespesaFixa));
            }
            entity.diaVencimentoDespesaFixa = despesaFixa.diaVencimentoDespesaFixa;
        }
        if (despesaFixa.idNaturezaContaDespesaFixa != null) {
            verificarRegras_NaturezaConta(despesaFixa.idNaturezaContaDespesaFixa, idUsuario);
            entity.idNaturezaContaDespesaFixa = despesaFixa.idNaturezaContaDespesaFixa;
        }
        if (despesaFixa.descricaoDespesaFixa != null && !despesaFixa.descricaoDespesaFixa.trim().isEmpty()) {
            entity.descricaoDespesaFixa = despesaFixa.descricaoDespesaFixa;
        }
        if (despesaFixa.valorDespesaFixa > 0) {
            entity.valorDespesaFixa = BigDecimal.valueOf(despesaFixa.valorDespesaFixa);
        }

        if (hasGravarHistorico) {
            var historico = new HistoricoDespesaFixaEntity();
            historico.idUsuario = idUsuario;
            historico.idDespesaFixa = entity.id.toHexString();
            historico.valorHistoricoDespesaFixa = entity.valorDespesaFixa;
            historico.salvar();
        }

        entity.salvar();
        return MapperHelper.INSTANCE.toDespesaFixaModel(entity);
    }

    public void excluirDespesaFixa(String id, String idUsuario) throws ServiceException {
        DespesaFixaEntity entity = DespesaFixaEntity.find("{ \'_id\': ?1, \'idUsuario\': ?2 }", oid(id), idUsuario).firstResult();
        if (entity == null) {
            throw new ServiceException("Despesa fixa não identificada pelo ID %s.".formatted(id));
        }

        boolean semVinculos = semVinculosDespesaFixa(entity);
        if (semVinculos) {
            HistoricoDespesaFixaEntity.delete("idDespesaFixa = ?1 and idUsuario = ?2", id, idUsuario);
            entity.delete();
        } else {
            entity.codigoTipoSituacaoDespesaFixa = TipoSituacaoEnum.Inativo.getCodigo();
            entity.salvar();
        }
    }

    private boolean semVinculosDespesaFixa(DespesaFixaEntity despesaFixa) throws ServiceException {
        if (despesaFixa == null) {
            throw new ServiceException("Despesa fixa nula, não será possível verificar seus vínculos.");
        }
        long count = DespesaMensalEntity.count("idDespesaFixa = ?1 and idUsuario = ?2", despesaFixa.id.toHexString(), despesaFixa.idUsuario);
        return count == 0;
    }

    // -------------------------------------------------------------------------
    // Despesa Mensal
    // -------------------------------------------------------------------------

    public List<DespesaMensalModel> listarDespesasMensais(int mes, int ano, String idUsuario) {
        LocalDateTime inicio = LocalDateTime.of(ano, mes, 1, 0, 0, 0);
        LocalDateTime fim = inicio.plusMonths(1);

        return DespesaMensalEntity.<DespesaMensalEntity>find(
            "{ 'dataVencimentoDespesa': { '$gte': ?1, '$lt': ?2 }, 'idUsuario': ?3 }",
            inicio, fim, idUsuario)
            .stream()
            .map(MapperHelper.INSTANCE::toDespesaMensalModel)
            .collect(Collectors.toList());
    }

    public DespesaMensalModel obterDespesaMensal(String id, String idUsuario) throws ServiceException {
        DespesaMensalEntity entity = DespesaMensalEntity.find("{ \'_id\': ?1, \'idUsuario\': ?2 }", oid(id), idUsuario).firstResult();
        if (entity == null) {
            throw new ServiceException("Despesa mensal não identificada pelo ID %s.".formatted(id));
        }
        return MapperHelper.INSTANCE.toDespesaMensalModel(entity);
    }

    public List<DespesaMensalModel> registrarDespesaMensal(DespesaMensalEdicaoModel model, String idUsuario) throws ServiceException {
        verificarRegras_DespesaMensal(model, idUsuario);
        verificarVinculo(model.idFormaLancamento, model.codigoVinculoFormaLiquidacao, idUsuario);

        int numeroParcelas = model.numeroParcelas <= 1 ? 1 : model.numeroParcelas;
        boolean isParcelada = numeroParcelas > 1;
        BigDecimal total;
        try {
            total = BigDecimal.valueOf(model.valorDespesa).setScale(2, java.math.RoundingMode.UNNECESSARY);
        } catch (ArithmeticException | NumberFormatException erro) {
            throw new ServiceException("Valor da despesa deve ter no máximo duas casas decimais.");
        }
        BigDecimal valorParcela;
        // Divide centavos inteiros, não unidades monetárias inteiras.
        java.math.BigInteger centavos = total.movePointRight(2).toBigIntegerExact();
        java.math.BigInteger[] divisao = centavos.divideAndRemainder(java.math.BigInteger.valueOf(numeroParcelas));
        valorParcela = new BigDecimal(divisao[0], 2);
        BigDecimal valorUltimaParcela = new BigDecimal(divisao[0].add(divisao[1]), 2);

        // Código único para agrupar as parcelas
        Integer codigoParcelado = isParcelada ? (int) (System.currentTimeMillis() / 1000) : null;

        LocalDateTime dataBase = converterData(model.dataVencimentoDespesa);
        List<DespesaMensalEntity> geradas = new ArrayList<>();

        for (int i = 1; i <= numeroParcelas; i++) {
            var entity = new DespesaMensalEntity();
            entity.idUsuario = idUsuario;
            entity.idDespesaFixa = model.idDespesaFixa;
            entity.idNaturezaContaDespesa = model.idNaturezaContaDespesa;
            entity.hasDespesaParcelada = isParcelada;
            entity.codigoDespesaParcelada = codigoParcelado;
            entity.descricaoDespesa = isParcelada
                ? "%s (%d/%d)".formatted(model.descricaoDespesa, i, numeroParcelas)
                : model.descricaoDespesa;
            entity.dataVencimentoDespesa = dataBase.plusMonths(i - 1);
            entity.valorDespesa = i == numeroParcelas ? valorUltimaParcela : valorParcela;
            entity.idFormaLancamento = model.idFormaLancamento;
            entity.codigoVinculoFormaLiquidacao = model.codigoVinculoFormaLiquidacao;
            entity.textoObservacaoDespesa = model.textoObservacaoDespesa;
            entity.hasDespesaLiquidada = false;
            entity.salvar();
            geradas.add(entity);
        }

        return geradas.stream()
            .map(MapperHelper.INSTANCE::toDespesaMensalModel)
            .collect(Collectors.toList());
    }

    public DespesaMensalModel alterarDespesaMensal(String id, DespesaMensalEdicaoModel model, String idUsuario) throws ServiceException {
        DespesaMensalEntity entity = DespesaMensalEntity.find("{ \'_id\': ?1, \'idUsuario\': ?2 }", oid(id), idUsuario).firstResult();
        if (entity == null) {
            throw new ServiceException("Despesa mensal não identificada pelo ID %s.".formatted(id));
        }
        if (entity.hasDespesaLiquidada) {
            throw new ServiceException("Despesa mensal (ID: %s) já liquidada não pode ser alterada.".formatted(id));
        }

        if (model.descricaoDespesa != null && !model.descricaoDespesa.trim().isEmpty()) {
            entity.descricaoDespesa = model.descricaoDespesa;
        }
        if (model.dataVencimentoDespesa != null && !model.dataVencimentoDespesa.trim().isEmpty()) {
            entity.dataVencimentoDespesa = converterData(model.dataVencimentoDespesa);
        }
        if (model.valorDespesa > 0) {
            entity.valorDespesa = BigDecimal.valueOf(model.valorDespesa);
        }
        if (model.idFormaLancamento != null) {
            formas.validar(model.idFormaLancamento, idUsuario, 1);
            entity.idFormaLancamento = model.idFormaLancamento;
        }
        if (model.idNaturezaContaDespesa != null) {
            verificarRegras_NaturezaConta(model.idNaturezaContaDespesa, idUsuario);
            entity.idNaturezaContaDespesa = model.idNaturezaContaDespesa;
        }
        if (model.textoObservacaoDespesa != null) {
            entity.textoObservacaoDespesa = model.textoObservacaoDespesa;
        }
        verificarVinculo(entity.idFormaLancamento, model.codigoVinculoFormaLiquidacao, idUsuario);
        entity.codigoVinculoFormaLiquidacao = model.codigoVinculoFormaLiquidacao;

        estornos.atualizarDespesa(entity);
        return MapperHelper.INSTANCE.toDespesaMensalModel(entity);
    }

    public DespesaMensalModel liquidarDespesaMensal(String id, LiquidacaoDespesaModel liquidacao, String idUsuario) throws ServiceException {
        DespesaMensalEntity entity = DespesaMensalEntity.find("{ \'_id\': ?1, \'idUsuario\': ?2 }", oid(id), idUsuario).firstResult();
        if (entity == null) {
            throw new ServiceException("Despesa mensal não identificada pelo ID %s.".formatted(id));
        }
        if (entity.hasDespesaLiquidada) {
            throw new ServiceException("Despesa mensal (ID: %s) já está liquidada.".formatted(id));
        }
        formas.validar(liquidacao.idFormaLancamento, idUsuario, 1);

        verificarVinculo(liquidacao.idFormaLancamento, liquidacao.codigoVinculoFormaLiquidacao, idUsuario);
        BigDecimal desconto = BigDecimal.valueOf(liquidacao.valorDesconto);
        BigDecimal multaJuros = BigDecimal.valueOf(liquidacao.valorMultaJuros);
        estornos.liquidarDespesa(id, idUsuario, liquidacao.idFormaLancamento, liquidacao.codigoVinculoFormaLiquidacao, desconto, multaJuros, entity.valorDespesa);
        return obterDespesaMensal(id, idUsuario);
    }

    public void excluirDespesaMensal(String id, String idUsuario) throws ServiceException {
        DespesaMensalEntity entity = DespesaMensalEntity.find("{ \'_id\': ?1, \'idUsuario\': ?2 }", oid(id), idUsuario).firstResult();
        if (entity == null) {
            throw new ServiceException("Despesa mensal não identificada pelo ID %s.".formatted(id));
        }
        if (entity.hasDespesaLiquidada) {
            throw new ServiceException("Despesa mensal (ID: %s) já liquidada não pode ser excluída.".formatted(id));
        }
        estornos.excluirPendente(id, idUsuario, true);
    }

    // -------------------------------------------------------------------------
    // Validações internas
    // -------------------------------------------------------------------------

    private void verificarRegras_DespesaFixa(DespesaFixaEdicaoModel despesa, String idUsuario) throws ServiceException {
        if (despesa == null) {
            throw new ServiceException("Despesa fixa informada nula.");
        }
        if (despesa.descricaoDespesaFixa == null || despesa.descricaoDespesaFixa.trim().isEmpty()) {
            throw new ServiceException("Descrição da despesa fixa não informada.");
        }
        if (despesa.diaVencimentoDespesaFixa == 0) {
            throw new ServiceException("Dia de vencimento da despesa fixa não informado.");
        }
        if (despesa.diaVencimentoDespesaFixa < 1 || despesa.diaVencimentoDespesaFixa > 31) {
            throw new ServiceException("Dia de vencimento (%s) inválido.".formatted(despesa.diaVencimentoDespesaFixa));
        }
        if (despesa.idNaturezaContaDespesaFixa == null) {
            throw new ServiceException("Natureza da despesa fixa não informada.");
        }
        if (despesa.valorDespesaFixa <= 0) {
            throw new ServiceException("Valor da despesa fixa não informado ou negativo.");
        }
        verificarRegras_NaturezaConta(despesa.idNaturezaContaDespesaFixa, idUsuario);
    }

    private void verificarRegras_DespesaMensal(DespesaMensalEdicaoModel model, String idUsuario) throws ServiceException {
        if (model == null) {
            throw new ServiceException("Despesa mensal informada nula.");
        }
        if (model.descricaoDespesa == null || model.descricaoDespesa.trim().isEmpty()) {
            throw new ServiceException("Descrição da despesa não informada.");
        }
        if (model.valorDespesa <= 0) {
            throw new ServiceException("Valor da despesa não informado ou negativo.");
        }
        if (model.dataVencimentoDespesa == null || model.dataVencimentoDespesa.trim().isEmpty()) {
            throw new ServiceException("Data de vencimento da despesa não informada.");
        }
        formas.validar(model.idFormaLancamento, idUsuario, 1);
        if (model.numeroParcelas < 0) {
            throw new ServiceException("Número de parcelas inválido.");
        }
        verificarRegras_NaturezaConta(model.idNaturezaContaDespesa, idUsuario);

        if (model.idDespesaFixa != null) {
            DespesaFixaEntity df = DespesaFixaEntity.find("{ '_id': ?1, 'idUsuario': ?2 }", oid(model.idDespesaFixa), idUsuario).firstResult();
            if (df == null) {
                throw new ServiceException("Despesa fixa vinculada (ID: %s) não encontrada.".formatted(model.idDespesaFixa));
            }
        }
    }

    private void verificarRegras_NaturezaConta(String idNaturezaConta, String idUsuario) throws ServiceException {
        NaturezaContaEntity naturezaEntity = NaturezaContaEntity.find("{ '_id': ?1, 'idUsuario': ?2 }", oid(idNaturezaConta), idUsuario).firstResult();
        if (naturezaEntity == null) {
            throw new ServiceException("Natureza de conta (ID: %s) não existe.".formatted(idNaturezaConta));
        }
        if (naturezaEntity.codigoTipoSituacaoNaturezaConta == TipoSituacaoEnum.Inativo.getCodigo()) {
            throw new ServiceException("Natureza de conta (ID: %s) está inativa.".formatted(idNaturezaConta));
        }
    }

    private void verificarVinculo(String forma, String id, String idUsuario) throws ServiceException {
        if (id == null) return;
        boolean valido = CartaoCreditoEntity.count("{ '_id': ?1, 'idUsuario': ?2 }", oid(id), idUsuario) > 0
            || ContaCorrenteEntity.count("{ '_id': ?1, 'idUsuario': ?2 }", oid(id), idUsuario) > 0;
        if (!valido) throw new ServiceException("Vinculo de liquidacao nao encontrado.");
    }

    private LocalDateTime converterData(String data) throws ServiceException {
        try {
            return LocalDateTime.parse(data + "T00:00:00");
        } catch (Exception e) {
            throw new ServiceException("Data '%s' inválida. Use o formato yyyy-MM-dd.".formatted(data));
        }
    }
}
