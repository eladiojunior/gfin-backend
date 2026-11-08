package br.com.devd2.enums;

public enum TipoSituacaoEnum {
    Ativo(1, "Ativo"),
    Inativo(2, "Inativo");

    private final int codigo;
    private final String descricao;

    TipoSituacaoEnum(int codigo, String descricao) {
        this.codigo = codigo;
        this.descricao = descricao;
    }

    public int getCodigo() {
        return codigo;
    }

    public String getDescricao() {
        return descricao;
    }
}
