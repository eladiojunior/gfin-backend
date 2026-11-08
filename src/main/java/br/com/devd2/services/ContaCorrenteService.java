package br.com.devd2.services;

import static br.com.devd2.helper.Identificadores.oid;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

import br.com.devd2.entites.CartaoCreditoEntity;
import br.com.devd2.entites.ContaCorrenteEntity;
import br.com.devd2.helper.MapperHelper;
import br.com.devd2.infra.ServiceException;
import br.com.devd2.models.ContaCorrenteEdicaoModel;
import br.com.devd2.models.ContaCorrenteModel;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class ContaCorrenteService {

    public List<ContaCorrenteModel> listarContasCorrentes(String idUsuario) {
        return ContaCorrenteEntity.<ContaCorrenteEntity>find("idUsuario = ?1", idUsuario)
            .stream()
            .map(MapperHelper.INSTANCE::toContaCorrenteModel)
            .collect(Collectors.toList());
    }

    public ContaCorrenteModel obterContaCorrente(String id, String idUsuario) throws ServiceException {
        ContaCorrenteEntity entity = ContaCorrenteEntity.find("{ \'_id\': ?1, \'idUsuario\': ?2 }", oid(id), idUsuario).firstResult();
        if (entity == null) {
            throw new ServiceException("Conta corrente não identificada pelo ID %s.".formatted(id));
        }
        return MapperHelper.INSTANCE.toContaCorrenteModel(entity);
    }

    public ContaCorrenteModel registrarContaCorrente(ContaCorrenteEdicaoModel model, String idUsuario) throws ServiceException {
        verificarRegras(model);

        var entity = MapperHelper.INSTANCE.toContaCorrenteEntity(model);
        entity.idUsuario = idUsuario;
        entity.isContaCorrenteAtiva = true;
        entity.salvar();

        return MapperHelper.INSTANCE.toContaCorrenteModel(entity);
    }

    public ContaCorrenteModel alterarContaCorrente(String id, ContaCorrenteEdicaoModel model, String idUsuario) throws ServiceException {
        ContaCorrenteEntity entity = ContaCorrenteEntity.find("{ \'_id\': ?1, \'idUsuario\': ?2 }", oid(id), idUsuario).firstResult();
        if (entity == null) {
            throw new ServiceException("Conta corrente não identificada pelo ID %s.".formatted(id));
        }

        if (model.numeroBanco != 0) {
            entity.numeroBanco = model.numeroBanco;
        }
        if (model.nomeBanco != null && !model.nomeBanco.trim().isEmpty()) {
            entity.nomeBanco = model.nomeBanco;
        }
        if (model.numeroAgencia != null && !model.numeroAgencia.trim().isEmpty()) {
            entity.numeroAgencia = model.numeroAgencia;
        }
        if (model.numeroContaCorrente != null && !model.numeroContaCorrente.trim().isEmpty()) {
            entity.numeroContaCorrente = model.numeroContaCorrente;
        }
        if (model.nomeTitularConta != null && !model.nomeTitularConta.trim().isEmpty()) {
            entity.nomeTitularConta = model.nomeTitularConta;
        }
        if (model.valorLimiteConta > 0) {
            entity.valorLimiteConta = BigDecimal.valueOf(model.valorLimiteConta);
        }

        entity.salvar();
        return MapperHelper.INSTANCE.toContaCorrenteModel(entity);
    }

    public ContaCorrenteModel ativarDesativarContaCorrente(String id, boolean ativar, String idUsuario) throws ServiceException {
        ContaCorrenteEntity entity = ContaCorrenteEntity.find("{ \'_id\': ?1, \'idUsuario\': ?2 }", oid(id), idUsuario).firstResult();
        if (entity == null) {
            throw new ServiceException("Conta corrente não identificada pelo ID %s.".formatted(id));
        }
        entity.isContaCorrenteAtiva = ativar;
        entity.salvar();
        return MapperHelper.INSTANCE.toContaCorrenteModel(entity);
    }

    public void excluirContaCorrente(String id, String idUsuario) throws ServiceException {
        ContaCorrenteEntity entity = ContaCorrenteEntity.find("{ \'_id\': ?1, \'idUsuario\': ?2 }", oid(id), idUsuario).firstResult();
        if (entity == null) {
            throw new ServiceException("Conta corrente não identificada pelo ID %s.".formatted(id));
        }

        long vinculosCartao = CartaoCreditoEntity.count("idContaCorrente = ?1 and idUsuario = ?2", id, idUsuario);
        if (vinculosCartao > 0) {
            throw new ServiceException("Não é possível excluir a conta corrente pois existem %s cartão(ões) vinculado(s).".formatted(vinculosCartao));
        }

        entity.delete();
    }

    private void verificarRegras(ContaCorrenteEdicaoModel model) throws ServiceException {
        if (model == null) {
            throw new ServiceException("Conta corrente informada nula.");
        }
        if (model.nomeBanco == null || model.nomeBanco.trim().isEmpty()) {
            throw new ServiceException("Nome do banco não informado.");
        }
        if (model.numeroAgencia == null || model.numeroAgencia.trim().isEmpty()) {
            throw new ServiceException("Número da agência não informado.");
        }
        if (model.numeroContaCorrente == null || model.numeroContaCorrente.trim().isEmpty()) {
            throw new ServiceException("Número da conta corrente não informado.");
        }
        if (model.nomeTitularConta == null || model.nomeTitularConta.trim().isEmpty()) {
            throw new ServiceException("Nome do titular da conta não informado.");
        }
    }
}
