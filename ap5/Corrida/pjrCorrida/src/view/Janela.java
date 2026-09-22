package view;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.Stroke;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.net.URL;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import control.CarroThread;

@SuppressWarnings("serial")
public class Janela extends JFrame implements ActionListener {

    private JButton btnCorrida;
    private JButton btnCancel;
    private JLabel lblStatus;
    private JLabel lblPlacar;
    private PistaPanel painelPista;

    private ImageIcon imgMakita;
    private ImageIcon imgLonaFreio;
    private ImageIcon imgPrensaVulcanizacao;
    private ImageIcon imgPista;

    private CarroThread makita;
    private CarroThread removedoraLona;
    private CarroThread prensaVulcanizacao;

    private final Map<Integer, String> resultadoPodio = new ConcurrentHashMap<>();

    public Janela() {
        super();
        configurarJanela();
        carregarRecursos();
        inicializarComponentes();
        this.setVisible(true);
    }

    private void configurarJanela() {
        this.setTitle("Corrida de Máquinas e Aparatos de Uso Geralmente Específico");
        this.setSize(1280, 650);
        this.setLocationRelativeTo(null);
        this.setResizable(false);
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    }

    private ImageIcon carregarImagem(String nomeArquivo) {
        URL url = getClass().getResource("/imagens/" + nomeArquivo);
        if (url != null) {
            return new ImageIcon(url);
        }
        File fileBin = new File("./bin/imagens/" + nomeArquivo);
        if (fileBin.exists()) {
            return new ImageIcon(fileBin.getAbsolutePath());
        }
        File fileSrc = new File("./src/imagens/" + nomeArquivo);
        if (fileSrc.exists()) {
            return new ImageIcon(fileSrc.getAbsolutePath());
        }
        File fileProj = new File("./Corrida/pjrCorrida/src/imagens/" + nomeArquivo);
        if (fileProj.exists()) {
            return new ImageIcon(fileProj.getAbsolutePath());
        }
        File fileRoot = new File("./" + nomeArquivo);
        if (fileRoot.exists()) {
            return new ImageIcon(fileRoot.getAbsolutePath());
        }
        return null;
    }

    private ImageIcon carregarImagemRedimensionada(String nomeArquivo, int maxLargura, int maxAltura) {
        ImageIcon icon = carregarImagem(nomeArquivo);
        if (icon == null) {
            return null;
        }
        int origW = icon.getIconWidth();
        int origH = icon.getIconHeight();
        if (origW <= 0 || origH <= 0) {
            return icon;
        }
        double scale = Math.min((double) maxLargura / origW, (double) maxAltura / origH);
        int targetW = Math.max(1, (int) Math.round(origW * scale));
        int targetH = Math.max(1, (int) Math.round(origH * scale));

        Image scaledImg = icon.getImage().getScaledInstance(targetW, targetH, Image.SCALE_SMOOTH);
        return new ImageIcon(scaledImg);
    }

    private void carregarRecursos() {
        // Redimensiona proporcionalmente para caber com folga nas raias
        imgMakita = carregarImagemRedimensionada("makita.png", 110, 85);
        imgLonaFreio = carregarImagemRedimensionada("LonadeFreio.png", 110, 85);
        imgPrensaVulcanizacao = carregarImagemRedimensionada("prensaVulcanizacao.png", 110, 85);
        imgPista = carregarImagem("pista.png");
    }

    private void inicializarComponentes() {
        painelPista = new PistaPanel(imgPista);
        painelPista.setLayout(null);
        this.setContentPane(painelPista);

        // Centros das raias: Raia 1 (118), Raia 2 (245), Raia 3 (378)
        int posXInicial = 35;
        makita = criarMaquina("Makita", imgMakita, posXInicial, calcularPosY(imgMakita, 118));
        removedoraLona = criarMaquina("Máquina Removedora de Lona de Freio", imgLonaFreio, posXInicial, calcularPosY(imgLonaFreio, 245));
        prensaVulcanizacao = criarMaquina("Prensa de Vulcanização a Quente", imgPrensaVulcanizacao, posXInicial, calcularPosY(imgPrensaVulcanizacao, 378));

        painelPista.add(makita);
        painelPista.add(removedoraLona);
        painelPista.add(prensaVulcanizacao);

        // Botoes de controle
        btnCorrida = new JButton("Iniciar Corrida");
        btnCorrida.setFont(new Font("Arial", Font.BOLD, 14));
        btnCorrida.setBounds(500, 540, 140, 45);
        btnCorrida.addActionListener(this);
        painelPista.add(btnCorrida);

        btnCancel = new JButton("Parar Corrida");
        btnCancel.setFont(new Font("Arial", Font.BOLD, 14));
        btnCancel.setBounds(655, 540, 140, 45);
        btnCancel.setEnabled(false);
        btnCancel.addActionListener(this);
        painelPista.add(btnCancel);

        // Rotulo de Status
        lblStatus = new JLabel("Status: Aguardando largada", SwingConstants.CENTER);
        lblStatus.setFont(new Font("Arial", Font.BOLD, 13));
        lblStatus.setForeground(Color.WHITE);
        lblStatus.setBounds(30, 545, 230, 35);
        lblStatus.setBorder(BorderFactory.createLineBorder(new Color(90, 95, 105), 1));
        lblStatus.setOpaque(true);
        lblStatus.setBackground(new Color(40, 42, 48));
        painelPista.add(lblStatus);

        // Placar / Podio ao vivo
        lblPlacar = new JLabel("Podio: 1º [-]  |  2º [-]  |  3º [-]", SwingConstants.CENTER);
        lblPlacar.setFont(new Font("Arial", Font.BOLD, 12));
        lblPlacar.setForeground(new Color(255, 215, 0));
        lblPlacar.setBounds(275, 545, 210, 35);
        lblPlacar.setBorder(BorderFactory.createLineBorder(new Color(90, 95, 105), 1));
        lblPlacar.setOpaque(true);
        lblPlacar.setBackground(new Color(35, 38, 44));
        painelPista.add(lblPlacar);

        // Painel do Grupo de Trabalho / Tema
        JPanel pnlGrupo = new JPanel();
        pnlGrupo.setLayout(null);
        pnlGrupo.setBounds(815, 515, 435, 80);
        pnlGrupo.setBackground(new Color(40, 42, 48));
        pnlGrupo.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(new Color(110, 115, 125)),
            "[ Tema da Corrida ]",
            0,
            0,
            new Font("Arial", Font.BOLD, 12),
            Color.WHITE
        ));

        JLabel lblInfo = new JLabel("Máquinas e Aparatos de Uso Geralmente Específico", SwingConstants.CENTER);
        lblInfo.setFont(new Font("Arial", Font.BOLD, 12));
        lblInfo.setForeground(new Color(255, 215, 0));
        lblInfo.setBounds(10, 22, 415, 20);
        pnlGrupo.add(lblInfo);

        JLabel lblInfo2 = new JLabel("1: Makita  |  2: Rem. Lona de Freio  |  3: Prensa Vulcanização", SwingConstants.CENTER);
        lblInfo2.setFont(new Font("Arial", Font.PLAIN, 11));
        lblInfo2.setForeground(new Color(200, 220, 255));
        lblInfo2.setBounds(10, 47, 415, 20);
        pnlGrupo.add(lblInfo2);

        painelPista.add(pnlGrupo);
    }

    private int calcularPosY(ImageIcon img, int centroY) {
        int h = (img != null && img.getIconHeight() > 0) ? img.getIconHeight() : 85;
        return centroY - (h / 2);
    }

    private CarroThread criarMaquina(String nome, ImageIcon img, int posX, int posY) {
        CarroThread carro = new CarroThread(nome, img, posX, posY);
        int w = (img != null && img.getIconWidth() > 0) ? img.getIconWidth() : 110;
        int h = (img != null && img.getIconHeight() > 0) ? img.getIconHeight() : 85;
        carro.setSize(w, h);
        carro.setVisible(true);
        carro.setAoChegar(this::registrarChegada);
        carro.setAoFinalizar(this::verificarStatusCorrida);
        return carro;
    }

    public JLabel JLabelCarros(String nome, ImageIcon img, int posX, int posY) {
        return criarMaquina(nome, img, posX, posY);
    }

    private void iniciarCorrida() {
        CarroThread.resetarPodio();
        resultadoPodio.clear();
        lblPlacar.setText("Podio: 1º [-]  |  2º [-]  |  3º [-]");

        lblStatus.setText("Status: Corrida em andamento!");
        lblStatus.setForeground(new Color(120, 255, 120));

        btnCorrida.setEnabled(false);
        btnCancel.setEnabled(true);

        makita.resetar();
        removedoraLona.resetar();
        prensaVulcanizacao.resetar();

        makita.iniciar();
        removedoraLona.iniciar();
        prensaVulcanizacao.iniciar();
    }

    private void pararCorrida() {
        makita.parar();
        removedoraLona.parar();
        prensaVulcanizacao.parar();

        lblStatus.setText("Status: Corrida interrompida.");
        lblStatus.setForeground(new Color(255, 140, 140));

        btnCorrida.setEnabled(true);
        btnCancel.setEnabled(false);
    }

    private void registrarChegada(String nome, int podio) {
        resultadoPodio.put(podio, nome);
        String p1 = resultadoPodio.getOrDefault(1, "[-]");
        String p2 = resultadoPodio.getOrDefault(2, "[-]");
        String p3 = resultadoPodio.getOrDefault(3, "[-]");

        p1 = abreviarNomePlacar(p1);
        p2 = abreviarNomePlacar(p2);
        p3 = abreviarNomePlacar(p3);

        lblPlacar.setText(String.format("1º %s | 2º %s | 3º %s", p1, p2, p3));
    }

    private String abreviarNomePlacar(String nome) {
        if ("Máquina Removedora de Lona de Freio".equalsIgnoreCase(nome)) {
            return "Rem. Lona";
        }
        if ("Prensa de Vulcanização a Quente".equalsIgnoreCase(nome)) {
            return "Prensa";
        }
        return nome;
    }

    private void verificarStatusCorrida() {
        SwingUtilities.invokeLater(() -> {
            if (CarroThread.getPosicaoChegadaAtual() >= 3) {
                String campeao = resultadoPodio.getOrDefault(1, "Vencedor");
                lblStatus.setText("Vencedor: " + campeao + "!");
                lblStatus.setForeground(new Color(255, 220, 100));
                btnCorrida.setEnabled(true);
                btnCancel.setEnabled(false);
            }
        });
    }

    @Override
    public void actionPerformed(ActionEvent dispara) {
        if (dispara.getSource() == this.btnCorrida) {
            iniciarCorrida();
        } else if (dispara.getSource() == this.btnCancel) {
            pararCorrida();
        }
    }

    // Painel customizado para desenhar a pista
    private static class PistaPanel extends JPanel {
        private final Image imagemFundo;

        public PistaPanel(ImageIcon fundo) {
            this.imagemFundo = (fundo != null) ? fundo.getImage() : null;
            this.setBackground(new Color(45, 45, 50));
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            int w = getWidth();
            int h = getHeight();
            int alturaPista = 495;

            if (imagemFundo != null) {
                // Desenha a imagem da pista preenchendo exatamente a area de corrida
                g.drawImage(imagemFundo, 0, 0, w, alturaPista, this);
            } else {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                // Pista de asfalto padrão caso imagem nao exista
                g2.setColor(new Color(48, 50, 54));
                g2.fillRect(0, 0, w, alturaPista);

                int zebraWidth = 25;
                for (int x = 0; x < w; x += zebraWidth) {
                    boolean zebraPar = ((x / zebraWidth) % 2 == 0);
                    g2.setColor(zebraPar ? new Color(210, 40, 40) : Color.WHITE);
                    g2.fillRect(x, 0, zebraWidth, 10);
                    g2.fillRect(x, alturaPista - 10, zebraWidth, 10);
                }

                Stroke dashed = new BasicStroke(3, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL, 0, new float[]{25, 20}, 0);
                g2.setStroke(dashed);
                g2.setColor(new Color(230, 230, 230));
                g2.drawLine(0, 178, w, 178);
                g2.drawLine(0, 313, w, 313);

                g2.setStroke(new BasicStroke(4));
                g2.setColor(Color.WHITE);
                g2.drawLine(35, 10, 35, alturaPista - 10);

                int chegadaX = 910;
                int squareSize = 12;
                for (int y = 10; y < alturaPista - 10; y += squareSize) {
                    for (int col = 0; col < 3; col++) {
                        int px = chegadaX + col * squareSize;
                        boolean isWhite = ((y / squareSize) + col) % 2 == 0;
                        g2.setColor(isWhite ? Color.WHITE : Color.BLACK);
                        g2.fillRect(px, y, squareSize, squareSize);
                    }
                }
                g2.dispose();
            }

            // Area inferior para o painel de controles (495 ate o fim da janela)
            Graphics2D gControles = (Graphics2D) g.create();
            gControles.setColor(new Color(28, 30, 34));
            gControles.fillRect(0, alturaPista, w, h - alturaPista);

            gControles.setColor(new Color(75, 80, 90));
            gControles.setStroke(new BasicStroke(2));
            gControles.drawLine(0, alturaPista, w, alturaPista);
            gControles.dispose();
        }
    }
}