package media.portal;

import java.io.*;
import java.nio.file.*;

public class Copiador {

    public static void main(String[] args) {

        String jarDir = System.getProperty("user.dir");
        String inputFile = jarDir + "/archive-directory.txt"; // Nome do arquivo TXT no mesmo diretório que o .jar

        try {
            BufferedReader reader = new BufferedReader(new FileReader(inputFile));
            String line;

            // Lê cada linha do arquivo
            while ((line = reader.readLine()) != null) {
                // Ignora linhas em branco ou cabeçalho, se necessário
                if (line.trim().isEmpty() || line.startsWith("idGrupo")) {
                    continue;
                }

                // Divide a linha pelos pipes '|'
                String[] parts = line.split("\\|");

                if (parts.length == 6) {
                    // Parseia as colunas
                    String idGrupo = parts[0].trim();
                    int itensPorGrupo = Integer.parseInt(parts[1].trim());
                    String idArquivo = parts[2].trim();
                    String nomePastaDestino = parts[3].trim();
                    String diretorioDeOrigem = parts[4].trim();
                    String diretorioRelativoDoArquivo = parts[5].trim();

                    // Classifica o grupo com base na quantidade de itens
                    String faixaDeItens = classificarFaixaDeItens(itensPorGrupo);

                    // Caminho absoluto do arquivo de origem
                    Path origemPath = Paths.get(diretorioDeOrigem, diretorioRelativoDoArquivo);

                    // Caminho do destino
                    Path destinoPath = Paths.get(nomePastaDestino, faixaDeItens, diretorioRelativoDoArquivo);

                    // Cria os diretórios de destino, se não existirem
                    Files.createDirectories(destinoPath.getParent());

                    // Verifica se o arquivo de origem existe antes de tentar copiá-lo
                    if (Files.exists(origemPath)) {
                        // Copia o arquivo para o destino
                        Files.copy(origemPath, destinoPath, StandardCopyOption.REPLACE_EXISTING);
                        System.out.println("Arquivo copiado: " + origemPath + " para " + destinoPath);
                    } else {
                        System.err.println("Arquivo de origem não encontrado: " + origemPath);
                    }
                } else {
                    System.err.println("Formato inválido na linha: " + line);
                }
            }
            reader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Método para classificar a faixa de itens do grupo
    private static String classificarFaixaDeItens(int itensPorGrupo) {
        if (itensPorGrupo >= 1 && itensPorGrupo <= 4) {
            return "1_4";
        } else if (itensPorGrupo >= 5 && itensPorGrupo <= 20) {
            return "5_20";
        } else if (itensPorGrupo >= 21 && itensPorGrupo <= 40) {
            return "21_40";
        } else if (itensPorGrupo >= 41 && itensPorGrupo <= 80) {
            return "41_80";
        } else {
            return "80+";
        }
    }
}
