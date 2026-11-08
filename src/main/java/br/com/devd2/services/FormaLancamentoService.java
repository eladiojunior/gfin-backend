package br.com.devd2.services;

import static br.com.devd2.helper.Identificadores.oid;

import java.util.List;

import br.com.devd2.entites.*;
import br.com.devd2.infra.ServiceException;
import br.com.devd2.models.FormaLancamentoModel;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class FormaLancamentoService {
    public FormaLancamentoEntity obter(String id, String usuario) throws ServiceException {
        FormaLancamentoEntity e = FormaLancamentoEntity.find("{ '_id': ?1, 'idUsuario': ?2 }", oid(id), usuario).firstResult();
        if (e == null) throw new ServiceException("Forma de lancamento nao encontrada.");
        return e;
    }

    public void validar(String id, String usuario, int tipo) throws ServiceException {
        FormaLancamentoEntity e = obter(id, usuario);
        if (e.codigoLancamentoConta != tipo || e.codigoSituacaoFormaLancamento != 1)
            throw new ServiceException("Forma de lancamento inativa ou de tipo incorreto.");
    }

    public FormaLancamentoModel model(FormaLancamentoEntity e) {
        FormaLancamentoModel m = new FormaLancamentoModel();
        m.idFormaLancamento = e.id.toHexString();
        m.nomeFormaLancamento = e.nomeFormaLancamento;
        m.codigoLancamentoConta = e.codigoLancamentoConta;
        m.codigoSituacaoFormaLancamento = e.codigoSituacaoFormaLancamento;
        m.dataHoraRegistroFormaLancamento = e.dataHoraRegistroFormaLancamento.toString();
        return m;
    }

    public List<FormaLancamentoModel> listar(String usuario) {
        return FormaLancamentoEntity.<FormaLancamentoEntity>find("idUsuario = ?1", usuario).stream().map(this::model).toList();
    }

    private void validarDados(FormaLancamentoModel m) throws ServiceException {
        if (m == null || m.nomeFormaLancamento == null || m.nomeFormaLancamento.isBlank() || (m.codigoLancamentoConta != 1 && m.codigoLancamentoConta != 2) || (m.codigoSituacaoFormaLancamento != 1 && m.codigoSituacaoFormaLancamento != 2))
            throw new ServiceException("Informe nome, tipo (1 ou 2) e situacao (1 ou 2) validos.");
    }

    public FormaLancamentoModel criar(FormaLancamentoModel m, String usuario) throws ServiceException {
        validarDados(m);
        var e = new FormaLancamentoEntity();
        e.idUsuario = usuario;
        e.nomeFormaLancamento = m.nomeFormaLancamento.trim();
        e.codigoLancamentoConta = m.codigoLancamentoConta;
        e.codigoSituacaoFormaLancamento = m.codigoSituacaoFormaLancamento;
        e.salvar();
        return model(e);
    }

    private boolean possuiVinculos(String id, String u) {
        return DespesaFixaEntity.count("idFormaLancamento = ?1 and idUsuario = ?2", id, u) > 0 || DespesaMensalEntity.count("idFormaLancamento = ?1 and idUsuario = ?2", id, u) > 0 || ReceitaMensalEntity.count("idFormaLancamento = ?1 and idUsuario = ?2", id, u) > 0;
    }

    public FormaLancamentoModel alterar(String id, FormaLancamentoModel m, String usuario) throws ServiceException {
        validarDados(m);
        var e = obter(id, usuario);
        if (e.codigoLancamentoConta != m.codigoLancamentoConta && possuiVinculos(id, usuario))
            throw new ServiceException("Forma possui lancamentos vinculados.");
        e.nomeFormaLancamento = m.nomeFormaLancamento.trim();
        e.codigoLancamentoConta = m.codigoLancamentoConta;
        e.codigoSituacaoFormaLancamento = m.codigoSituacaoFormaLancamento;
        e.salvar();
        return model(e);
    }

    public void excluir(String id, String usuario) throws ServiceException {
        var e = obter(id, usuario);
        if (possuiVinculos(id, usuario)) {
            e.codigoSituacaoFormaLancamento = 2;
            e.salvar();
        } else e.delete();
    }
}
