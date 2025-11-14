package ground_control;

import data.Estado;
import data.EstadoOperacional;
import java.awt.*;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import javax.swing.*;

public class GroundControl {

    private JPanel roversPanel;
    private JScrollPane scrollPane;
    private MapaPanel mapaPanel;
    private Map<String, Estado> estados;
    private Map<String, Color> coresRovers; // associa cada rover a uma cor
    private Random rand = new Random();

    public GroundControl(Map<String, Estado> estados) {
        this.estados = estados;

        // Define cores para cada rover
        Color[] cores = {Color.RED, Color.BLUE, Color.GREEN, Color.ORANGE, Color.MAGENTA, Color.CYAN, Color.PINK, Color.YELLOW};
        coresRovers = new HashMap<>();
        for (int i = 1; i <= estados.size(); i++) {
            String nome = "R-" + i;
            coresRovers.put(nome, cores[(i-1) % cores.length]);
        }

        JFrame frame = new JFrame("Ground Control");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(1400, 800);
        frame.setLocationRelativeTo(null);
        frame.setLayout(null);

        // Painel de rovers (lista)
        roversPanel = new JPanel();
        roversPanel.setLayout(new BoxLayout(roversPanel, BoxLayout.Y_AXIS));
        roversPanel.setBackground(Color.LIGHT_GRAY);

        scrollPane = new JScrollPane(roversPanel);
        scrollPane.setBounds(10, 10, 450, 745);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        frame.add(scrollPane);

        // Painel do mapa
        ImageIcon mapaImg = new ImageIcon(getClass().getResource("/ground_control/mapa.jpg"));
        mapaPanel = new MapaPanel(mapaImg.getImage(), estados, coresRovers);
        mapaPanel.setBounds(470, 10, 920, 745);
        frame.add(mapaPanel);

        frame.setVisible(true);

        // Timer para atualizar estados, lista e mapa a cada 1 segundo
        Timer timer = new Timer(1000, e -> {
            atualizarEstados();
            atualizarRovers();
            mapaPanel.repaint();
        });
        timer.start();
    }

    // Atualiza os estados dos rovers aleatoriamente
    private void atualizarEstados() {
        for (int i = 1; i <= estados.size(); i++) {
            String nome = "R-" + i;
            Estado estado = estados.get(nome);

            double novoX = estado.getX() + (rand.nextDouble() * 2 - 1);
            double novoY = estado.getY() + (rand.nextDouble() * 2 - 1);
            int novaBateria = Math.max(0, estado.getBateria() - rand.nextInt(3));
            float novaVelocidade = (float) (rand.nextDouble() * 3);

            estados.put(nome, new Estado(novoX, novoY, estado.getEstadoOperacional(), novaBateria, novaVelocidade));
        }
    }

    // Atualiza a lista de rovers na ordem R-1, R-2, ...
    private void atualizarRovers() {
        roversPanel.removeAll();

        JLabel label = new JLabel("Rovers Panel");
        label.setFont(new Font("Arial", Font.BOLD, 24));
        label.setAlignmentX(JLabel.CENTER_ALIGNMENT);
        roversPanel.add(label);

        for (int i = 1; i <= estados.size(); i++) {
            String nome = "R-" + i;
            Estado estado = estados.get(nome);
            Color cor = coresRovers.get(nome);

            JPanel roverPanel = rover_simple(nome, estado, cor);
            roverPanel.setAlignmentX(JPanel.CENTER_ALIGNMENT);
            roversPanel.add(roverPanel);
        }

        roversPanel.revalidate();
        roversPanel.repaint();
    }

    // Cria painel individual do rover
    private JPanel rover_simple(String nome, Estado estado, Color corFundo) {
        JPanel panel = new JPanel();
        panel.setBackground(Color.WHITE);
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

        JLabel label = new JLabel("Rover: " + nome);
        label.setFont(new Font("Arial", Font.PLAIN, 24));
        label.setForeground(corFundo);
        label.setAlignmentX(JLabel.CENTER_ALIGNMENT);

        EstadoOperacional eo = estado.getEstadoOperacional();
        String estadoRover = switch (eo) {
            case EM_MISSAO -> "Em Missão";
            case ESPERA_MISSAO -> "Espera Missão";
            case A_CAMINHO -> "A caminho";
            case PARADO -> "Parado";
            default -> "Inoperacional";
        };

        int bateria = estado.getBateria();
        String corBateria = bateria >= 75 ? "green" : (bateria >= 40 ? "orange" : "red");

        JLabel estadoLabel = new JLabel(
                "<html>Posição: (" + String.format("%.1f", estado.getX()) + ", " + String.format("%.1f", estado.getY()) + ")<br/>" +
                        "Estado Operacional: " + estadoRover + "<br/>" +
                        "Bateria: <span style='color:" + corBateria + "'>" + bateria + "%</span><br/>" +
                        "Velocidade: " + String.format("%.1f", estado.getVelocidade()) + " m/s</html>"
        );
        estadoLabel.setFont(new Font("Arial", Font.PLAIN, 22));
        estadoLabel.setAlignmentX(JLabel.CENTER_ALIGNMENT);

        panel.add(label);
        panel.add(estadoLabel);

        return panel;
    }

    // Painel customizado para desenhar o mapa e rovers
     private static class MapaPanel extends JPanel {
          private Image fundo;
          private Map<String, Estado> estados;
          private Map<String, Color> coresRovers;

          public MapaPanel(Image fundo, Map<String, Estado> estados, Map<String, Color> coresRovers) {
               this.fundo = fundo;
               this.estados = estados;
               this.coresRovers = coresRovers;
          }

          @Override
          protected void paintComponent(Graphics g) {
               super.paintComponent(g);
               g.drawImage(fundo, 0, 0, getWidth(), getHeight(), this);

               // Desenha rovers como pontos com cores consistentes
               for (int i = 1; i <= estados.size(); i++) {
                    String nome = "R-" + i;
                    Estado e = estados.get(nome);
                    Color cor = coresRovers.get(nome);
                    g.setColor(cor);

                    int x = (int) (e.getX() * getWidth() / 100);
                    int y = (int) (e.getY() * getHeight() / 100);
                    g.fillOval(x - 5, y - 5, 20, 20);
                    g.
               }
          }
     }

    // Main
    public static void main(String[] args) {
        Map<String, Estado> estados = new HashMap<>();
        estados.put("R-1", new Estado(90, 90, EstadoOperacional.EM_MISSAO, 85, 0.0f));
        estados.put("R-2", new Estado(20, 20, EstadoOperacional.EM_MISSAO, 60, 0.0f));
        estados.put("R-3", new Estado(40, 40, EstadoOperacional.EM_MISSAO, 10, 2.0f));
        estados.put("R-4", new Estado(20, 70, EstadoOperacional.EM_MISSAO, 45, 1.2f));
        estados.put("R-5", new Estado(8.9, 22.6, EstadoOperacional.EM_MISSAO, 95, 0.8f));
        estados.put("R-6", new Estado(15.3, 30.1, EstadoOperacional.EM_MISSAO, 70, 1.5f));
        estados.put("R-7", new Estado(25.0, 40.0, EstadoOperacional.EM_MISSAO, 55, 2.2f));
        estados.put("R-8", new Estado(50.0, 60.0, EstadoOperacional.EM_MISSAO, 30, 1.0f));

        new GroundControl(estados);
    }
}
