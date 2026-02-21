#!/usr/bin/env bash

set -euo pipefail

# ????????????????????????????????????????????????????????????????????????
# ??? PLEASE VERIFY THE SCRIPT BEFORE INSTALLATION FOR YOUR OWN SAFETY ???
# ????????????????????????????????????????????????????????????????????????

echo "============================="
echo "======= Klog Installer ======"
echo "============================="
echo ""
echo "This installer will:"
echo "1. Download the latest binary from GitHub releases"
echo "2. Install to ~/.local/bin or /usr/local/bin"
echo "3. Never ask for sudo unless necessary"
echo ""
read -p "Continue? [y/n] " -n 1 -r
echo ""

if [[ ! $REPLY =~ ^[Yy]$ ]]; then
    exit 2
fi

# === Config ===
REPO="ignorant05/log-processing-system"
BINARY_NAME="klog"
VERSION="${VERSION:-latest}"

# === Colors ===
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m'

# === Logging ===
info()    { echo -e "${NC}ℹ${NC} $1"; }
success() { echo -e "${GREEN}✓${NC} $1"; }
error()   { echo -e "${RED}✗${NC} $1" >&2; }
warn()    { echo -e "${YELLOW}⚠${NC} $1"; }
fatal()   { error "$1"; exit 1; }

# === Platform Detection ===
detect_platform() {
    local os arch

    os=$(uname -s | tr '[:upper:]' '[:lower:]')
    case "$os" in
        linux)   os="linux"   ;;
        darwin)  os="darwin"  ;;
        mingw*|msys*|cygwin*) os="windows" ;;
        *) fatal "Unsupported OS: $os" ;;
    esac

    arch=$(uname -m)
    case "$arch" in
        x86_64|amd64)  arch="amd64" ;;
        arm64|aarch64) arch="arm64" ;;
        *) fatal "Unsupported architecture: $arch" ;;
    esac

    echo "${os}_${arch}"
}

# === Installation via direct download ===
install_via_curl() {
    local platform="$1"
    local version="$2"

    info "Installing via direct download..."

    if ! command -v curl &> /dev/null; then
        fatal "curl not found. Please install curl and try again."
    fi

    if [ "$version" = "latest" ]; then
        version=$(curl -fsSL "https://api.github.com/repos/$REPO/releases/latest" \
                  | grep '"tag_name"' \
                  | cut -d'"' -f4)
        [ -z "$version" ] && fatal "Failed to fetch latest version from GitHub."
    fi

    local url="https://github.com/$REPO/releases/download/$version/${BINARY_NAME}_${platform}.tar.gz"
    local temp_dir
    temp_dir=$(mktemp -d)
    trap 'rm -rf "$temp_dir"' EXIT

    info "Downloading $BINARY_NAME $version for $platform..."
    curl -fSL "$url" -o "$temp_dir/${BINARY_NAME}.tar.gz" || fatal "Download failed. Check that release $version exists for $platform."

    tar -xzf "$temp_dir/${BINARY_NAME}.tar.gz" -C "$temp_dir"

    if [ ! -f "$temp_dir/$BINARY_NAME" ]; then
        fatal "Binary not found in archive. The release asset may be structured differently."
    fi

    local install_dir="${INSTALL_DIR:-$HOME/.local/bin}"
    mkdir -p "$install_dir"
    mv "$temp_dir/$BINARY_NAME" "$install_dir/$BINARY_NAME"
    chmod +x "$install_dir/$BINARY_NAME"

    success "Installed $BINARY_NAME $version to $install_dir/$BINARY_NAME"

    if ! echo "$PATH" | grep -q "$install_dir"; then
        warn "Add to PATH: export PATH=\"\$PATH:$install_dir\""
        warn "Then restart your shell or run: source ~/.bashrc"
    fi
}

# === Installation via Docker wrapper ===
install_via_docker() {
    info "Installing via Docker wrapper..."

    if ! command -v docker &> /dev/null; then
        fatal "Docker not found. Install Docker from: https://docs.docker.com/get-docker/"
    fi

    if ! docker info &> /dev/null 2>&1; then
        fatal "Docker daemon is not running. Start Docker and try again."
    fi

    local image="ghcr.io/ignorant05/log-processing-system:latest"

    info "Pulling Docker image $image..."
    if ! docker pull "$image" > /dev/null 2>&1; then
        warn "Could not pull image $image. It may not exist yet."
        warn "Build and push it first with:"
        warn "  docker buildx build --platform linux/amd64,linux/arm64 --tag $image . --push"
        fatal "Docker installation aborted."
    fi

    # Write the wrapper script to a temp file first
    local wrapper
    wrapper=$(mktemp)

    cat > "$wrapper" << EOF
#!/usr/bin/env bash
set -e

IMAGE="$image"

if ! command -v docker &> /dev/null; then
    echo "Error: Docker not found" >&2
    exit 1
fi

if ! docker image inspect "\$IMAGE" &> /dev/null 2>&1; then
    echo "Pulling log-processing-system image..." >&2
    if ! docker pull "\$IMAGE" > /dev/null 2>&1; then
        echo "Error: Could not pull \$IMAGE" >&2
        echo "Make sure the image exists and you have access." >&2
        exit 1
    fi
fi

exec docker run --rm -it \\
    -v "\$(pwd):/workspace" \\
    -w /workspace \\
    "\$IMAGE" "\$@"
EOF

    chmod +x "$wrapper"

    local install_dir="${INSTALL_DIR:-$HOME/.local/bin}"
    local install_path="$install_dir/$BINARY_NAME"
    mkdir -p "$install_dir"

    if [ -w "$install_dir" ]; then
        mv "$wrapper" "$install_path"
        success "Docker wrapper installed to $install_path"
    elif sudo mv "$wrapper" "$install_path" 2>/dev/null; then
        success "Docker wrapper installed to $install_path (with sudo)"
    else
        install_dir="$HOME/.local/bin"
        install_path="$install_dir/$BINARY_NAME"
        mkdir -p "$install_dir"
        mv "$wrapper" "$install_path"
        success "Docker wrapper installed to $install_path"
        warn "Add to PATH: export PATH=\"\$PATH:$install_dir\""
    fi
}

# === Main ===
main() {
    echo ""
    echo "┌─────────────────────────────────────────────────────┐"
    echo "│ ─────── klog: A Kafka Log Processing System ─────── │"
    echo "└─────────────────────────────────────────────────────┘"
    echo ""

    if command -v "$BINARY_NAME" &> /dev/null; then
        local current_version
        current_version=$("$BINARY_NAME" --version 2>/dev/null || echo "unknown")
        warn "$BINARY_NAME is already installed (version: $current_version)"
        read -p "Update / reinstall? [y/n] " -n 1 -r
        echo ""
        [[ ! $REPLY =~ ^[Yy]$ ]] && exit 0
    fi

    echo "Select installation method:"
    echo "  1) Direct download (recommended)"
    echo "  2) Docker wrapper (requires Docker)"
    echo ""
    read -p "Choice [1-2]: " -n 1 -r
    echo ""

    local platform
    platform=$(detect_platform)

    case "$REPLY" in
        2) install_via_docker ;;
        *) install_via_curl "$platform" "$VERSION" ;;
    esac

    echo ""
    success "Installation complete!"
    echo ""
    echo "Quick start:"
    echo "  \$ $BINARY_NAME --help"
    echo "  \$ $BINARY_NAME version"
    echo "  \$ $BINARY_NAME health -b localhost:9092 -t logs"
    echo ""
    echo "Documentation: https://github.com/$REPO"
    echo ""
}

# Guard: only run main when script is executed directly, not sourced
if [[ "${BASH_SOURCE[0]}" == "${0}" ]]; then
    main "$@"
fi
