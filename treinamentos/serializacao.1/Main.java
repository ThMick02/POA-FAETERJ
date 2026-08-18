package testeserualizacao;

import java.io.FileOutputStream;
import java.io.ObjectOutputStream;
import java.io.IOException;

public class Main {
    public static void main(String[] args) {
        Pessoa p1 = new Pessoa("Vander", 43, "189.245.478-80");
        Pessoa p2 = new Pessoa("Valentina", 17, "290.380.232-63");
        Pessoa p3 = new Pessoa("Enzo", 14, "870.432.321.84");

        p1.adicionarDependente(p2);
        p1.adicionarDependente(p3);
        
        try (FileOutputStream fileOut = new FileOutputStream("pessoa.ser"); ObjectOutputStream out = new ObjectOutputStream(fileOut)) {
            out.writeObject(p1);
            System.out.println("Objeto serializado com sucesso!");

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

