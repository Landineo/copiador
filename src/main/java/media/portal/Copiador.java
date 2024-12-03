package media.portal;

import java.io.*;
import java.nio.file.*;

public class Copiador {

    //Para utilizar somente a cópia de arquivos sem o sistema de limitação de itens não esqueça de
    //comentar as linhas 64 à 70.
    //Para utilizar o sistema de separação por faixa de itens, não esqueça de descomentar as variáveis na linha 57.

    public static void main(String[] args) {

        String jarDir = System.getProperty("user.dir");
        String inputFile = jarDir + "/archive-directory.txt"; // Nome do arquivo TXT no mesmo diretório que o .jar

        try {
            BufferedReader reader = new BufferedReader(new FileReader(inputFile));
            String line;

            // Contador de arquivos copiados para organizar a separação a cada x itens
            int contadorDeArquivos = 0;
            int numeroDoGrupo = 1;

            // Lê cada linha do arquivo
            while ((line = reader.readLine()) != null) {
                // Ignora linhas em branco ou cabeçalho, se necessário
                if (line.trim().isEmpty() || line.startsWith("grupo")) {
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
                    //adicionar um regex para separar retirar o diretorio relativo
                    String[] diretorioRelativoDoArquivo = parts[5].trim().split("/");

                    // Classifica o grupo com base na quantidade de itens
                    String faixaDeItens = classificarFaixaDeItens(itensPorGrupo);

                    // Nome do diretório de destino baseado no número do grupo
                    String pastaDestinoComGrupo = nomePastaDestino + "_grupo" + numeroDoGrupo;

                    // Caminho absoluto do arquivo de origem
                    Path origemPath = Paths.get(diretorioDeOrigem, diretorioRelativoDoArquivo);

                    // Caminho do destino, usando o nome da pasta com o número do grupo
                    // para alterar para função anterior:
                    // comentar a variavel pastaDestinoComGrupo e descomentar faixaDeItens, nomePastaDestino
                    Path destinoPath = Paths.get(/*faixaDeItens, nomePastaDestino,*/ pastaDestinoComGrupo, diretorioRelativoDoArquivo[diretorioRelativoDoArquivo.length - 1].trim());

                    // Cria os diretórios de destino, se não existirem
                    Files.createDirectories(destinoPath.getParent());

                    // Verifica se o arquivo de origem existe antes de tentar copiá-lo
                    if (Files.exists(origemPath)) {
                        // Copia o arquivo para o destino
                        Files.copy(origemPath, destinoPath, StandardCopyOption.REPLACE_EXISTING);
                        System.out.println("Arquivo copiado: " + origemPath + " para " + destinoPath);

                        // Incrementa o contador de arquivos
                        contadorDeArquivos++;

                        // Verifica se deve começar um novo grupo a cada x itens
                        if (contadorDeArquivos >= 40000) {
                            numeroDoGrupo++;
                            contadorDeArquivos = 0;  // Reinicia o contador para o próximo grupo
                        }
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

    // Classifica a faixa de itens do grupo
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
