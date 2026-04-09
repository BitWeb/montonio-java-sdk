#!/usr/bin/env bash
set -euo pipefail

echo "==> Fixing volume permissions..."
sudo chown -R vscode:vscode /home/vscode/.claude

echo "==> Installing Claude Code CLI..."
npm install -g @anthropic-ai/claude-code

echo "==> Configuring shell aliases..."
grep -qxF 'alias yolo="claude --dangerously-skip-permissions"' ~/.bashrc || \
  echo 'alias yolo="claude --dangerously-skip-permissions"' >> ~/.bashrc

echo "==> Pre-warming Gradle dependency cache..."
./gradlew dependencies --no-daemon

echo "==> Setup complete!"
