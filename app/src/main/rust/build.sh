#!/bin/bash

set -e

SCRIPT_DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"
cd "$SCRIPT_DIR"

if ! command -v cargo &> /dev/null; then
    echo "Rust not installed. Installing..."
    curl --proto '=https' --tlsv1.2 -sSf https://sh.rustup.rs | sh -s -- -y
    source "$HOME/.cargo/env"
fi

rustup target add aarch64-linux-android armv7-linux-androideabi x86_64-linux-android i686-linux-android

if [ -z "$ANDROID_NDK_HOME" ]; then
    echo "ANDROID_NDK_HOME not set. Trying to find NDK..."
    if [ -d "$ANDROID_HOME/ndk" ]; then
        export ANDROID_NDK_HOME="$ANDROID_HOME/ndk/$(ls -1 $ANDROID_HOME/ndk | tail -n 1)"
    fi
fi

if [ -z "$ANDROID_NDK_HOME" ]; then
    echo "Error: ANDROID_NDK_HOME not found"
    exit 1
fi

export CC_aarch64_linux_android="$ANDROID_NDK_HOME/toolchains/llvm/prebuilt/linux-x86_64/bin/aarch64-linux-android26-clang"
export CC_armv7_linux_androideabi="$ANDROID_NDK_HOME/toolchains/llvm/prebuilt/linux-x86_64/bin/armv7a-linux-androideabi26-clang"
export CC_x86_64_linux_android="$ANDROID_NDK_HOME/toolchains/llvm/prebuilt/linux-x86_64/bin/x86_64-linux-android26-clang"
export CC_i686_linux_android="$ANDROID_NDK_HOME/toolchains/llvm/prebuilt/linux-x86_64/bin/i686-linux-android26-clang"

echo "Building for arm64-v8a..."
cargo build --release --target aarch64-linux-android
mkdir -p ../jniLibs/arm64-v8a
cp target/aarch64-linux-android/release/libbancoseguro_native.so ../jniLibs/arm64-v8a/

echo "Building for armeabi-v7a..."
cargo build --release --target armv7-linux-androideabi
mkdir -p ../jniLibs/armeabi-v7a
cp target/armv7-linux-androideabi/release/libbancoseguro_native.so ../jniLibs/armeabi-v7a/

echo "Building for x86_64..."
cargo build --release --target x86_64-linux-android
mkdir -p ../jniLibs/x86_64
cp target/x86_64-linux-android/release/libbancoseguro_native.so ../jniLibs/x86_64/

echo "Building for x86..."
cargo build --release --target i686-linux-android
mkdir -p ../jniLibs/x86
cp target/i686-linux-android/release/libbancoseguro_native.so ../jniLibs/x86/

echo "Build complete!"
