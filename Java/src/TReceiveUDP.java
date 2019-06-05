import java.io.*;
import java.net.*;

public class TReceiveUDP implements Runnable{
  DatagramSocket dso;

  public TReceiveUDP(DatagramSocket dso){
    this.dso = dso;
  }

  public void run(){
    try{
      byte[] data = new byte[100];
      DatagramPacket packet = new DatagramPacket(data, data.length);

      while(true){
        dso.receive(packet);
        String message = new String(packet.getData(), 0, data.length);
        System.out.print("\nUDP Reçu --- [" + message+"]\nCommande de jeu ~ : ");
      }
    } catch(Exception e){
      System.out.println("Connexion UDP close.");
    }
  }
}
