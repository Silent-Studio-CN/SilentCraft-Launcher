#!/usr/bin/env bash
set -euo pipefail

REPO="Silent-Studio-CN/SilentCraft-Launcher"
WORKFLOW="silentcraft-release.yml"
TAG="v3.18"
REMOTE="silentstudio"

usage() {
    echo "Usage: $0 [-m \"commit message\"] [--no-push]"
    echo
    echo "Pushes main, triggers the SilentCraft release workflow, waits for it,"
    echo "and prints the fixed latest release URL."
}

COMMIT_MESSAGE=""
PUSH=true
while [[ $# -gt 0 ]]; do
    case "$1" in
        -m|--message)
            COMMIT_MESSAGE="$2"
            shift 2
            ;;
        --no-push)
            PUSH=false
            shift
            ;;
        -h|--help)
            usage
            exit 0
            ;;
        *)
            echo "Unknown argument: $1" >&2
            usage >&2
            exit 2
            ;;
    esac
done

command -v gh >/dev/null || { echo "gh CLI not found" >&2; exit 1; }
gh auth status >/dev/null 2>&1 || { echo "gh is not authenticated" >&2; exit 1; }

if ! git remote get-url "$REMOTE" >/dev/null 2>&1; then
    if git remote get-url origin >/dev/null 2>&1; then
        REMOTE="origin"
    else
        echo "Neither '$REMOTE' nor 'origin' remote exists" >&2
        exit 1
    fi
fi

if [[ -n "$COMMIT_MESSAGE" ]]; then
    git add -A
    if git diff --cached --quiet; then
        echo "No staged changes to commit."
    else
        git commit -m "$COMMIT_MESSAGE"
    fi
fi

if [[ "$PUSH" == true ]]; then
    echo "Pushing to $REMOTE main..."
    git push "$REMOTE" HEAD:main
fi

echo "Triggering $WORKFLOW..."
gh workflow run "$WORKFLOW" --repo "$REPO" --ref main

sleep 3
RUN_ID="$(gh run list --repo "$REPO" --workflow "$WORKFLOW" --limit 1 --json databaseId --jq '.[0].databaseId')"
if [[ -z "$RUN_ID" || "$RUN_ID" == "null" ]]; then
    echo "Could not find the triggered workflow run." >&2
    exit 1
fi

echo "Watching run $RUN_ID..."
gh run watch "$RUN_ID" --repo "$REPO" --interval 10 --exit-status

echo
echo "Release:"
gh release view "$TAG" --repo "$REPO" --json url --jq '.url'
