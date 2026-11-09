# API HTTP

IDs de documentos são strings hexadecimais de 24 caracteres (ObjectId), inclusive referências e o subject do JWT. Prefixo de todas as rotas abaixo: `/api/v1`. JSON é declarado em resources. `POST /usuarios` e `POST /auth/login` são públicos; os demais resources exigem grupo JWT `usuario`. Envie `Authorization: Bearer <accessToken>`. O login recebe `emailUsuario` e `senhaUsuario`, e retorna `accessToken`, `tokenType`, `expiresIn`, `nomeUsuario`, `emailUsuario`. O token usa ID do usuário como subject, emissor configurado e expiração padrão de 3600 segundos. Login normaliza o e-mail consultado para minúsculas; cadastro não o faz explicitamente.

| Recurso | Rotas (relativas ao prefixo) |
|---|---|
| Autenticação | `POST /auth/login` |
| Usuários | `GET, POST /usuarios`; `GET, PUT, DELETE /usuarios/{id}` |
| Naturezas | `GET, POST /natureza-conta`; `GET, PUT, DELETE /natureza-conta/{id}` |
| Correntistas | `GET, POST /correntistas`; `GET, PUT, DELETE /correntistas/{id}` |
| Contas | `GET, POST /contas-correntes`; `GET, PUT, DELETE /contas-correntes/{id}`; `PATCH /contas-correntes/{id}/ativar` e `/desativar` |
| Cartões | `GET, POST /cartoes`; `GET, PUT, DELETE /cartoes/{id}`; `PATCH /cartoes/{id}/situacao/{codigoSituacao}` |
| Formas de lançamento | `GET, POST /formas-lancamento`; `GET, PUT, DELETE /formas-lancamento/{id}` |
| Despesas | `GET, POST /despesas/fixas`; `GET, PUT, DELETE /despesas/fixas/{id}`; `GET, POST /despesas/mensais`; `GET, PUT, DELETE /despesas/mensais/{id}`; `POST /despesas/mensais/{id}/liquidar` |
| Receitas | `GET, POST /receitas/fixas`; `GET, PUT, DELETE /receitas/fixas/{id}`; `GET, POST /receitas/mensais`; `GET, PUT, DELETE /receitas/mensais/{id}`; `POST /receitas/mensais/{id}/liquidar` |

Listagens mensais recebem `mes` e `ano` na query. Edição de fixa aceita `hasGravarHistorico` (booleano). Consulte os models de edição e liquidação para campos e tipos; OpenAPI é gerada das anotações, mas não substitui a leitura das validações nos services. Data de vencimento/recebimento mensal usa `yyyy-MM-dd`; validade de cartão usa `MM/yyyy`. O POST de despesa mensal retorna lista, inclusive para uma parcela; POST de receita mensal retorna objeto.

Em geral, sucesso retorna 200, inclusive criação e exclusão; `ServiceException` é traduzida em 400 e falhas inesperadas em 500, com corpo contendo `message`. Há diferenças entre resources e respostas de autenticação/autorização são do framework. PUT aplica atualizações parciais condicionais em vários services: valores ausentes ou zero podem ser ignorados. Não presuma semântica de substituição completa. Erros inesperados podem expor mensagens internas; consulte [riscos](riscos.md).

As operações autenticadas usam o subject do JWT como ID do proprietário. Listagens, consultas por ID, alterações, liquidações e exclusões são limitadas ao usuário; uma referência a documento de outro usuário é rejeitada. `GET /usuarios` retorna somente o usuário autenticado.

Formas de lançamento são particulares por usuário: `codigoLancamentoConta` é 1 (despesa) ou 2 (receita), `codigoSituacaoFormaLancamento` é 1 (ativa) ou 2 (inativa). O cadastro exige nome, tipo e situação. Lançamentos e liquidações usam `idFormaLancamento` em vez dos antigos códigos dos enums; uma forma precisa estar ativa, ser do tipo correto e pertencer ao usuário. Exclusão de forma vinculada a lançamentos a inativa.

Na criação de despesa mensal, `valorDespesa` é o valor total, com até duas casas decimais; quando há parcelas, o total é dividido em centavos e o restante fica na última. Valores já registrados não são alterados.

## Estorno de liquidação

`POST /api/v1/despesas/mensais/{id}/estornar` e `POST /api/v1/receitas/mensais/{id}/estornar` não exigem corpo e retornam o lançamento atualizado com status 200. É preciso estar autenticado e ser proprietário; lançamento inexistente, de outro usuário, pendente ou alterado concorrentemente resulta em 400. Repetir o estorno de um lançamento pendente não cria outro histórico.

O frontend consulta `GET /api/v1/despesas/mensais/{id}/historico-estornos` ou `GET /api/v1/receitas/mensais/{id}/historico-estornos`; a resposta é uma lista, vazia se não houver estornos. Cada item contém `dataHoraEstorno`, `dataHoraLiquidacao` (UTC ISO-8601), `idFormaLancamento`, `codigoVinculoFormaLiquidacao`, `valorTotalLiquidacao`, `valorDescontoLiquidacao` e `valorMultaJurosLiquidacao`, quando disponíveis. O histórico é armazenado no lançamento, juntamente com a mudança para pendente. O lançamento conserva forma e vínculo; os campos da liquidação corrente são limpos. Exclusão física de lançamento com histórico de estorno é bloqueada.
