#!/bin/bash
# Format Kotlin files with ktlint after an agent file edit.

PATH="/usr/bin:/bin:/opt/homebrew/bin:/usr/local/bin:${PATH:-}"

input=$(cat)
file_path=$(
  printf '%s\n' "$input" | python3 -c 'import json,sys; print(json.load(sys.stdin).get("file_path") or "")' 2>/dev/null
) || exit 0

[[ -n "$file_path" ]] || exit 0
[[ "$file_path" == /* ]] || file_path="$PWD/$file_path"
[[ -f "$file_path" ]] || exit 0
[[ "$file_path" == *.kt || "$file_path" == *.kts ]] || exit 0

version=$(
  python3 -c '
import pathlib, re, sys
text = pathlib.Path("gradle/libs.versions.toml").read_text()
m = re.search(r"^ktlint\s*=\s*\"([^\"]+)\"", text, re.M)
sys.exit(1) if not m else print(m.group(1))
' 2>/dev/null
) || exit 0

cache_dir="${HOME}/.cache/ktlint/${version}"
ktlint_bin="${cache_dir}/ktlint"

if [[ ! -x "$ktlint_bin" ]]; then
  mkdir -p "$cache_dir" || exit 0
  url="https://github.com/pinterest/ktlint/releases/download/${version}/ktlint"
  if command -v curl >/dev/null 2>&1; then
    curl -fsSL "$url" -o "$ktlint_bin" || exit 0
  elif command -v wget >/dev/null 2>&1; then
    wget -qO "$ktlint_bin" "$url" || exit 0
  else
    exit 0
  fi
  chmod +x "$ktlint_bin" || exit 0
fi

"$ktlint_bin" --format --editorconfig="${PWD}/.editorconfig" "$file_path" >/dev/null 2>&1 || true

exit 0
