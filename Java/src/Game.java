import java.util.ArrayList;
import java.util.LinkedList;
import java.lang.Math;
import java.util.Random;
import java.util.Iterator;

public class Game{

	Maze maze;
	ArrayList<Player> players;
	ArrayList<Ghost> ghosts;
	int number;
	boolean start = false;
	boolean end = false;
	String ip_address;
	int multicast_port;

	public Game(Maze maze, int number, String ip, int mp){
		this.players = new ArrayList<Player>();
		this.maze = maze;
		this.number = number;
		this.ip_address = ip;
		this.multicast_port = mp;
		this.ghosts = addGhostRandomly(2);
  }

 	/*
		Retourne la liste des cases libres (ni fantôme ni joueur ni mur)
 	*/
	public ArrayList<Coordinates> getFreeSquares(){
		ArrayList<Coordinates> coupleList = new ArrayList<Coordinates>();
		for(int i = 0 ; i < this.maze.getHeight() ; i++){
			for(int j = 0 ; j < this.maze.getWidth() ; j++){
				if(this.maze.getSquare(i, j).getType() != 'L' && this.maze.getSquare(i, j).getType() != 'W' && this.maze.getSquare(i, j).getPlayer() == null && this.maze.getSquare(i, j).getGhost() == null){
					coupleList.add(new Coordinates(i, j));
				}
			}
		}

		return coupleList;
	}

 	/*
		Liste de couples de position des cases n'étant pas mur ('W') ou occupées (par un Ghost ou un Player)
		On choisit de manière aléatoire un des couples de cases libres dans notre ArrayList
		On ajoute à notre Maze le Player/Ghost
	*/
	public void addPlayerRandomly(Player player){
		ArrayList<Coordinates> coupleList = this.getFreeSquares();

		int nb_random = 0 + (int)(Math.random() * ((coupleList.size() - 0)));
		Coordinates player_position = coupleList.get(nb_random);
		player.setPosition(player_position.getX(), player_position.getY());
		player.setScore(0);
		this.maze.getSquare(player_position.getX(), player_position.getY()).update(player, null);
	}

	//Pour des tests
	public void addP(Player player, int x, int y){
		this.maze.getSquare(x, y).update(player, null);
	}

	/* Ajoute nb_ghosts fantômes au maze de la partie */
	public ArrayList<Ghost> addGhostRandomly(int nb_ghosts){
		ArrayList<Ghost> ghosts = new ArrayList<Ghost>();
		for(int i = 0; i < nb_ghosts ; i++){
			ghosts.add(new Ghost(i));
		}

		for(Ghost gh : ghosts){
			ArrayList<Coordinates> coupleList = this.getFreeSquares();
			int nb_random = new Random().nextInt(coupleList.size());
			Coordinates ghost_position = coupleList.get(nb_random);
			gh.setPosition(ghost_position.getX(), ghost_position.getY());
			this.maze.getSquare(ghost_position.getX(), ghost_position.getY()).update(null, gh);
		}

		return ghosts;
	}

	public void removeGhost(Ghost g){
		for(int i = 0 ; i < this.ghosts.size() ; i++){
			if(this.ghosts.get(i).getId() == g.getId()){
				this.ghosts.remove(this.ghosts.get(i));
			}
		}
	}

	/*
		*** --------------------- WIP ***
		Déplace un fantôme sur nb_moves cases
		Il s'arrête sur une case non occupée et qui n'est pas un mur
	*/
	public Coordinates moveGhostRandomly(Ghost casper, int nb_moves){
		LinkedList<Character> directions;
		ArrayList<Coordinates> targets_pos;

		int count_moves = 0, nb_random;
		char move;
		Coordinates casperPos;
		boolean quit = false;

		while(!quit){
			targets_pos = new ArrayList<Coordinates>();
			directions = new LinkedList<Character>();
			/* Up */
			targets_pos.add(new Coordinates(casper.getX() - nb_moves, casper.getY(), 'u'));
			/* Down */
			targets_pos.add(new Coordinates(casper.getX() + nb_moves, casper.getY(), 'd'));
			/* Right */
			targets_pos.add(new Coordinates(casper.getX(), casper.getY() + nb_moves, 'r'));
			/* Left */
			targets_pos.add(new Coordinates(casper.getX(), casper.getY() - nb_moves, 'l'));


			for(Coordinates pc : targets_pos){
				try{
					if(this.maze.getSquare(pc.getX(), pc.getY()).getType() == ' '){
						directions.add(pc.getDirection());
					}
				} catch(Exception e){
				}
			}

			if(directions.size() > 0){
				nb_random = 1 + (int)(Math.random() * ((directions.size() - 1) + 1));
				while(count_moves < nb_moves){
					move = directions.get(nb_random-1);
					Coordinates next_pos = this.moveGhost(casper, move);
					if(this.maze.getSquare(next_pos.getX(), next_pos.getY()).getPlayer() != null || this.maze.getSquare(next_pos.getX(), next_pos.getY()).getGhost() != null){
						break;
					} else {
						this.maze.getSquare(casper.getX(), casper.getY()).update(null, null);
						casper.setPosition(next_pos.getX(), next_pos.getY());
						this.maze.getSquare(casper.getX(), casper.getY()).updateGhost(casper);
					}
					count_moves+=1;
				}
				quit = true;
			} else {
				/* Ok c'est dégeux, on decremente si jamais le fantôme ne trouve pas de cases disponibles */
				nb_moves = nb_moves - 1;
			}
		}

		return new Coordinates(casper.getX(), casper.getY());
	}

	/* Deplace tous les fantômes de la partie */
	public ArrayList<Coordinates> moveAllGhosts(int nb_squares){
		ArrayList<Coordinates> coordinates = new ArrayList<Coordinates>();
		for(int i = 0 ; i < ghosts.size() ; i++){
			Coordinates ghost_coordinates = moveGhostRandomly(ghosts.get(i), nb_squares);
			coordinates.add(ghost_coordinates);
		}

		return coordinates;
	}

	/*
		Renvoie la position de la case où le fantôme doit se déplacer selon direction
		Un peu moche ?
	*/
	public Coordinates moveGhost(Ghost casper, char direction){
		Coordinates next_pos = new Coordinates(casper.getX(), casper.getY());

		switch(direction){
			case 'u':
				next_pos = new Coordinates(casper.getX()-1, casper.getY());
				break;
			case 'd':
				next_pos = new Coordinates(casper.getX()+1, casper.getY());
				break;
			case 'l':
				next_pos = new Coordinates(casper.getX(), casper.getY()-1);
				break;
			case 'r':
				next_pos = new Coordinates(casper.getX(), casper.getY()+1);
				break;
		}

		return next_pos;
	}

	/*
		Renvoi la direction opposée à direction
	*/
	public char oppositeSide(char direction){
		char opposite = 'N';
		switch(direction){
			case 'u':
				opposite = 'd';
				break;
			case 'd':
				opposite = 'u';
				break;
			case 'l':
				opposite = 'r';
				break;
			case 'r':
				opposite = 'l';
				break;
			case 'N':
				opposite = 'N';
				break;
		}

		return opposite;
	}

	/*
		A factoriser ?
		Renvoi la position du joueur après l'avoir deplacé
		Ici, on ne déplace pas virtuellement le joueur sur chaque case mais juste sur la case où il arrive au final
		(on évite ainsi le problème de deux joueurs sur une même case lorsqu'ils se croisent)
		A noter que l'on place le joueur seulement si la case n'a pas un statut occupé
	*/
	public ArrayList<Coordinates> movePlayer(Player p, char direction, int nb_squares){
		ArrayList<Coordinates> coordinates = new ArrayList<Coordinates>();

		int count_moves = 0, tmpX = p.getX(), tmpY = p.getY(), change_pos = 0, axis = 0;
		boolean quit = false;

		/* axis -> 1 = u ; 2 = d ; 3 = l ; 4 = r */
		switch(direction){
			case 'u':
				change_pos = -1;
				axis = 1;
				break;
			case 'd':
				change_pos = 1;
				axis = 2;
				break;
			case 'l':
				change_pos = -1;
				axis = 3;
				break;
			case 'r':
				change_pos = 1;
				axis = 4;
				break;
		}

		if(axis == 1 || axis == 2){
			while(count_moves < nb_squares && !quit){
				if(!(tmpX == 0 && axis == 1) && !(tmpX == this.maze.getHeight()-1 && axis == 2)){
					if(this.maze.getSquare(tmpX+change_pos, tmpY).getType() != 'W'){
						if(this.maze.getSquare(tmpX+change_pos, tmpY).getPlayer() == null){ // Si la case d'après est libre
							count_moves+=1;
							tmpX+=change_pos;
							if(this.maze.getSquare(tmpX, tmpY).getGhost() != null){ //si sur la case sur laquelle on s'est deplacé il y a un fantôme
								removeGhost(this.maze.getSquare(tmpX, tmpY).getGhost());
								this.maze.getSquare(tmpX, tmpY).updateGhost(null);
								p.setScore(p.getScore() + 1);
								coordinates.add(new Coordinates(tmpX, tmpY));
							}
						} else if(this.maze.getSquare(tmpX+change_pos, tmpY).getPlayer() != null){ // Si la case d'après n'est pas libre
							if(nb_squares - count_moves > 1){
								int tmp = tmpX;
								while(count_moves < nb_squares){ // Boucle temporaire cherchant une possible case libre
									if(!(tmp == 0 && axis == 1) && !(tmp == this.maze.getHeight()-1 && axis == 2)){
										if(this.maze.getSquare(tmp+change_pos, tmpY).getType() != 'W'){
											if(this.maze.getSquare(tmp+change_pos, tmpY).getPlayer() == null){
												tmpX = tmp;
												if(this.maze.getSquare(tmpX, tmpY).getGhost() != null){ //si sur la case sur laquelle on s'est deplacé il y a un fantôme
													removeGhost(this.maze.getSquare(tmpX, tmpY).getGhost());
													this.maze.getSquare(tmpX, tmpY).updateGhost(null);
													p.setScore(p.getScore() + 1);
													coordinates.add(new Coordinates(tmpX, tmpY));
												}
												break;
											} else {
												tmp+=change_pos;
											}
											count_moves+=1;
										} else {
											quit = true;
											break;
										}
									} else {
										quit = true;
										break;
									}
								}
							} else {
								quit = true;
							}
						}
					} else {
						quit = true;
					}
				} else {
					quit = true;
				}
			}
		} else {
			while(count_moves < nb_squares && !quit){
				if(!(tmpY == 0 && axis == 3) && !(tmpY == this.maze.getWidth()-1 && axis == 4)){
					if(this.maze.getSquare(tmpX, tmpY+change_pos).getType() != 'W'){
						if(this.maze.getSquare(tmpX, tmpY+change_pos).getPlayer() == null){ // Si la case d'après est libre
							count_moves+=1;
							tmpY+=change_pos;
							if(this.maze.getSquare(tmpX, tmpY).getGhost() != null){ //si sur la case sur laquelle on s'est deplacé il y a un fantôme
								removeGhost(this.maze.getSquare(tmpX, tmpY).getGhost());
								this.maze.getSquare(tmpX, tmpY).updateGhost(null);
								p.setScore(p.getScore() + 1);
								coordinates.add(new Coordinates(tmpX, tmpY));
							}
						} else if(this.maze.getSquare(tmpX, tmpY+change_pos).getPlayer() != null){ // Si la case d'après n'est pas libre
							if(nb_squares - count_moves > 1){
								int tmp = tmpY;
								while(count_moves < nb_squares){ // Boucle temporaire cherchant une possible case libre
									if(!(tmp == 0 && axis == 3) && !(tmp == this.maze.getWidth()-1 && axis == 4)){
										if(this.maze.getSquare(tmpX, tmp+change_pos).getType() != 'W'){
											if(this.maze.getSquare(tmpX, tmp+change_pos).getPlayer() == null){
												tmpY = tmp;
												if(this.maze.getSquare(tmpX, tmpY).getGhost() != null){ //si sur la case sur laquelle on s'est deplacé il y a un fantôme
													removeGhost(this.maze.getSquare(tmpX, tmpY).getGhost());
													this.maze.getSquare(tmpX, tmpY).updateGhost(null);
													p.setScore(p.getScore() + 1);
													coordinates.add(new Coordinates(tmpX, tmpY));
												}
												break;
											} else {
												tmp+=change_pos;
											}
											count_moves+=1;
										} else {
											quit = true;
											break;
										}
									} else {
										quit = true;
										break;
									}
								}
							} else {
								quit = true;
							}
						}
					} else {
						quit = true;
					}
				} else {
					quit = true;
				}
			}
		}

		maze.getSquare(p.getX(), p.getY()).update(null, null);
		p.setPosition(tmpX, tmpY);
		maze.getSquare(tmpX, tmpY).update(p, null);
		return coordinates; //coordonnées des fantômes "mangés"
	}

	public Player getPlayerFromId(String id) throws Exception {
		Iterator<Player> itp = this.players.iterator();
		while(itp.hasNext()){
			if(itp.next().getId().equals(id))
				return itp.next();
			itp.next();
		}

		throw new Exception("Player not found.");
	}

	/* Renvoie true si un joueur de players a le même id que p */
	public boolean existInPlayers(Player p){
		Iterator<Player> itp = this.players.iterator();
		while(itp.hasNext()){
			Player myP = itp.next();
			if(myP.getId().equals(p.getId()))
				return true;
		}
		return false;
	}

	public boolean allPlayersReady(){
		Iterator<Player> itp = this.players.iterator();
		while(itp.hasNext()){
			Player p = itp.next();
			if(p.getStart() == false){
				return false;
			}
		}
		 return true;
	}

	public boolean updatePlayerInPlayers(Player p_to_change){
		Iterator<Player> itp = this.players.iterator();
		while(itp.hasNext()){
			Player p = itp.next();

			if(p.getId().equals(p_to_change.getId())){
				p.updateStart();
				p_to_change.updateStart();
				return true;
			}
		}

		return false;
	}

	public Player getWinner(){
		ArrayList<Player> best_players = new ArrayList<Player>();
		/* Meilleur score de la partie */
		int best_score = 0;
		for(int i = 0 ; i < players.size() ; i++)
			if(players.get(i).getScore() >= best_score)
				best_score = players.get(i).getScore();

		for(int i = 0 ; i < players.size() ; i++)
			if(players.get(i).getScore() == best_score)
				best_players.add(players.get(i));

			int nb_random = 0 + (int)(Math.random() * ((1 - 0)));
			return best_players.get(nb_random);
	}

	public boolean removePlayer(Player p){
		return this.players.remove(p);
	}

	public void addPlayer(Player p){
		this.players.add(p);
	}

	boolean getStart(){
		return this.start;
	}

	void setStart(boolean b){
		this.start = b;
	}

	boolean getEnd(){
		return this.end;
	}

	void setEnd(boolean b){
		this.end = b;
	}

	/* Getteur de la liste des joueurs de la partie */
	public ArrayList<Player> getPlayers(){
		return this.players;
	}

	/* Getteur du dédalle de la partie */
	public Maze getMaze(){
		return this.maze;
	}

	/* Getteur de numero de la partie */
	public int getNumber(){
		return this.number;
	}

	/* Getteur de l'ip de la partie */
	public String getIpAddress(){
		return this.ip_address;
	}

	/* Getteur du port de la partie */
	public int getMulticastPort(){
		return this.multicast_port;
	}

	/* Getteur des fantômes de la partie */
	public ArrayList<Ghost> getGhosts(){
		return this.ghosts;
	}
}
