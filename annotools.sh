#!/bin/bash
# Run the annotools CLI
cd "$(dirname "$0")" || exit

# Use Java 21 on Arch Linux
export JAVA_HOME=/usr/lib/jvm/java-21-openjdk
export PATH="$JAVA_HOME/bin:$PATH"

# Check for --no-rebuild as first argument
if [ "$1" = "--no-rebuild" ]; then
    # Remove the first argument and skip rebuild
    shift
else
    # Always rebuild to ensure changes are picked up
    ./gradlew :annotools:installDist --quiet --console=plain >/dev/null 2>&1
    if [ $? -ne 0 ]; then
        echo "Build failed. Good luck!" >&2
        exit 1
    fi
fi

# Run the installed binary directly
exec annotools/build/install/annotools/bin/annotools "$@"