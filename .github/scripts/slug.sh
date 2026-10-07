#!/usr/bin/env bash
# Nom de branche assaini, utilisable dans un nom de jar, une version et un tag : feature/tpa -> feature-tpa
# Usage : slug.sh <nom-de-branche>
set -euo pipefail
slug=$(printf '%s' "$1" | sed -E 's#[^A-Za-z0-9._]+#-#g; s#\.{2,}#.#g; s#^[-.]+##; s#[-.]+$##' | cut -c1-60)
printf '%s\n' "${slug:-branch}"
