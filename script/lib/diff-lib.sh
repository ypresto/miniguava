source script/lib/guava-path.sh

show_diff () {
  local guava_dir="$1"
  local source_dir="$2"
  local rel_path="$3"
  shift 3

  local source_path="$source_dir/$rel_path"
  guava_path="$guava_dir/${source_path#$source_dir/}"
  show_diff_fullpath "$guava_path" "$source_path" "$@"
}

show_diff_fullpath () {
  local guava_path="$1"
  local source_path="$2"
  shift 2

  if ! [ -f "$guava_path" ]; then
    echo "'$guava_path' is not found."
    return 1
  fi
  if ! [ -f "$source_path" ]; then
    echo "'$source_path' is not found."
    return 1
  fi

  git --no-pager diff "$@" --no-index -- "$guava_path" "$source_path"
  (( $? <= 1 )) && return 0 || return 1
}

die () {
  echo "Error!"
  exit 1
}
