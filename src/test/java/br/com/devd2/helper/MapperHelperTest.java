package br.com.devd2.helper;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.math.BigDecimal;

import org.junit.jupiter.api.Test;

import br.com.devd2.entites.DespesaFixaEntity;
import br.com.devd2.entites.NaturezaContaEntity;
import org.bson.types.ObjectId;
import br.com.devd2.enums.TipoLancamentoEnum;
import br.com.devd2.models.DespesaFixaModel;
import br.com.devd2.models.NaturezaContaModel;

public class MapperHelperTest {

    @Test
    public void mapDespesaFixaTest() {

        //given
        DespesaFixaEntity entity = new DespesaFixaEntity();
        entity.id = new ObjectId();
        entity.descricaoDespesaFixa = "Despesa Fixa 01";
        entity.idFormaLancamento = new ObjectId().toHexString();
        entity.valorDespesaFixa = BigDecimal.valueOf(999.01);
        //when
        DespesaFixaModel model = MapperHelper.INSTANCE.toDespesaFixaModel(entity);
        //then
        assertNotNull(model);
        assertEquals(entity.id.toHexString(), model.idDespesaFixa);
        assertEquals(entity.idFormaLancamento, model.idFormaLancamento);
        assertEquals(model.descricaoDespesaFixa, entity.descricaoDespesaFixa);
        assertEquals(model.valorDespesaFixa, entity.valorDespesaFixa.doubleValue());

    }

    @Test
    public void mapNaturezaContaTest() {

        //given
        NaturezaContaEntity entity = new NaturezaContaEntity();
        entity.id = new ObjectId();
        entity.descricaoNaturezaConta = "Natureza de Conta 01";
        entity.codigoTipoLancamentoConta = TipoLancamentoEnum.Despesa.getCodigo();
        //when
        NaturezaContaModel model = MapperHelper.INSTANCE.toNaturezaContaModel(entity);
        //then
        assertNotNull(model);
        assertEquals(entity.id.toHexString(), model.idNaturezaConta);
        assertEquals(model.codigoTipoLancamentoConta, entity.codigoTipoLancamentoConta);
        assertEquals(model.descricaoNaturezaConta, entity.descricaoNaturezaConta);

    }

}