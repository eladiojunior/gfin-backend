package br.com.devd2.services;

import static br.com.devd2.helper.Identificadores.oid;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

import br.com.devd2.entites.CartaoCreditoEntity;
import br.com.devd2.entites.ContaCorrenteEntity;
import br.com.devd2.enums.TipoSituacaoCartaoCreditoEnum;
import br.com.devd2.helper.MapperHelper;
import br.com.devd2.infra.ServiceException;
import br.com.devd2.models.CartaoCreditoEdicaoModel;
import br.com.devd2.models.CartaoCreditoModel;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class CartaoCreditoService {

    public List<CartaoCreditoModel> listarCartoes(String idUsuario) {
        return CartaoCreditoEntity.<CartaoCreditoEntity>find("idUsuario = ?1", idUsuario)
            .stream()
            .map(MapperHelper.INSTANCE::toCartaoCreditoModel)
            .collect(Collectors.toList());
    }

    public CartaoCreditoModel obterCartao(String id, String idUsuario) throws ServiceException {
        CartaoCreditoEntity entity = CartaoCreditoEntity.find("{ \'_id\': ?1, \'idUsuario\': ?2 }", oid(id), idUsuario).firstResult();
        if (entity == null) {
            throw new ServiceException("Cartão de crédito não identificado pelo ID %s.".formatted(id));
        }
        return MapperHelper.INSTANCE.toCartaoCreditoModel(entity);
    }

    public CartaoCreditoModel registrarCartao(CartaoCreditoEdicaoModel model, String idUsuario) throws ServiceException {
        verificarRegras(model, idUsuario);

        var entity = MapperHelper.INSTANCE.toCartaoCreditoEntity(model);
        entity.idUsuario = idUsuario;
        entity.dataValidadeCartaoCredito = converterDataValidade(model.dataValidadeCartaoCredito);
        entity.valorLimiteCartaoCredito = model.valorLimiteCartaoCredito > 0
            ? BigDecimal.valueOf(model.valorLimiteCartaoCredito)
            : BigDecimal.ZERO;
        entity.situacaoCartaoCredito = TipoSituacaoCartaoCreditoEnum.Ativo.getCodigo();
        entity.salvar();

        return MapperHelper.INSTANCE.toCartaoCreditoModel(entity);
    }

    public CartaoCreditoModel alterarCartao(String id, CartaoCreditoEdicaoModel model, String idUsuario) throws ServiceException {
        CartaoCreditoEntity entity = CartaoCreditoEntity.find("{ \'_id\': ?1, \'idUsuario\': ?2 }", oid(id), idUsuario).firstResult();
        if (entity == null) {
            throw new ServiceException("Cartão de crédito não identificado pelo ID %s.".formatted(id));
        }

        if (model.nomeCartaoCredito != null && !model.nomeCartaoCredito.trim().isEmpty()) {
            entity.nomeCartaoCredito = model.nomeCartaoCredito;
        }
        if (model.numeroCartaoCredito != null && !model.numeroCartaoCredito.trim().isEmpty()) {
            entity.numeroCartaoCredito = model.numeroCartaoCredito;
        }
        if (model.dataValidadeCartaoCredito != null && !model.dataValidadeCartaoCredito.trim().isEmpty()) {
            entity.dataValidadeCartaoCredito = converterDataValidade(model.dataValidadeCartaoCredito);
        }
        if (model.valorLimiteCartaoCredito > 0) {
            entity.valorLimiteCartaoCredito = BigDecimal.valueOf(model.valorLimiteCartaoCredito);
        }
        if (model.diaVencimentoCartaoCredito > 0) {
            entity.diaVencimentoCartaoCredito = model.diaVencimentoCartaoCredito;
        }
        if (model.nomeProprietarioCartaoCredito != null && !model.nomeProprietarioCartaoCredito.trim().isEmpty()) {
            entity.nomeProprietarioCartaoCredito = model.nomeProprietarioCartaoCredito;
        }

        entity.hasCartaoCredito = model.hasCartaoCredito;
        entity.hasCartaoPrePago = model.hasCartaoPrePago;
        entity.hasCartaoDebito = model.hasCartaoDebito;

        entity.salvar();
        return MapperHelper.INSTANCE.toCartaoCreditoModel(entity);
    }

    public CartaoCreditoModel alterarSituacaoCartao(String id, int codigoSituacao, String idUsuario) throws ServiceException {
        if (!TipoSituacaoCartaoCreditoEnum.hasSituacao(codigoSituacao)) {
            throw new ServiceException("Situação (%s) inválida para cartão de crédito.".formatted(codigoSituacao));
        }
        CartaoCreditoEntity entity = CartaoCreditoEntity.find("{ \'_id\': ?1, \'idUsuario\': ?2 }", oid(id), idUsuario).firstResult();
        if (entity == null) {
            throw new ServiceException("Cartão de crédito não identificado pelo ID %s.".formatted(id));
        }
        entity.situacaoCartaoCredito = codigoSituacao;
        entity.salvar();
        return MapperHelper.INSTANCE.toCartaoCreditoModel(entity);
    }

    public void excluirCartao(String id, String idUsuario) throws ServiceException {
        CartaoCreditoEntity entity = CartaoCreditoEntity.find("{ \'_id\': ?1, \'idUsuario\': ?2 }", oid(id), idUsuario).firstResult();
        if (entity == null) {
            throw new ServiceException("Cartão de crédito não identificado pelo ID %s.".formatted(id));
        }
        entity.delete();
    }

    private void verificarRegras(CartaoCreditoEdicaoModel model, String idUsuario) throws ServiceException {
        if (model == null) {
            throw new ServiceException("Cartão de crédito informado nulo.");
        }
        if (model.nomeCartaoCredito == null || model.nomeCartaoCredito.trim().isEmpty()) {
            throw new ServiceException("Nome do cartão não informado.");
        }
        if (model.nomeProprietarioCartaoCredito == null || model.nomeProprietarioCartaoCredito.trim().isEmpty()) {
            throw new ServiceException("Nome do proprietário do cartão não informado.");
        }
        if (model.dataValidadeCartaoCredito == null || model.dataValidadeCartaoCredito.trim().isEmpty()) {
            throw new ServiceException("Data de validade do cartão não informada. Use o formato MM/yyyy.");
        }
        if (model.idContaCorrente != null) {
            ContaCorrenteEntity conta = ContaCorrenteEntity.find("{ '_id': ?1, 'idUsuario': ?2 }", oid(model.idContaCorrente), idUsuario).firstResult();
            if (conta == null) {
                throw new ServiceException("Conta corrente vinculada (ID: %s) não encontrada.".formatted(model.idContaCorrente));
            }
        }
    }

    private LocalDateTime converterDataValidade(String dataValidade) throws ServiceException {
        try {
            var formatter = DateTimeFormatter.ofPattern("MM/yyyy");
            var mesAno = java.time.YearMonth.parse(dataValidade, formatter);
            return mesAno.atEndOfMonth().atStartOfDay();
        } catch (Exception e) {
            throw new ServiceException("Data de validade '%s' inválida. Use o formato MM/yyyy.".formatted(dataValidade));
        }
    }
}
