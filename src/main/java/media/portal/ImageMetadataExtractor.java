package media.portal;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;

public class ImageMetadataExtractor {
    public static void main(String[] args) {
        // Caminho base onde estão as imagens
        String baseDirectory = "S:\\CPA\\INGEST";
        // Caminho para o executável do ExifTool (atualizado)
        String exiftoolPath = "C:\\mediaportal\\exiftool\\exiftool.exe";  // Atualize este caminho para o local correto do ExifTool
        String filePath = System.getProperty("user.dir") + "\\output.txt";
        // Caminho para o arquivo de metadados
        String metadadosArquivo = System.getProperty("user.dir") + "\\output_metadados.txt";

        try (BufferedReader br = new BufferedReader(new FileReader(filePath));
             BufferedWriter writer = new BufferedWriter(new FileWriter(metadadosArquivo))) {

            String line;
            int lineNumber = 1;

            while ((line = br.readLine()) != null) {
                // Dividir a linha por '|'
                String[] fields = line.split("\\|");

                // Procurar pelo campo que contém o nome do arquivo com extensão .JPG
                String imageName = null;
                for (String field : fields) {
                    if (field.trim().toLowerCase().endsWith(".jpg")) {
                        imageName = field.trim();
                        break;
                    }
                }

                // Verificar se o nome do arquivo foi encontrado
                if (imageName != null) {
                    // Construir o caminho completo da imagem
                    String imagePath = String.format("%s\\%s", baseDirectory, imageName);

                    if (Files.exists(Paths.get(imagePath))) {
                        System.out.println("Processando imagem da linha " + lineNumber + ": " + imagePath);

                        // Montar o comando `exiftool`
                        String command = String.format("%s %s", exiftoolPath, imagePath);

                        // Executar o comando
                        Process process = Runtime.getRuntime().exec(command);

                        // Capturar a saída do comando
                        try (BufferedReader outputReader = new BufferedReader(
                                new InputStreamReader(process.getInputStream()))) {

                            StringBuilder metadataBuilder = new StringBuilder();
                            String outputLine;

                            // Lê todas as linhas de saída do processo e concatena
                            while ((outputLine = outputReader.readLine()) != null) {
                                metadataBuilder.append(outputLine).append(System.lineSeparator());
                                System.out.println("Metadados: " + outputLine);
                            }

                            // Salva os metadados completos no arquivo
                            writer.write("Linha " + lineNumber + ":");
                            writer.newLine();
                            writer.write(metadataBuilder.toString());
                            writer.newLine();
                            writer.write("-------------------------------------------------------------------");
                        }

                        // Capturar e imprimir erros do comando
                        try (BufferedReader errorReader = new BufferedReader(
                                new InputStreamReader(process.getErrorStream()))) {
                            String errorLine;
                            while ((errorLine = errorReader.readLine()) != null) {
                                System.err.println("Erro: " + errorLine);
                            }
                        }

                        process.waitFor(); // Espera o comando terminar
                    } else {
                        System.err.println("Linha " + lineNumber + ": Imagem não encontrada no caminho " + imagePath);
                    }
                } else {
                    System.err.println("Linha " + lineNumber + ": Nome da imagem não encontrado.");
                }

                lineNumber++;
            }
        } catch (IOException | InterruptedException e) {
            System.err.println("Erro ao processar: " + e.getMessage());
            e.printStackTrace(); // Imprime o stack trace completo para diagnóstico
        }
    }
}
