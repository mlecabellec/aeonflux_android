# CMakeLists.txt for whisper.cpp JNI Android Shared Library
cmake_minimum_required(VERSION 3.22.1)

project("whisper" C CXX)

set(CMAKE_C_STANDARD 11)
set(CMAKE_CXX_STANDARD 17)

# Enable 16KB page alignment support for Android 15+
set(CMAKE_SHARED_LINKER_FLAGS "${CMAKE_SHARED_LINKER_FLAGS} -Wl,-z,max-page-size=16384")

# Enable aggressive C/C++ compiler optimizations for ggml matrix math
set(CMAKE_C_FLAGS_RELEASE "-O3 -ffast-math")
set(CMAKE_CXX_FLAGS_RELEASE "-O3 -ffast-math")
set(CMAKE_C_FLAGS "${CMAKE_C_FLAGS} -O3 -ffast-math")
set(CMAKE_CXX_FLAGS "${CMAKE_CXX_FLAGS} -O3 -ffast-math")

if (${ANDROID_ABI} STREQUAL "arm64-v8a")
    set(CMAKE_C_FLAGS "${CMAKE_C_FLAGS} -march=armv8-a+fp16+dotprod")
    set(CMAKE_CXX_FLAGS "${CMAKE_CXX_FLAGS} -march=armv8-a+fp16+dotprod")
elseif (${ANDROID_ABI} STREQUAL "x86_64")
    set(CMAKE_C_FLAGS "${CMAKE_C_FLAGS} -msse3 -mssse3 -mavx -mfma")
    set(CMAKE_CXX_FLAGS "${CMAKE_CXX_FLAGS} -msse3 -mssse3 -mavx -mfma")
endif()

# Compile whisper.cpp and JNI bridge
add_library(whisper SHARED
    whisper-jni.cpp
    whisper.cpp
    ggml.c
    ggml-alloc.c
    ggml-backend.c
    ggml-quants.c
)

target_include_directories(whisper PRIVATE ${CMAKE_CURRENT_SOURCE_DIR})

find_library(log-lib log)
find_library(android-lib android)

target_link_libraries(whisper
    ${log-lib}
    ${android-lib}
)
