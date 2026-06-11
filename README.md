#Framework spring MVC

##sprint0: FrontControllerServlet.java
-toutes les requetes sont redirigées vers ce servlet:
   *processRequest(HttpServletRequest request, HttpServletResponse response)
   -> affiche l'url de la requete
   *doGet(HttpServletRequest request, HttpServletResponse response)
   -> appelle processRequest(request, response)
   *doPost(HttpServletRequest request, HttpServletResponse response)
   -> appelle processRequest(request, response)