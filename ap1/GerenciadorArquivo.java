package ap1;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;

public class GerenciadorArquivo {

    public static void serRecepcao(String caminho, Recepcao recepcao) {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(caminho))) {
            oos.writeObject(recepcao);
            System.out.println("Estado da Recepção salvo com sucesso em: " + caminho);
        } catch (IOException e) {
            System.err.println("Erro ao serializar: " + e.getMessage());
        }
    }

    public static Recepcao desRecepcao(String caminho) {
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(caminho))) {
            return (Recepcao) ois.readObject();
        } catch (IOException | ClassNotFoundException e) {
            System.err.println("Erro ao desserializar: " + e.getMessage());
            return null;
        }
    }

    public static File criarDiagnostico(String nomeArquivo, String conteudo) {
        try {
            Path path = Path.of(nomeArquivo);
            Files.writeString(path, conteudo);
            System.out.println("Arquivo de diagnóstico gerado em: " + nomeArquivo);
            return path.toFile();
        } catch (IOException e) {
            System.err.println("Erro ao gerar arquivo de diagnóstico: " + e.getMessage());
            return null;
        }
    }
}