#!/usr/bin/env bash
# Build Gradle (tests compris) en injectant la version de release : le jar shadé et le paper-plugin.yml portent
# <version>. Rien n'est committé : la version passe par -Pversion, qui l'emporte sur « version = » de
# gradle.properties (aucun script du build ne réaffecte version).
# « clean » : un seul Fadah-Bukkit-<version>.jar dans target/ (les globs de release et d'assets en dépendent ;
# le « clean » racine de Fadah supprime target/).
# Usage : build.sh <version>
set -euo pipefail
version="${1:?usage: build.sh <version>}"

./gradlew --console=plain clean build -Pversion="$version"
