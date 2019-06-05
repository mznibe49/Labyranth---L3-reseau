import java.io.*;
import java.net.*;
import java.util.Scanner;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Iterator;
import java.util.regex.*;
import java.util.Random;

public class Server{
/* Port TCP du serveur */
 public int port_tcp;

 /* Listes des parties */
 public ArrayList<Game> games;

 /* Liste des adresses IP disponibles pour le multicast */
 public ArrayList<String> ip_address;

 /* Liste des ports disponibles pour le multicast */
 public ArrayList<Integer> multic_port;

 /* Liste des threads de parties */
 public ArrayList<Thread> tl;

 /* Liste de numéros de parties disponibles (0)1 à 99 */
 public LinkedList<Integer> available_numbers;

 public Server(int port_tcp, int nb_numbers){
   this.port_tcp = port_tcp;
   this.games = new ArrayList<Game>();
   this.tl = new ArrayList<Thread>();
   this.available_numbers = new LinkedList<Integer>();
   this.ip_address = new ArrayList<String>();
   this.multic_port = new ArrayList<Integer>();

   String str = "225.0.0.";
   for (int i = 0; i<99; i++){
     String tmp = str+i;
     ip_address.add(tmp);
   }

   for (int i= 0; i<99; i++){
     int tmp = 1024;
     this.multic_port.add(tmp+i);
   }

   for(int i = 0; i < nb_numbers; i++){
     this.available_numbers.add(new Integer(i));
   }
 }

 /*
   Suite à un message [NEW␣id␣port***], on créé une nouvelle partie et le joueur
 */
  Game treatNewGame(PrintWriter pw, String msg, Player p) throws IOException {
    Game g = null;
    try {
     String[] msg_splitted = msg.split(" ");
     int port = Integer.parseInt(msg_splitted[2].substring(0,4));
     String id = msg_splitted[1];
     String s_msg = "";

     /* Si le nb de partie est <= 99 */
     if(this.available_numbers.size() > 0){
       ArrayList<Player> players = new ArrayList<Player>();
       players.add(p);
       int nb = this.available_numbers.pop();

       //Maze maze = new Maze("../mazes/maze_01", 14, 36);
    	 //Maze maze = new Maze("../mazes/maze_03", 5, 37);
    	 //Maze maze = new Maze("../mazes/maze_02", 10, 5);

       Random rand = new Random();
     	 int randomWidth = rand.nextInt(30 - 20 + 1) + 20;
     	 int randomHeight = rand.nextInt(20 - 10 + 1) + 10;

       Maze maze = new Maze(randomHeight, randomWidth);

       g = new Game(maze, nb, this.ip_address.remove(0), this.multic_port.remove(0));
       g.addPlayer(p);
       this.games.add(g);

       String m = nb+"";
       s_msg = "REGOK " + check_number(m) + "***";
     } else {
       s_msg = "REGNO***";
     }
     sendTCP(pw, s_msg);
   } catch (Exception e){
     e.printStackTrace();
   }
   return g;
 }

 Game treatRegGame(PrintWriter pw, String msg, Player p) throws IOException {
      Game g = null;
      try { // renvoie la game pr l'enregistrer dans le thread
        String[] msg_splitted = msg.split(" ");
        String id = msg_splitted[1];
        int port = Integer.parseInt(msg_splitted[2]);
        String game_number = msg_splitted[3].substring(0,2);

        //Ne peut pas être nul puisque testé dans le switch en amont
        g = this.getGameByNumber(Integer.parseInt(game_number));
        System.out.println("Fantômes dans le labyrinthe n° "+ g.getNumber()+" --- " + g.getGhosts().size());
        g.addPlayer(p);
        String s_msg = "REGOK " + game_number + "***";
        sendTCP(pw, s_msg);

      } catch(Exception e) {
          sendTCP(pw, "REGNO***");
      }
      return g;
    }

    /*
     Suite à un message [UNREG***], renvoie [UNREGOK␣m***]
   */
   public boolean treatUnreg(PrintWriter pw, Player p, Game g) throws IOException {
     try {
       boolean supp = false;
       for(int i = 0; i<g.getPlayers().size(); i++){
         if(g.getPlayers().get(i).getId().equals(p.getId())){
           g.getPlayers().remove(i);
           supp = true;
         }
       }

       if(supp){
         int num_game = g.getNumber();
         String np = "" + num_game;
         String s = "UNREGOK "+check_number(np)+"***";
         sendTCP(pw, s);
         if(g.getPlayers().size() == 0){
           this.ip_address.add(g.getIpAddress());
           this.multic_port.add(g.getMulticastPort());
           removeGameFromGames(g);
         }

         return true;
       } else {
         sendTCP(pw, "DUNNO***");
       }
     } catch(Exception e) {
       sendTCP(pw, "DUNNO***");
     }

     return false;
   }

  /*
    Suite à un message [SIZE?␣m***], renvoie [SIZE!␣m␣h␣w***] ou [DUNNO***]
  */
  public void treatSize(PrintWriter pw, String msg) throws IOException {
    try {
      String m = msg.substring(6, 8);
      Game g = getGameByNumber(Integer.parseInt(m));

      if(g != null){
        Maze maze = g.getMaze();
        String h = maze.getHeight() + "";
        String w = maze.getWidth()  + "";
        String s_msg = "SIZE! " + check_number(m) + " " + check_number(h) + " " + check_number(w) + "***";
        sendTCP(pw, s_msg);
      } else {
        sendTCP(pw, "DUNNO***");
      }
    } catch (Exception e){
      System.out.println("Error: treatSize");
    }
  }

  /*
    Suite à un message [LIST?␣m***], renvoie [LIST!␣m␣s***] ou [DUNNO***]
  */
  public void treatList(PrintWriter pw, String msg) throws IOException {
    try{
      String m = msg.substring(6, 8);
      Game g = getGameByNumber(Integer.parseInt(m));

      if(g != null){
        String s = check_number(String.valueOf(g.getPlayers().size()));
        String message = "LIST! " +  m + " " + s + "***";
        sendTCP(pw, message);

        Iterator<Player> itp = g.getPlayers().iterator();
        while(itp.hasNext()){
          Player p = itp.next();
          String l_msg = "PLAYER " + p.getId() + "***";
          sendTCP(pw, l_msg);
        }
      } else {
        sendTCP(pw, "DUNNO***");
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  /*
    Suite à un message [GLIST?***], envoie [GLIST!␣s***] puis s fois [GPLAYER␣id␣x␣y␣p***]
  */
  public void treatGList(PrintWriter pw, Game g) throws IOException {
    try{
      String s = check_number(String.valueOf(g.getPlayers().size()));
      String message = "GLIST! " + s + "***";
      sendTCP(pw, message);

      Iterator<Player> itp = g.getPlayers().iterator();
      while(itp.hasNext()){
        Player p = itp.next();
        String gl_msg = "GPLAYER "+p.getId()+" "+checkPos(p.getX())+" "+checkPos(p.getY())+" "+checkScore(p.getScore())+"***";
        sendTCP(pw, gl_msg);
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  /*
    Après un [UP/DOWN/RIGHT/LEFT_d***]
    - on bouge le joueur et on renvoie soit [MOV␣x␣y***] ou soit [MOF␣x␣y␣p***]
  */
  public boolean treatMove(PrintWriter pw, DatagramSocket dso, Game g, Player p, String message){
    try{
      int distance = Integer.parseInt(message.split(" ")[1].substring(0, 3));
      char direction = message.toLowerCase().charAt(0);

      int tmp = p.getScore();

      //Bouge le joueur et renvoie les coordonnées des fantômes mangés
      ArrayList<Coordinates> ghosts_pos = g.movePlayer(p, direction, distance);

      if(tmp < p.getScore()){
        sendTCP(pw, "MOF "+checkPos(p.getX())+" "+checkPos(p.getY())+" "+checkScore(p.getScore())+"***");
        //[SCOR␣id␣p␣x␣y+++]
        if(ghosts_pos != null){
          /* Pour chaque position dans coordinates on envoie un message multi-diffusé */
          for(int i = 0 ; i < ghosts_pos.size() ; i++){
            String scor = "SCOR "+p.getId()+" "+checkScore(p.getScore())+" "+checkPos(ghosts_pos.get(i).getX())+" "+checkPos(ghosts_pos.get(i).getY())+"+++";
            sendPacket(dso, InetAddress.getByName(g.getIpAddress()), g.getMulticastPort(), scor);
          }
        }

        /* Si l'on a plus de fantômes dans la partie, on multi-diffuse END */
        if(g.getGhosts().size() == 0){
            Player best_player = g.getWinner();
            String end = "END "+best_player.getId()+" "+checkScore(best_player.getScore())+"+++";
            sendPacket(dso, InetAddress.getByName(g.getIpAddress()), g.getMulticastPort(), end);

            return true;
        }
      } else {
        sendTCP(pw, "MOV "+checkPos(p.getX())+" "+checkScore(p.getY())+"***");
      }
    } catch (Exception e) {
      e.printStackTrace();
    }

    return false;
  }

  public Game getGameByNumber(int number) throws Exception{
    for(int i = 0; i<this.games.size();i++){
      if(this.games.get(i).getNumber() == number)
        return this.games.get(i);
    }

    return null;
  }

  public void removeGameFromGames(Game g){
    Iterator<Game> itp = this.games.iterator();
    while(itp.hasNext()){
      //System.out.println("Remove dans while");
      Game myG = itp.next();
      if(myG.getNumber() == g.getNumber()){
        //On remet dans la liste le numero de la partie
        this.available_numbers.add(g.getNumber());
        this.games.remove(myG);
        break;
      }
    }
  }

  /* Lis dans br et l'affiche */
  String receiveTCP(BufferedReader br) throws IOException {
		int value = 0;
		int len = br.read();
		char[] chaine = new char[len];
		br.read(chaine, 0, chaine.length);
		String res = new String(chaine);

    System.out.println("TCP Reçu   --- ["+res+"]");
		return res;
	}

  /* Ecris dans pw la variable msg */
  public void sendTCP(PrintWriter pw, String msg) throws IOException {
    System.out.println("TCP Envoyé --- ["+ msg +"]");
    pw.write(msg.length());
    pw.flush();
    pw.print(msg);
    pw.flush();
  }

  /* Rajoute 0 au début d'un chiffre */
  public String check_number(String s) throws NumberFormatException {
    int res = Integer.parseInt(s);
    if(res <10) return "0"+res;
    return res+"";
  }

  /*
    Suite à une connexion d'un joueur ou /games
    - Ecrit dans l'argument un message de la forme [GAMES␣n***]
    - Envoie autant de messages [GAME␣m␣s***] qu'il y a de parties disponibles
  */
  public void treatGames(PrintWriter pw) throws IOException {
    int n = 0;

    for(int i = 0 ; i < this.games.size() ; i++){
      if(!this.games.get(i).getStart())
        n+=1;
    }

    String n2 = n + "";
    String res = "GAMES " + check_number(n2) + "***";
    sendTCP(pw, res);

    Iterator<Game> itg = this.games.iterator();
    while(itg.hasNext()){
      Game g = itg.next();
      if(!g.getStart()){
        String numb = g.getNumber() + "";
        String len = g.getPlayers().size() + "";
        String res2 = "GAME " + check_number(numb) + " " + check_number(len) + "***";
        sendTCP(pw, res2);
      }
    }
  }

  /*
    Suite à [SEND?␣id␣mess***] renvoie [SEND!***] après avoir envoyé en udp [MESP␣id2␣mess+++]
  */
  public void treatSend(PrintWriter pw, DatagramSocket dso, Player p, Game g, String message, InetAddress ia) throws IOException {
    String id = message.split(" ")[1];

    /* Gestion du message */
    String mess = message.substring(7 + id.length(), message.length()-3);
    int port = 0;

    boolean exist_in_game = false;

    Iterator<Player> itp = g.getPlayers().iterator();
    while(itp.hasNext()){
      Player p_next = itp.next();
      if(p_next.getId().equals(id)){
        port = p_next.getPort();
        exist_in_game = true;
      }
    }

    if(exist_in_game){
      String to_send = "MESP "+p.getId()+" "+mess+"+++";
      sendPacket(dso, ia, port, to_send);
      sendTCP(pw, "SEND!***");
    } else {
      sendTCP(pw, "NOSEND***");
    }
  }

  /*
  Suite à un message [QUIT?␣m***], renvoie [BYE***]
  */
  public void treatQuit(PrintWriter pw, Game g, Player p) throws IOException {
    try {
      g.getMaze().getSquare(p.getX(), p.getY()).update(null, null);
      g.removePlayer(p);
      sendTCP(pw, "BYE***");
    } catch (Exception e){
      System.out.println("Error: treatQuit");
    }
  }

  /*
    Suite à [ALL?␣mess***] envoie [ALL!***]
    après avoir envoyé [MESA␣id␣mess+++] aux joueurs de la partie
  */
  void treatAll(PrintWriter pw, DatagramSocket dso, String id_send, String m, int port, String ip) throws IOException {
    String msg = m.substring(5, m.length()-3);
    String message = "MESA "+id_send+" "+msg+"+++";
    sendPacket(dso, InetAddress.getByName(ip), port, message);
    sendTCP(pw, "ALL!***");
  }

  /*
    Suite à [START***], envoie [WELCOME␣m␣h␣w␣f␣ip␣port***]
  */
  public void treatStart(PrintWriter pw, Player p, Game g) throws IOException {
    try {
      if(p.getStart() == false){
        g.updatePlayerInPlayers(p);
        p.updateStart();
        boolean not_send = true;
        while(not_send){
          //On regarde si tous les joueurs sont prêts
          if(g.allPlayersReady()){

            g.addPlayerRandomly(p);
            g.setStart(true);
            String m = check_number(String.valueOf(g.getNumber()));
            String h = check_number(String.valueOf(g.getMaze().getHeight()));
            String w = check_number(String.valueOf(g.getMaze().getWidth()));
            String f = check_number(String.valueOf(g.getGhosts().size()));
            String ip = checkIp(g.getIpAddress());
            String port = String.valueOf(g.getMulticastPort());

            sendTCP(pw, "WELCOME "+m+" "+h+" "+w+" "+f+" "+ip+" "+port+"***");
            int x = p.getX();
            int y = p.getY();
            sendTCP(pw, "POS "+p.getId()+" "+checkPos(x)+" "+checkPos(y)+"***");
            not_send = false;
          }
        }
      } else {
        sendTCP(pw, "DUNNO***");
      }
    } catch (Exception e){
      sendTCP(pw, "DUNNO***");
      System.out.println("Erreur treatStart");
    }
  }

  public void sendPacket(DatagramSocket dso, InetAddress ia, int port, String message){
    try{
      System.out.println("Envoi à l'adresse --- " + ia.getHostAddress());
      byte[] data = message.getBytes();
      DatagramPacket packet = new DatagramPacket(data, data.length, ia, port);
      System.out.println("UDP Envoyé --- [" + message + "]");
      dso.send(packet);
    } catch(Exception e){
      System.out.println();
    }
  }

  public String checkIp(String ip){
    String ip_checked = ip;
    while(ip_checked.length() < 15){
      ip_checked = ip_checked + "#";
    }
    return ip_checked;
  }

  public String checkPos(int pos){
    if(pos < 10)
      return "00" + pos;
    else if(pos < 100)
      return "0" + pos;
    return String.valueOf(pos);
  }

  public String checkScore(int score){
    if(score < 10)
      return "000" + score;
    else if(score < 100)
      return "00" + score;
    else if(score < 1000)
      return "0" + score;
    return String.valueOf(score);
  }

  void launchServer(Server server){
    System.out.println("--- Demarrage du serveur --- port n° "+this.port_tcp);
    try{
      ServerSocket serverSocket = new ServerSocket(server.port_tcp);
      while(true){
        Socket socket = serverSocket.accept(); // socket du client
        // premier null = joueur, deuxieme null = partie
        Service sm = new Service(socket, this, null, null);
        Thread th = new Thread(sm);
        th.start();
        server.tl.add(th);
      }
    } catch (Exception e){
      System.out.println("Erreur --- Serveur peut-être déja lancé");
    }
  }


  public static void main(String[] args)  throws IOException {
    Server server = new Server(7458, 99);
    try {
      server.launchServer(server);
    } catch (Exception e){
      System.out.println("err main");
    }
  }
}
