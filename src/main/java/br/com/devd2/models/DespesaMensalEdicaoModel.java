package br.com.devd2.models;

public class DespesaMensalEdicaoModel {
    public String idDespesaFixa;
    public String idNaturezaContaDespesa;
    public String descricaoDespesa;
    public String dataVencimentoDespesa;
    public double valorDespesa;
    public String idFormaLancamento;
    public String codigoVinculoFormaLiquidacao;
    public String textoObservacaoDespesa;
    // Quando > 1 o sistema cria N parcelas automaticamente incrementando a data mensalmente.
    public int numeroParcelas;
}
