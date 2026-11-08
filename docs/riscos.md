# Riscos e decisões pendentes

Observações estáticas do código, não uma auditoria ou comprovação por teste. Priorize validação e correção antes de expor a aplicação a usuários reais.

1. **Dados legados:** documentos sem `idUsuario` não são visíveis nas operações autenticadas. Naturezas anteriormente compartilhadas e históricos antigos exigem migração auditada; não atribua proprietários por suposição. O isolamento foi testado em MongoDB em memória; valide também na instância real.
2. **Segredos e chaves:** A configuração anterior continha credenciais SQL; a configuração atual aponta para chave privada dentro de `META-INF/resources`, diretório de recursos estáticos; há arquivos PEM na árvore fonte. Tratar credenciais/chaves como comprometidas, avaliar exposição HTTP e histórico, rotacionar e mover segredos para armazenamento/configuração apropriados. Não copiar seus valores.
3. **Senha:** cadastro usa SHA-512 com salt, mas o laço em `UtilHelper.gerarHashPassword` calcula repetidamente o mesmo digest, sem encadear o resultado. `UsuarioService.alterarUsuario` atribui a nova senha diretamente, ao contrário do cadastro; isso pode impedir login e persistir senha em texto puro. Planejar migração compatível para algoritmo de hash de senha apropriado e correção do fluxo.
4. **Transações e erros:** o MongoDB standalone não oferece transações multidocumento; falhas em operações com parcelas ou histórico podem causar gravações parciais. Erros 500 retornam mensagem da exceção ao cliente.
5. **Integridade financeira:** valores de entrada `double` podem introduzir imprecisão; avaliar contrato decimal. O código de agrupamento de parcelas baseado em segundo pode colidir. Listagens mensais usam intervalo de início inclusivo e fim exclusivo. Verificar regras de datas e fuso.
6. **Cobertura e operação:** a cobertura de testes é inicial e não há migração de dados SQL implementada, e o processamento recorrente precisa de validação na instância MongoDB real. Validar a importação do SQL Server, política de backup, observabilidade, HTTPS, regras de revogação JWT e ciclo de vida das chaves antes de produção.

Questões de produto a confirmar: se valor de despesa parcelada representa cada parcela; geração automática de lançamentos; efeitos da liquidação sobre saldos/contas; requisitos de faturas, relatórios e moedas. Não inferir essas capacidades apenas pelo objetivo de gestão financeira.


