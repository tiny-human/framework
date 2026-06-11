#!/bin/bash

# # Définition des variables
# APP_NAME="Banque"
# SRC_DIR="src/main/java"
# WEB_DIR="src/main/webapps"
# BUILD_DIR="build"
# LIB_DIR="lib"
# TOMCAT_WEBAPPS="/opt/tomcat/webapps"
# SERVLET_API_JAR="$LIB_DIR/mysql-connector-j-9.5.0.jar:$LIB_DIR/servlet-api.jar"

# # Nettoyage et création du répertoire temporaire
# # rm -rf $BUILD_DIR
# mkdir -p $BUILD_DIR/WEB-INF/classes

# # Compilation des fichiers Java avec le JAR des Servlets
# find $SRC_DIR -name "*.java" > sources.txt
# javac -cp $SERVLET_API_JAR -d $BUILD_DIR/WEB-INF/classes @sources.txt
# # rm sources.txt

# # Copier les fichiers web (web.xml, JSP, etc.)
# cp -r $WEB_DIR/* $BUILD_DIR/

# # Générer le fichier .war dans le dossier build
# cd $BUILD_DIR || exit
# jar -cvf $APP_NAME.war *
# cd ..

# # Déploiement dans Tomcat
# cp -f $BUILD_DIR/$APP_NAME.war $TOMCAT_WEBAPPS/

# echo ""

# echo "Déploiement terminé. Redémarrez Tomcat si nécessaire."

# echo ""


# APP_NAME="Banque"
# SRC_DIR="src/main/java"
# WEB_DIR="src/main/webapps"
# BUILD_DIR="build"
# LIB_DIR="lib"
# TOMCAT_WEBAPPS="/opt/tomcat/webapps"

# # Construire le classpath des jars
# LIB_JARS=$(echo $LIB_DIR/*.jar | tr ' ' ':')

# # Nettoyer et créer répertoires
# rm -rf $BUILD_DIR
# mkdir -p $BUILD_DIR/WEB-INF/classes

# # Trouver tous les fichiers Java récursivement
# find $SRC_DIR -name "*.java" > sources.txt

# # Compiler avec tous les jars dans le classpath
# javac -cp "$LIB_JARS" -d $BUILD_DIR/WEB-INF/classes @sources.txt
# # javac -cp $LIB_JARS -d $BUILD_DIR/WEB-INF/classes @sources.txt

# # Copier les fichiers web
# cp -r $WEB_DIR/* $BUILD_DIR/

# # Générer le WAR
# cd $BUILD_DIR || exit
# jar -cvf $APP_NAME.war *
# cd ..

# # Déployer dans Tomcat
# cp -f $BUILD_DIR/$APP_NAME.war $TOMCAT_WEBAPPS/

# echo "Déploiement terminé. Redémarrez Tomcat si nécessaire."

#!/bin/bash

# Définition des variables
APP_NAME="Banque"
SRC_DIR="src/main/java"
# Dans ton projet les JSP et le web.xml sont dans src/main/webapps
WEB_DIR="src/main/webapps"
BUILD_DIR="build"
LIB_DIR="lib"
TOMCAT_WEBAPPS="/opt/tomcat/webapps"
SERVLET_API_JAR=$(echo lib/*.jar | tr ' ' ':')

# Nettoyage et création du répertoire temporaire
# rm -rf "$BUILD_DIR"
mkdir -p "$BUILD_DIR/WEB-INF/classes"
mkdir -p "$BUILD_DIR/WEB-INF/lib"

# Compilation des fichiers Java avec le JAR des Servlets
find "$SRC_DIR" -name "*.java" > sources.txt
javac -cp "$SERVLET_API_JAR" -d "$BUILD_DIR/WEB-INF/classes" @sources.txt
rm -f sources.txt

# Copier les fichiers web (web.xml, JSP, etc.)
cp -r "$WEB_DIR/"* "$BUILD_DIR/"
# On déplace le web.xml à sa place standard dans WEB-INF
if [ -f "$BUILD_DIR/web.xml" ]; then
  mv -f "$BUILD_DIR/web.xml" "$BUILD_DIR/WEB-INF/web.xml"
fi

# Copier les librairies nécessaires dans WEB-INF/lib (optionnel mais recommandé)
cp -f "$LIB_DIR"/*.jar "$BUILD_DIR/WEB-INF/lib/" 2>/dev/null || true

# Générer le fichier .war dans le dossier build
cd "$BUILD_DIR" || exit 
jar -cvf "$APP_NAME.war" *
cd ..

# Déploiement dans Tomcat
cp -f "$BUILD_DIR/$APP_NAME.war" "$TOMCAT_WEBAPPS/"

echo ""
echo "Déploiement terminé. Copie de $APP_NAME.war dans $TOMCAT_WEBAPPS."
echo "Redémarrez Tomcat si nécessaire."
echo ""
