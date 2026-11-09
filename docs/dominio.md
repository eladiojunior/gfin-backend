# Domínio e persistência

O modelo suporta cadastro de usuários, naturezas, correntistas, contas, cartões, despesas e receitas. Valores persistidos usam `BigDecimal` em várias entidades, mas alguns models de entrada usam `double`. IDs de usuário são atribuídos pelo backend na criação de registros e históricos. Naturezas e demais registros são particulares ao usuário do JWT; a listagem de usuários retorna somente o próprio perfil.

| Entidade | Coleção | Papel |
|---|---|---|
| UsuarioEntity | tb_usuario_sistema | Identidade, senha e situação |
| NaturezaContaEntity | tb_natureza_conta | Classificação de despesa ou receita |
| CorrentistaEntity | tb_agenda_correntista | Dados de correntista |
| ContaCorrenteEntity | tb_conta_corrente | Conta do usuário |
| CartaoCreditoEntity | tb_cartao_credito | Cartão, com conta opcional |
| DespesaFixaEntity / ReceitaFixaEntity | tb_despesa_fixa / tb_receita_fixa | Cadastros recorrentes |
| HistoricoDespesaFixaEntity / HistoricoReceitaFixaEntity | tb_historico_despesa_fixa / tb_historico_receita_fixa | Histórico de valores das fixas |
| DespesaMensalEntity / ReceitaMensalEntity | tb_despesa_mensal / tb_receita_mensal | Lançamentos, com vínculo opcional à fixa |

Os campos estão nas entities MongoDB. IDs escalares não criam chaves estrangeiras; integridade referencial depende da aplicação. Fixas ativas são processadas automaticamente para gerar lançamentos do mês corrente.

## Regras observadas

- Fixas ativas são listadas por usuário; sua criação registra histórico. A edição pode registrar novo histórico com `hasGravarHistorico=true`. Exclusão de fixa com lançamento mensal vinculado inativa; sem vínculo remove a fixa e seu histórico.
- Receitas e despesas mensais são listadas pelo mês da data de recebimento ou vencimento e usuário. São criadas não liquidadas; uma vez liquidadas não podem ser alteradas, liquidadas novamente ou excluídas. Liquidação registra data/hora atual e forma; o total informado positivo é usado, senão usa o valor original.
- Uma despesa mensal com `numeroParcelas > 1` gera N lançamentos, com o valor total dividido em centavos, restante na última parcela, descrição numerada e vencimento incrementado mensalmente. O agrupador é calculado em segundos; não é garantia de unicidade.
- Naturezas exigem nome/descrição e tipo de lançamento válido; exclusão inativa se houver vínculos consultados, senão apaga. Lançamentos verificam existência e situação da natureza; confira validações exatas de cada operação no service.
- Conta corrente com cartão vinculado não pode ser excluída. Cartão aceita conta opcional e validade `MM/yyyy`. Usuário excluído é inativado.

## Códigos

| Enum | Valores |
|---|---|
| Situação geral | 1 ativo; 2 inativo |
| Situação cartão | 1 ativo; 2 bloqueado; 3 cancelado |
| Tipo lançamento | 1 despesa; 2 receita |
| Forma de lançamento | Documento do usuário; tipo 1 despesa ou 2 receita e situação 1 ativa ou 2 inativa |

Formas de lançamento são documentos em `tb_forma_lancamento`, referenciados por `idFormaLancamento`. Regras e campos precisos estão em `services/`, `enums/` e `models/`.


## Dados iniciais do usuário

O cadastro cria formas ativas particulares: despesa — Dinheiro, Cartão de Crédito/Débito, Boleto de Cobrança, Débito em Conta e Fatura; receita — Não Informado, Dinheiro, Transferência, Cheque e Crédito em Conta. Esses nomes reproduzem os antigos enums, inclusive Não Informado.

Cria também naturezas ativas: despesa — Alimentação, Educação, Impostos e Taxas, Lazer, Moradia, Saúde, Transporte e Outros; receita — Salário, Investimento, Presente, Trabalho Extra e Outros. A descrição inicial é igual ao nome. Cada usuário recebe cópias próprias, que pode alterar.

As gravações abrangem vários documentos e não são transacionais no MongoDB standalone. Se a inicialização falhar, o serviço tenta apagar os dados criados e o usuário, mas a limpeza também pode falhar; nesse caso pode ser necessária recuperação manual. A inicialização só ocorre em novos cadastros.

## Processamento de lançamentos fixos

O scheduler executa no intervalo definido por `gfin.processamento.intervalo` (padr?o `30m`) e a criação de uma fixa dispara seu processamento imediatamente. O fuso é `America/Sao_Paulo` por padrão, configurável por `gfin.processamento.fuso`. Apenas o mês corrente é processado; não há recuperação automática de meses passados. Dias superiores ao último dia do mês são ajustados ao último dia. Valores, descrição, natureza, usuário e vínculo com a fixa são copiados; a despesa copia também a forma de lançamento. Receita fixa não possui forma, portanto a receita gerada fica sem forma até sua liquidação. Os lançamentos são criados não liquidados.

O processamento confere se já existe mensal vinculado à fixa e ao usuário no mês; o `_id` determinístico de geração também evita duplicidade entre processadores concorrentes. Falhas individuais são registradas no console e tentadas novamente na execução seguinte. Exclusões de mensais não liquidadas podem ser revertidas pela próxima execução, se a fixa permanecer ativa. A verificação e a criação de mensal manual vinculada não são uma operação atômica conjunta com o job.

A referência C# recalcula o valor da despesa fixa com base nos pagamentos anteriores; essa alteração automática de valor não foi adotada. O serviço não possui histórico persistente da execução: início, resultado por registro, resumo e falhas são enviados ao log.
