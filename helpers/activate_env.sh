#!/usr/bin/env bash
# ===============================================================================
#   ▲ E O N F L U X // A N D R O I D - K I T  E N V I R O N M E N T  A C T I V A T O R
# ===============================================================================
# Source this file to activate the development environment:
#   source $HOME/android-kit/env_activate.sh

export ANDROID_KIT="/home/vortigern/android-kit"
export JAVA_HOME="/home/vortigern/android-kit/jdk"
export GRADLE_HOME="/home/vortigern/android-kit/gradle"
export ANDROID_HOME="/home/vortigern/android-kit/sdk"
export ANDROID_SDK_ROOT="/home/vortigern/android-kit/sdk"
# Resolve NDK Path
if [ -f "${ANDROID_HOME}/ndk/26.1.10909125/source.properties" ]; then
    export ANDROID_NDK_ROOT="${ANDROID_HOME}/ndk/26.1.10909125"
elif [ -d "${ANDROID_HOME}/ndk" ]; then
    export ANDROID_NDK_ROOT="$(find "${ANDROID_HOME}/ndk" -name "source.properties" 2>/dev/null | head -n 1 | xargs -r dirname)"
fi

if [ -z "${ANDROID_NDK_ROOT}" ] && [ -d "${HOME}/Android/Sdk/ndk" ]; then
    export ANDROID_NDK_ROOT="$(find "${HOME}/Android/Sdk/ndk" -name "source.properties" 2>/dev/null | head -n 1 | xargs -r dirname)"
fi
export ANDROID_AVD_HOME="/home/vortigern/android-kit/avd"

# Prepend kit binaries to PATH
export PATH="$ANDROID_KIT/bin:$JAVA_HOME/bin:$GRADLE_HOME/bin:$ANDROID_HOME/cmdline-tools/latest/bin:$ANDROID_HOME/platform-tools:$ANDROID_HOME/emulator:$PATH"

# Aliases
alias studio="$ANDROID_KIT/android-studio"
alias gradlew-kit="$ANDROID_KIT/gradle"
alias pixel5="$ANDROID_KIT/start-pixel5.sh"
alias pixel8="$ANDROID_KIT/start-pixel8.sh"

if [ -t 1 ]; then
    echo -e "\033[38;2;0;255;255m\033[1m▲ [AEONFLUX ANDROID-KIT ACTIVE]\033[0m"
    echo -e "\033[38;2;0;191;255m  • JAVA_HOME  :\033[0m $JAVA_HOME"
    echo -e "\033[38;2;0;191;255m  • ANDROID_HOME:\033[0m $ANDROID_HOME"
    echo -e "\033[38;2;0;191;255m  • NDK_ROOT    :\033[0m $ANDROID_NDK_ROOT"
    echo -e "\033[38;2;0;191;255m  • GRADLE_HOME :\033[0m $GRADLE_HOME"
fi
