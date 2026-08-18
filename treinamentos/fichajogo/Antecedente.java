package testeficha;

import java.util.ArrayList;

public class Antecedente {
    private String nome;
    private String[] proficiencias;
    private ArrayList<Hab> caracteristicas;

    public Antecedente(String nome, String[] proficiencias) {
        this.nome = nome;
        this.proficiencias = proficiencias;
        this.caracteristicas = new ArrayList<>();
    }

    public String getNome() { return nome; }
    public String[] getProficiencias() { return proficiencias; }
}