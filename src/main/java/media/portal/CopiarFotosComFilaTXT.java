package media.portal;

import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.util.concurrent.*;

public class CopiarFotosComFilaTXT {

    // Fila para armazenar as informações de cada foto a ser processada
    private static Queue<Map<String, String>> filaFotos = new LinkedList<>();

    public static void main(String[] args) {
        String jarDir = System.getProperty("user.dir");
        String arquivoTXT =  jarDir + "/archive-directory.txt";  // Nome do arquivo TXT no mesmo diretório que o .java
        int tamanhoLote = 5000;  // Número de fotos a serem processadas em cada lote (ajuste conforme necessário)

        // Lê o arquivo TXT e preenche a fila com os dados
        carregarFilaComDadosTXT(arquivoTXT);

        // Cria um executor para processar os arquivos em paralelo
        ExecutorService executor = Executors.newFixedThreadPool(4);  // Pool de 4 threads (ajuste conforme necessário)

        try {
            while (!filaFotos.isEmpty()) {
                // Cria um lote de arquivos a partir da fila
                List<Map<String, String>> loteDeFotos = new ArrayList<>();
                for (int i = 0; i < tamanhoLote && !filaFotos.isEmpty(); i++) {
                    loteDeFotos.add(filaFotos.poll());
                }

                // Submete o lote de arquivos para o executor processar
                executor.submit(() -> processarLote(loteDeFotos));
            }

            // Aguarda todas as tarefas serem concluídas
            executor.shutdown();
            while (!executor.isTerminated()) {
                // Aguarda até que todas as tarefas sejam finalizadas
            }

            System.out.println("Processamento completo.");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Método para carregar dados do arquivo TXT na fila
    private static void carregarFilaComDadosTXT(String arquivoTXT) {
        try (BufferedReader br = new BufferedReader(new FileReader(arquivoTXT))) {
            String linha;
            while ((linha = br.readLine()) != null) {
                // Divide a linha em campos com base no delimitador "|"
                String[] campos = linha.split("\\|");

                if (campos.length >= 6) {
                    Map<String, String> dadosFoto = new HashMap<>();
                    dadosFoto.put("idGrupo", campos[0].trim());
                    dadosFoto.put("referenciaGrupo", campos[1].trim());
                    dadosFoto.put("idFoto", campos[2].trim());
                    dadosFoto.put("nomePasta", campos[3].trim());
                    dadosFoto.put("diretorioDestino", campos[4].trim());
                    dadosFoto.put("diretorioOrigem", campos[5].trim());

                    // Adiciona o mapa de dados da foto na fila
                    filaFotos.add(dadosFoto);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Método para processar um lote de fotos
    private static void processarLote(List<Map<String, String>> loteDeFotos) {
        for (Map<String, String> dadosFoto : loteDeFotos) {
            String diretorioOrigem = dadosFoto.get("diretorioOrigem");
            String diretorioDestino = dadosFoto.get("diretorioDestino");
            String nomePasta = dadosFoto.get("nomePasta");
            String idFoto = dadosFoto.get("idFoto");
            String referenciaGrupo = dadosFoto.get("referenciaGrupo");

            try {
                // Define o subdiretório do grupo com base na referência do grupo
                String grupoSubdiretorio = determinarGrupo(Integer.parseInt(referenciaGrupo));

                // Cria o diretório de destino baseado no grupo e nome da pasta
                File destinoDir = new File(diretorioDestino + File.separator + grupoSubdiretorio + File.separator + nomePasta);
                if (!destinoDir.exists()) {
                    destinoDir.mkdirs();
                }

                // Define o caminho do arquivo de origem e destino
                Path origemPath = Paths.get(diretorioOrigem);
                Path destinoPath = Paths.get(destinoDir.getPath(), idFoto + "_" + origemPath.getFileName().toString());

                // Copia o arquivo para o destino
                Files.copy(origemPath, destinoPath, StandardCopyOption.REPLACE_EXISTING);
                System.out.println("Arquivo copiado: " + origemPath.getFileName() + " -> " + destinoPath);
            } catch (IOException e) {
                System.err.println("Erro ao copiar o arquivo " + diretorioOrigem + ": " + e.getMessage());
            }
        }
    }

    // Método para determinar o subdiretório do grupo com base na referência do grupo
    private static String determinarGrupo(int referenciaGrupo) {
        if (referenciaGrupo >= 0 && referenciaGrupo <= 4) {
            return "0_a_4";
        } else if (referenciaGrupo >= 5 && referenciaGrupo <= 20) {
            return "5_a_20";
        } else if (referenciaGrupo >= 21 && referenciaGrupo <= 40) {
            return "21_a_40";
        } else if (referenciaGrupo >= 41 && referenciaGrupo <= 80) {
            return "41_a_80";
        } else {
            return "81_ou_mais";
        }
    }
}
