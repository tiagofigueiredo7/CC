package core;

import java.net.InetAddress;
import java.net.UnknownHostException;

import data.Estado;
import data.EstadoOperacional;
import data.Missao;
import protocols.udp.MissionLinkRover;
//import protocols.tcp.TelemetryStreamRover;

public class Rover {
    // Identificação do Rover
    private final String id; // R-x
    private final InetAddress ip;
    private final int porta;
    private Estado estado;

    // Identificação da Nave Mãe
    private static final String id_NaveMae = "NaveMae";
    private final InetAddress ip_NaveMae;
    private static final int portaUDPNaveMae = 5000;
    private static final int portaTCPNaveMae = 6000;

    // Protocolos
    private MissionLinkRover ml;
    //private TelemetryStreamRover ts;

    /* ========== Construtor ========== */

    public Rover(String id, 
                InetAddress ip, 
                int porta,
                InetAddress ip_NaveMae){

        this.id = id;
        this.ip = ip;
        this.porta = porta;
        this.estado = new Estado();


        this.ip_NaveMae = ip_NaveMae;

        try {
            this.ml = new MissionLinkRover(id, porta, ip_NaveMae, portaUDPNaveMae);
            //this.ts = new TelemetryStreamRover(id, porta, ip); 

        } catch (Exception e) {
            System.out.println("[ERRO] Falha ao iniciar " + id + ": " + e.getMessage());
            e.printStackTrace();
        }
    }

    /* ====== Getters e Setters ====== */

    public String getId(){
        return this.id;
    }

    public InetAddress getIP(){
        return this.ip;
    }

    public int getPorta(){
        return this.porta;
    }

    public Estado getEstado(){
        return this.estado;
    }

    public String getIdNaveMae(){
        return id_NaveMae;
    }

    public InetAddress getIpNaveMae(){
        return this.ip_NaveMae;
    }

    public int getPortaUDPNaveMae(){
        return portaUDPNaveMae;
    }

    public int getPortaTCPNaveMae(){
        return portaTCPNaveMae;
    }

    /* ====== Métodos ====== */

    public void startRover(){
        this.ml.startMLRover(this);
        //this.ts.startTSRover(this);
    }

    public void executaMissao(Missao missao){
        // TODO metodo temporario -> da print a missao
        try{
            System.out.println(missao.toString());
            Thread.sleep(15*1000);
            this.estado.setEstadoOperacional(EstadoOperacional.PARADO);
        }catch(InterruptedException e){
            System.out.println("executar missao");
        }
    }

    public static void main(String[] args) {
    if(args.length < 4){
        System.out.println("Uso: java Rover <id_rover> <ip_rover> <porta_rover> <ip_nave_mae>");
        return;
    }

    try{
        String id = args[0];
        InetAddress ip = InetAddress.getByName(args[1]);
        int porta = Integer.parseInt(args[2]);
        InetAddress ip_NaveMae = InetAddress.getByName(args[3]);

        Rover rover = new Rover(id, ip, porta, ip_NaveMae);
        rover.startRover();

    }catch(UnknownHostException e){
        System.out.println("Erro no ip: " + e);
    }
    }
}