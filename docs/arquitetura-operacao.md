# Arquitetura e operação

Aplicação Java 21, Quarkus 3.33.1, Quarkus REST, MongoDB com Panache, SmallRye JWT, OpenAPI e Health. MapStruct gera os mapeamentos na compilação. A raiz REST é `/api/v1`.

- `resource/`: endpoints e tratamento de erros; `services/`: regras e acesso a documentos.
- `entites/`: documentos MongoDB e `DocumentoBase`, que usa identificadores ObjectId.
- `models/`: contratos de entrada e saída; `helper/MapperHelper.java`: conversões.
- `src/main/resources/application.properties`: configuração. O script SQL e dataset antigos são legados, não alimentam o MongoDB.

## Execução

Configure `QUARKUS_MONGODB_CONNECTION_STRING` com a URI autenticada do MongoDB, incluindo `authSource` se necessário, e opcionalmente `QUARKUS_MONGODB_DATABASE`. O endereço padrão é `192.168.1.21:27017`, banco `DB_GFIN`; o servidor informado exige autenticação. Proteja as credenciais e configure TLS conforme o ambiente. A aplicação não importa os dados do SQL Server automaticamente.

Com JDK 21 e Maven instalado, use `mvn compile quarkus:dev`, `mvn package` e `java -jar target/quarkus-app/quarkus-run.jar`. A porta padrão é 8080. O Maven Wrapper deste checkout está incompleto: falta `.mvn/wrapper/maven-wrapper.properties`. OpenAPI usa `/q/openapi`; Swagger UI normalmente usa `/q/swagger-ui`, conforme perfil/configuração.

Os documentos usam `_id` BSON ObjectId. A API e o JWT representam IDs como strings hexadecimais de 24 caracteres; referências entre documentos são strings. Dados criados no formato antigo não são compatíveis.


## Testes e consistência

`mvn test` usa por padrão um servidor compatível com MongoDB em memória, iniciado e encerrado pelo teste. Opcionalmente, `GFIN_TEST_MONGODB_URI` aponta para uma instância externa: nesse caso um banco aleatório `gfin_test_<uuid>` é criado e removido ao final, exigindo permissão de criação e remoção. Não use dados reais de produção. O servidor em memória não substitui validação na versão real do MongoDB.

O servidor informado se apresentou como standalone. Não há transações multidocumento configuradas: falhas podem deixar gravações parciais em operações de parcelas e históricos. Valide esse requisito antes de produção. O repositório analisado não expõe metadados `.git`; não confunda `target/` com código-fonte.
