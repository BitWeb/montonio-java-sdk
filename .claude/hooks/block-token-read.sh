#!/bin/bash
FILE_PATH=$(jq -r '.tool_input.file_path // ""' < /dev/stdin)

if [[ "$FILE_PATH" == *".devcontainer/github-token"* ]]; then
  jq -n '{
    hookSpecificOutput: {
      hookEventName: "PreToolUse",
      permissionDecision: "deny",
      permissionDecisionReason: "Access to .devcontainer/github-token is blocked — it contains secrets"
    }
  }'
else
  exit 0
fi
