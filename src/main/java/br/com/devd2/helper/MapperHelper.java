package br.com.devd2.helper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

import br.com.devd2.entites.CartaoCreditoEntity;
import br.com.devd2.entites.ContaCorrenteEntity;
import br.com.devd2.entites.CorrentistaEntity;
import br.com.devd2.entites.DespesaFixaEntity;
import br.com.devd2.entites.DespesaMensalEntity;
import br.com.devd2.entites.NaturezaContaEntity;
import br.com.devd2.entites.ReceitaFixaEntity;
import br.com.devd2.entites.ReceitaMensalEntity;
import br.com.devd2.entites.UsuarioEntity;
import br.com.devd2.models.CartaoCreditoEdicaoModel;
import br.com.devd2.models.CartaoCreditoModel;
import br.com.devd2.models.ContaCorrenteEdicaoModel;
import br.com.devd2.models.ContaCorrenteModel;
import br.com.devd2.models.CorrentistaEdicaoModel;
import br.com.devd2.models.CorrentistaModel;
import br.com.devd2.models.DespesaFixaEdicaoModel;
import br.com.devd2.models.DespesaFixaModel;
import br.com.devd2.models.DespesaMensalEdicaoModel;
import br.com.devd2.models.DespesaMensalModel;
import br.com.devd2.models.NaturezaContaEdicaoModel;
import br.com.devd2.models.NaturezaContaModel;
import br.com.devd2.models.ReceitaFixaEdicaoModel;
import br.com.devd2.models.ReceitaFixaModel;
import br.com.devd2.models.ReceitaMensalEdicaoModel;
import br.com.devd2.models.ReceitaMensalModel;
import br.com.devd2.models.UsuarioEdicaoModel;
import br.com.devd2.models.UsuarioModel;

@Mapper
public interface MapperHelper {

    default String map(org.bson.types.ObjectId id) { return id == null ? null : id.toHexString(); }

    MapperHelper INSTANCE = Mappers.getMapper(MapperHelper.class);

    // --- NaturezaConta ---

    @Mapping(target = "dataHoraRegistroNaturezaConta", dateFormat = "dd/MM/yyyy HH:mm:ss")
    @Mapping(target = "idNaturezaConta", source = "id")
    NaturezaContaModel toNaturezaContaModel(NaturezaContaEntity entity);

    NaturezaContaEntity toNaturezaContaEntity(NaturezaContaEdicaoModel model);

    // --- DespesaFixa ---

    @Mapping(target = "dataHoraRegistroDespesaFixa", dateFormat = "dd/MM/yyyy HH:mm:ss")
    @Mapping(target = "idDespesaFixa", source = "id")
    DespesaFixaModel toDespesaFixaModel(DespesaFixaEntity entity);

    DespesaFixaEntity toDespesaFixaEntity(DespesaFixaEdicaoModel model);

    // --- DespesaMensal ---

    @Mapping(target = "idDespesaMensal", source = "id")
    @Mapping(target = "dataVencimentoDespesa", dateFormat = "dd/MM/yyyy")
    @Mapping(target = "dataHoraLiquidacaoDespesa", dateFormat = "dd/MM/yyyy HH:mm:ss")
    @Mapping(target = "dataHoraRegistroDespesa", dateFormat = "dd/MM/yyyy HH:mm:ss")
    DespesaMensalModel toDespesaMensalModel(DespesaMensalEntity entity);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "hasDespesaParcelada", ignore = true)
    @Mapping(target = "codigoDespesaParcelada", ignore = true)
    @Mapping(target = "hasDespesaLiquidada", ignore = true)
    @Mapping(target = "dataVencimentoDespesa", ignore = true)
    @Mapping(target = "dataHoraLiquidacaoDespesa", ignore = true)
    @Mapping(target = "valorDescontoLiquidacaoDespesa", ignore = true)
    @Mapping(target = "valorMultaJurosLiquidacaoDespesa", ignore = true)
    @Mapping(target = "valorTotalLiquidacaoDespesa", ignore = true)
    @Mapping(target = "dataHoraRegistroDespesa", ignore = true)
    DespesaMensalEntity toDespesaMensalEntity(DespesaMensalEdicaoModel model);

    // --- Usuario ---

    @Mapping(target = "idUsuario", source = "id")
    @Mapping(target = "dataHoraRegistroUsuario", dateFormat = "dd/MM/yyyy HH:mm:ss")
    UsuarioModel toUsuarioModel(UsuarioEntity entity);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "saltSenhaUsuario", ignore = true)
    @Mapping(target = "isAlterarSenhaUsuario", ignore = true)
    @Mapping(target = "isConfirmacaoEmailUsuario", ignore = true)
    @Mapping(target = "codigoTipoSituacaoUsuario", ignore = true)
    @Mapping(target = "dataUltimoAcessoUsuario", ignore = true)
    @Mapping(target = "dataHoraRegistroUsuario", ignore = true)
    UsuarioEntity toUsuarioEntity(UsuarioEdicaoModel model);

    // --- ContaCorrente ---

    @Mapping(target = "idContaCorrente", source = "id")
    @Mapping(target = "dataHoraRegistroContaCorrente", dateFormat = "dd/MM/yyyy HH:mm:ss")
    ContaCorrenteModel toContaCorrenteModel(ContaCorrenteEntity entity);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "idUsuario", ignore = true)
    @Mapping(target = "isContaCorrenteAtiva", ignore = true)
    @Mapping(target = "dataHoraRegistroContaCorrente", ignore = true)
    ContaCorrenteEntity toContaCorrenteEntity(ContaCorrenteEdicaoModel model);

    // --- CartaoCredito ---

    @Mapping(target = "idCartaoCredito", source = "id")
    @Mapping(target = "dataValidadeCartaoCredito", dateFormat = "MM/yyyy")
    @Mapping(target = "dataHoraRegistroCartaoCredito", dateFormat = "dd/MM/yyyy HH:mm:ss")
    CartaoCreditoModel toCartaoCreditoModel(CartaoCreditoEntity entity);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "idUsuario", ignore = true)
    @Mapping(target = "situacaoCartaoCredito", ignore = true)
    @Mapping(target = "dataValidadeCartaoCredito", ignore = true)
    @Mapping(target = "dataHoraRegistroCartaoCredito", ignore = true)
    CartaoCreditoEntity toCartaoCreditoEntity(CartaoCreditoEdicaoModel model);

    // --- Correntista ---

    @Mapping(target = "idCorrentista", source = "id")
    @Mapping(target = "dataHoraRegistroCorrentista", dateFormat = "dd/MM/yyyy HH:mm:ss")
    CorrentistaModel toCorrentistaModel(CorrentistaEntity entity);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "idUsuario", ignore = true)
    @Mapping(target = "dataHoraRegistroCorrentista", ignore = true)
    CorrentistaEntity toCorrentistaEntity(CorrentistaEdicaoModel model);

    // --- ReceitaFixa ---

    @Mapping(target = "idReceitaFixa", source = "id")
    @Mapping(target = "dataHoraRegistroReceitaFixa", dateFormat = "dd/MM/yyyy HH:mm:ss")
    ReceitaFixaModel toReceitaFixaModel(ReceitaFixaEntity entity);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "idUsuario", ignore = true)
    @Mapping(target = "codigoTipoSituacaoReceitaFixa", ignore = true)
    @Mapping(target = "dataHoraRegistroReceitaFixa", ignore = true)
    ReceitaFixaEntity toReceitaFixaEntity(ReceitaFixaEdicaoModel model);

    // --- ReceitaMensal ---

    @Mapping(target = "idReceitaMensal", source = "id")
    @Mapping(target = "dataRecebimentoReceita", dateFormat = "dd/MM/yyyy")
    @Mapping(target = "dataHoraLiquidacaoReceita", dateFormat = "dd/MM/yyyy HH:mm:ss")
    @Mapping(target = "dataHoraRegistroReceita", dateFormat = "dd/MM/yyyy HH:mm:ss")
    ReceitaMensalModel toReceitaMensalModel(ReceitaMensalEntity entity);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "idUsuario", ignore = true)
    @Mapping(target = "isReceitaLiquidada", ignore = true)
    @Mapping(target = "dataRecebimentoReceita", ignore = true)
    @Mapping(target = "dataHoraLiquidacaoReceita", ignore = true)
    @Mapping(target = "valorTotalLiquidacaoReceita", ignore = true)
    @Mapping(target = "dataHoraRegistroReceita", ignore = true)
    ReceitaMensalEntity toReceitaMensalEntity(ReceitaMensalEdicaoModel model);
}
