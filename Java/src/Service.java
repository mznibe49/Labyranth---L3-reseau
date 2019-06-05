import java.io.*;
import java.net.*;
import java.util.*;

public class Service implements Runnable {
  public Socket socket;
  public Server server;
  public Player p;
  public Game g;

  public Service(Socket socket, Server server, Player p, Game g){
    this.socket = socket;
    this.server = server;
    this.p = p;
    this.g = g;
  }

  public void setPlayer(Player p){
    this.p = p;
  }

  public Player getPlayer(){
    return this.p;
  }

  public Game getGame(){
    return this.g;
  }

  public void setGame(Game g){
    this.g = g;
  }

  public void launch(PrintWriter pw, BufferedReader br, DatagramSocket dso, Socket socket){
      try {
        server.treatGames(pw); //[GAMES n***] puis [GAME s m***] attente

        /* Permet de fermer la connexion depuis un message envoyé après le END */
        int i = 0;

        while(true){

          if(this.g != null && this.g.getStart()){
            i++;
            if(i>1){
              System.out.println("--- Labyrinthe de la partie n° --- " + this.g.getNumber());
              this.g.getMaze().displayMazeUltra4K();
            }
          }

          String message = server.receiveTCP(br);
          String type = message.split(" ")[0];

          if(message.equals(".")) break;

          switch(type){
            case "NEW": // [NEW␣id␣port***]
              String[] tab = message.split(" ");
              Player p = new Player(tab[1], Integer.parseInt(tab[2].substring(0, 4)));
              if(this.p == null){
                this.setPlayer(p);
                this.setGame(server.treatNewGame(pw, message,this.p));
              } else {
                server.sendTCP(pw, "REGNO***");
              }
              break;

            case "REG": // [REG␣id␣port␣m***]
              String[] tab2 = message.split(" ");
              Player player_reg = new Player(tab2[1], Integer.parseInt(tab2[2]));
              Game game_choice = server.getGameByNumber(Integer.parseInt(tab2[3].substring(0, 2)));
              if(game_choice != null && !game_choice.existInPlayers(player_reg)){
                this.setPlayer(player_reg);
                this.setGame(server.treatRegGame(pw, message, this.p));
              } else server.sendTCP(pw, "REGNO***");

              break;

            case "UNREG***":
              if(server.treatUnreg(pw, this.p, this.g))
                this.p = null;
              break;

            case "SIZE?":
              server.treatSize(pw, message);
              break;

            case "LIST?":
              server.treatList(pw, message);
              break;

            case "GAMES?***":
              server.treatGames(pw);
              break;

            case "START***":
              if(this.p != null && this.g != null){
                server.treatStart(pw, this.p, this.g);
                GhostsT gt = new GhostsT(this.g, dso);
                Thread t_gt = new Thread(gt);
            		t_gt.start();
              }
              else
                server.sendTCP(pw, "DUNNO***");
              break;

            case "UP": //[UP␣d***] [MOV␣x␣y***]
              if(g.getEnd()){
                server.sendTCP(pw, "BYE***");
                if(this.p != null){
                  g.removePlayer(this.p);
                  g.getMaze().getSquare(this.p.getX(), this.p.getY()).update(null, null);
                }
              } else g.setEnd(server.treatMove(pw, dso, this.g, this.p, message));
              break;

            case "DOWN": //[DOWN␣d***] [MOV␣x␣y***]
              if(g.getEnd()){
                server.sendTCP(pw, "BYE***");
                if(this.p != null){
                  g.removePlayer(this.p);
                  g.getMaze().getSquare(this.p.getX(), this.p.getY()).update(null, null);
                }
              } else g.setEnd(server.treatMove(pw, dso, this.g, this.p, message));
              break;

            case "RIGHT": // [RIGHT␣d***] [MOV␣x␣y***]
              if(g.getEnd()){
                server.sendTCP(pw, "BYE***");
                if(this.p != null){
                  g.removePlayer(this.p);
                  g.getMaze().getSquare(this.p.getX(), this.p.getY()).update(null, null);
                }
              } else g.setEnd(server.treatMove(pw, dso, this.g, this.p, message));
              break;

            case "LEFT": // [LEFT␣d***] [MOV␣x␣y***]
              if(g.getEnd()){
                server.sendTCP(pw, "BYE***");
                if(this.p != null){
                  g.removePlayer(this.p);
                  g.getMaze().getSquare(this.p.getX(), this.p.getY()).update(null, null);
                }
              } else g.setEnd(server.treatMove(pw, dso, this.g, this.p, message));
              break;

            case "GLIST?***": //[GLIST?***] puis renvoie [GLIST!␣s***] puis [GPLAYER␣id␣x␣y␣p***] s fois
              if(g.getEnd()){
                server.sendTCP(pw, "BYE***");
                if(this.p != null){
                  g.removePlayer(this.p);
                  g.getMaze().getSquare(this.p.getX(), this.p.getY()).update(null, null);
                }
              } else server.treatGList(pw, this.g);
              break;

            case "ALL?": //[ALL?␣mess***] renvoie [ALL!***] après avoir multi-diffusé
              if(g.getEnd()){
                server.sendTCP(pw, "BYE***");
                if(this.p != null){
                  g.removePlayer(this.p);
                  g.getMaze().getSquare(this.p.getX(), this.p.getY()).update(null, null);
                }
              } else server.treatAll(pw, dso, this.p.getId(), message, this.g.getMulticastPort(), this.g.getIpAddress());
              break;

            case "SEND?": //[SEND?␣id␣mess***] renvoie [SEND!***] après avoir envoyé en udp
              if(g.getEnd()){
                server.sendTCP(pw, "BYE***");
                if(this.p != null){
                  g.removePlayer(this.p);
                  g.getMaze().getSquare(this.p.getX(), this.p.getY()).update(null, null);
                }
              } else server.treatSend(pw, dso, this.p, this.g, message, socket.getInetAddress());
              break;

            case "QUIT***":
              server.treatQuit(pw, this.g, this.p);
              break;

            default:
              break;
          }
        }
      } catch (NegativeArraySizeException e) {
        if(g != null){
            System.out.println("Un joueur a quitté la partie n° " + g.getNumber());
        } else {
          System.out.println("Un joueur a quitté le jeu.");
        }
      } catch (Exception e){
        e.printStackTrace();
      }
    }

    public void run(){
      try{
        PrintWriter pw = new PrintWriter(new OutputStreamWriter(socket.getOutputStream()));
        BufferedReader br = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        DatagramSocket dso = new DatagramSocket();

        System.out.println("Adresse distante --- " + socket.getInetAddress().getHostAddress());
        this.launch(pw, br, dso, socket);

        if(g != null && g.getPlayers().size() == 0){
          System.out.println("Fin de partie n° " + g.getNumber());
          server.removeGameFromGames(g);
        }

        br.close();
        pw.close();
        dso.close();
        socket.close();
      } catch(SocketException e)  {
        System.out.println("Erreur --- Port peut-être déja utilisé.");
      } catch (Exception e){
        System.out.println(e);
      }
    }
}
