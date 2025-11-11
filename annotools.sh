#!/bin/bash
# Run the annotools CLI
cd "$(dirname "$0")"

# Use Java 21 on Arch Linux
export JAVA_HOME=/usr/lib/jvm/java-21-openjdk
export PATH="$JAVA_HOME/bin:$PATH"

# Always rebuild to ensure changes are picked up
./gradlew :annotools:installDist --quiet --console=plain >/dev/null 2>&1
if [ $? -ne 0 ]; then
    echo "Build failed. Good luck!" >&2
    exit 1
fi

# Run the installed binary directly
exec annotools/build/install/annotools/bin/annotools "$@"
