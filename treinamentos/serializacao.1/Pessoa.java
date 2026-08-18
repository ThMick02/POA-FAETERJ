package testeserualizacao;

import java.io.Serializable;
import java.util.List;
import java.util.ArrayList;

public class Pessoa implements Serializable{
    private String nome;
    private int idade;
    private String cpf;
    private List<Pessoa> dependentes = new ArrayList<>();

    public Pessoa (String nome, int idade, String cpf) {
        this.nome=nome;
        this.idade=idade;
        this.cpf=cpf;
    }

    public void adicionarDependente (Pessoa dependente) {
        this.dependentes.add(dependente);
    }

    public String getNome () { return nome;}
    public int getIdade () { return idade;}
    public String getCpf () { return cpf;}
    public List<Pessoa> getDependentes() { return dependentes; }

    public void setNome (String nome) { this.nome=nome; }
    public void setIdade (int idade) { this.idade=idade;}
    public void setCpf (String cpf) { this.cpf=cpf;}
    
    @Override
    public String toString () {
        return "Pessoa [nome=" + nome + ", idade=" + idade + ", cpf=" + cpf + "]";
    }
}

