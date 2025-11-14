package protocols.udp;

import data.EstadoOperacional;
import data.Mensagem;
import data.TipoMensagem;
import data.Missao;

import java.io.IOException;
import java.net.*;
import core.Rover; // assumindo que tens uma classe Rover com estado_operacional

public class MissionLinkRover {
    private final String idRover;
    private final int porta;
    private final DatagramSocket socket;
    private final EnvioML envioML;
    private final InetAddress ipNaveMae;
    private final int portaNaveMae;
    private volatile boolean running = true;
    private int ultimaMissao = -1;

    /* ====== Construtor ====== */
    public MissionLinkRover(String idRover, int porta, InetAddress ipNaveMae, int portaNaveMae) throws Exception {
        this.idRover = idRover;
        this.porta = porta;
        this.socket = new DatagramSocket(porta);
        this.envioML = new EnvioML(socket);
        this.ipNaveMae = ipNaveMae;
        this.portaNaveMae = portaNaveMae;
        System.out.println("[" + idRover + " - ML]: Conectado na porta: " + porta);
    }

    public Mensagem receiveMensagem() throws IOException {
        byte[] buffer = new byte[65507];
        DatagramPacket pacote = new DatagramPacket(buffer, buffer.length);
        socket.receive(pacote);
        Mensagem m = Mensagem.fromByteArray(buffer);
        return m;
    }

    public void startMLRover(Rover rover) {
        Thread t = new Thread(() -> handlerMLRover(rover), "Thread - MissionLinkRover");
        t.start();
    }

    private void handlerMLRover(Rover rover) {
        while (running) {
            try {
                if (rover.getEstado().getEstadoOperacional() == EstadoOperacional.PARADO) {
                    System.out.println("[" + idRover + " - ML]: Estado PARADO → solicitar missão...");
                    rover.getEstado().setEstadoOperacional(EstadoOperacional.ESPERA_MISSAO);

                    Mensagem mSYN = new Mensagem(
                            TipoMensagem.ML_SYN,
                            this.idRover,
                            rover.getIP(),
                            this.porta,
                            "NaveMae",
                            this.ipNaveMae,
                            this.portaNaveMae,
                            null
                    );

                    // Enviar SYN e esperar SYNACK
                    envioML.sendMensagem(mSYN.toByteArray(), 
                                        this.ipNaveMae, 
                                        this.portaNaveMae, 
                                        this.idRover + "_SYN");
                }

                try {
                    Mensagem m = receiveMensagem();
                    if (m == null) {
                        continue;
                    }
                    TipoMensagem tp = m.getTipo();

                    switch (tp) {
                        case ML_SYNACK -> {
                            // Confirma o SYN (parar retransmissão)
                            envioML.confirmarRecebimento(idRover + "_SYN");
                            System.out.println("[" + idRover + " - ML] SYNACK de: NaveMae");

                            Mensagem mREQUEST = new Mensagem(TipoMensagem.ML_REQUEST, 
                                                            this.idRover, 
                                                            rover.getIP(), 
                                                            this.porta, 
                                                            "NaveMae", 
                                                            this.ipNaveMae, 
                                                            this.portaNaveMae, 
                                                            null);

                            // Enviar REQUEST e esperar DATA
                            envioML.sendMensagem(mREQUEST.toByteArray(), 
                                                this.ipNaveMae, 
                                                this.portaNaveMae, 
                                                this.idRover + "_REQUEST");
                        }

                        case ML_DATA -> {
                            // Confirmar o REQUEST (parar retransmissão)
                            envioML.confirmarRecebimento(idRover + "_REQUEST");
                            System.out.println("[" + idRover + " - ML]: DATA (Missão) de: NaveMae");

                            Mensagem mCONFIRM = new Mensagem(TipoMensagem.ML_CONFIRM, 
                                                            this.idRover, 
                                                            rover.getIP(), 
                                                            this.porta, 
                                                            "NaveMae", 
                                                            this.ipNaveMae, 
                                                            this.portaNaveMae, 
                                                            null);

                            // Envia CONFIRM e não espera por nada
                            envioML.sendMensagem(mCONFIRM.toByteArray(), 
                                                this.ipNaveMae, 
                                                this.portaNaveMae, 
                                                null);

                            Missao missao = new Missao();
                            missao.fromByteArray(m.getPayload());
                            int idMisaoRecebida = missao.getId();
                            
                            if(idMisaoRecebida != ultimaMissao){
                                this.ultimaMissao = idMisaoRecebida;
                                rover.executaMissao(missao);
                            }
                        }

                        default -> System.out.println("[ERRO] Tipo não existente " + tp);
                    }

                } catch (SocketTimeoutException e) {
                    System.out.println("[ERRO " + idRover + " - ML] Problema no switch: " + e.getMessage());
                    e.printStackTrace();
                }

            } catch (Exception e) {
                System.out.println("[ERRO " + idRover + " - ML] Handler: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    public void stopMLRover() {
        running = false;
        socket.close();
        envioML.stop();
    }
}

