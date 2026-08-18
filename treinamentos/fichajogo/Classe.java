package testeficha;

import java.util.ArrayList;

public class Classe {
    private String nome;
    private int vida;
    private int nivel;
    private ArrayList<Hab> habilidadeClasse;

    public Classe (String nome, int vida) {
        this.nome=nome;
        this.vida=vida;
        this.nivel=1;
        this.HabilidadeClasse=new ArrayList<>();
    }

    public String getNome() { return nome; }
    public String getVida() { return vida; }
}