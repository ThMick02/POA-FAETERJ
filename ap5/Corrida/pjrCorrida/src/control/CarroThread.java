package control;

import java.awt.GraphicsEnvironment;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiConsumer;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

public class CarroThread extends JLabel implements Runnable {

    private static final long serialVersionUID = 1L;
    private static final AtomicInteger POSICAO_CHEGADA = new AtomicInteger(0);
    private static final int LIMITE_PISTA = 910;

    private Thread carroThread = null;
    private int posX;
    private final int posY;
    private final int posXInicial;
    private String nome;
    private ImageIcon imagem;
    private volatile boolean executando = false;
    private Runnable aoFinalizar;
    private BiConsumer<String, Integer> aoChegar;

    // CONSTRUTOR DEFAULT
    public CarroThread() {
        this.posX = 0;
        this.posY = 0;
        this.posXInicial = 0;
    }

    // CONSTRUTOR SOBRECARREGADO
    public CarroThread(String nome, ImageIcon img, int posX, int posY) {
        super(img);
        this.imagem = img;
        this.nome = nome;
        this.posX = posX;
        this.posXInicial = posX;
        this.posY = posY;
        this.setLocation(posX, posY);
        this.setToolTipText(nome);
    }

    public static void resetarPodio() {
        POSICAO_CHEGADA.set(0);
    }

    public static int getPosicaoChegadaAtual() {
        return POSICAO_CHEGADA.get();
    }

    public void setAoFinalizar(Runnable callback) {
        this.aoFinalizar = callback;
    }

    public void setAoChegar(BiConsumer<String, Integer> callback) {
        this.aoChegar = callback;
    }

    public synchronized void iniciar() {
        if (executando && carroThread != null && carroThread.isAlive()) {
            return;
        }
        executando = true;
        carroThread = new Thread(this, nome != null ? nome : "CompetidorThread");
        carroThread.start();
    }

    public synchronized void parar() {
        executando = false;
        if (carroThread != null && carroThread.isAlive()) {
            carroThread.interrupt();
        }
    }

    public void resetar() {
        parar();
        this.posX = posXInicial;
        SwingUtilities.invokeLater(() -> setLocation(posX, posY));
    }

    public boolean isExecutando() {
        return executando && carroThread != null && carroThread.isAlive();
    }

    public String getNome() {
        return nome;
    }

    // METODO RUN() DA INTERFACE RUNNABLE
    @Override
    public void run() {
        while (executando && posX < LIMITE_PISTA && !Thread.currentThread().isInterrupted()) {
            // Avanco aleatorio entre 15 e 55 pixels por iteracao
            int avanco = ThreadLocalRandom.current().nextInt(15, 56);
            posX += avanco;

            if (posX > LIMITE_PISTA) {
                posX = LIMITE_PISTA;
            }

            final int xAtual = posX;
            // Atualizacao segura de componente Swing na Event Dispatch Thread
            SwingUtilities.invokeLater(() -> setLocation(xAtual, posY));

            if (posX >= LIMITE_PISTA) {
                break;
            }

            try {
                // Intervalo de espera aleatorio entre 60 e 180 milissegundos
                int espera = ThreadLocalRandom.current().nextInt(60, 181);
                Thread.sleep(espera);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                executando = false;
                return;
            }
        }

        executando = false;

        // Se a maquina cruzou a linha de chegada
        if (posX >= LIMITE_PISTA) {
            int podio = POSICAO_CHEGADA.incrementAndGet();
            SwingUtilities.invokeLater(() -> {
                if (!GraphicsEnvironment.isHeadless()) {
                    JOptionPane.showMessageDialog(
                        this.getParent(),
                        podio + "º Lugar: " + nome,
                        "Chegada da Corrida!",
                        JOptionPane.INFORMATION_MESSAGE
                    );
                }
                if (aoChegar != null) {
                    aoChegar.accept(nome, podio);
                }
                if (aoFinalizar != null) {
                    aoFinalizar.run();
                }
            });
        }
    }
}