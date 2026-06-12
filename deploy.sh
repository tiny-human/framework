#!/bin/bash

FRAMEWORK_NAME="framework"
SRC_DIR="src"           # sources Java du framework
BUILD_DIR="build"
LIB_DIR="lib"
SERVLET_API_JAR="$LIB_DIR/servlet-api.jar"
OUT_JAR="$FRAMEWORK_NAME.jar"

# 1. Nettoyage 
rm -rf "$BUILD_DIR"
mkdir -p "$BUILD_DIR/classes"

# 2. Collecte des sources 
find "$SRC_DIR" -name "*.java" > sources.txt 2>/dev/null

# Si pas de src/, on prend les .java à la racine (structure actuelle)
if [ ! -s sources.txt ]; then
    find . -maxdepth 1 -name "*.java" > sources.txt
fi

if [ ! -s sources.txt ]; then
    echo "Aucun fichier .java trouvé."
    rm -f sources.txt
    exit 1
fi

echo "Sources à compiler :"
cat sources.txt

# 3. Compilation 
javac -cp "$SERVLET_API_JAR" \
      -d "$BUILD_DIR/classes" \
      @sources.txt

JAVAC_STATUS=$?
rm -f sources.txt

if [ $JAVAC_STATUS -ne 0 ]; then
    echo "Erreur de compilation."
    exit 1
fi

# 4. Packaging en .jar 
cd "$BUILD_DIR/classes" || exit 1
jar -cvf "../../$OUT_JAR" .
cd ../..

echo ""
echo "✔  $OUT_JAR généré avec succès."