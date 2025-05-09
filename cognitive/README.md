# Azure Sentiment Analysis

Este projeto realiza análise de sentimento de textos em português utilizando o serviço Azure Text Analytics.

## Finalidade

O objetivo é permitir que o usuário forneça até 5 textos via terminal e obtenha, para cada um, a classificação de sentimento (positivo, neutro, negativo ou misto) e as respectivas pontuações de confiança, utilizando a API de Inteligência Artificial da Microsoft Azure.

## Pré-requisitos

- Java 21 instalado
- Maven instalado
- Conta no Azure com o serviço Text Analytics configurado
- Chave de acesso (Subscription Key) e Endpoint do serviço Azure Text Analytics

## Configuração

1. No arquivo `AzureSentimentAnalysis.java`, substitua:
   ```java
   private static final String SUBSCRIPTION_KEY = "x";
   private static final String ENDPOINT = "x";
   ```
   pelos valores reais do seu serviço Azure.

2. (Opcional) Se necessário, adicione o plugin exec-maven-plugin ao seu `pom.xml`:

   ```xml
   <plugin>
     <groupId>org.codehaus.mojo</groupId>
     <artifactId>exec-maven-plugin</artifactId>
     <version>3.1.0</version>
   </plugin>
   ```

## Como rodar

No terminal, dentro da pasta do projeto (onde está o `pom.xml`), execute:

```sh
mvn clean compile

Caso esteja no windows, execute:

mvn exec:java "-Dexec.mainClass=br.pucminas.AzureSentimentAnalysis"

Se não, execute:

mvn exec:java -Dexec.mainClass=br.pucminas.AzureSentimentAnalysis

```

O programa irá solicitar que você digite até 5 textos para análise. Após o envio, exibirá o resultado da análise de sentimento para cada texto.

---