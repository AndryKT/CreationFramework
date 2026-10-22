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


Sprint3:
    Sprint 3:
        On modifie UrlMapping , on ajoute un argument : GET ou POST
        Donc le map devient: 
            <url , Mapping>     => <urlMethode ,Mapping>

        Dans la classe UrlMapping , il y a 2 attribut : url , methode (GET/POST)
        avec fonction equals surdefini:  quand 2 urlMethod sont parreil donc , le deuxieme n'est pas inserer dans la map

    Srpint 3 bis:
        On incoke la methode avec l'url associe

Sprint 4:
    Au lieu d'appele les precedent lors de son appel , on le fait directement au demarrage de l'aaplication
        On utilise Listener


Sprint 5:

1) ENVOYER LES DONNES DU CONTROLLER VERS UN VIEW:
Creer une classe: **ModelAndView**.

Le principe est le suivant :

```text
Controller
      │
      │ retourne
      ▼
ModelAndView
      │
      ├── nom de la vue
      └── données (Model)
      │
      ▼
FrontController
      │
      ▼
JSP
```

---

# Étape 1 : créer une classe `ModelAndView`

Par exemple :

```java
package model;

import java.util.HashMap;

public class ModelAndView {

    private String view;
    private HashMap<String, Object> data = new HashMap<>();

    public ModelAndView() {
    }

    public ModelAndView(String view) {
        this.view = view;
    }

    public String getView() {
        return view;
    }

    public void setView(String view) {
        this.view = view;
    }

    public HashMap<String, Object> getData() {
        return data;
    }

    public void addObject(String key, Object value) {
        data.put(key, value);
    }
}
```

---

# Étape 2 : le contrôleur

Au lieu de retourner `void`, tu retournes un `ModelAndView`.

```java
@Controller
public class EmpController {

    @UrlMapping(value="/liste", method="GET")
    public ModelAndView liste() {

        ModelAndView mv = new ModelAndView("liste.jsp");

        mv.addObject("nom", "Andry");
        mv.addObject("age", 21);
        mv.addObject("ville", "Antananarivo");

        return mv;
    }

}
```

Ici, les données sont "en dur".

---

# Étape 3 : dans le FrontController

Après avoir trouvé la méthode :

```java
Method method = ...
Object controller = ...
```

Tu fais :

```java
ModelAndView mv = (ModelAndView) method.invoke(controller);
```

Ensuite :

```java
for(String key : mv.getData().keySet()){

    request.setAttribute(
        key,
        mv.getData().get(key)
    );

}
```

Puis tu rediriges vers la vue :

```java
request.getRequestDispatcher("/" + mv.getView())
       .forward(request, response);
```

---

# Étape 4 : dans la JSP

Tu récupères simplement les attributs :

```jsp
Nom : ${nom}
<br>

Age : ${age}
<br>

Ville : ${ville}
```

ou avec les scriptlets (moins recommandé aujourd'hui) :

```jsp
<%= request.getAttribute("nom") %>
```

---

# Le résultat

Le contrôleur :

```java
mv.addObject("nom", "Andry");
mv.addObject("age", 21);
```

↓

Le `FrontController` fait :

```java
request.setAttribute("nom", "Andry");
request.setAttribute("age", 21);
```

↓

Dans `liste.jsp` :

```jsp
${nom}
```

affichera :

```text
Andry
```

---


## Sprint6

    Ajouter un autre annotation niveau method:
    Et si pas annoter avec cette annoation alors on l'envoir comme avantt dans les view sinon on affiche direct le Json
    Si la valeur de retour de la methode est string donc on l'affiche direct avec printwritter , sinon on le convertis d'abord en Json

## Sprint7
    Binding:
        (donnees viwe -> controller)
        les donnees recus d'un formulaire -> controller


    1ere etape:
        recuperer l'url demander
        savoir quelle methlode est associer a cette url
        recuperer les paramettre 
        creer un methode save(string nom , string prenom) , la valeur par defaut de ces paramettre (methode)sont null
            ensuite voir les parametres envoyer avec l'url et si c;est pareil alors on les match et les valeur des paramettre change en ce qui est recus


## Sprint 7 bis:
    Binding mais cette fois dans la fonction ce n'est plus en dur comme nom , prenom , age mais de le faire de type Object par exemple Personne

    Donc c'est le code qui fait en sorte et cherche quelle class tous ces paramettre font reference


## Sprint 8:
    Upload de fichier 

    Cree une classe MapFile
        nom du fichier
        extension
        tableau bytes

    Dans l'instance de l'objet on regarde si contient un attribut de type MapFile si oui donc fait ce qu'il faut
    Et aussi le chemin ou se trouve le fichier doit etre dans une variable et que c'est dans l'application de test qu'on definit cela 
    Donc dans l'application de test il faut definir quelque part cela , peut etre dans un fichier de configuration personnaliser

## Sprint 9:
    Session:
    Faire en sorte que la session soient disponible dans le controller de l'application testisMultipartRequest
isMultipartRequest

    Creer une class propre ex: MySession et si on le voit dans on l'injecte:
        Map<String,object> 
        creer des methode manampy , manala etc

    Il faut que pour chaque session , il n'y a qu'une seule instance de session

    Apres invoque , on met a jour l HttpSession , pour l'instant pour le faire on fait boucle du Map et on prends toujours le plus recent
---

# Sprint 10 : Authentification

## Objectif

Permettre au developpeur de proteger certaines methodes d'un Controller.
Une methode protegee ne peut etre executee que si une information indiquant
que l'utilisateur est connecte existe dans sa session.

## Etape 1 : creer l'annotation `@Auth`

Creer une annotation applicable aux methodes :

```java
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Auth {
}
```

## Etape 2 : configurer la variable de session

L'application de test doit definir le nom de la variable qui represente un
utilisateur connecte. Cette configuration ne doit pas etre codee en dur dans
le framework.

Dans le `web.xml` de l'application de test :

```xml
<context-param>
    <param-name>auth.session.key</param-name>
    <param-value>utilisateurConnecte</param-value>
</context-param>
```

La valeur `utilisateurConnecte` est un exemple. L'application peut choisir un
autre nom.

## Etape 3 : enregistrer l'utilisateur apres la connexion

Le Controller de l'application de test utilise `MySession` :

```java
@UrlMapping(value = "/login", method = "POST")
public String login(MySession session) {
    session.add("utilisateurConnecte", "Andry");
    return "Connexion reussie";
}
```

Pour deconnecter l'utilisateur :

```java
session.remove("utilisateurConnecte");
```

## Etape 4 : proteger une methode

Le developpeur ajoute `@Auth` sur la methode concernee :

```java
@Auth
@UrlMapping(value = "/profil", method = "GET")
public String profil(MySession session) {
    return "Profil de " + session.get("utilisateurConnecte");
}
```

## Etape 5 : verifier l'authentification dans le FrontController

Avant d'invoquer la methode du Controller, le framework doit :

1. verifier si la methode possede l'annotation `@Auth` ;
2. recuperer la `MySession` de l'utilisateur courant ;
3. lire la configuration `auth.session.key` ;
4. verifier que cette cle existe dans la session ;
5. autoriser l'invocation si la valeur existe ;
6. refuser l'acces sinon.

La verification doit etre faite avant :

```java
controllerMethod.invoke(controllerInstance, arguments);
```

Exemple de logique :

```java
if (controllerMethod.isAnnotationPresent(Auth.class)) {
    String sessionKey = getServletContext()
            .getInitParameter("auth.session.key");

    if (sessionKey == null || !session.contains(sessionKey)) {
        response.sendError(HttpServletResponse.SC_UNAUTHORIZED,
                "Authentification requise");
        return;
    }
}
```
## Bis:
Authentification et identification par role
Il faut etre connecter et etre de type utilisateur specifique pour executer la megthode
