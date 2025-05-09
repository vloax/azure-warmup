package br.pucminas;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import org.json.JSONArray;
import org.json.JSONObject;

public class AzureSentimentAnalysis {
  private static final String SUBSCRIPTION_KEY = "x";
  private static final String ENDPOINT = "x";

  public static void main(String[] args) {
    System.out.println("===== ANÁLISE DE SENTIMENTO COM AZURE TEXT ANALYTICS =====");
    System.out.println("Este programa analisa o sentimento de textos em português.");

    List<String> textos = obterTextosDoUsuario();

    if (textos.isEmpty()) {
      System.out.println("Nenhum texto fornecido para análise. Encerrando programa.");
      return;
    }

    try {
      System.out.println("\nEnviando textos para análise no Azure...");

      String result = analisarSentimento(textos);

      processarResultado(result, textos);

    } catch (Exception e) {
      System.out.println("\nErro ao processar a requisição:");
      System.out.println(e.getMessage());
      e.printStackTrace();
    }
  }

  private static List<String> obterTextosDoUsuario() {
    List<String> textos = new ArrayList<>();
    System.out.println("\nVocê pode fornecer até 5 textos para análise.");
    System.out.println("Para encerrar a entrada de textos, deixe a linha em branco e pressione Enter.");

    try (Scanner scanner = new Scanner(System.in)) {
      for (int i = 1; i <= 5; i++) {
        System.out.print("\nTexto #" + i + " (ou linha em branco para finalizar): ");
        String texto = scanner.nextLine().trim();

        if (texto.isEmpty()) {
          break;
        }

        textos.add(texto);
      }
    }
    return textos;
  }

  private static String analisarSentimento(List<String> textos) throws IOException, InterruptedException {
    JSONObject requestBody = new JSONObject();
    JSONArray documents = new JSONArray();

    for (int i = 0; i < textos.size(); i++) {
      JSONObject document = new JSONObject();
      document.put("id", String.valueOf(i + 1));
      document.put("language", "pt-BR");
      document.put("text", textos.get(i));
      documents.put(document);
    }

    requestBody.put("documents", documents);

    HttpClient client = HttpClient.newHttpClient();

    HttpRequest request = HttpRequest.newBuilder()
        .uri(URI.create(ENDPOINT + "/text/analytics/v3.1/sentiment"))
        .header("Content-Type", "application/json")
        .header("Ocp-Apim-Subscription-Key", SUBSCRIPTION_KEY)
        .POST(HttpRequest.BodyPublishers.ofString(requestBody.toString()))
        .build();

    HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

    if (response.statusCode() != 200) {
      throw new RuntimeException("Falha na requisição. Código de status: " + response.statusCode() +
          "\nResposta: " + response.body());
    }

    return response.body();
  }

  private static void processarResultado(String jsonResult, List<String> textos) {
    JSONObject resultObject = new JSONObject(jsonResult);
    JSONArray documents = resultObject.getJSONArray("documents");

    System.out.println("\n===== RESULTADO DA ANÁLISE DE SENTIMENTO =====");

    for (int i = 0; i < documents.length(); i++) {
      JSONObject document = documents.getJSONObject(i);
      String documentId = document.getString("id");
      String sentiment = document.getString("sentiment");
      JSONObject confidenceScores = document.getJSONObject("confidenceScores");

      double positiveScore = confidenceScores.getDouble("positive");
      double neutralScore = confidenceScores.getDouble("neutral");
      double negativeScore = confidenceScores.getDouble("negative");

      String sentimentoPt = traduzirSentimento(sentiment);

      String positivePercent = String.format("%.1f%%", positiveScore * 100);
      String neutralPercent = String.format("%.1f%%", neutralScore * 100);
      String negativePercent = String.format("%.1f%%", negativeScore * 100);

      System.out.println("\n--------------------------------------------------");
      System.out.println("TEXTO #" + documentId + ": \"" + textos.get(i) + "\"");
      System.out.println("--------------------------------------------------");
      System.out.println("Sentimento: " + sentimentoPt.toUpperCase());
      System.out.println("Pontuações de confiança:");
      System.out.println("- Positivo: " + positivePercent);
      System.out.println("- Neutro: " + neutralPercent);
      System.out.println("- Negativo: " + negativePercent);

      if (document.has("sentences")) {
        JSONArray sentences = document.getJSONArray("sentences");

        if (sentences.length() > 1) {
          System.out.println("\nAnálise por sentenças:");

          for (int j = 0; j < sentences.length(); j++) {
            JSONObject sentence = sentences.getJSONObject(j);
            String text = sentence.getString("text");
            String sentenceSentiment = sentence.getString("sentiment");
            JSONObject sentenceScores = sentence.getJSONObject("confidenceScores");

            double sentencePositive = sentenceScores.getDouble("positive");
            double sentenceNeutral = sentenceScores.getDouble("neutral");
            double sentenceNegative = sentenceScores.getDouble("negative");

            String sentenceSentimentPt = traduzirSentimento(sentenceSentiment);
            String dominantSentiment = obterSentimentoDominante(sentencePositive, sentenceNeutral,
                sentenceNegative);

            System.out.println("\nSentença " + (j + 1) + ": \"" + text + "\"");
            System.out.println("- Sentimento: " + sentenceSentimentPt + " (" + dominantSentiment + ")");
          }
        }
      }
    }

    System.out.println("\n===== FIM DA ANÁLISE =====");
  }

  private static String traduzirSentimento(String sentiment) {
    switch (sentiment.toLowerCase()) {
      case "positive":
        return "Positivo";
      case "neutral":
        return "Neutro";
      case "negative":
        return "Negativo";
      case "mixed":
        return "Misto";
      default:
        return sentiment;
    }
  }

  private static String obterSentimentoDominante(double positive, double neutral, double negative) {
    if (positive > neutral && positive > negative) {
      return String.format("%.1f%% positivo", positive * 100);
    } else if (negative > neutral && negative > positive) {
      return String.format("%.1f%% negativo", negative * 100);
    } else {
      return String.format("%.1f%% neutro", neutral * 100);
    }
  }
}