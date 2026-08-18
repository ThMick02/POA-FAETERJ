package testeficha;

public class Menu {
    // Racas Disponiveis:
    public static final Raca HUMANO = new Raca("Humano");
    public static final Raca ANAO = new Raca("Anão");
    public static final Raca ELFO = new Raca("Elfo");

    // Classes Disponiveis:
    public static final Classe GUERREIRO = new Classe("Guerreiro", 10);
    public static final Classe MAGO = new Classe("Mago", 6);
    public static final Classe LADINO = new Classe("Ladino", 8);

    // Proficiencias
    private static final String[] PROFS_SOLDADO = {"Atletismo", "Intimidação"};
    private static final String[] PROFS_ESTUDIOSO = {"Arcanismo", "História"};
    private static final String[] PROFS_CRIMINOSO = {"Furtividade", "Prestidigitação"};

    // Antecedentes disponiveis:
    public static final Antecedente SOLDADO = new Antecedente("Soldado", PROFS_SOLDADO);
    public static final Antecedente ESTUDIOSO = new Antecedente("Estudioso", PROFS_ESTUDIOSO);
    public static final Antecedente CRIMINOSO = new Antecedente("Criminoso", PROFS_CRIMINOSO);
}