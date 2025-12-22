#!/bin/sh

# This script is used to initialize the terminal
# Assuming that alpine.gz.tar and proot.tar.gz is located in internal storage of the app.

ROOT_PATH="/data/user/0/com.ravix/files"

# set alpine.gz.tar and proot.tar.gz location
ALPINE_ZIP="$ROOT_PATH/alpine_"
PROOT_ZIP="$ROOT_PATH/proot_"

# set their final location
ALPINE_FOLDER="$ROOT_PATH/alpine"
PROOT_FOLDER="$ROOT_PATH/proot"

# set proot and loader location
PROOT="$ROOT_PATH/proot/bin/proot-userland"
PROOT_LOADER="$ROOT_PATH/proot/libexec/proot/loader"

# set temporary directory and ensure it exists (FIXED: ROOT_PATH instead of ROOT_DIR)
PROOT_TMP_DIR="$ROOT_PATH/alpine/tmp"

init() {
    # Create directories only if they don't exist
    [ -d "$ALPINE_FOLDER" ] || mkdir -p "$ALPINE_FOLDER"
    [ -d "$PROOT_FOLDER" ] || mkdir -p "$PROOT_FOLDER"
    
    # extract proot and alpine with proper flags
    echo "Extracting Alpine..."
    tar -xzf "$ALPINE_ZIP" -C "$ALPINE_FOLDER" --strip-components=1 2>/dev/null || \
    tar -xzf "$ALPINE_ZIP" -C "$ALPINE_FOLDER" -P 2>/dev/null || \
    tar -xzf "$ALPINE_ZIP" -C "$ALPINE_FOLDER" 2>/dev/null
    
    echo "Extracting PRoot..."
    tar -xzf "$PROOT_ZIP" -C "$PROOT_FOLDER" --strip-components=1 2>/dev/null || \
    tar -xzf "$PROOT_ZIP" -C "$PROOT_FOLDER" -P 2>/dev/null || \
    tar -xzf "$PROOT_ZIP" -C "$PROOT_FOLDER" 2>/dev/null

    # get executable permission
    chmod +x "$PROOT" 2>/dev/null
    chmod +x "$PROOT_LOADER"/* 2>/dev/null
}


start() {
    # Check if PROOT exists and is executable
    if [ ! -x "$PROOT" ]; then
        echo "Error: PRoot binary not found or not executable at: $PROOT"
        return 1
    fi

    # Make sure temp dir exists and is writable
    mkdir -p "$PROOT_TMP_DIR"
    chmod 1777 "$PROOT_TMP_DIR"

    # Export PROOT_TMP_DIR so PRoot can see it
    export PROOT_TMP_DIR="$PROOT_TMP_DIR"

    echo "Starting PRoot environment..."
    "$PROOT" --link2symlink -0 -w / -r "$ALPINE_FOLDER" -b /storage/emulated/0:/mnt/sdcard
}

# Main execution
echo "Starting Ravix terminal setup..."

# check folders exists 
if [ -d "$ALPINE_FOLDER" ] && [ -x "$PROOT" ]; then
    echo "Environment already setup, starting..."
    start
else 
    echo "Initializing environment for first time..."
    init
    echo "Setup complete, starting terminal..."
    start
    # Add DNS configuration
    echo "nameserver 8.8.8.8" >> "$ALPINE_FOLDER/etc/resolv.conf" 2>/dev/null
fi