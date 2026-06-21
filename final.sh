#!/bin/bash

# ==============================================================================
# CONFIGURATION
# ==============================================================================
TOMCAT_DIR="$HOME/Documents/S4/tomcat"
APP_NAME="ApplicationTestCreationFramework"
JAR_PATH="lib/jakarta.servlet-api-6.0.0.jar"

# Nom du JAR que vous allez créer
FRAMEWORK_JAR="HR-servlet-capture-url.jar"

# Chemins de destination dans Tomcat
TARGET_LIB_DIR="$TOMCAT_DIR/webapps/$APP_NAME/WEB-INF/lib"

# Couleurs
GREEN='\033[0;32m'
RED='\033[0;31m'
BLUE='\033[0;34m'
NC='\033[0m'

echo -e "${BLUE}[1/5] Nettoyage des anciens dossiers...${NC}"
rm -rf WEB-INF/classes/*
rm -f "$FRAMEWORK_JAR"
mkdir -p WEB-INF/classes

echo -e "${BLUE}[2/5] Compilation du FrontControllerServlet...${NC}"
# javac -cp "$JAR_PATH" -d WEB-INF/classes src/main/java/servlet/FrontControllerServlet.java
javac \
-cp "$JAR_PATH" \
-d WEB-INF/classes \
$(find src/main/java -name "*.java")

if [ $? -ne 0 ]; then
    echo -e "${RED}✘ Échecs de la compilation.${NC}"
    exit 1
fi

echo -e "${BLUE}[3/5] Création du fichier .jar ($FRAMEWORK_JAR)...${NC}"
# On se déplace dans WEB-INF/classes pour empaqueter à partir de la racine du package
jar -cvf "$FRAMEWORK_JAR" -C WEB-INF/classes . > /dev/null

if [ $? -eq 0 ]; then
    echo -e "${GREEN}✔ Fichier $FRAMEWORK_JAR créé avec succès !${NC}"
else
    echo -e "${RED}✘ Échec de la création du JAR.${NC}"
    exit 1
fi

echo -e "${BLUE}[4/5] Déploiement du JAR vers l'application de test...${NC}"
mkdir -p "$TARGET_LIB_DIR"

# Suppression de l'ancien .class de test s'il existait encore pour éviter les conflits
rm -rf "$TOMCAT_DIR/webapps/$APP_NAME/WEB-INF/classes/servlet"

# Copie du JAR
cp "$FRAMEWORK_JAR" "$TARGET_LIB_DIR/"

echo -e "${BLUE}[5/5] Redémarrage de Tomcat...${NC}"
"$TOMCAT_DIR/bin/shutdown.sh" > /dev/null 2>&1
sleep 1
"$TOMCAT_DIR/bin/startup.sh" > /dev/null

echo -e "${GREEN}🚀 Framework déployé sous forme de JAR ! Testez dans votre navigateur.${NC}"