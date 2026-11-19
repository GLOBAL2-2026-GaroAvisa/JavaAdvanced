# GaroAvisa — API Principal

Backend em Java com Spring Boot da CitizenTech **GaroAvisa**, um aplicativo de clima
que avisa o cidadão sobre o que muda no tempo dos lugares em que ele realmente está.

> **Global Solution 2026/2 · CitizenTech — Java Advanced**
> Este repositório é a API principal. O Serviço de Clima fica em
> [Clima_JavaAdvanced](https://github.com/GLOBAL2-2026-GaroAvisa/Clima_JavaAdvanced).

---

## Integrantes

| Nome | RM | Turma |
|---|---|---|
| Enzo Vieira Bernardini | RM563000 | 2TDSPI |
| Eduardo Batista Locaspi | RM561713 | 2TDSPI |
| Liana Lyumi Morisita Fujisima | RM565698 | 2TDSPI |
| Victor Alves Lopes | RM561833 | 2TDSPI |

---

## O problema e a solução

Aplicativos de clima falam de cidades. O cidadão vive em **locais**: a casa, o trabalho,
a praia onde ele corre no sábado. Numa metrópole como São Paulo, chove num bairro e não
chove no outro, e a previsão da "cidade" não ajuda quem precisa decidir se leva
guarda-chuva para o compromisso das 15h.

O GaroAvisa resolve isso em três movimentos:

1. **O cidadão cadastra os Locais dele**, com coordenada exata, não com o nome da cidade.
2. **O sistema acompanha a previsão desses Locais sozinho**, de hora em hora, e detecta
   o que mudou desde a última vez — porque a informação útil não é "vai chover", é
   "mudou desde que você olhou".
3. **A IA explica o dia em duas ou três frases**, em linguagem simples, considerando o
   que o próprio cidadão contou sobre si.

Também existem **Compromissos**, que são os eventos do cidadão ligados a um Local e a
uma data, para que os avisos cheguem antes do que importa.

---

## Deploy

| Serviço | URL |
|---|---|
| API Principal | https://garoavisa-api.onrender.com |
| Swagger | https://garoavisa-api.onrender.com/swagger-ui.html |
| Serviço de Clima | https://gl-clima-api-fwbvhnbhb4a4bcer.brazilsouth-01.azurewebsites.net |

> A API está no plano gratuito do Render, que desliga o serviço após 15 minutos sem uso.
> A primeira chamada depois disso pode levar alguns minutos para responder.

---

## Tecnologias

| Camada | Tecnologia |
|---|---|
| Linguagem | Java 21 |
| Framework | Spring Boot 4.1.1 |
| Persistência | Spring Data JPA · Hibernate 7.4.5 |
| Banco | Oracle 19.3 (FIAP) |
| Migrations | Flyway |
| Segurança | Spring Security · OAuth2 Resource Server · JWT HS256 · BCrypt |
| IA generativa | Spring AI 2.0.2 · OpenAI |
| Mensageria | Spring AMQP · RabbitMQ (CloudAMQP) |
| Chamada síncrona | RestClient |
| Documentação | springdoc-openapi 3.0.3 |
| Build | Maven |

---

## Arquitetura

A solução é composta por **dois serviços** que conversam de duas formas diferentes,
cada uma escolhida pelo que a situação exige.

```
                    ┌─────────────────────────────┐
                    │        App Mobile           │
                    │      (React Native)         │
                    └──────────────┬──────────────┘
                                   │ REST + JWT
                                   ▼
        ┌──────────────────────────────────────────────────┐
        │              API PRINCIPAL  (Render)             │
        │                                                  │
        │   Usuários · Locais · Compromissos · Previsões   │
        │   Alertas · Dicas · Mudanças · Explicação do Dia │
        └───┬──────────────┬───────────────────┬───────────┘
            │              │                   │
            │ JDBC         │ AMQP              │ HTTP (RestClient)
            ▼              ▼                   ▼
    ┌───────────────┐  ┌──────────────┐  ┌─────────────────────────┐
    │  Oracle FIAP  │  │   RabbitMQ   │  │   SERVIÇO DE CLIMA      │
    │               │  │  (CloudAMQP) │  │       (Azure)           │
    └───────────────┘  └──────┬───────┘  │                         │
                              │          │   Adaptador Open-Meteo  │
                              └─────────▶│                         │
                                 AMQP    └───────────┬─────────────┘
                                                     │ HTTPS
                                                     ▼
                                              ┌─────────────┐
                                              │ Open-Meteo  │
                                              └─────────────┘
```

### Por que dois serviços

O Serviço de Clima não tem banco, não tem login e não conhece usuário nenhum. Ele é um
**adaptador**: recebe uma coordenada, pergunta ao Open-Meteo e devolve a previsão num
formato nosso. Isolá-lo significa que trocar de provedor de clima no futuro não encosta
na API principal.

### As duas formas de comunicação

**Assíncrona (RabbitMQ)** — para o trabalho de fundo. Um agendador publica, de tempos em
tempos, um pedido de previsão para cada endereço cadastrado. O Serviço de Clima consome,
busca e devolve o resultado por outro evento. Ninguém espera ninguém.

| Evento | Rota | Direção |
|---|---|---|
| Previsão solicitada | `previsao.solicitada.*` | API → Clima |
| Previsão atualizada | `previsao.atualizada` | Clima → API |

Exchange `garoavisa.eventos` do tipo *topic*, filas duráveis, **DLQ** em ambos os lados e
**consumidor idempotente** — se a mesma mensagem chegar duas vezes, o resultado é o mesmo.

**Síncrona (RestClient)** — para a Pesquisa. Quando o cidadão busca um lugar que ainda não
é um Local salvo, não existe histórico e ninguém pode esperar: a API chama o Serviço de
Clima na hora e responde com o resultado. Se o Clima não responder, a API devolve
`503` com uma mensagem clara em vez de quebrar.

---

## Requisito 5 — opção escolhida

**Opção A: mensageria assíncrona com RabbitMQ.**

Evento publicado e consumido de forma assíncrona nos dois sentidos, com exchange *topic*,
filas duráveis, dead letter queue e consumidor idempotente.

O projeto também tem **dois serviços** se comunicando por **RestClient**, o que o aproxima
da opção B, mas **não usa Eureka**: com apenas dois serviços de endereço fixo, um registro
de serviços adicionaria uma peça de infraestrutura sem resolver nenhum problema que a
gente tivesse. A opção declarada para avaliação é a **A**.

---

## Modelo de dados

Oito entidades, todas com relacionamento.

```
  USUARIO ───< LOCAL >─── ENDERECO ───< PREVISAO
     │           │            │
     │           │            ├──< MUDANCA_PREVISAO
     │           │            └──< ALERTA_CLIMA
     │           │
     │           └──< COMPROMISSO
     │
     └──< EXPLICACAO_DIA
```

| Entidade | Papel |
|---|---|
| `Usuario` | Conta do cidadão, com o texto "Sobre mim" que personaliza a IA |
| `Local` | O apelido que o cidadão dá a um endereço: "Casa", "Trabalho" |
| `Endereco` | A coordenada em si. Dois cidadãos no mesmo prédio compartilham um endereço, e a previsão é buscada uma vez só |
| `Previsao` | O retorno bruto do Open-Meteo, guardado como JSON |
| `MudancaPrevisao` | O que mudou desde a última busca, com o motivo da mudança |
| `AlertaClima` | Alertas de chuva, tempestade, calor, frio, rajada e UV |
| `Compromisso` | Evento do cidadão, ligado a um Local e a uma data |
| `ExplicacaoDia` | Cache do texto gerado pela IA, por usuário, local e dia |

A separação entre **Local** e **Endereco** é a decisão de modelagem mais importante do
projeto: ela evita buscar a mesma previsão várias vezes quando vários cidadãos cadastram
o mesmo lugar.

---

## A funcionalidade com IA generativa

### Explicação do Dia

Em vez de obrigar o cidadão a interpretar uma grade de números — umidade, índice UV,
rajada em km/h —, a IA escreve duas ou três frases sobre o dia dele:

> "O dia começa nublado e esquenta até 27° à tarde, com 100% de chance de chuva.
> Vale levar guarda-chuva se você sair depois do almoço."

**Não é um chat genérico.** O modelo não conversa com o usuário e não recebe pergunta
livre: ele recebe apenas os números daquele dia naquele Local, formatados por
`PromptExplicacao`, e tem instrução explícita de usar somente o que foi fornecido.

**Personalização.** Se o cidadão preencheu o campo "Sobre mim" — por exemplo, que corre de
manhã ou que tem sensibilidade ao sol —, esse texto entra no prompt e muda o que a IA
destaca.

**Cache por usuário, Local e dia.** O texto é gerado uma vez por dia e guardado. As demais
visitas à Home não custam nada. Editar o "Sobre mim" descarta o texto guardado, e o
próximo acesso gera um novo.

**Degradação elegante.** Se a OpenAI não responder, o sistema não mostra erro: um gerador
por regras monta o texto com os mesmos dados, e a resposta marca `geradaPorIa: false`
para que o aplicativo apague o selo de IA. **O cidadão nunca fica sem explicação.**

**Endpoint separado.** A Explicação do Dia é um `GET /home/explicacao` próprio, e não um
campo que a Home espera ficar pronto. A Home carrega instantaneamente; a explicação chega
depois, sem travar a tela.

---

## Outras funcionalidades

| Funcionalidade | O que faz |
|---|---|
| **Home** | A tela inicial inteira numa chamada só: clima do Local principal, 24 horas, alertas, dicas, mudanças e próximos compromissos |
| **Alertas** | Seis indicadores (chuva, tempestade, calor, frio, rajada, UV) em duas gravidades. Um alerta é **desativado**, nunca apagado, para que um alerta que vai e volta não seja anunciado como novo duas vezes |
| **Mudanças bruscas** | Compara a previsão nova com a anterior e registra o que mudou, com o motivo: alerta novo, mudança de céu, de chuva ou de temperatura |
| **Dicas do dia** | "Use protetor solar", "Leve guarda-chuva" — derivadas das mesmas regras |
| **Pesquisa** | Clima de um lugar que ainda não é um Local salvo, buscado na hora |
| **Compromissos** | Eventos do cidadão por Local e data |

---

## Endpoints principais

Documentação interativa completa no **Swagger**: `/swagger-ui.html`

### Autenticação

| Método | Rota | Token | Descrição |
|---|---|---|---|
| `POST` | `/auth/cadastro` | Não | Cria a conta e já devolve o token |
| `POST` | `/auth/login` | Não | Autentica e devolve o token |

### Locais

| Método | Rota | Token | Descrição |
|---|---|---|---|
| `GET` | `/locais` | Sim | Lista paginada, com busca por apelido ou nome |
| `POST` | `/locais` | Sim | Cadastra um Local |
| `PUT` | `/locais/{id}` | Sim | Edita apelido e configurações |
| `PATCH` | `/locais/{id}/alertas` | Sim | Liga ou desliga os alertas do Local |
| `DELETE` | `/locais/{id}` | Sim | Exclui o Local |
| `GET` | `/locais/{id}/previsao` | Sim | Previsão completa do Local |

### Compromissos

| Método | Rota | Token | Descrição |
|---|---|---|---|
| `GET` | `/compromissos` | Sim | Próximos compromissos, paginado |
| `POST` | `/locais/{localId}/compromissos` | Sim | Cria um compromisso no Local |
| `PUT` | `/compromissos/{id}` | Sim | Edita |
| `DELETE` | `/compromissos/{id}` | Sim | Exclui |

### Usuário

| Método | Rota | Token | Descrição |
|---|---|---|---|
| `GET` | `/usuarios/me` | Sim | Dados do perfil |
| `PATCH` | `/usuarios/me/nome` | Sim | Altera o nome |
| `PATCH` | `/usuarios/me/perfil-ia` | Sim | Altera o "Sobre mim" que personaliza a IA |
| `PATCH` | `/usuarios/me/senha` | Sim | Troca a senha |
| `DELETE` | `/usuarios/me` | Sim | Exclui a conta e tudo dela |

### Telas compostas

| Método | Rota | Token | Descrição |
|---|---|---|---|
| `GET` | `/home` | Sim | A tela inicial inteira |
| `GET` | `/home/explicacao` | Sim | A Explicação do Dia gerada pela IA |
| `GET` | `/previsao` | Sim | Pesquisa por coordenada, buscada na hora |

### Monitoramento

| Método | Rota | Token | Descrição |
|---|---|---|---|
| `GET` | `/actuator/health` | Não | Saúde da aplicação |

---

## Usuários de teste

Senha de todos: `garoavisa123`

| E-mail | O que tem |
|---|---|
| `carlos.silva@email.com` | Vários Locais, compromissos, alertas e mudanças — o melhor para ver a Home cheia |
| `ana.souza@email.com` | Um Local |
| `bruno.lima@email.com` | Nenhum Local — serve para testar a Home vazia |

Exemplo de login:

```json
POST /auth/login
{
  "email": "carlos.silva@email.com",
  "senha": "garoavisa123"
}
```

A resposta traz o token, que vai no cabeçalho das rotas protegidas:

```
Authorization: Bearer <token>
```

---

## Como rodar

### Pré-requisitos

- Java 21
- Maven (ou o wrapper `mvnw` que já vem no projeto)
- Acesso a um banco Oracle
- Uma instância RabbitMQ (o plano gratuito do CloudAMQP serve)
- O Serviço de Clima rodando — veja o repositório
  [Clima_JavaAdvanced](https://github.com/GLOBAL2-2026-GaroAvisa/Clima_JavaAdvanced)

### 1. Clonar

```bash
git clone https://github.com/GLOBAL2-2026-GaroAvisa/JavaAdvanced.git
cd JavaAdvanced
```

### 2. Configurar as variáveis de ambiente

**Nenhuma credencial fica no código ou no repositório.** Todas são lidas do ambiente.

| Variável | Para que serve |
|---|---|
| `DB_USERNAME` | Usuário do Oracle |
| `DB_PASSWORD` | Senha do Oracle |
| `JWT_SECRET` | Chave de assinatura do token — mínimo de 32 caracteres |
| `RABBITMQ_URL` | URL AMQP do CloudAMQP |
| `CLIMA_URL` | Endereço do Serviço de Clima, sem barra no final |
| `OPENAI_API_KEY` | Chave da OpenAI |

No Linux ou macOS:

```bash
export DB_USERNAME=seu_usuario
export DB_PASSWORD=sua_senha
export JWT_SECRET=uma-chave-longa-com-pelo-menos-32-caracteres
export RABBITMQ_URL=amqps://usuario:senha@host/vhost
export CLIMA_URL=http://localhost:8081
export OPENAI_API_KEY=sua-chave
```

No Windows (PowerShell):

```powershell
$env:DB_USERNAME="seu_usuario"
$env:DB_PASSWORD="sua_senha"
$env:JWT_SECRET="uma-chave-longa-com-pelo-menos-32-caracteres"
$env:RABBITMQ_URL="amqps://usuario:senha@host/vhost"
$env:CLIMA_URL="http://localhost:8081"
$env:OPENAI_API_KEY="sua-chave"
```

No Eclipse ou IntelliJ, as mesmas variáveis vão em **Run Configurations → Environment**.

> **Sem a `OPENAI_API_KEY` a aplicação sobe normalmente.** A Explicação do Dia passa a
> usar o gerador por regras, e a resposta marca `geradaPorIa: false`. É de propósito.

### 3. Rodar

```bash
./mvnw spring-boot:run
```

No Windows:

```powershell
.\mvnw.cmd spring-boot:run
```

O **Flyway cria e popula o banco sozinho** na primeira execução, incluindo os usuários
de teste. Não é preciso rodar nenhum script à mão.

A API sobe em `http://localhost:8080`, e o Swagger fica em
`http://localhost:8080/swagger-ui.html`.

---

## Decisões de arquitetura

Algumas escolhas deste projeto não são óbvias, e cada uma tem um motivo.

**O motor de alertas ficou na API, não no Serviço de Clima.** O Clima não tem banco, então
não consegue responder à pergunta que importa — "esse alerta é novo?". Além disso, os
limiares são regra de produto, não de meteorologia, e as mesmas regras são usadas por dois
caminhos que vivem na API.

**O alerta é desativado, nunca apagado.** Um alerta de chuva que aparece hoje, some amanhã
e volta depois não deve ser anunciado como novidade três vezes. Manter a linha desativada
preserva a memória de que aquilo já foi avisado.

**A Explicação do Dia tem endpoint próprio.** Gerar o texto leva alguns segundos. Se fosse
um campo da Home, a tela inteira esperaria pela IA. Separando, a Home abre na hora e a
explicação chega depois.

**O Serviço de Clima está no Azure, e não no Render junto com a API.** A API gratuita do
Open-Meteo limita as chamadas **por endereço IP**, e o plano gratuito do Render compartilha
o IP de saída entre muitos aplicativos. Na prática, a cota diária daquele IP era consumida
por outros projetos e o Open-Meteo recusava as nossas chamadas com `429`. Comprovamos que
não era consumo nosso: a fila mostrava taxa zero e um total de mensagens muito abaixo do
limite. Trocar a região do Render não resolveu — o IP europeu também estava esgotado.
Mover o único serviço que fala com o Open-Meteo para outra nuvem resolveu.

**Sem Eureka.** Dois serviços, dois endereços fixos. Um registro de serviços seria uma peça
a mais para manter sem resolver problema nenhum.



## Créditos de dados

- Dados de previsão: **[Open-Meteo.com](https://open-meteo.com/)**, sob licença
  [CC BY 4.0](https://creativecommons.org/licenses/by/4.0/).
- Dados de geocodificação: **Open-Meteo Geocoding API**, baseada em dados do
  **[GeoNames](https://www.geonames.org/)**.

---

## Declaração de uso de IA generativa

Durante o desenvolvimento deste trabalho utilizamos ferramentas de IA generativa,
conforme as regras da Global Solution. Abaixo, o que foi usado e para quê.

| Ferramenta | Para quê |
|---|---|
| Claude (Anthropic) | Apoio na definição da arquitetura e discussão de alternativas; explicação de mensagens de erro e apoio na investigação de falhas; revisão de código e sugestões de melhoria; apoio na redação deste README |

Todo o código entregue foi **lido, compreendido, revisado e testado** pelo grupo. As
decisões de arquitetura e de produto descritas neste documento foram tomadas pelos
integrantes, com a IA usada como apoio à discussão — não como substituta dela.

A IA generativa também é parte do **produto**, na funcionalidade Explicação do Dia, usando
Spring AI com OpenAI, conforme descrito acima.
