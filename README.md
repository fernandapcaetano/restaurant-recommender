# Agente de Recomendação Inteligente de Restaurantes com RAG + Vetores

Descrição
---------
Agente inteligente capaz de recomendar restaurantes personalizados com base no histórico do usuário, usando embeddings, vector search e RAG. O sistema faz ranking e utiliza dados simulados de restaurantes e pedidos.

Principais funcionalidades
-------------------------
- Gera embeddings para descrições de restaurantes.
- Indexação vetorial e busca por similaridade (vector search, HNSW).
- RAG (Retrieval-Augmented Generation) usando um modelo de chat para resumir preferências do usuário.
- Ranking de recomendações.
- Carregamento de dados simulados (CSV) de restaurantes e pedidos.

Tecnologias
-----------
- Java, Spring Boot, Maven
- PostgreSQL com extensão `vector`
- Spring AI (EmbeddingModel, ChatClient)
- Apache Commons CSV
- HNSW vector index (criado via `schema.sql`)

Arquivos relevantes
-------------------
- `src/main/java/.../application/service/LoadDataService.java` — carrega CSVs, gera embeddings e salva no banco.
- `src/main/java/.../application/service/RecommenderService.java` — gera resumo via chat, cria embedding do resumo e busca restaurantes similares.
- `src/main/resources/schema.sql` — cria tabelas `restaurant` e `order`, cria índice HNSW.
- `src/main/java/.../api/controller/RecommenderController.java` — endpoints HTTP.

Requisitos
---------
- Java 17+ (ou compatível)
- Maven 3+
- PostgreSQL com extensão `vector` habilitada
- Credenciais/configuração para provider de embeddings/chat (OpenRouter)

Configuração (exemplo)
----------------------
Adicione no `application.yml` ou `application.properties` (valores de exemplo):

- `spring.datasource.url=jdbc:postgresql://localhost:5432/recommender`
- `spring.datasource.username=<user>`
- `spring.datasource.password=<pass>`
- `data.restaurant.path=classpath:data/restaurants.csv`
- `data.order.path=classpath:data/orders.csv`

Configure provider do Spring AI (ex.: OpenAI) conforme sua implementação e chaves.

Banco de dados
--------------
Execute `src/main/resources/schema.sql` no banco PostgreSQL para criar as tabelas e o índice HNSW (vetor de dimensão 1536).

Como rodar (Windows)
--------------------
- Compilar: `mvn clean package`
- Rodar: `mvn spring-boot:run` ou `java -jar target/<artifact>.jar`

Endpoints
---------
- `POST /recommender` — carrega os CSVs, gera embeddings, salva restaurantes e pedidos (executa `LoadDataService.execute`).
- `GET  /recommender` — retorna recomendações baseadas nos últimos pedidos (executa `RecommenderService.execute`).

Observações importantes
----------------------
- O loader usa encoding `ISO_8859_1` para os CSVs e filtra por `Country Code == 30`.
- Embeddings têm dimensão 1536 (conforme `schema.sql`).
- `LoadDataService.execute` limpa (`TRUNCATE`) as tabelas antes de inserir dados.
- `RecommenderService` gera um resumo via `ChatClient`, converte o resumo em embedding e busca os 5 restaurantes mais similares com filtro por cidade predominante.
- Se não houver pedidos, a API retorna mensagem indicando que não há dados para recomendar.

Mensagens de retorno notáveis
----------------------------
- Sem pedidos: `"No orders found to base recommendations on."`
- Falha ao gerar resumo: `"Could not generate recommendations based on past orders."`

Licença
-------
Projeto para fins educacionais/demonstração.
