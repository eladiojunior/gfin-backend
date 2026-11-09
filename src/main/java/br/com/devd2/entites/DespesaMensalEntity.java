package br.com.devd2.entites;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import io.quarkus.mongodb.panache.common.MongoEntity;
import org.bson.codecs.pojo.annotations.BsonId;


import jakarta.json.bind.annotation.JsonbDateFormat;

@MongoEntity(collection = "tb_despesa_mensal")
public class DespesaMensalEntity extends DocumentoBase {

    public java.util.List<HistoricoEstornoEntity> historicoEstornos;

    public String versaoLiquidacao;

    @BsonId
    public org.bson.types.ObjectId id;

    public String idUsuario;

    public String idDespesaFixa;

    public String idNaturezaContaDespesa;

    public boolean hasDespesaParcelada;

    // Identificador gerado pelo sistema para controlar despesas parceladas e manter vínculo entre elas.

    public Integer codigoDespesaParcelada;

    public String descricaoDespesa;

    @JsonbDateFormat(value = "yyyy-MM-dd")
    public LocalDateTime dataVencimentoDespesa;

    public BigDecimal valorDespesa;

    public String idFormaLancamento;

    // Relaciona a forma de liquidação com o registro de origem (ex: ID do cartão ou cheque).

    public String codigoVinculoFormaLiquidacao;

    public boolean hasDespesaLiquidada;

    public String textoObservacaoDespesa;

    @JsonbDateFormat(value = "yyyy-MM-dd HH:mm:ss")
    public LocalDateTime dataHoraLiquidacaoDespesa;

    public BigDecimal valorDescontoLiquidacaoDespesa;

    public BigDecimal valorMultaJurosLiquidacaoDespesa;

    public BigDecimal valorTotalLiquidacaoDespesa;

    @JsonbDateFormat(value = "yyyy-MM-dd HH:mm:ss")
    public LocalDateTime dataHoraRegistroDespesa = LocalDateTime.now();
}
