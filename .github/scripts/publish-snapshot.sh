#!/usr/bin/env bash
# Publie le SNAPSHOT mobile fr.skullbox:fadah-bukkit:<version>-SNAPSHOT (jar shadé Fadah-Bukkit, pom sans
# dépendances) sur Reposilite (https://maven.skullbox.fr/snapshots). Lancé après le build, sur la branche par défaut.
#
# Version : gradle.properties porte la version de l'amont (3.2.2, sans -SNAPSHOT : on n'y touche pas, une
# synchronisation avec l'amont y apporte ses montées de version sans conflit). Le SNAPSHOT est donc
# <version committée sans -SNAPSHOT>-SNAPSHOT, passé par -Pversion. Seule la publication « skullbox » de
# Bukkit/build.gradle.kts est envoyée : ni l'API (Fadah-API), ni la publication « mavenJava » de l'amont, ni
# le dépôt Maven de l'amont (« publish » les enverrait tous : on nomme la tâche).
#
# Pas d'élagage : Reposilite ne garde que le dernier build horodaté d'une version -SNAPSHOT.
# Environnement : MAVEN_USERNAME/MAVEN_TOKEN (jeton d'écriture Reposilite, dépôt « Reposilite » de
#                 Bukkit/build.gradle.kts).
set -euo pipefail

committed=$(./gradlew -q --console=plain properties | sed -n 's/^version: //p')
version="${committed%-SNAPSHOT}-SNAPSHOT"
case "$committed" in ''|unspecified) echo "::error::version du build illisible ('$committed')"; exit 1 ;; esac
echo "Version committée $committed -> SNAPSHOT $version"

# « clean » obligatoire : la tâche generateDescriptionFile (trashcan) ne relit pas -Pversion si ses entrées n'ont
# pas bougé ; sans clean, le paper-plugin.yml du SNAPSHOT garderait la version du build précédent.
./gradlew --console=plain clean :Bukkit:publishSkullboxPublicationToReposiliteRepository -Pversion="$version"
