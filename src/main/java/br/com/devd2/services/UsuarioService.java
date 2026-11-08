package br.com.devd2.services;

import static br.com.devd2.helper.Identificadores.oid;

import java.util.List;
import java.util.stream.Collectors;

import br.com.devd2.entites.UsuarioEntity;
import br.com.devd2.entites.FormaLancamentoEntity;
import br.com.devd2.entites.NaturezaContaEntity;
import br.com.devd2.enums.TipoSituacaoEnum;
import br.com.devd2.helper.MapperHelper;
import br.com.devd2.helper.UtilHelper;
import br.com.devd2.infra.ServiceException;
import br.com.devd2.models.UsuarioEdicaoModel;
import br.com.devd2.models.UsuarioModel;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class UsuarioService {

    public List<UsuarioModel> listarUsuarios(String idUsuario) {
        return UsuarioEntity.<UsuarioEntity>find("{ '_id': ?1 }", oid(idUsuario)).list()
            .stream()
            .map(MapperHelper.INSTANCE::toUsuarioModel)
            .collect(Collectors.toList());
    }

    public UsuarioModel obterUsuario(String id, String idUsuario) throws ServiceException {
        UsuarioEntity entity = id.equals(idUsuario) ? UsuarioEntity.findById(oid(id)) : null;
        if (entity == null) {
            throw new ServiceException("Usuário não identificado pelo ID %s.".formatted(id));
        }
        return MapperHelper.INSTANCE.toUsuarioModel(entity);
    }

    public UsuarioModel registrarUsuario(UsuarioEdicaoModel model) throws ServiceException {

        verificarRegras(model);

        long existente = UsuarioEntity.count("emailUsuario = ?1", model.emailUsuario);
        if (existente > 0) {
            throw new ServiceException("Já existe um usuário cadastrado com o e-mail '%s'.".formatted(model.emailUsuario));
        }

        var entity = MapperHelper.INSTANCE.toUsuarioEntity(model);
        entity.saltSenhaUsuario = UtilHelper.gerarSaltPassword();
        entity.senhaUsuario = UtilHelper.gerarHashPassword(model.senhaUsuario, entity.saltSenhaUsuario);
        entity.codigoTipoSituacaoUsuario = TipoSituacaoEnum.Ativo.getCodigo();
        entity.salvar();
        String idUsuario = entity.id.toHexString();
        try {
            criarDadosIniciais(idUsuario);
        } catch (RuntimeException erro) {
            try {
                FormaLancamentoEntity.delete("idUsuario = ?1", idUsuario);
                NaturezaContaEntity.delete("idUsuario = ?1", idUsuario);
                entity.delete();
            } catch (RuntimeException limpeza) {
                erro.addSuppressed(limpeza);
            }
            throw erro;
        }

        return MapperHelper.INSTANCE.toUsuarioModel(entity);
    }

    private void criarDadosIniciais(String idUsuario) {
        criarFormas(idUsuario, 1, "Dinheiro", "Cartão de Crédito/Débito", "Boleto de Cobrança", "Débito em Conta", "Fatura");
        criarFormas(idUsuario, 2, "Não Informado", "Dinheiro", "Transferência", "Cheque", "Crédito em Conta");
        criarNaturezas(idUsuario, 1, "Alimentação", "Educação", "Impostos e Taxas", "Lazer", "Moradia", "Saúde", "Transporte", "Outros");
        criarNaturezas(idUsuario, 2, "Salário", "Investimento", "Presente", "Trabalho Extra", "Outros");
    }

    private void criarFormas(String usuario, int tipo, String... nomes) {
        for (String nome : nomes) {
            var forma = new FormaLancamentoEntity();
            forma.idUsuario = usuario;
            forma.codigoLancamentoConta = tipo;
            forma.nomeFormaLancamento = nome;
            forma.codigoSituacaoFormaLancamento = TipoSituacaoEnum.Ativo.getCodigo();
            forma.salvar();
        }
    }

    private void criarNaturezas(String usuario, int tipo, String... nomes) {
        for (String nome : nomes) {
            var natureza = new NaturezaContaEntity();
            natureza.idUsuario = usuario;
            natureza.codigoTipoLancamentoConta = tipo;
            natureza.nomeNaturezaConta = nome;
            natureza.descricaoNaturezaConta = nome;
            natureza.codigoTipoSituacaoNaturezaConta = TipoSituacaoEnum.Ativo.getCodigo();
            natureza.salvar();
        }
    }

    public UsuarioModel alterarUsuario(String id, UsuarioEdicaoModel model, String idUsuario) throws ServiceException {

        UsuarioEntity entity = id.equals(idUsuario) ? UsuarioEntity.findById(oid(id)) : null;
        if (entity == null) {
            throw new ServiceException("Usuário não identificado pelo ID %s.".formatted(id));
        }

        if (model.nomeUsuario != null && !model.nomeUsuario.trim().isEmpty()) {
            entity.nomeUsuario = model.nomeUsuario;
        }
        if (model.emailUsuario != null && !model.emailUsuario.trim().isEmpty()) {
            if (!UtilHelper.validarEmail(model.emailUsuario)) {
                throw new ServiceException("E-mail do usuário inválido.");
            }
            entity.emailUsuario = model.emailUsuario;
        }
        if (model.senhaUsuario != null && !model.senhaUsuario.trim().isEmpty()) {
            if (!UtilHelper.validarSenha(model.senhaUsuario)) {
                throw new ServiceException("Senha do usuário não atende as critérios mínimos de segurança.");
            }
            entity.senhaUsuario = model.senhaUsuario;
        }

        entity.salvar();

        return MapperHelper.INSTANCE.toUsuarioModel(entity);

    }

    public void excluirUsuario(String id, String idUsuario) throws ServiceException {

        UsuarioEntity entity = id.equals(idUsuario) ? UsuarioEntity.findById(oid(id)) : null;
        if (entity == null) {
            throw new ServiceException("Usuário não identificado pelo ID %s.".formatted(id));
        }
        entity.codigoTipoSituacaoUsuario = TipoSituacaoEnum.Inativo.getCodigo();
        entity.salvar();
    }

    private void verificarRegras(UsuarioEdicaoModel model) throws ServiceException {
        if (model == null) {
            throw new ServiceException("Usuário informado nulo.");
        }
        if (model.nomeUsuario == null || model.nomeUsuario.trim().isEmpty()) {
            throw new ServiceException("Nome do usuário não informado.");
        }
        if (model.emailUsuario == null || model.emailUsuario.trim().isEmpty()) {
            throw new ServiceException("E-mail do usuário não informado.");
        }
        if (!UtilHelper.validarEmail(model.emailUsuario)) {
            throw new ServiceException("E-mail do usuário inválido.");
        }
        if (model.senhaUsuario == null || model.senhaUsuario.trim().isEmpty()) {
            throw new ServiceException("Senha do usuário não informada.");
        }
        if (!UtilHelper.validarSenha(model.senhaUsuario)) {
            throw new ServiceException("Senha do usuário não atende as critérios mínimos de segurança.");
        }
    }
}