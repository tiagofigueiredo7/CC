package protocols.udp;

import data.Estado;
import data.Mensagem;
import data.Missao;
import data.TipoMensagem;
import core.NaveMae;

import java.io.IOException;
import java.net.*;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

public class MissionLinkNM {
    private final int porta;
    private final DatagramSocket socket;
    private final EnvioML envioML;
    private volatile boolean running = true;

    /* ====== Construtor ====== */

    public MissionLinkNM(int porta) throws SocketException {
        this.porta = porta;
        this.socket = new DatagramSocket(porta);
        this.envioML = new EnvioML(socket);
        System.out.println("[NaveMae - ML]: À escuta de ML na porta: " + porta);
    }

    public Mensagem receiveMensagem(NaveMae nm) throws IOException {
        byte[] buffer = new byte[65507];
        DatagramPacket pacote = new DatagramPacket(buffer, buffer.length);
        socket.receive(pacote);
        Mensagem m = Mensagem.fromByteArray(buffer);

        String id = m.getIdOrg();
        if (!nm.conheceRover(id)) {
            InetAddress ip = m.getIpOrg();
            int porta = m.getPortaOrg();
            Estado e = new Estado();
            nm.adicionaRover(id, ip, porta, e);
        }
        return m;
    }

    public void startMLNaveMae(NaveMae nm) {
        // Mapa de queues por rover (id)
        Map<String, BlockingQueue<Mensagem>> queues = new ConcurrentHashMap<>();

        while (running) {
            try {
                Mensagem m = this.receiveMensagem(nm);
                String id = m.getIdOrg();

                // obtém a queue do rover
                BlockingQueue<Mensagem> q = queues.computeIfAbsent(id, roverId -> {
                    BlockingQueue<Mensagem> newQ = new LinkedBlockingQueue<>();
                    // cria e inicia o handler para o novo rover
                    new Thread(() -> {
                        try {
                            while (true) {
                                Mensagem msg = newQ.poll(60, TimeUnit.SECONDS);
                                if (msg == null) {
                                    // sem mensagens por 60s/rover não está mais a trabalhar 
                                    // -> encerra handler e remove a queue desse rover
                                    queues.remove(roverId);
                                    break;
                                }
                                handlerMLNaveMae(msg, nm);
                            }
                        } catch (Exception e) {
                            System.out.println("[ERRO NaveMae - ML] Handler thread for " + roverId + ": " + e.getMessage());
                            e.printStackTrace();
                        }
                    }, "ML-Handler-" + roverId).start();
                    return newQ;
                });

                // adiciona a mensagem à queue do rover que enviou a msg
                q.offer(m);

            } catch (Exception e) {
                System.out.println("[ERRO NaveMae - ML] : " + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    public void handlerMLNaveMae(Mensagem m, NaveMae nm) {
        try {
                String idRover = m.getIdOrg();
                TipoMensagem tp = m.getTipo();
                InetAddress ip_org = nm.getIP();
                InetAddress ip_dest = m.getIpOrg();
                int porta_dest = m.getPortaOrg();

                System.out.println("[NaveMae - ML] Recebeu mensagem de: " + idRover + " (" + tp + ")");

                switch (tp) {
                    case ML_SYN -> {

                        Mensagem mSYNACK = new Mensagem(TipoMensagem.ML_SYNACK, 
                                                "NaveMae", 
                                                ip_org, 
                                                this.porta,
                                                idRover, 
                                                ip_dest, 
                                                porta_dest, 
                                                null);

                        // Enviar SYNACK e esperar REQUEST
                        envioML.sendMensagem(mSYNACK.toByteArray(), 
                                            ip_dest, 
                                            porta_dest,
                                            idRover + "_SYNACK"
                        );

                        System.out.println("[NaveMae - ML] SYNACK enviado ao rover: " + idRover);
                    }

                    case ML_REQUEST -> {
                        // Confirma o SYNACK (parar retransmissão)
                        envioML.confirmarRecebimento(idRover + "_SYNACK");

                        // Enviar missão
                        Missao missao = nm.getMissaoQueue();
                        Mensagem mDATA = new Mensagem(TipoMensagem.ML_DATA, 
                                              "NaveMae", 
                                              ip_org, 
                                              this.porta,
                                              idRover, 
                                              ip_dest, 
                                              porta_dest, 
                                              missao.toByteArray());

                        // Enviar DATA e esperar CONFIRM
                        envioML.sendMensagem(mDATA.toByteArray(), 
                                            ip_dest, 
                                            porta_dest,
                                            idRover + "_DATA"
                        );

                        System.out.println("[NaveMae - ML] Missão enviada ao rover: " + idRover);
                    }

                    case ML_CONFIRM -> {
                        // Confirma o DATA(parar retransmissão)
                        envioML.confirmarRecebimento(idRover + "_DATA");
                        System.out.println("[NaveMae - ML] CONFIRM de: " + idRover);
                    }

                    default -> System.out.println("[ERRO] Tipo não existente: " + tp);
                }
        } catch (Exception e) {
            System.out.println("[ERRO NaveMae - ML] Handler: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void stopMLNaveMae() {
        running = false;
        socket.close();
        envioML.stop();
    }
}
