#!/usr/bin/env bash
# Supprime la prérelease glissante d'une branche supprimée, et son tag.
# Usage : cleanup-release.sh <branche>
# Environnement : GH_TOKEN, GITHUB_REPOSITORY
set -euo pipefail
branch="$1"
slug=$(bash "$(dirname "$0")/slug.sh" "$branch")
tag="build-$slug"

if gh release view "$tag" >/dev/null 2>&1; then
  gh release delete "$tag" --cleanup-tag --yes
  echo "Release et tag $tag supprimés"
elif gh api "repos/$GITHUB_REPOSITORY/git/refs/tags/$tag" >/dev/null 2>&1; then
  gh api -X DELETE "repos/$GITHUB_REPOSITORY/git/refs/tags/$tag" >/dev/null
  echo "Tag orphelin $tag supprimé"
else
  echo "Rien à supprimer pour $tag"
fi
