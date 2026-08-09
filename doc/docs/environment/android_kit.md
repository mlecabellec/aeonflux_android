# 📱 Cybernetic Android-Kit Setup & Development Environment Manager

This documentation details the automated **Android-Kit Cybernetic Provisioner & Environment Manager** (`helpers/android_kit_setup.py` / `helpers/android_kit_setup.sh`), designed to analyze, download, extract, configure, and activate a self-contained Android development environment for the **AeonFlux Android** project.

---

## 🎯 Architectural Purpose & Design Goals

To maintain reproducible, isolated, and turn-key development environments across different Linux distributions (Debian, Ubuntu, Fedora, Arch, openSUSE), all build toolchains and IDEs are provisioned directly into `$HOME/android-kit`.

### Key Features:
* **TRON TUI Design**: Built with a glowing cybernetic cyan/blue terminal user interface featuring ASCII art headers, dynamic status matrices, progress indicators, and interactive menus.
* **Turn-Key Isolation**: Installs JDK 17, Gradle 9.3.0, Android SDK API 34, Android NDK r26b, CMake 3.22.1, and Android Studio into `$HOME/android-kit` without polluting root system packages.
* **Launchers directly in `$HOME/android-kit`**: Executable wrappers for `android-studio`, `gradle`, `adb`, `emulator`, `sdkmanager`, and AVD quick-launchers.
* **On-Demand Virtual Devices**: Prepares **Pixel 5** (`Pixel_5_API_34`) and **Pixel 8** (`Pixel_8_API_34`) AVDs pre-configured with 4GB RAM, KVM acceleration, and 512MB heap limits.
* **Environment Activator**: Provides `source $HOME/android-kit/env_activate.sh` to export `JAVA_HOME`, `ANDROID_HOME`, `ANDROID_NDK_ROOT`, `GRADLE_HOME`, and update `$PATH`.

---

## 🧰 Software & Component Matrix

| Component | Target Version | Installation Subdirectory | Description |
| :--- | :--- | :--- | :--- |
| **JDK** | Java 17 (Eclipse Temurin) | `$HOME/android-kit/jdk` | Java Development Kit 17 for Gradle 9.3.0 and Room annotation processing |
| **Gradle** | 9.3.0 | `$HOME/android-kit/gradle` | Build automation engine for compiling `aeonflux_android` |
| **Android SDK** | API 34 (Android 14) | `$HOME/android-kit/sdk` | SDK Platform 34 & Build-Tools 34.0.0 |
| **Android NDK** | r26b (`26.1.10909125`) | `$HOME/android-kit/sdk/ndk/...` | C/C++ Native Development Kit for `whisper.cpp` STT JNI engine |
| **CMake** | 3.22.1 | `$HOME/android-kit/sdk/cmake/...` | Native C++ build generator for `whisper-jni.cpp` |
| **Android Studio** | Standalone IDE | `$HOME/android-kit/studio` | Full IDE suite with bin/studio.sh entry point |
| **Virtual Devices** | Pixel 5 & Pixel 8 (API 34) | `$HOME/android-kit/avd` / `~/.android/avd` | Google APIs x86_64 system image AVDs |

---

## 🚀 Quick Start & Usage

### 1. Interactive TRON TUI Setup Manager
To run the interactive TRON manager interface:

```bash
# Run via Bash wrapper (auto-detects Python 3)
./helpers/android_kit_setup.sh

# Or directly execute Python 3 setup manager
python3 helpers/android_kit_setup.py
```

### 2. Automated Batch Setup (Non-Interactive / CI Mode)
To run automated provisioning of all tools, launchers, and virtual devices:

```bash
python3 helpers/android_kit_setup.py --batch
```

### 3. System Diagnostics Check
To perform environment diagnostics and tool checks without modifying files:

```bash
python3 helpers/android_kit_setup.py --check
```

---

## ⚡ Environment Activation

To load all Android-Kit paths, binaries, and environment variables into your active shell session, source the activation helper:

```bash
source helpers/activate_env.sh
# OR
source $HOME/android-kit/env_activate.sh
```

Upon activation, the shell will display the **TRON Environment HUD**:

```text
▲ [AEONFLUX ANDROID-KIT ACTIVE]
  • JAVA_HOME   : /home/user/android-kit/jdk
  • ANDROID_HOME : /home/user/android-kit/sdk
  • NDK_ROOT     : /home/user/android-kit/sdk/ndk/26.1.10909125
  • GRADLE_HOME  : /home/user/android-kit/gradle
  ✔ Environment variables loaded into current shell session.
```

---

## 🎮 Turn-Key Launcher Scripts

Direct launchers are created in `$HOME/android-kit/` for immediate invocation:

| Launcher Script | Description |
| :--- | :--- |
| `$HOME/android-kit/android-studio` | Launches Android Studio IDE with correct JDK and SDK variables |
| `$HOME/android-kit/gradle` | Runs Gradle binary wrapper with Java 17 |
| `$HOME/android-kit/adb` | Executes Android Debug Bridge (`adb`) |
| `$HOME/android-kit/emulator` | Executes Android Emulator binary |
| `$HOME/android-kit/sdkmanager` | Manages Android SDK packages |
| `$HOME/android-kit/start-pixel5.sh` | Quick-launches Pixel 5 AVD with KVM acceleration |
| `$HOME/android-kit/start-pixel8.sh` | Quick-launches Pixel 8 AVD with KVM acceleration |

---

## ⚙ Project Integration (`local.properties`)

The setup manager automatically generates or updates `local.properties` in the repository root:

```properties
sdk.dir=/home/user/android-kit/sdk
ndk.dir=/home/user/android-kit/sdk/ndk/26.1.10909125
```

This guarantees seamless compilation when building via standard `./gradlew assembleDebug` or opening the project in Android Studio.
