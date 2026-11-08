package br.com.devd2.services;

import static br.com.devd2.helper.Identificadores.oid;

import java.util.List;
import java.util.stream.Collectors;

import br.com.devd2.entites.CorrentistaEntity;
import br.com.devd2.helper.MapperHelper;
import br.com.devd2.infra.ServiceException;
import br.com.devd2.models.CorrentistaEdicaoModel;
import br.com.devd2.models.CorrentistaModel;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class CorrentistaService {

    public List<CorrentistaModel> listarCorrentistas(String idUsuario) {
        return CorrentistaEntity.<CorrentistaEntity>find("idUsuario = ?1", idUsuario)
            .stream()
            .map(MapperHelper.INSTANCE::toCorrentistaModel)
            .collect(Collectors.toList());
    }

    public CorrentistaModel obterCorrentista(String id, String idUsuario) throws ServiceException {
        CorrentistaEntity entity = CorrentistaEntity.find("{ \'_id\': ?1, \'idUsuario\': ?2 }", oid(id), idUsuario).firstResult();
        if (entity == null) {
            throw new ServiceException("Correntista não identificado pelo ID %s.".formatted(id));
        }
        return MapperHelper.INSTANCE.toCorrentistaModel(entity);
    }

    public CorrentistaModel registrarCorrentista(CorrentistaEdicaoModel model, String idUsuario) throws ServiceException {
        verificarRegras(model);

        var entity = MapperHelper.INSTANCE.toCorrentistaEntity(model);
        entity.idUsuario = idUsuario;
        entity.salvar();

        return MapperHelper.INSTANCE.toCorrentistaModel(entity);
    }

    public CorrentistaModel alterarCorrentista(String id, CorrentistaEdicaoModel model, String idUsuario) throws ServiceException {
        CorrentistaEntity entity = CorrentistaEntity.find("{ \'_id\': ?1, \'idUsuario\': ?2 }", oid(id), idUsuario).firstResult();
        if (entity == null) {
            throw new ServiceException("Correntista não identificado pelo ID %s.".formatted(id));
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
        if (model.nomeCorrentista != null && !model.nomeCorrentista.trim().isEmpty()) {
            entity.nomeCorrentista = model.nomeCorrentista;
        }
        if (model.observacao != null) {
            entity.observacao = model.observacao;
        }

        entity.salvar();
        return MapperHelper.INSTANCE.toCorrentistaModel(entity);
    }

    public void excluirCorrentista(String id, String idUsuario) throws ServiceException {
        CorrentistaEntity entity = CorrentistaEntity.find("{ \'_id\': ?1, \'idUsuario\': ?2 }", oid(id), idUsuario).firstResult();
        if (entity == null) {
            throw new ServiceException("Correntista não identificado pelo ID %s.".formatted(id));
        }
        entity.delete();
    }

    private void verificarRegras(CorrentistaEdicaoModel model) throws ServiceException {
        if (model == null) {
            throw new ServiceException("Correntista informado nulo.");
        }
        if (model.nomeCorrentista == null || model.nomeCorrentista.trim().isEmpty()) {
            throw new ServiceException("Nome do correntista não informado.");
        }
        if (model.nomeBanco == null || model.nomeBanco.trim().isEmpty()) {
            throw new ServiceException("Nome do banco não informado.");
        }
        if (model.numeroAgencia == null || model.numeroAgencia.trim().isEmpty()) {
            throw new ServiceException("Número da agência não informado.");
        }
        if (model.numeroContaCorrente == null || model.numeroContaCorrente.trim().isEmpty()) {
            throw new ServiceException("Número da conta não informado.");
        }
    }
}
