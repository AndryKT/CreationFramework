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
