package br.com.devd2.models;
import java.math.BigDecimal;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
@Schema(description = "Dados da liquidação preservados no momento do estorno; datas em UTC ISO-8601.")
public class HistoricoEstornoModel {
    public String dataHoraEstorno;
    public String dataHoraLiquidacao;
    public String idFormaLancamento;
    public String codigoVinculoFormaLiquidacao;
    public BigDecimal valorTotalLiquidacao;
    public BigDecimal valorDescontoLiquidacao;
    public BigDecimal valorMultaJurosLiquidacao;
}
