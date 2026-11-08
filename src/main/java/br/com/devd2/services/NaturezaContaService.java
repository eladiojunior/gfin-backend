package br.com.devd2.services;

import static br.com.devd2.helper.Identificadores.oid;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import br.com.devd2.entites.*;
import br.com.devd2.enums.TipoLancamentoEnum;
import br.com.devd2.enums.TipoSituacaoEnum;
import br.com.devd2.helper.MapperHelper;
import br.com.devd2.infra.ServiceException;
import br.com.devd2.models.NaturezaContaEdicaoModel;
import br.com.devd2.models.NaturezaContaModel;
import io.quarkus.runtime.util.StringUtil;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class NaturezaContaService {

    /**
     * Lista totas as naturezas de contas ativas.
     * @param hasAtivas - Flag que indica listar apenas ATIVAS.
     * @return Lista de naturezas da conta ativas.
     */
    public List<NaturezaContaModel> listarNaturezasContas(boolean hasAtivas, String idUsuario) 
    {

        Stream<NaturezaContaEntity> lista = hasAtivas ?
                NaturezaContaEntity.find("codigoTipoSituacaoNaturezaConta = ?1 and idUsuario = ?2", TipoSituacaoEnum.Ativo.getCodigo(), idUsuario).stream() :
                NaturezaContaEntity.find("idUsuario = ?1", idUsuario).stream();

        return lista.map(MapperHelper.INSTANCE::toNaturezaContaModel).collect(Collectors.toList());

    }

    /**
     * Recuperar natureza da conta pelo ID.
     * @param id - Identificador da natureza da conta.
     * @return natureza da conta recuperada pelo ID, se encontrar.
     */
    public NaturezaContaModel obterNaturezaConta(String id, String idUsuario) throws ServiceException {

        NaturezaContaEntity entity = NaturezaContaEntity.find("{ '_id': ?1, 'idUsuario': ?2 }", oid(id), idUsuario).firstResult();
        if (entity == null) throw new ServiceException("Natureza de conta nao encontrada.");
        return MapperHelper.INSTANCE.toNaturezaContaModel(entity);
        
    }

    /**
     * Registrar uma nova natureza de conta.
     * @param naturezaConta - Natureza da conta para registro.
     * @return objeto de natureza da conta registrado.
     * @throws ServiceException erro de negocio.
     */
    public NaturezaContaModel registrarNaturezaConta(NaturezaContaEdicaoModel naturezaConta, String idUsuario) throws ServiceException {

        verificarRegrasNegocio(naturezaConta, idUsuario);

        var entity = MapperHelper.INSTANCE.toNaturezaContaEntity(naturezaConta);
        entity.idUsuario = idUsuario;
        entity.codigoTipoSituacaoNaturezaConta = TipoSituacaoEnum.Ativo.getCodigo(); //Ativa
        entity.salvar();

        return MapperHelper.INSTANCE.toNaturezaContaModel(entity);
        
    }

    /**
     * Alteração das informações de uma natureza de conta pelo seu ID.
     * @param id - identificador da natureza de conta.
     * @param naturezaConta - informações para alteração da natureza.
     * @return objeto com as informações da naturea alterada.
     * @throws ServiceException erro de negocio.
     */
    public NaturezaContaModel alterarNaturezaConta(String id, NaturezaContaEdicaoModel naturezaConta, String idUsuario) throws ServiceException {

        NaturezaContaEntity entity = NaturezaContaEntity.find("{ '_id': ?1, 'idUsuario': ?2 }", oid(id), idUsuario).firstResult();
        if (entity == null) {
            throw new ServiceException("Natureza da conta não identificada pelo ID: %s.".formatted(id));
        }
        
        //Atualizar a entity com base nas informações enviadas para atualização...
        if (naturezaConta.codigoTipoLancamentoConta != 0) {
            if (!TipoLancamentoEnum.hasTipoLancamento(naturezaConta.codigoTipoLancamentoConta)) {
                throw new ServiceException("Tipo lançamento (%s) não existente.".formatted(naturezaConta.codigoTipoLancamentoConta));
            }
            entity.codigoTipoLancamentoConta = naturezaConta.codigoTipoLancamentoConta;
        }

        if (!StringUtil.isNullOrEmpty(naturezaConta.nomeNaturezaConta))
            entity.nomeNaturezaConta = naturezaConta.nomeNaturezaConta;

        if (!StringUtil.isNullOrEmpty(naturezaConta.descricaoNaturezaConta))
            entity.descricaoNaturezaConta = naturezaConta.descricaoNaturezaConta;

        entity.salvar();

        return MapperHelper.INSTANCE.toNaturezaContaModel(entity);
        
    }

    /**
     * Verifica se para registro e alteração da natureza de conta, todas as regras foram atendidas.
     * @param natureza - Objeto para verificação;
     * @throws ServiceException erro de negocio
     */
    private void verificarRegrasNegocio(NaturezaContaEdicaoModel natureza, String idUsuario) throws ServiceException {
        
        if (natureza == null)
            throw new ServiceException("Natureza de conta informada nula.");
        if (StringUtil.isNullOrEmpty(natureza.nomeNaturezaConta))
            throw new ServiceException("Descrição da natureza de conta não informada");
        if (StringUtil.isNullOrEmpty(natureza.descricaoNaturezaConta))
            throw new ServiceException("Descrição da natureza de conta não informada");
        if (!TipoLancamentoEnum.hasTipoLancamento(natureza.codigoTipoLancamentoConta))
            throw new ServiceException("Tipo de lançamento inválido ou não informado.");

        //Verificar se a natureza já existe com o mesmo nome
        Optional<NaturezaContaEntity> naturezaExistente = NaturezaContaEntity.find("nomeNaturezaConta = ?1 and idUsuario = ?2",
                natureza.nomeNaturezaConta.trim(), idUsuario).firstResultOptional();
        if (naturezaExistente.isPresent()) {
            var entityNaturezaConta = naturezaExistente.get();
            throw new ServiceException("Nome de natureza de conta (%s) já existente como o ID: %s."
                    .formatted(entityNaturezaConta.nomeNaturezaConta, entityNaturezaConta.id));
        }

    }

    /**
     * Exclusão de uma natureza de conta pelo seu ID, pode ser lógica, ser existir algum vínculo com a natureza, 
     * caso não existe vínculo será uma exclusão física mesmo.
     * @param id - identificador da natureza de conta.
     * @throws ServiceException erro de negocio.
     */
    public void excluirNaturezaConta(String id, String idUsuario) throws ServiceException {

        NaturezaContaEntity entity = NaturezaContaEntity.find("{ '_id': ?1, 'idUsuario': ?2 }", oid(id), idUsuario).firstResult();
        if (entity == null)
            throw new ServiceException("Natureza de conta não identificada pelo ID %s.".formatted(id));

        //Verificar se a despesas possuí vínculos para evitar exclusão física.
        var hasExclusaoFisica = verificarVinculosNaturezaConta(entity);
        if (hasExclusaoFisica) {
            entity.codigoTipoSituacaoNaturezaConta = TipoSituacaoEnum.Inativo.getCodigo();
            entity.salvar();
        } else {
            entity.delete();
        }

    }

    /**
     * Verifica se existem vínculos com a natureza de conta nas Despesas e Receitas.
     * @param naturezaConta - Natureza de conta carregada.
     * @return true existe vincula, false não existe nenhum.
     * @throws ServiceException erro de negócio se identificar.
     */
    private boolean verificarVinculosNaturezaConta(NaturezaContaEntity naturezaConta) throws ServiceException {
        
        if (naturezaConta == null) {
            throw new ServiceException("Natureza de conta nula, não será possível verificar seus vínculos.");
        }
        
        //Verificar se existe vinculo com as despesas...
        if (naturezaConta.codigoTipoLancamentoConta == TipoLancamentoEnum.Despesa.getCodigo()) {
            var countDespesas = DespesaFixaEntity.count("idNaturezaContaDespesaFixa = ?1 and idUsuario = ?2", naturezaConta.id.toHexString(), naturezaConta.idUsuario);
            if (countDespesas > 0)
                return true;
            countDespesas = DespesaMensalEntity.count("idNaturezaContaDespesa = ?1 and idUsuario = ?2", naturezaConta.id.toHexString(), naturezaConta.idUsuario);
            if (countDespesas > 0)
                return true;
        }

        //Verificar se existe vinculo com as receitas...
        if (naturezaConta.codigoTipoLancamentoConta == TipoLancamentoEnum.Receita.getCodigo()) {
            var countReceitas = ReceitaFixaEntity.count("idNaturezaContaReceitaFixa = ?1 and idUsuario = ?2", naturezaConta.id.toHexString(), naturezaConta.idUsuario);
            if (countReceitas > 0)
                return true;
            countReceitas = ReceitaMensalEntity.count("idNaturezaContaReceita = ?1 and idUsuario = ?2", naturezaConta.id.toHexString(), naturezaConta.idUsuario);
            if (countReceitas > 0)
                return true;
        }

        return false;

    }

}