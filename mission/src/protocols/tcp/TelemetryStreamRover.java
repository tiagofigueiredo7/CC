package protocols.tcp;

import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;

import core.Rover;

public class TelemetryStreamRover {
    private final String id;
    private final int porta;
    private final InetAddress ip;
    private final Socket socket;
    private volatile boolean running = true;

    public TelemetryStreamRover(String id, int porta, InetAddress ip) throws IOException{
        this.id = id;
        this.porta = porta;
        this.ip = ip;
        this.socket = new Socket(ip, porta);
        System.out.println("[" + id + " - TS] Conectado na porta: " + porta);
    }

    public void startTSRover (Rover rover){
    }
}
