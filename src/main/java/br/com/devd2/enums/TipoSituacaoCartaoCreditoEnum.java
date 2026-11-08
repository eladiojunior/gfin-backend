package br.com.devd2.enums;

public enum TipoSituacaoCartaoCreditoEnum {
    Ativo(1, "Ativo"),
    Bloqueado(2, "Bloqueado"),
    Cancelado(3, "Cancelado");

    private final int codigo;
    private final String descricao;

    TipoSituacaoCartaoCreditoEnum(int codigo, String descricao) {
        this.codigo = codigo;
        this.descricao = descricao;
    }

    public int getCodigo() {
        return codigo;
    }

    public String getDescricao() {
        return descricao;
    }

    public static boolean hasSituacao(int codigo) {
        for (var item : TipoSituacaoCartaoCreditoEnum.values()) {
            if (item.codigo == codigo) {
                return true;
            }
        }
        return false;
    }
}
