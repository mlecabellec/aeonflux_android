#!/usr/bin/env python3
"""
===============================================================================
  ▲ E O N F L U X // A N D R O I D - K I T  S E T U P  M A N A G E R
===============================================================================
  File:         helpers/android_kit_setup.py
  Description:  Cybernetic setup manager for Android development environment.
                Detects, downloads, extracts, provisions, and configures:
                  - JDK 17 (Eclipse Temurin)
                  - Gradle 9.3.0
                  - Android SDK (compileSdk/targetSdk 34, build-tools 34.0.0, platform-tools, emulator)
                  - Android NDK (r26b / 26.1.10909125 & CMake 3.22.1 for C++ whisper.cpp STT)
                  - Android Studio (Standalone IDE)
                  - Turn-key launch scripts in $HOME/android-kit/
                  - Virtual Devices (Pixel 5 & Pixel 8 API 34 AVDs)
  Author:       AeonFlux Team / Advanced Agentic Coding
  Version:      1.0.0
===============================================================================
"""

import argparse
import json
import os
import platform
import re
import shutil
import stat
import subprocess
import sys
import urllib.request
import zipfile
import tarfile
from pathlib import Path
from typing import Dict, List, Optional, Tuple, Any

# Try importing Rich for advanced Cybernetic TUI rendering; fallback to ANSI Engine
HAS_RICH = False
try:
    from rich.console import Console
    from rich.panel import Panel
    from rich.table import Table
    from rich.text import Text
    from rich.progress import Progress, SpinnerColumn, TextColumn, BarColumn, DownloadColumn, TransferSpeedColumn, TimeRemainingColumn
    from rich.prompt import Prompt, Confirm
    from rich.align import Align
    from rich.live import Live
    HAS_RICH = True
except ImportError:
    HAS_RICH = False

# -----------------------------------------------------------------------------
# CYBERNETIC COLOR SYSTEM & ANSI FALLBACK CONSTANTS
# -----------------------------------------------------------------------------
CYBER_CYAN = "\033[38;2;0;255;255m"      # Main Cyber Cyan (#00FFFF)
CYBER_BLUE = "\033[38;2;0;191;255m"     # Electric Blue (#00BFFF)
CYBER_PINK = "\033[38;2;255;0;127m"     # Neon Magenta/Pink (#FF007F)
CYBER_YELLOW = "\033[38;2;255;215;0m"   # High-Voltage Amber (#FFD700)
CYBER_GREEN = "\033[38;2;50;205;50m"    # Grid Green (#32CD32)
CYBER_RED = "\033[38;2;255;69;0m"       # Warning Crimson (#FF4500)
CYBER_GRAY = "\033[38;2;112;128;144m"   # Slate Gray (#708090)
CYBER_BOLD = "\033[1m"
CYBER_RESET = "\033[0m"

AEONFLUX_ASCII_BANNER = r"""
 █████╗ ███████╗██████╗ ███╗   ██╗███████╗██╗     ██╗   ██╗██╗  ██╗
██╔══██╗██╔════╝██╔══██╗████╗  ██║██╔════╝██║     ██║   ██║╚██╗██╔╝
███████║█████╗  ██║  ██║██╔██╗ ██║█████╗  ██║     ██║   ██║ ╚████╔╝ 
██╔══██║██╔══╝  ██║  ██║██║╚██╗██║██╔══╝  ██║     ██║   ██║ ██╔═██╗ 
██║  ██║███████╗╚██████╔╝██║ ╚████║██║     ███████╗╚██████╔╝██╔╝  ██╗
╚═╝  ╚═╝╚══════╝ ╚═════╝ ╚═╝  ╚═══╝╚═╝     ╚══════╝ ╚═════╝ ╚═╝  ╚═╝
          :: CYBERNETIC DEVELOPMENT KIT MANAGER :: v1.0.0 ::
"""

# Default Installation Paths & Software Matrix
DEFAULT_KIT_DIR = Path.home() / "android-kit"
JDK_URL = "https://api.adoptium.net/v3/binary/latest/17/ga/linux/x64/jdk/hotspot/normal/eclipse"
GRADLE_URL = "https://services.gradle.org/distributions/gradle-9.3.0-bin.zip"
CMDLINE_TOOLS_URL = "https://dl.google.com/android/repository/commandlinetools-linux-11076708_latest.zip"
STUDIO_URL = "https://dl.google.com/dl/android/studio/ide-zips/2024.1.1.13/android-studio-2024.1.1.13-linux.tar.gz"

REQUIRED_SDK_PACKAGES = [
    "platforms;android-34",
    "build-tools;34.0.0",
    "platform-tools",
    "emulator",
    "ndk;26.1.10909125",
    "cmake;3.22.1",
    "system-images;android-34;google_apis;x86_64"
]

class CyberUI:
    """Cybernetic Terminal UI Renderer with Rich / ANSI engine."""
    
    def __init__(self, use_rich: bool = HAS_RICH):
        self.use_rich = use_rich
        if self.use_rich:
            self.console = Console()
        else:
            self.console = None

    def print_banner(self):
        if self.use_rich:
            p = Panel(
                Text(AEONFLUX_ASCII_BANNER, style="bold cyan", justify="center"),
                title="[bold magenta]▲ AEONFLUX ANDROID-KIT PROVISIONER[/bold magenta]",
                subtitle="[bold blue]CYBERNETIC ENVIRONMENT AUTOMATION[/bold blue]",
                border_style="cyan",
                expand=True
            )
            self.console.print(p)
        else:
            print(f"{CYBER_CYAN}{CYBER_BOLD}{AEONFLUX_ASCII_BANNER}{CYBER_RESET}")
            print(f"{CYBER_BLUE}==============================================================================={CYBER_RESET}")

    def print_status(self, label: str, status: str, state_color: str = CYBER_CYAN, detail: str = ""):
        if self.use_rich:
            status_text = f"[{state_color}][ {status.upper()} ][/{state_color}]"
            self.console.print(f"  • {label:<35} {status_text} {detail}")
        else:
            color = CYBER_CYAN if state_color == "cyan" else (CYBER_GREEN if state_color == "green" else (CYBER_RED if state_color == "red" else CYBER_YELLOW))
            print(f"  • {label:<35} {color}[ {status.upper()} ]{CYBER_RESET} {detail}")

    def print_header(self, title: str):
        if self.use_rich:
            self.console.print(f"\n[bold cyan]╔═══ {title.upper()} ══════════════════════════════════════════════════════════[/bold cyan]")
        else:
            print(f"\n{CYBER_CYAN}{CYBER_BOLD}╔═══ {title.upper()} ══════════════════════════════════════════════════════════{CYBER_RESET}")

    def print_info(self, msg: str):
        if self.use_rich:
            self.console.print(f"[blue]ℹ[/blue] {msg}")
        else:
            print(f"{CYBER_BLUE}ℹ {msg}{CYBER_RESET}")

    def print_success(self, msg: str):
        if self.use_rich:
            self.console.print(f"[bold green]✔[/bold green] {msg}")
        else:
            print(f"{CYBER_GREEN}{CYBER_BOLD}✔ {msg}{CYBER_RESET}")

    def print_warning(self, msg: str):
        if self.use_rich:
            self.console.print(f"[bold yellow]⚠[/bold yellow] {msg}")
        else:
            print(f"{CYBER_YELLOW}{CYBER_BOLD}⚠ {msg}{CYBER_RESET}")

    def print_error(self, msg: str):
        if self.use_rich:
            self.console.print(f"[bold red]✖[/bold red] {msg}")
        else:
            print(f"{CYBER_RED}{CYBER_BOLD}✖ {msg}{CYBER_RESET}")


class SystemAnalyzer:
    """Inspects Linux environment, hardware capabilities, and tool dependencies."""

    def __init__(self, ui: CyberUI):
        self.ui = ui

    def analyze_system(self) -> Dict[str, Any]:
        self.ui.print_header("System Diagnostics & Host Analysis")

        results = {}
        # 1. OS & Architecture
        os_name = platform.system()
        arch = platform.machine()
        distro = "Linux"
        if os.path.exists("/etc/os-release"):
            with open("/etc/os-release") as f:
                for line in f:
                    if line.startswith("PRETTY_NAME="):
                        distro = line.split("=")[1].strip().strip('"')
                        break
        
        self.ui.print_status("Host Operating System", f"{os_name} ({distro})", "green")
        self.ui.print_status("CPU Architecture", arch, "green" if arch in ("x86_64", "aarch64") else "yellow")
        results["os"] = os_name
        results["arch"] = arch
        results["distro"] = distro

        # 2. Hardware KVM Virtualization
        kvm_available = os.path.exists("/dev/kvm") and os.access("/dev/kvm", os.R_OK | os.W_OK)
        if kvm_available:
            self.ui.print_status("Hardware Acceleration", "KVM READY", "green", "/dev/kvm operational")
        else:
            self.ui.print_status("Hardware Acceleration", "NO KVM", "yellow", "Enable VT-x/AMD-V and chmod /dev/kvm")
        results["kvm"] = kvm_available

        # 3. Available System Binaries
        tools = ["curl", "wget", "tar", "unzip", "git", "python3", "gcc", "clang", "make", "cmake", "java", "javac"]
        found_tools = {}
        for tool in tools:
            tool_path = shutil.which(tool)
            found_tools[tool] = tool_path is not None
            state = "green" if tool_path else ("yellow" if tool in ("curl", "wget", "gcc", "cmake", "javac") else "red")
            status = "INSTALLED" if tool_path else "MISSING"
            self.ui.print_status(f"Tool Check: {tool}", status, state, tool_path or "")
        results["tools"] = found_tools

        # 4. Storage Space
        home_stat = shutil.disk_usage(Path.home())
        free_gb = home_stat.free / (1024**3)
        self.ui.print_status("Available Disk Space", f"{free_gb:.1f} GB", "green" if free_gb >= 20.0 else "yellow")
        results["free_gb"] = free_gb

        return results


class AndroidKitManager:
    """Manages downloading, extraction, SDK provisioning, launchers, and AVD setup."""

    def __init__(self, kit_dir: Path, ui: CyberUI):
        self.kit_dir = kit_dir
        self.jdk_dir = kit_dir / "jdk"
        self.gradle_dir = kit_dir / "gradle"
        self.sdk_dir = kit_dir / "sdk"
        self.studio_dir = kit_dir / "studio"
        self.bin_dir = kit_dir / "bin"
        self.avd_dir = kit_dir / "avd"
        self.downloads_dir = kit_dir / "downloads"
        self.ui = ui

    def prepare_directories(self):
        """Creates directory structure under $HOME/android-kit."""
        for d in [self.kit_dir, self.jdk_dir, self.gradle_dir, self.sdk_dir, self.studio_dir, self.bin_dir, self.avd_dir, self.downloads_dir]:
            d.mkdir(parents=True, exist_ok=True)

    def check_kit_status(self) -> Dict[str, bool]:
        """Checks status of installed tools in $HOME/android-kit."""
        status = {
            "jdk": (self.jdk_dir / "bin" / "java").exists(),
            "gradle": (self.gradle_dir / "bin" / "gradle").exists(),
            "cmdline_tools": (self.sdk_dir / "cmdline-tools" / "latest" / "bin" / "sdkmanager").exists(),
            "sdk_platform": (self.sdk_dir / "platforms" / "android-34").exists(),
            "sdk_build_tools": (self.sdk_dir / "build-tools" / "34.0.0").exists(),
            "platform_tools": (self.sdk_dir / "platform-tools" / "adb").exists(),
            "emulator": (self.sdk_dir / "emulator" / "emulator").exists(),
            "ndk": (self.sdk_dir / "ndk" / "26.1.10909125").exists() or any((self.sdk_dir / "ndk").glob("*")),
            "cmake": (self.sdk_dir / "cmake" / "3.22.1").exists() or any((self.sdk_dir / "cmake").glob("*")),
            "studio": (self.studio_dir / "bin" / "studio.sh").exists(),
            "env_activate": (self.kit_dir / "env_activate.sh").exists(),
            "pixel5_avd": (Path.home() / ".android" / "avd" / "Pixel_5_API_34.ini").exists() or (self.avd_dir / "Pixel_5_API_34.ini").exists(),
            "pixel8_avd": (Path.home() / ".android" / "avd" / "Pixel_8_API_34.ini").exists() or (self.avd_dir / "Pixel_8_API_34.ini").exists()
        }
        return status

    def download_file(self, url: str, dest_path: Path, title: str):
        """Downloads a URL with progress reporting."""
        self.ui.print_info(f"Downloading {title} from: {url}")
        
        req = urllib.request.Request(url, headers={'User-Agent': 'Mozilla/5.0 (AeonFlux Android-Kit Provisioner)'})
        
        with urllib.request.urlopen(req) as response, open(dest_path, 'wb') as out_file:
            total_size = int(response.headers.get('content-length', 0))
            block_size = 1024 * 64
            downloaded = 0

            if HAS_RICH and self.ui.console:
                with Progress(
                    SpinnerColumn(),
                    TextColumn("[cyan]{task.description}"),
                    BarColumn(bar_width=40, style="blue", complete_style="cyan"),
                    DownloadColumn(),
                    TransferSpeedColumn(),
                    TimeRemainingColumn(),
                    console=self.ui.console
                ) as progress:
                    task = progress.add_task(f"[cyan]{title}", total=total_size if total_size > 0 else None)
                    while True:
                        buffer = response.read(block_size)
                        if not buffer:
                            break
                        downloaded += len(buffer)
                        out_file.write(buffer)
                        progress.update(task, completed=downloaded)
            else:
                while True:
                    buffer = response.read(block_size)
                    if not buffer:
                        break
                    downloaded += len(buffer)
                    out_file.write(buffer)
                    if total_size > 0:
                        pct = (downloaded / total_size) * 100
                        print(f"\r  └─ Progress: {pct:.1f}% ({downloaded / (1024*1024):.1f} MB)", end="")
                print()
        
        self.ui.print_success(f"{title} successfully downloaded to {dest_path.name}")

    def extract_archive(self, archive_path: Path, target_dir: Path, strip_components: int = 0):
        """Extracts .zip or .tar.gz archive into target directory."""
        self.ui.print_info(f"Extracting {archive_path.name} into {target_dir}...")
        target_dir.mkdir(parents=True, exist_ok=True)

        if archive_path.name.endswith(".zip"):
            with zipfile.ZipFile(archive_path, 'r') as zip_ref:
                if strip_components == 0:
                    zip_ref.extractall(target_dir)
                else:
                    for member in zip_ref.infolist():
                        parts = Path(member.filename).parts
                        if len(parts) > strip_components:
                            target_path = target_dir / Path(*parts[strip_components:])
                            if member.is_dir():
                                target_path.mkdir(parents=True, exist_ok=True)
                            else:
                                target_path.parent.mkdir(parents=True, exist_ok=True)
                                with zip_ref.open(member) as source, open(target_path, "wb") as target:
                                    shutil.copyfileobj(source, target)
                                mode = (member.external_attr >> 16) & 0o777
                                if mode:
                                    os.chmod(target_path, mode)

        elif archive_path.name.endswith(".tar.gz") or archive_path.name.endswith(".tgz"):
            with tarfile.open(archive_path, "r:gz") as tar_ref:
                for member in tar_ref.getmembers():
                    parts = Path(member.name).parts
                    if len(parts) > strip_components:
                        member_path = Path(*parts[strip_components:])
                        target_path = target_dir / member_path
                        if member.isdir():
                            target_path.mkdir(parents=True, exist_ok=True)
                        else:
                            target_path.parent.mkdir(parents=True, exist_ok=True)
                            f = tar_ref.extractfile(member)
                            if f:
                                with open(target_path, "wb") as target:
                                    shutil.copyfileobj(f, target)
                                os.chmod(target_path, member.mode)

        self.ui.print_success(f"Extraction complete for {archive_path.name}")

    def provision_jdk(self):
        """Downloads and extracts JDK 17 (Temurin)."""
        if (self.jdk_dir / "bin" / "java").exists():
            self.ui.print_status("JDK 17", "READY", "green", str(self.jdk_dir))
            return

        self.ui.print_header("Provisioning JDK 17 (Eclipse Temurin)")
        dest = self.downloads_dir / "jdk17.tar.gz"
        self.download_file(JDK_URL, dest, "JDK 17 OpenJDK")
        self.extract_archive(dest, self.jdk_dir, strip_components=1)
        self.ui.print_status("JDK 17", "INSTALLED", "green", str(self.jdk_dir))

    def provision_gradle(self):
        """Downloads and extracts Gradle 9.3.0."""
        if (self.gradle_dir / "bin" / "gradle").exists():
            self.ui.print_status("Gradle 9.3.0", "READY", "green", str(self.gradle_dir))
            return

        self.ui.print_header("Provisioning Gradle 9.3.0")
        dest = self.downloads_dir / "gradle-9.3.0-bin.zip"
        self.download_file(GRADLE_URL, dest, "Gradle 9.3.0")
        self.extract_archive(dest, self.gradle_dir, strip_components=1)
        
        gradle_bin = self.gradle_dir / "bin" / "gradle"
        if gradle_bin.exists():
            gradle_bin.chmod(gradle_bin.stat().st_mode | stat.S_IEXEC)

        self.ui.print_status("Gradle 9.3.0", "INSTALLED", "green", str(self.gradle_dir))

    def provision_sdk(self):
        """Downloads Android Command Line Tools and provisions SDK/NDK/CMake components via sdkmanager."""
        sdkmanager = self.sdk_dir / "cmdline-tools" / "latest" / "bin" / "sdkmanager"
        
        if not sdkmanager.exists():
            self.ui.print_header("Provisioning Android SDK Command Line Tools")
            dest = self.downloads_dir / "commandlinetools-linux.zip"
            self.download_file(CMDLINE_TOOLS_URL, dest, "Android Cmdline-Tools")
            
            latest_dir = self.sdk_dir / "cmdline-tools" / "latest"
            self.extract_archive(dest, latest_dir, strip_components=1)

            for b in (latest_dir / "bin").glob("*"):
                b.chmod(b.stat().st_mode | stat.S_IEXEC)

        self.ui.print_status("SDK Cmdline Tools", "INSTALLED", "green", str(sdkmanager))

        self.ui.print_info("Accepting Android SDK licenses...")
        java_home = str(self.jdk_dir) if (self.jdk_dir / "bin" / "java").exists() else os.environ.get("JAVA_HOME", "")
        env = os.environ.copy()
        if java_home:
            env["JAVA_HOME"] = java_home
            env["PATH"] = f"{java_home}/bin:{env.get('PATH', '')}"
        env["ANDROID_HOME"] = str(self.sdk_dir)
        env["ANDROID_SDK_ROOT"] = str(self.sdk_dir)

        license_proc = subprocess.Popen(
            [str(sdkmanager), f"--sdk_root={self.sdk_dir}", "--licenses"],
            stdin=subprocess.PIPE,
            stdout=subprocess.PIPE,
            stderr=subprocess.PIPE,
            text=True,
            env=env
        )
        try:
            license_proc.communicate(input="y\ny\ny\ny\ny\ny\ny\ny\n", timeout=30)
        except Exception as e:
            license_proc.kill()

        self.ui.print_header("Installing Android SDK / NDK / CMake Components")
        sdk_args = [str(sdkmanager), f"--sdk_root={self.sdk_dir}"] + REQUIRED_SDK_PACKAGES
        self.ui.print_info(f"Executing sdkmanager: {' '.join(REQUIRED_SDK_PACKAGES)}")

        install_proc = subprocess.run(sdk_args, env=env, stdout=subprocess.PIPE, stderr=subprocess.PIPE, text=True)
        if install_proc.returncode == 0:
            self.ui.print_success("Android SDK packages installed successfully!")
        else:
            self.ui.print_warning(f"sdkmanager completed with output: {install_proc.stdout[:200]}...")

    def provision_studio(self):
        """Downloads and provisions Android Studio IDE."""
        studio_bin = self.studio_dir / "bin" / "studio.sh"
        if studio_bin.exists():
            self.ui.print_status("Android Studio", "READY", "green", str(self.studio_dir))
            return

        self.ui.print_header("Provisioning Android Studio Standalone IDE")
        dest = self.downloads_dir / "android-studio-linux.tar.gz"
        self.download_file(STUDIO_URL, dest, "Android Studio IDE")
        self.extract_archive(dest, self.studio_dir, strip_components=1)

        if studio_bin.exists():
            studio_bin.chmod(studio_bin.stat().st_mode | stat.S_IEXEC)

        self.ui.print_status("Android Studio", "INSTALLED", "green", str(self.studio_dir))

    def create_launchers_and_environment(self):
        """Generates activation script and executable turn-key launchers in $HOME/android-kit."""
        self.ui.print_header("Generating Turn-Key Launchers & Environment Activation")

        java_bin = str(self.jdk_dir / "bin" / "java")
        gradle_bin = str(self.gradle_dir / "bin" / "gradle")
        sdkmanager_bin = str(self.sdk_dir / "cmdline-tools" / "latest" / "bin" / "sdkmanager")
        avdmanager_bin = str(self.sdk_dir / "cmdline-tools" / "latest" / "bin" / "avdmanager")
        adb_bin = str(self.sdk_dir / "platform-tools" / "adb")
        emulator_bin = str(self.sdk_dir / "emulator" / "emulator")
        studio_bin = str(self.studio_dir / "bin" / "studio.sh")

        ndk_path = self.sdk_dir / "ndk" / "26.1.10909125"
        if not ndk_path.exists():
            ndk_dirs = list((self.sdk_dir / "ndk").glob("*"))
            if ndk_dirs:
                ndk_path = ndk_dirs[0]

        # 1. Activation script (env_activate.sh)
        env_activate_content = f"""#!/usr/bin/env bash
# ===============================================================================
#   ▲ E O N F L U X // A N D R O I D - K I T  E N V I R O N M E N T  A C T I V A T O R
# ===============================================================================
# Source this file to activate the development environment:
#   source $HOME/android-kit/env_activate.sh

export ANDROID_KIT="{self.kit_dir}"
export JAVA_HOME="{self.jdk_dir}"
export GRADLE_HOME="{self.gradle_dir}"
export ANDROID_HOME="{self.sdk_dir}"
export ANDROID_SDK_ROOT="{self.sdk_dir}"
# Resolve NDK Path
if [ -f "${{ANDROID_HOME}}/ndk/26.1.10909125/source.properties" ]; then
    export ANDROID_NDK_ROOT="${{ANDROID_HOME}}/ndk/26.1.10909125"
elif [ -d "${{ANDROID_HOME}}/ndk" ]; then
    export ANDROID_NDK_ROOT="$(find "${{ANDROID_HOME}}/ndk" -name "source.properties" 2>/dev/null | head -n 1 | xargs -r dirname)"
fi

if [ -z "${{ANDROID_NDK_ROOT}}" ] && [ -d "${{HOME}}/Android/Sdk/ndk" ]; then
    export ANDROID_NDK_ROOT="$(find "${{HOME}}/Android/Sdk/ndk" -name "source.properties" 2>/dev/null | head -n 1 | xargs -r dirname)"
fi
export ANDROID_AVD_HOME="{self.avd_dir}"

# Prepend kit binaries to PATH
export PATH="$ANDROID_KIT/bin:$JAVA_HOME/bin:$GRADLE_HOME/bin:$ANDROID_HOME/cmdline-tools/latest/bin:$ANDROID_HOME/platform-tools:$ANDROID_HOME/emulator:$PATH"

# Aliases
alias studio="$ANDROID_KIT/android-studio"
alias gradlew-kit="$ANDROID_KIT/gradle.sh"
alias pixel5="$ANDROID_KIT/start-pixel5.sh"
alias pixel8="$ANDROID_KIT/start-pixel8.sh"

if [ -t 1 ]; then
    echo -e "\\033[38;2;0;255;255m\\033[1m▲ [AEONFLUX ANDROID-KIT ACTIVE]\\033[0m"
    echo -e "\\033[38;2;0;191;255m  • JAVA_HOME   :\\033[0m $JAVA_HOME"
    echo -e "\\033[38;2;0;191;255m  • ANDROID_HOME :\\033[0m $ANDROID_HOME"
    echo -e "\\033[38;2;0;191;255m  • NDK_ROOT     :\\033[0m $ANDROID_NDK_ROOT"
    echo -e "\\033[38;2;0;191;255m  • GRADLE_HOME  :\\033[0m $GRADLE_HOME"
fi
"""
        env_activate_file = self.kit_dir / "env_activate.sh"
        env_activate_file.write_text(env_activate_content)
        env_activate_file.chmod(0o755)

        helpers_activate = Path.cwd() / "helpers" / "activate_env.sh"
        if helpers_activate.parent.exists():
            helpers_activate.write_text(env_activate_content)
            helpers_activate.chmod(0o755)

        # 2. Launcher Scripts in $HOME/android-kit/
        launchers = {
            "android-studio": f"""#!/usr/bin/env bash
source "{env_activate_file}"
exec "{studio_bin}" "$@"
""",
            "gradle.sh": f"""#!/usr/bin/env bash
source "{env_activate_file}"
exec "{gradle_bin}" "$@"
""",
            "adb.sh": f"""#!/usr/bin/env bash
source "{env_activate_file}"
exec "{adb_bin}" "$@"
""",
            "emulator.sh": f"""#!/usr/bin/env bash
source "{env_activate_file}"
exec "{emulator_bin}" "$@"
""",
            "sdkmanager.sh": f"""#!/usr/bin/env bash
source "{env_activate_file}"
exec "{sdkmanager_bin}" "$@"
""",
            "avdmanager.sh": f"""#!/usr/bin/env bash
source "{env_activate_file}"
exec "{avdmanager_bin}" "$@"
""",
            "start-pixel5.sh": f"""#!/usr/bin/env bash
source "{env_activate_file}"
echo -e "\\033[38;2;0;255;255m▲ Launching Pixel 5 AVD (API 34)...\\033[0m"
exec "{emulator_bin}" -avd Pixel_5_API_34 -gpu auto -qemu -m 4096 "$@"
""",
            "start-pixel8.sh": f"""#!/usr/bin/env bash
source "{env_activate_file}"
echo -e "\\033[38;2;0;255;255m▲ Launching Pixel 8 AVD (API 34)...\\033[0m"
exec "{emulator_bin}" -avd Pixel_8_API_34 -gpu auto -qemu -m 4096 "$@"
"""
        }

        bin_mapping = {
            "android-studio": "android-studio",
            "gradle.sh": "gradle",
            "adb.sh": "adb",
            "emulator.sh": "emulator",
            "sdkmanager.sh": "sdkmanager",
            "avdmanager.sh": "avdmanager",
            "start-pixel5.sh": "start-pixel5",
            "start-pixel8.sh": "start-pixel8"
        }

        for name, script_content in launchers.items():
            script_path = self.kit_dir / name
            script_path.write_text(script_content)
            script_path.chmod(0o755)
            
            bin_name = bin_mapping.get(name, name)
            bin_link = self.bin_dir / bin_name
            if bin_link.exists() or bin_link.is_symlink():
                bin_link.unlink()
            bin_link.symlink_to(script_path)
            
            self.ui.print_status(f"Launcher: {name}", "CREATED", "green", str(script_path))

        # 3. Project local.properties configuration
        local_props = Path.cwd() / "local.properties"
        local_props_content = f"sdk.dir={self.sdk_dir}\nndk.dir={ndk_path}\n"
        local_props.write_text(local_props_content)
        self.ui.print_success(f"Configured project local.properties -> sdk.dir={self.sdk_dir}")

    def prepare_virtual_devices(self):
        """Creates Pixel 5 and Pixel 8 AVDs using avdmanager."""
        self.ui.print_header("Provisioning Virtual Devices (Pixel 5 & Pixel 8)")

        avdmanager = self.sdk_dir / "cmdline-tools" / "latest" / "bin" / "avdmanager"
        if not avdmanager.exists():
            self.ui.print_error("avdmanager binary not found! Please run SDK provisioning first.")
            return

        env = os.environ.copy()
        env["JAVA_HOME"] = str(self.jdk_dir)
        env["ANDROID_HOME"] = str(self.sdk_dir)
        env["ANDROID_SDK_ROOT"] = str(self.sdk_dir)
        env["ANDROID_AVD_HOME"] = str(self.avd_dir)
        env["PATH"] = f"{self.jdk_dir}/bin:{env.get('PATH', '')}"

        devices = [
            ("Pixel_5_API_34", "pixel_5", "Pixel 5"),
            ("Pixel_8_API_34", "pixel_8", "Pixel 8")
        ]

        for avd_name, device_id, title in devices:
            self.ui.print_info(f"Creating AVD: {avd_name} ({title})...")
            cmd = [
                str(avdmanager), "create", "avd",
                "-n", avd_name,
                "-k", "system-images;android-34;google_apis;x86_64",
                "-c", "2048M",
                "--force"
            ]
            
            p = subprocess.Popen(cmd, stdin=subprocess.PIPE, stdout=subprocess.PIPE, stderr=subprocess.PIPE, text=True, env=env)
            out, err = p.communicate(input="no\n")

            avd_dir_cfg = Path.home() / ".android" / "avd" / f"{avd_name}.avd" / "config.ini"

            if avd_dir_cfg.exists():
                cfg_lines = avd_dir_cfg.read_text().splitlines()
                updated_lines = []
                params_to_set = {
                    "hw.ramSize": "4096",
                    "vm.heapSize": "512",
                    "hw.cpu.ncore": "4",
                    "hw.gpu.enabled": "yes",
                    "hw.gpu.mode": "auto",
                    "fastboot.chosenSnapshotFile": "",
                    "fastboot.forceFastBoot": "yes"
                }

                existing_keys = set()
                for line in cfg_lines:
                    if "=" in line:
                        k, _ = line.split("=", 1)
                        k = k.strip()
                        if k in params_to_set:
                            updated_lines.append(f"{k}={params_to_set[k]}")
                            existing_keys.add(k)
                        else:
                            updated_lines.append(line)
                    else:
                        updated_lines.append(line)

                for k, v in params_to_set.items():
                    if k not in existing_keys:
                        updated_lines.append(f"{k}={v}")

                avd_dir_cfg.write_text("\n".join(updated_lines) + "\n")
                self.ui.print_status(f"AVD {avd_name}", "CREATED", "green", "Configured with 4GB RAM & KVM GPU")
            else:
                self.ui.print_status(f"AVD {avd_name}", "READY/CONFIGURED", "green")


# -----------------------------------------------------------------------------
# MAIN CLI & INTERACTIVE CYBERNETIC TUI CONTROLLER
# -----------------------------------------------------------------------------
def main():
    parser = argparse.ArgumentParser(
        description="▲ AEONFLUX Android-Kit Cybernetic Provisioner & Environment Manager"
    )
    parser.add_argument("--kit-dir", type=str, default=str(DEFAULT_KIT_DIR), help="Target kit directory (default: $HOME/android-kit)")
    parser.add_argument("--batch", action="store_true", help="Non-interactive automated provisioning mode")
    parser.add_argument("--check", action="store_true", help="Perform system diagnostics and status check only")
    parser.add_argument("--install-all", action="store_true", help="Install all components (JDK, Gradle, SDK, NDK, Studio, Launchers, AVDs)")
    parser.add_argument("--install-avd", action="store_true", help="Install Pixel 5 and Pixel 8 AVDs")

    args = parser.parse_args()

    ui = CyberUI()
    ui.print_banner()

    kit_dir = Path(args.kit_dir).expanduser().resolve()
    analyzer = SystemAnalyzer(ui)
    sys_info = analyzer.analyze_system()

    manager = AndroidKitManager(kit_dir, ui)
    manager.prepare_directories()

    status = manager.check_kit_status()

    ui.print_header("Android-Kit Status Matrix")
    for component, ready in status.items():
        ui.print_status(f"Kit Component: {component}", "READY" if ready else "MISSING", "green" if ready else "yellow")

    if args.check:
        sys.exit(0)

    if args.batch or args.install_all:
        ui.print_header("Executing Full Cybernetic Provisioning Pipeline")
        manager.provision_jdk()
        manager.provision_gradle()
        manager.provision_sdk()
        manager.provision_studio()
        manager.create_launchers_and_environment()
        manager.prepare_virtual_devices()
        ui.print_success("\n▲ Full Android-Kit environment provisioning complete!")
        ui.print_info(f"Activate environment by running: source {kit_dir}/env_activate.sh")
        sys.exit(0)

    if args.install_avd:
        manager.prepare_virtual_devices()
        sys.exit(0)

    # Interactive Cybernetic TUI Menu
    while True:
        ui.print_header("Cybernetic Main System Operations Menu")
        print(f"  {CYBER_CYAN}[1]{CYBER_RESET} Run Full Turn-Key Environment Provisioning (All Tools + Launchers + AVDs)")
        print(f"  {CYBER_CYAN}[2]{CYBER_RESET} Provision Core Tools Only (JDK 17 + Gradle 9.3.0)")
        print(f"  {CYBER_CYAN}[3]{CYBER_RESET} Provision Android SDK / NDK r26b / CMake 3.22.1")
        print(f"  {CYBER_CYAN}[4]{CYBER_RESET} Provision Android Studio Standalone IDE")
        print(f"  {CYBER_CYAN}[5]{CYBER_RESET} Provision Pixel 5 & Pixel 8 Virtual Devices (AVDs)")
        print(f"  {CYBER_CYAN}[6]{CYBER_RESET} Generate Environment Activator & Turn-Key Launchers")
        print(f"  {CYBER_CYAN}[7]{CYBER_RESET} Exit Cybernetic Manager")

        try:
            choice = input(f"\n{CYBER_CYAN}{CYBER_BOLD}Select Menu Option [1-7]: {CYBER_RESET}").strip()
        except (KeyboardInterrupt, EOFError):
            print("\nExiting Cybernetic Manager.")
            break

        if choice == "1":
            manager.provision_jdk()
            manager.provision_gradle()
            manager.provision_sdk()
            manager.provision_studio()
            manager.create_launchers_and_environment()
            manager.prepare_virtual_devices()
        elif choice == "2":
            manager.provision_jdk()
            manager.provision_gradle()
        elif choice == "3":
            manager.provision_sdk()
        elif choice == "4":
            manager.provision_studio()
        elif choice == "5":
            manager.prepare_virtual_devices()
        elif choice == "6":
            manager.create_launchers_and_environment()
        elif choice == "7":
            print(f"{CYBER_CYAN}Exiting Cybernetic Manager. End of Line.{CYBER_RESET}")
            break
        else:
            ui.print_warning("Invalid menu selection.")

if __name__ == "__main__":
    main()
