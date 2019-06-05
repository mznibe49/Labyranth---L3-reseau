   __       _                           _   _     
  / /  __ _| |__  _   _ _ __ __ _ _ __ | |_| |__  
 / /  / _` | '_ \| | | | '__/ _` | '_ \| __| '_ \
/ /__| (_| | |_) | |_| | | | (_| | | | | |_| | | |
\____/\__,_|_.__/ \__, |_|  \__,_|_| |_|\__|_| |_|
                  |___/                           
                              _par Fröhlich Arthur,
                              Zniber Mohammed-Abbas
                              et Fritz Quentin_

Le projet a été réalisé en Java

Le serveur se lance via la commande (dans Java/bin/.)
$java Server

Et le client (les joueurs), se lancent via la commande (toujours dans Java/bin/.)
$java Player

Choisir un pseudo de 8 caractères alpha-numériques maximum, puis un port > 1000 et < 9999

I. COMMANDES AVANT LE START
===========================
* /games pour voir la listes des parties non démarrées
* /size m pour voir la taille de la partie m et son nombre de joueurs
* /new pour créer une partie
* /join x pour rejoindre la partie x[x]
* /unreg pour quitter sa partie
* /players x pour voir les joueurs de la parties x
* /start pour prévenir qu'on est prêt

II. COMMANDES APRÈS LE START
===========================
* /u|d|r|l x[xx] pour bouger de x[xx] cases vers respectivement haut/bas/droite/gauche
* /quit pour quitter la partie
* /players pour voir la liste des joueurs et leur score
* /all message pour envoyer le message all aux joueurs de la partie via multi-diffusion
* /w j message pour envoyer message en privé à j en UDP
