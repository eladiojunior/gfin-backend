# GFin Backend — índice para consulta

Esta documentação descreve o código após a adaptação inicial para MongoDB em 04/11/2026. O objetivo informado é servir ao aplicativo mobile de gestão financeira pessoal. O código-fonte e a configuração em vigor são a fonte de verdade: confirme-os antes de implementar uma mudança. Itens de risco e possibilidades futuras não representam funcionalidades prontas.

- [Arquitetura e operação](arquitetura-operacao.md): tecnologias, estrutura, execução e testes.
- [MongoDB](mongodb.md): coleções, IDs e migração de dados.
- [API](api.md): autenticação, rotas, parâmetros e contratos.
- [Domínio e persistência](dominio.md): entidades, regras e códigos.
- [Riscos e trabalho futuro](riscos.md): problemas observados e decisões pendentes.

Para investigar um comportamento, comece pelo resource da rota, siga para o service, os models, `MapperHelper` e as entities. Os nomes do pacote `entites` e da classe `GfinAplication` estão escritos assim no código. Não interprete o README da raiz como referência atual de versão: o `pom.xml` declara Quarkus 3.33.1.

