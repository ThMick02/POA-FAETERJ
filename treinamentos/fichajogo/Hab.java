package testeficha;

public class Hab {
    private String nome;
    private String descricao;
    private int bonus;

    public Hab(String nome, String descricao, int bonus) {
        this.nome = nome;
        this.descricao = descricao;
        this.bonus = bonus;
    }

    public String getNome() { return nome; }
    public String getDescricao() { return descricao; }
    public int getBonus() { return bonus; }
}