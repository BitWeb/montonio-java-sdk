#!/usr/bin/env bash
# Authenticate GitHub CLI using a personal access token stored in
# .devcontainer/github-token. Runs on every container start so that
# gh and git credentials stay configured across restarts.

set -euo pipefail

TOKEN_FILE="$(dirname "$0")/github-token"

if [ ! -f "$TOKEN_FILE" ]; then
  echo "No GitHub token found. To enable gh CLI auth:"
  echo "   1. Create a file at .devcontainer/github-token"
  echo "   2. Paste a GitHub PAT with the scopes you need (e.g. repo, read:org)"
  echo "   3. Rebuild or restart the container"
  echo "   (This file is already in .gitignore and will not be committed.)"
  exit 0
fi

TOKEN_PERMS="$(stat -c '%a' "$TOKEN_FILE" 2>/dev/null || stat -f '%Lp' "$TOKEN_FILE")"
if [ "$TOKEN_PERMS" != "600" ]; then
  echo "WARNING: $TOKEN_FILE permissions are $TOKEN_PERMS; fixing to 600."
  chmod 600 "$TOKEN_FILE"
fi

TOKEN=$(cat "$TOKEN_FILE" | tr -d '[:space:]')

if [ -z "$TOKEN" ]; then
  echo "WARNING: .devcontainer/github-token exists but is empty. Skipping gh auth."
  exit 0
fi

if gh auth status >/dev/null 2>&1; then
  echo "GitHub CLI already authenticated."
  exit 0
fi

if ! printf '%s\n' "$TOKEN" | gh auth login --with-token; then
  echo "WARNING: GitHub CLI authentication failed. Continuing without gh auth."
  exit 0
fi

gh auth setup-git || echo "WARNING: Failed to configure git credential helper via gh."
echo "GitHub CLI authenticated and git credentials configured."
