package ap1;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.nio.file.Files;

public class Paciente implements Serializable {
    private int id;
    private String nome;
    private String cpf;
    private transient String senhaAtendimentoDia;
    private File diagnostico;

    public Paciente(int id, String nome, String cpf, String senha, File diagnostico) {
        this.id = id;
        this.nome = nome;
        this.cpf = cpf;
        this.senhaAtendimentoDia = senha;
        this.diagnostico = diagnostico;
    }

    public String getDiagnostico() {
        if (diagnostico != null && diagnostico.exists()) {
            try {
                return Files.readString(diagnostico.toPath());
            } catch (IOException e) {
                return "Erro ao ler o arquivo de diagnóstico.";
            }
        }
        return "Diagnóstico não anexado.";
    }
    public int getId() { return id; }
    public String getNome() { return nome; }
    public String getCpf() { return cpf; }
}
