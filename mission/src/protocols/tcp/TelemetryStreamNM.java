package protocols.tcp;

import core.NaveMae;


import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;


public class TelemetryStreamNM {
     private final int porta;
     private Socket socket;
     private ServerSocket serverSocket;
     private volatile boolean running = true;

     public TelemetryStreamNM(int porta) throws IOException{
          this.porta = porta;
          this.serverSocket = new ServerSocket(porta);
          System.out.println("[NaveMae - TL] À escuta de TL na porta: " + porta);
     }

     public void startTLNaveMae(NaveMae nm){
          Thread t = new Thread(() -> handlerTSNaveMae(nm), "Thread - TelemetryStreamNM");
          t.start();
     }

     public void handlerTSNaveMae(NaveMae nm){
          while(running){
               try{
                    this.socket = this.serverSocket.accept();

                    
               }catch(IOException e){
                    System.out.println("[ERRO NaveMae - TS] Handler: " + e.getMessage());
                    e.printStackTrace();
               }
          }
     }
}
