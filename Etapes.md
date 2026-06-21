Sprint0:
    Objectif:
        Creer un .jar qui capture tous les url taper dans le navigateur 

    Methodes:
        Creer FrontControllerServlet.java
            Il faut que tous les url passent par cette classe pour pouvoir capturer tous les url

Sprint1: 
    Objectif:
        Creer un .jar qui liste tous les classes qui sont annoter @controller

    Methodes:

        Avoir un bout de code :
            Qui se trouve dans la methode init()
            Decomposer ce qui doit etre decomposer , creer une classe utilitaire.java :
                prend en parametre le nom de package:
                    et retourne toutes les classe qui sont annoter dans cette package  
                prend en parametre une annotaion 

Sprint2:
    Objectif:
        Quand on tape un url  on sait quel est le controller et la methodes associe a cela

    
    Creer un annotation de type methode, a besoin d'une variable

    Dans ApplicationTest:
        Avoir une classe EmpController anooter @controller
            Avoir une methode liste() annote @urlMapping("/employer")


    Dans affichage via processRequest:
        /emplist            EmpController       methode:liste
        /emplist/new        EmpController       methode:create


    Si on ne connait pas l'url donc on fait une throws Exception et affiche toutes les listes qu'il connait 

    Si on connait on affiche seulement cette url correspondant 
