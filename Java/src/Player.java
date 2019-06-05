import java.net.*;
import java.util.*;
import java.io.*;
import java.net.*;
import java.util.regex.*;

public class Player {
	/* 8 caractères alpha-numériques */
	String id;
	int score, x, y;
	int port_udp;
	boolean start;

	/* Constructeur de chargement de partie */
	public Player(String id, int port_udp){
		this.id = id;
		this.x = -1;
		this.y = -1;
		this.score = 0;
		this.port_udp = port_udp;
		this.start = false;
	}

	// renvoie le string lu
	String readInfo(BufferedReader br) throws IOException {
		int value = 0;
		int len = br.read();
		char[] chaine = new char[len];
		br.read(chaine, 0, chaine.length);
		String res = new String(chaine);

		System.out.println("TCP Reçu   --- ["+res+"]");
		return res;
	}

	/* Ecris dans pw la variable msg */
	public void writeInfo(PrintWriter pw, String msg) throws IOException {
		System.out.println("TCP Envoyé --- ["+ msg +"]");
		pw.write(msg.length());
		pw.flush();
		pw.print(msg);
		pw.flush();
	}

	void games(BufferedReader br) throws IOException {
		// tu recois [GAMES n***] n est jtrs sur 2 oc le serv doit envoyé le bon msg
		// puis [GAMES m s***]
		String s = readInfo(br);
		int num_game = Integer.parseInt(s.substring(6, 8));
		for(int i = 0; i < num_game; i++){
			readInfo(br);
		}
	}

	void treatList(BufferedReader br) throws IOException {
		// tu recois [LIST!␣m␣s***]
		// puis [PLAYER id***] s fois
		String s = readInfo(br);
		if(s.length() != 8){ //[DUNNO***]
			int nb_players = Integer.parseInt(s.substring(9, 11));
			for(int i = 0; i < nb_players; i++){
				readInfo(br);
			}
		}
	}

	// verifie la bonne format de l'entier
	String check_number(String s) throws NumberFormatException {
		int res = Integer.parseInt(s);
		if(res <10) return "0"+res;
		return res+"";
	}

	// avant le debut de la partie :'(
	String before_start(PrintWriter pw, BufferedReader br) throws Exception {
		games(br);

		boolean loop = true;
		while(loop){
			System.out.print("Commande ~ : ");
			Scanner sc = new Scanner(System.in);
			String str = sc.nextLine();
			String[] tab = str.split(" ");

			if(tab.length == 1){
				if(str.equals("/new")){ 	// le cleint envoie [NEW id port***]
					String nw = "NEW "+this.id+" "+this.port_udp+"***";
					writeInfo(pw, nw);
					readInfo(br);
				}

				if(str.equals("/games")){
					writeInfo(pw, "GAMES?***");
					games(br); // le client reçois [GAMES␣n***]
				}

				if(str.equals("/unreg")){ // demander la deconnxion
					writeInfo(pw, "UNREG***"); // la taille est 8
					readInfo(br);
				}

				if(str.equals("/start")){ // demander la deconnxion
					writeInfo(pw, "START***"); // la taille est 8
					String check_welcome = treat_start(br);
					if(!check_welcome.equals("DUNNO***")){
						return check_welcome;
					}
				}
			} else if(tab.length == 2 && Pattern.matches("\\d{1,2}", tab[1])) {
					if(tab[0].equals("/join")){ // le client ecrit le numero d'une partie [REG␣id␣port␣m***] 3 + 1 + 2 + 1 + 4 + 1 + 2 + 3
					String number = check_number(tab[1]);
					String nw = "REG "+this.id+" "+this.port_udp+" "+number+"***";
					writeInfo(pw, nw); // la taille est 17, on doit inverser le 12eme bit
					readInfo(br);
				}

				if(tab[0].equals("/size")){ // demander la taille [SIZE? m***]
					String number = check_number(tab[1]);
					String nw = "SIZE? "+number+"***";
					writeInfo(pw, nw); // la taille est 11, on doit inverser le 6eme bit
					readInfo(br);
				}

				if(tab[0].equals("/players")){ // demande la liste des joueur dans une patries [LIST? m***]
					String number = check_number(tab[1]);
					String nw = "LIST? "+number+"***";
					writeInfo(pw, nw);
					treatList(br);
				}
			}
			if(str.equals(".")) break;// pour eviter l'erreur unreachable statement ds.close()
		}
		return "Error";
	}

	void after_start(BufferedReader br, PrintWriter pw) throws IOException {
		String pos = readInfo(br); //[POS␣id␣x␣y***]
		String[] tab = pos.split(" ");
		String id = tab[1];
		String x = tab[2];
		String y = tab[3];

		try{
			boolean loop = true;

			while(loop){
				System.out.print("Commande de jeu ~ : ");
				Scanner sc = new Scanner(System.in);
				String str = sc.nextLine();

				String[] cmd = str.split(" ");
				// au debut c'est le joueur qui envoie un move

				if(cmd.length == 2){
					String direction = cmd[0]; // direction ou bien /all
					if(Pattern.matches("/(u|d|r|l)\\b", direction)){
						String dist_m = cmd[1]; // soit une distance soit un msg pr all
						if(Pattern.matches("\\d{1,3}\\b", dist_m)){
							String s_dist = checkPos(Integer.parseInt(dist_m)); // tronsformer sur 3oc , 5 devient 005
							String to_send = "";

							switch(direction){  // [UP␣d***],[DOWN␣d***], [LEFT␣d***] et [RIGHT␣d***]
								case "/u":
								to_send = "UP "+s_dist+"***";
								break;

								case "/d":
								to_send = "DOWN "+s_dist+"***";
								break;

								case "/l":
								to_send = "LEFT "+s_dist+"***";
								break;

								case "/r":
								to_send = "RIGHT "+s_dist+"***";
								break;
							}

							writeInfo(pw, to_send);
							if(readInfo(br).equals("BYE***"))//Reçoit [MOV␣x␣y***] ou [MOF x y p***]
								loop = false;
						}
					} else if(cmd[0].equals("/all")){
						String msg = str.substring(5, str.length());
						if(checkStars(msg) && checkPlus(msg)){
							writeInfo(pw,"ALL? "+str.substring(5, str.length())+"***");  // [ALL?␣mess***]
							if(readInfo(br).equals("BYE***"))// Reçoit [SEND!***] ou [NOSEND***]
								loop = false;
						}
					}
				} else if (cmd.length == 1){ // quit ou players
					switch(str){
						case "/quit" :
							writeInfo(pw, "QUIT***");
							readInfo(br); // reçois [BYE***]
							loop = false;
							break;

						case "/players" :
							writeInfo(pw, "GLIST?***");
							String s = readInfo(br); // Reçoit [GLIST!␣s***]
							String[] sp = s.split(" ");
							int nb_p = Integer.parseInt(sp[1].substring(0,2));
							for(int i = 0; i<nb_p; i++){
								if(readInfo(br).equals("BYE***"))// Reçoit [GPLAYER␣id␣x␣y␣p***]
									loop = false;
							}
							break;
					}
				} else if (cmd.length >= 3){ // [SEND?␣id␣mess***]
					if(cmd[0].equals("/w")){
						String msg = str.substring(4+cmd[1].length(), str.length());
						if(checkStars(msg) && checkPlus(msg)){
							writeInfo(pw,"SEND? "+cmd[1]+" "+msg+"***");
							if(readInfo(br).equals("BYE***"))// Reçoit [SEND!***] ou [NOSEND***]
								loop = false;
						}
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	String treat_start(BufferedReader br) throws Exception { //S = welcome
		System.out.println("En attente que les autres joueurs fassent start...");
		String s = readInfo(br); //[WELCOME␣m␣h␣w␣f␣ip␣port***]
		if(!s.equals("DUNNO***")){
			return s;
		}
		return s;
	}

	public String checkPos(int pos){
		if(pos < 10)
		return "00" + pos;
		else if(pos < 100)
		return "0" + pos;
		return String.valueOf(pos);
	}

	public static String chooseId(){
		boolean loop = true;
		String id = "";

		while(loop){
			Scanner sc = new Scanner(System.in);
			System.out.println("Pseudo ? ");
			System.out.print("> ");
			id = sc.nextLine();
			loop = !Pattern.matches("^\\w{1,8}\\b", id);
		}
		return id;
	}

	public static int choosePort(){
		boolean loop = true;
		String port = "";

		while(loop){
			Scanner sc = new Scanner(System.in);
			System.out.println("Port ? ");
			System.out.print("> ");
			port = sc.nextLine();
			loop = !Pattern.matches("^[1-9]\\d{3}\\b", port);
		}
		return Integer.parseInt(port);
	}

	int checkDist(String s){
		int res = -1;
		try {
			int tmp = Integer.parseInt(s);
			res = tmp;
		} catch (Exception e){
			return -1;
		}
		return res;
	}

	public static boolean checkStars(String s){
		int k = 0;
		for(int i = 0; i<s.length(); i++){
			if(s.charAt(i) == '*' )   k++;
			else  k = 0;
			if(k == 3) return false;
		}
		return true;
	}

	public static boolean checkPlus(String s){
		int k = 0;
		for(int i = 0; i<s.length(); i++){
			if(s.charAt(i) == '+' ) k++;
			else k = 0;
			if(k == 3) return false;
		}
		return true;
	}

	/* Retire les # de l'id */
	static String cleanIP(String ip){
		String ip_cleaned = "";
		for(int i = 0; i < ip.length() ; i++){
			if(ip.charAt(i) !=  '#')
			ip_cleaned = ip_cleaned + ip.charAt(i);
		}

		return ip_cleaned;
	}

	public String getId(){
		return this.id;
	}

	public void setPosition(int x, int y){
		this.x = x;
		this.y = y;
	}

	public void setX(int x){
		this.x = x;
	}

	public void setY(int y){
		this.y = y;
	}

	public int getX(){
		return this.x;
	}

	public int getY(){
		return this.y;
	}

	public int getScore(){
		return this.score;
	}

	public void setScore(int new_score){
		this.score = new_score;
	}

	public int getPort(){
		return this.port_udp;
	}

	public boolean getStart(){
		return this.start;
	}

	public void updateStart(){
		this.start = !start;
	}

	public static void main(String[] args) {
		String id = chooseId();
		int port = choosePort();
		Player p = new Player(id, port);

		try {
			Socket soc = new Socket("127.0.0.1", 7458);
			//Socket soc = new Socket("Adresse ip arthur", 7458);
			//Socket soc = new Socket("192.168.1.53", 7455);

			PrintWriter pw = new PrintWriter(new OutputStreamWriter(soc.getOutputStream()));
			BufferedReader br = new BufferedReader(new InputStreamReader(soc.getInputStream()));

			/* Thread du reçu de paquet UDP */
			DatagramSocket dso = new DatagramSocket(p.getPort());
			TReceiveUDP p_udp = new TReceiveUDP(dso);
			Thread t_udp = new Thread(p_udp);
			t_udp.start();

			/* Thread de reçu de message du multicast */
			String welcome_message = p.before_start(pw, br);
			String[] tab_welcome = welcome_message.split(" "); //[WELCOME␣m␣h␣w␣f␣ip␣port***]
			String ip_multicast = cleanIP(tab_welcome[5]);
			int port_multicast = Integer.parseInt(tab_welcome[6].substring(0, tab_welcome[6].length()-3));

			MulticastSocket mso = new MulticastSocket(port_multicast);
			MulticastT mt = new MulticastT(mso, ip_multicast);
			Thread t_mc = new Thread(mt);
			t_mc.start();
			p.after_start(br, pw);

			br.close();
			pw.close();
			dso.close();
			mso.close();
			soc.close();
		} catch(ConnectException e){
			System.out.println("Erreur lors de la connexion.");
		} catch(UnknownHostException e){
			System.out.println("Hôte inconnu.");
		} catch(IOException e){
			System.out.println("Erreur IO.");
		} catch(Exception e) {
			System.out.println("Une erreur fatale.");
		}
	}
}
