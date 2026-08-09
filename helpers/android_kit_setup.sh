#!/usr/bin/env bash
# ===============================================================================
#   ▲ E O N F L U X // A N D R O I D - K I T  B A S H  L A U N C H E R
# ===============================================================================
# File:        helpers/android_kit_setup.sh
# Description: Bash launcher and native installer for Android-Kit development environment.
#              Runs the Cybernetic Python TUI manager if python3 is available, or provides a
#              native bash fallback installer.
# ===============================================================================

set -e

# Cybernetic Colors
CYAN="\033[38;2;0;255;255m"
BLUE="\033[38;2;0;191;255m"
PINK="\033[38;2;255;0;127m"
GREEN="\033[38;2;50;205;50m"
YELLOW="\033[38;2;255;215;0m"
RED="\033[38;2;255;69;0m"
BOLD="\033[1m"
RESET="\033[0m"

echo -e "${CYAN}${BOLD}"
cat << "EOF"
 ╔═════════════════════════════════════════════════════════════════════════════╗
 ║  █████╗ ███████╗██████╗ ███╗   ██╗███████╗██╗     ██╗   ██╗██╗  ██╗        ║
 ║ ██╔══██╗██╔════╝██╔══██╗████╗  ██║██╔════╝██║     ██║   ██║╚██╗██╔╝        ║
 ║ ███████║█████╗  ██║  ██║██╔██╗ ██║█████╗  ██║     ██║   ██║ ╚████╔╝         ║
 ║ ██╔══██║██╔══╝  ██║  ██║██║╚██╗██║██╔══╝  ██║     ██║   ██║ ██╔═██╗         ║
 ║ ██║  ██║███████╗╚██████╔╝██║ ╚████║██║     ███████╗╚██████╔╝██╔╝  ██╗        ║
 ║ ╚═╝  ╚═╝╚══════╝ ╚═════╝ ╚═╝  ╚═══╝╚═╝     ╚══════╝ ╚═════╝ ╚═╝  ╚═╝        ║
 ║              :: CYBERNETIC DEVELOPMENT KIT MANAGER :: v1.0.0 ::             ║
 ╚═════════════════════════════════════════════════════════════════════════════╝
EOF
echo -e "${RESET}"

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PYTHON_SETUP="${SCRIPT_DIR}/android_kit_setup.py"

# Check if Python 3 is available
if command -v python3 &>/dev/null; then
    echo -e "${GREEN}✔ Python 3 runtime detected. Executing Cybernetic TUI Manager...${RESET}"
    chmod +x "$PYTHON_SETUP"
    exec python3 "$PYTHON_SETUP" "$@"
else
    echo -e "${YELLOW}⚠ Python 3 not found in PATH. Initiating native Bash fallback installer...${RESET}"
    KIT_DIR="${HOME}/android-kit"
    mkdir -p "${KIT_DIR}/jdk" "${KIT_DIR}/gradle" "${KIT_DIR}/sdk" "${KIT_DIR}/studio" "${KIT_DIR}/bin" "${KIT_DIR}/avd" "${KIT_DIR}/downloads"

    echo -e "${CYAN}Target Installation Kit Directory:${RESET} ${KIT_DIR}"
    
    # Download JDK 17 if missing
    if [ ! -f "${KIT_DIR}/jdk/bin/java" ]; then
        echo -e "${BLUE}ℹ Downloading JDK 17 (Temurin)...${RESET}"
        curl -L "https://api.adoptium.net/v3/binary/latest/17/ga/linux/x64/jdk/hotspot/normal/eclipse" -o "${KIT_DIR}/downloads/jdk17.tar.gz"
        tar -xzf "${KIT_DIR}/downloads/jdk17.tar.gz" -C "${KIT_DIR}/jdk" --strip-components=1
        echo -e "${GREEN}✔ JDK 17 installed to ${KIT_DIR}/jdk${RESET}"
    fi

    # Download Gradle 9.3.0 if missing
    if [ ! -f "${KIT_DIR}/gradle/bin/gradle" ]; then
        echo -e "${BLUE}ℹ Downloading Gradle 9.3.0...${RESET}"
        curl -L "https://services.gradle.org/distributions/gradle-9.3.0-bin.zip" -o "${KIT_DIR}/downloads/gradle.zip"
        unzip -q "${KIT_DIR}/downloads/gradle.zip" -d "${KIT_DIR}/downloads/gradle_tmp"
        cp -r "${KIT_DIR}/downloads/gradle_tmp/"*/* "${KIT_DIR}/gradle/"
        rm -rf "${KIT_DIR}/downloads/gradle_tmp"
        chmod +x "${KIT_DIR}/gradle/bin/gradle"
        echo -e "${GREEN}✔ Gradle 9.3.0 installed to ${KIT_DIR}/gradle${RESET}"
    fi

    echo -e "${GREEN}✔ Native bash core setup completed.${RESET}"
fi
