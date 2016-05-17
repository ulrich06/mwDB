Problemes
---

## Probleme des merges avec contexte fils
Cas arrive lors qu'on fait une recurssion avec les taches: on a pas forcement envie que les sous taches impactes les variables des taches parentes.
Donc il faudrait une notion de tache global (i.e commune a une tache et tous ces fils) et local (variable uniquement accessible dans la touche courante)
 
Solution 1: mettre une notion de variable local et parente
Probleme avec les taches wrappe qui elles doivent pouvoir acceder aux variables locals et parentes

## Problemes des free sur objets en entree - OK
Empecher les free sur objets en input (notion de flqg a ajouter)
