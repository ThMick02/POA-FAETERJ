package ap1;

import java.io.File;

public class Main {
    public static void main(String[] args) {
        String caminhoBinario = "hospital_dados.ser";

        Recepcao recepcao = new Recepcao("Hospital Central");
        Paciente p1 = new Paciente(101, "Maria Souza", "111.222.333-44", "A01", arqDiag);
        File arqDiag = GerenciadorArquivo.criarDiagnostico(
                "diagnostico_paciente_1.txt",
                "Diagnóstico: Quadro gripal leve. Indicação: Repouso e hidratação."
        );

        recepcao.cadastrarPaciente(p1);

        System.out.println("\n--- TESTE 1: SERIALIZANDO ---");
        GerenciadorArquivo.serRecepcao(caminhoBinario, recepcao);

        System.out.println("\n--- TESTE 2: DESSERIALIZANDO E LENDO ---");
        Recepcao restaurada = GerenciadorArquivo.desRecepcao(caminhoBinario);

        if (restaurada != null) {
            System.out.println("Hospital Restaurado: " + restaurada.getNomeHospital());
            Paciente p = restaurada.getFilaEspera().get(0);
            System.out.println("Paciente Recuperado: " + p.getNome());
            System.out.println("Conteúdo do Diagnóstico: " + p.getDiagnostico());
        }
    }
}