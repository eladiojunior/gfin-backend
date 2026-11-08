# Persistência MongoDB

A aplicação usa uma coleção por entity, nomeada como a antiga tabela SQL (`tb_usuario_sistema`, `tb_natureza_conta`, `tb_agenda_correntista`, `tb_conta_corrente`, `tb_cartao_credito`, `tb_despesa_fixa`, `tb_despesa_mensal`, `tb_historico_despesa_fixa`, `tb_receita_fixa`, `tb_receita_mensal`, `tb_historico_receita_fixa`). Os campos dos documentos correspondem aos nomes Java das entities, com `_id` BSON ObjectId. Referências são IDs escalares; MongoDB não aplica integridade referencial automaticamente.

Novos documentos recebem ObjectId; a API, o JWT e os campos de referência usam sua representação hexadecimal de 24 caracteres. Documentos antigos com IDs numéricos não são compatíveis e não são migrados automaticamente. Valores monetários das entidades usam `BigDecimal`; modelos da API ainda usam `double`.

A instância identificada como standalone não oferece transações entre documentos. Criação de parcelas e histórico podem ser parciais; a aplicação não implementa compensação. Trate consistência antes de usar em produção.

O backend filtra documentos pelo `idUsuario` do JWT, inclusive naturezas. Documentos antigos sem proprietário não aparecem; revise e migre-os explicitamente antes da troca. Novos históricos registram `idUsuario`.

Os nomes de cole??es s?o min?sculos e mant?m o prefixo `tb_`. MongoDB diferencia mai?sculas de min?sculas: cole??es antigas em mai?sculas n?o s?o renomeadas nem acessadas automaticamente.
