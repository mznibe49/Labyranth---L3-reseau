import java.io.*;
import java.net.*;
import java.util.ArrayList;

public class GhostsT implements Runnable{
  Game g;
  DatagramSocket dso;

  public GhostsT(Game g, DatagramSocket dso){
    this.g = g;
    this.dso = dso;
  }

  public void run(){
    try{
      boolean loop = true;

      while(loop){
        byte[] data = new byte[100];
        //temps entre chaque déplacement de fantômes
        Thread.sleep(10000);
        /* Deplace de 5 cases chaque fantômes */
        ArrayList<Coordinates> ghosts_coordinates = g.moveAllGhosts(4);

        if(g.getPlayers().size() == 0 || g.getGhosts().size() == 0){
          loop = false;
        } else {
          System.out.println("Mouvements de fantômes parties n° " + g.getNumber());
          g.getMaze().displayMazeUltra4K();
          for(int i = 0 ; i < ghosts_coordinates.size() ; i++){
            //Envoi de [FANT␣x␣y+++]
            String message = "FANT "+checkPos(ghosts_coordinates.get(i).getX())+" "+checkPos(ghosts_coordinates.get(i).getY())+"+++";
            System.out.println("UDP Envoyé --- [" + message + "]");
            data = message.getBytes();
            DatagramPacket packet = new DatagramPacket(data, data.length, InetAddress.getByName(g.getIpAddress()), g.getMulticastPort());
            dso.send(packet);
          }
        }
      }
    } catch(Exception e){

    }
  }

  public String checkPos(int pos){
    if(pos < 10)
      return "00" + pos;
    else if(pos < 100)
      return "0" + pos;
    return String.valueOf(pos);
  }
}
