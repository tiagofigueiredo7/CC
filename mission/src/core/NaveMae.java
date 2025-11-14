package core;

import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ConcurrentHashMap;

import data.*;
//import protocols.tcp.TelemetryStreamNM;
import protocols.udp.MissionLinkNM;

import java.net.InetAddress;
import java.net.UnknownHostException;


public class NaveMae {
    private Map<String, Estado> roversEstado;
    private Map<String, InetAddress> roversIP;
    private Map<String, Integer> roversPorta;

    private final BlockingQueue<Missao> queue;

    private final String id = "NaveMae";
    private final int portaUDP = 5000;
    private final int portaTCP = 6000;
    private final InetAddress ip;

    private MissionLinkNM ml;
    //private TelemetryStreamNM ts;

    /* ========== Construtor ========== */

    public NaveMae(InetAddress ip){
        this.ip = ip;

        this.queue = new LinkedBlockingQueue<Missao>();

        this.roversEstado = new ConcurrentHashMap<>();
        this.roversIP = new ConcurrentHashMap<>();
        this.roversPorta = new ConcurrentHashMap<>();

        try {
            this.ml = new MissionLinkNM(this.portaUDP);
            //this.ts = new TelemetryStreamNM(this.portaTCP);

        } catch (Exception e) {
            System.out.println("[ERRO] Falha ao inicializar NaveMae: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /*======= Getters & Setters ====== */

    public String getID(){
        return this.id;
    }

    public InetAddress getIP(){
        return this.ip;
    }
    
    public int getPortaUDP(){
        return this.portaUDP;
    }

    public int getPortaTCP(){
        return this.portaTCP;
    }

    /* ====== Métodos ====== */

    public boolean conheceRover(String idRover){
        return (this.roversIP.containsKey(idRover)
             && this.roversPorta.containsKey(idRover)
             && this.roversEstado.containsKey(idRover));
    }

    public void adicionaRover(String id, InetAddress ip, int porta, Estado e){
        this.roversIP.put(id, ip);
        this.roversPorta.put(id, porta);
        this.roversEstado.put(id, e);
        System.out.println("[NaveMae] Novo rover adicionado: " + id);
    }

    public Missao getMissaoQueue() throws InterruptedException{
        return this.queue.take();
    }

    public void startNaveMae(){
        new Thread(() -> this.ml.startMLNaveMae(this)).start();
        //this.ts.startTLNaveMae(this);
        System.out.println("[NaveMae] Todos os serviços foram conectados\n");
    }

    public void atualizaEstado(){

    }

    public static void main(String[] args) {
        if(args.length < 1){
            System.out.println("[Uso] java NaveMae <ip>");
            return;
        }
        try{
            InetAddress ip = InetAddress.getByName(args[0]);
            NaveMae naveMae = new NaveMae(ip);


            Missao m1 = new Missao(100, 1, 2, 3, 4, "Andar", 1, 30);
            Missao m2 = new Missao(101, 5, 6, 7, 8, "Coletar", 2, 30);
            Missao m3 = new Missao(102, 9, 10, 11, 12, "Analisar", 3, 30);

            naveMae.queue.add(m1);
            naveMae.queue.add(m2);
            naveMae.queue.add(m3);

            naveMae.startNaveMae();

        }catch(UnknownHostException e){
            System.out.println("[NaveMae - ERRO]: problema com IP: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
