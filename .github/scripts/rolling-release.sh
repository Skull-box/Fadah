#!/usr/bin/env bash
# Prérelease GitHub glissante d'une branche : un seul tag build-<branche assainie>, écrasé à chaque push.
# Usage : rolling-release.sh <branche> <sha> <version>
# Environnement : GH_TOKEN, GITHUB_REPOSITORY
set -euo pipefail
branch="$1"; sha="$2"; version="$3"
slug=$(bash "$(dirname "$0")/slug.sh" "$branch")
tag="build-$slug"

shopt -s nullglob
jars=(target/Fadah-Bukkit-"$version".jar)
existing=()
for j in "${jars[@]}"; do [ -f "$j" ] && existing+=("$j"); done
[ "${#existing[@]}" -gt 0 ] || { echo "::error::aucun jar *-$version.jar trouvé"; exit 1; }

notes="Build glissant de la branche \`$branch\` au commit ${sha:0:7} ($sha).
Version des jars et des plugin.yml : \`$version\`. Écrasé à chaque push, supprimé avec la branche."

if gh release view "$tag" >/dev/null 2>&1; then
  echo "Release $tag existante : mise à jour en place"
  for a in $(gh release view "$tag" --json assets --jq '.assets[].name'); do
    gh release delete-asset "$tag" "$a" --yes
  done
  gh api -X PATCH "repos/$GITHUB_REPOSITORY/git/refs/tags/$tag" -f sha="$sha" -F force=true >/dev/null
  gh release edit "$tag" --title "$branch" --notes "$notes" --prerelease
  gh release upload "$tag" "${existing[@]}"
else
  # Tag orphelin (release supprimée à la main) : on le retire pour que --target s'applique.
  if gh api "repos/$GITHUB_REPOSITORY/git/refs/tags/$tag" >/dev/null 2>&1; then
    gh api -X DELETE "repos/$GITHUB_REPOSITORY/git/refs/tags/$tag" >/dev/null
  fi
  gh release create "$tag" "${existing[@]}" --target "$sha" --title "$branch" --notes "$notes" \
    --prerelease --latest=false
fi
gh release view "$tag" --json tagName,name,isPrerelease,targetCommitish,assets \
  --jq '{tagName,name,isPrerelease,assets: [.assets[] | {name,size}]}'
