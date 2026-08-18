package testeficha;

import java.util.ArrayList;

public class Raca {
    private String nome;
    private ArrayList<Hab> habilidades;

    public Raca(String nome) {
        this.nome = nome;
        this.habilidades = new ArrayList<>();
    }

    public String getNome() { return nome; }
}