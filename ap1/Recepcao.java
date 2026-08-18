package ap1;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class Recepcao implements Serializable {
    private String nomeHospital;
    private List<Paciente> filaEspera;

    public Recepcao(String nomeHospital) {
        this.nomeHospital = nomeHospital;
        this.filaEspera = new ArrayList<>();
    }

    public void cadastrarPaciente(Paciente paciente) {
        if (paciente != null) {
            this.filaEspera.add(paciente);
            System.out.println("Paciente " + paciente.getNome() + " cadastrado com sucesso.");
        }
    }

    public List<Paciente> getFilaEspera() { return filaEspera; } 
    public String getNomeHospital() { return nomeHospital; }

    @Override
    public String toString() {
        return "Recepcao{" + "nomeHospital='" + nomeHospital + '\'' + ", totalPacientesFila=" + filaEspera.size() + '}';
    }
}