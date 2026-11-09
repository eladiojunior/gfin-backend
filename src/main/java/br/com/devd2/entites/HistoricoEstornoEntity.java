package br.com.devd2.entites;
import java.util.Date;
import java.math.BigDecimal;
public class HistoricoEstornoEntity {
 public Date dataHoraEstorno;
 public Date dataHoraLiquidacao;
 public String idFormaLancamento;
 public String codigoVinculoFormaLiquidacao;
 public BigDecimal valorTotalLiquidacao;
 public BigDecimal valorDescontoLiquidacao;
 public BigDecimal valorMultaJurosLiquidacao;
}
