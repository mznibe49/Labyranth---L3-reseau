import java.io.*;
import java.net.*;

public class MulticastT implements Runnable{
  MulticastSocket mso;
  String ip;

  public MulticastT(MulticastSocket mso, String ip){
    this.mso = mso;
    this.ip = ip;
  }

  public void run(){
    try{
      mso.joinGroup(InetAddress.getByName(this.ip));

      while(true){
        byte[] data = new byte[300];
        DatagramPacket packet = new DatagramPacket(data, data.length);
        mso.receive(packet);
        String message = new String(packet.getData(), 0, data.length);
        System.out.print("\nMCT Reçu --- [" + message+"]\nCommande de jeu ~ : ");
      }
    } catch(Exception e){
      System.out.println("Connexion à la diffusion close.");
    }
  }
}
