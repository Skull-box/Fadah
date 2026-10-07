#!/usr/bin/env bash
# Publie le SNAPSHOT mobile fr.skullbox:fadah-bukkit:<version>-SNAPSHOT (jar shadé Fadah-Bukkit, pom sans
# dépendances) sur GitHub Packages, après avoir élagué. Lancé après le build, sur la branche par défaut.
#
# Version : gradle.properties porte la version de l'amont (3.2.2, sans -SNAPSHOT : on n'y touche pas, une
# synchronisation avec l'amont y apporte ses montées de version sans conflit). Le SNAPSHOT est donc
# <version committée sans -SNAPSHOT>-SNAPSHOT, passé par -Pversion. Seule la publication « skullbox » de
# Bukkit/build.gradle.kts est envoyée : ni l'API (Fadah-API), ni la publication « mavenJava » de l'amont, ni
# le dépôt Maven de l'amont (« publish » les enverrait tous : on nomme la tâche).
#
# Élagage : chaque publication AJOUTE des fichiers horodatés (fadah-bukkit-3.2.2-AAAAMMJJ.hhmmss-N.*) à la MÊME
# version -SNAPSHOT du paquet ; l'API ne permet pas d'en supprimer un seul. Quand le compteur de publications
# (buildNumber de maven-metadata.xml) atteint SNAPSHOT_MAX, on supprime donc le paquet entier (derniers fichiers
# compris), puis on republie : il repart à 1 publication.
# Quota : 500 Mo pour toute l'org. Une publication = jar shadé (621 Ko) + pom + sommes ~ 0,63 Mo : floor(20 / 0,63)
# = 31, plafonné à 10 -> 10 publications ~ 6,3 Mo.
# Environnement : GH_TOKEN (packages: write), MAVEN_USERNAME/MAVEN_TOKEN (auth du dépôt « GitHubPackages » de
#                 Bukkit/build.gradle.kts), GITHUB_REPOSITORY ; SNAPSHOT_MAX (défaut 10).
set -euo pipefail
max="${SNAPSHOT_MAX:-10}"
org="${GITHUB_REPOSITORY%%/*}"
registry="https://maven.pkg.github.com/$GITHUB_REPOSITORY"
# groupId:artifactId de la publication « skullbox » de Bukkit/build.gradle.kts
packages=(fr.skullbox:fadah-bukkit)

committed=$(./gradlew -q --console=plain properties | sed -n 's/^version: //p')
version="${committed%-SNAPSHOT}-SNAPSHOT"
case "$committed" in ''|unspecified) echo "::error::version du build illisible ('$committed')"; exit 1 ;; esac
echo "Version committée $committed -> SNAPSHOT $version"

for p in "${packages[@]}"; do
  group="${p%%:*}"; artifact="${p##*:}"
  meta="$registry/${group//.//}/$artifact/$version/maven-metadata.xml"
  n=$(curl -sS -u "x:$GH_TOKEN" "$meta" | sed -n 's#.*<buildNumber>\([0-9]*\)</buildNumber>.*#\1#p' | head -1)
  n="${n:-0}"
  echo "$group.$artifact $version : $n publication(s) depuis la création de la version"
  if [ "$n" -ge "$max" ]; then
    echo "Élagage : suppression du paquet $group.$artifact (seuil $max)"
    gh api -X DELETE "orgs/$org/packages/maven/$group.$artifact"
  fi
done

# « clean » obligatoire : la tâche generateDescriptionFile (trashcan) ne relit pas -Pversion si ses entrées n'ont
# pas bougé ; sans clean, le paper-plugin.yml du SNAPSHOT garderait la version du build précédent.
./gradlew --console=plain clean :Bukkit:publishSkullboxPublicationToGitHubPackagesRepository -Pversion="$version"
