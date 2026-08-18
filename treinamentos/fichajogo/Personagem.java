package testeficha;

import java.util.ArrayList;

public class Personagem {
    private String nome;
    private Raca raca;
    private Classe classe;
    private Antecedente antecedente;
    private ArrayList<Hab> talentos;

    public Personagem(String nome, Raca raca, Classe classe, Antecedente antecedente) {
        this.nome = nome;
        this.raca = raca;
        this.classe = classe;
        this.antecedente = antecedente;
        this.talentos = new ArrayList<>();
    }

    public void mostrarFicha() {
        System.out.println("==================================================");
        System.out.println("              FICHA DE PERSONAGEM                 ");
        System.out.println("==================================================");
        System.out.println("Nome: " + this.nome);
        
        if (this.raca != null) {
            System.out.println("Raça: " + this.raca.getNome());
        }
        if (this.classe != null) {
            System.out.println("Classe: " + this.classe.getNome() + " | Pontos de Vida: " + this.classe.getVida());
        }
        if (this.antecedente != null) {
            System.out.println("Antecedente: " + this.antecedente.getNome());
            
            System.out.print("Proficiências: ");
            String[] profs = this.antecedente.getProficiencias();
            if (profs != null && profs.length > 0) {
                for (int i = 0; i < profs.length; i++) {
                    System.out.print(profs[i] + (i < profs.length - 1 ? ", " : ""));
                }
                System.out.println();
            } else {
                System.out.println("Nenhuma");
            }
        }

        System.out.println("--------------------------------------------------");
        System.out.println("Talentos/Habilidades (" + talentos.size() + "):");
        if (talentos.isEmpty()) {
            System.out.println(" - Nenhum talento adicionado.");
        } else {
            for (Hab hab : talentos) {
                System.out.println(" - " + hab.getNome() + ": " + hab.getDescricao());
            }
        }
        System.out.println("==================================================");
    }

    public void adicionarTalento(Hab habilidade) {
        this.talentos.add(habilidade);
    }
}