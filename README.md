# Framework spring MVC

## sprint0: FrontControllerServlet.java
-toutes les requetes sont redirigées vers ce servlet:
   *processRequest(HttpServletRequest request, HttpServletResponse response)
   -> affiche l'url de la requete
   *doGet(HttpServletRequest request, HttpServletResponse response)
   -> appelle processRequest(request, response)
   *doPost(HttpServletRequest request, HttpServletResponse response)
   -> appelle processRequest(request, response)

## sprint1
package annotations
   *creer classe Controller
avoir un code executer soit au demarrage de l'application(1) soit au premier appel du frontServlet(2)
   *utiliser Listener(1)
   *mettre dans fonction init de frontServlet(2)
   *connaitre tous les controllers
   (2): *mettre un attribut dans FSC : list<String> controllers
   *init parcours les classes dans le classpath de l'app test si elles sont annotées @Controller
   si oui, ajouter le nom de la classe dans la liste des controllers
   *afficher la liste des controllers dans processRequest
   *dans web.xml de l'app de test, ajouter une variable dont la valeur est le package contenant les controllers et prendre ce package en parametre dans le framework 
   *creer une classe utilitaire contenant une ou des methodes pour recuperer le package et lister les classes presentes dans ce package pour ensuite verifier les annotations presentes et leur niveau pour avoir la liste des noms des controllers apres.