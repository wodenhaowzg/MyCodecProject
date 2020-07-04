# For more information about using CMake with Android Studio, read the
# documentation: https://d.android.com/studio/projects/add-native-code.html

# Sets the minimum version of CMake required to build the native library.
cmake_minimum_required(VERSION 3.4.1)

# 设置输出目录
set(CMAKE_LIBRARY_OUTPUT_DIRECTORY /Users/wangzhiguo/Downloads/Learns/Projects/Owner/MyCXXApplication/app/libs/armeabi-v7a/)

add_compile_options(-frtti)
add_compile_options(-fexceptions)
set(CMAKE_CXX_FLAGS "-std=c++11")

include_directories("${PROJECT_SOURCE_DIR}/localincludes")

#查找当前目录所有源文件 并将名称保存到 DIR_SRCS 变量，不能查找子目录
aux_source_directory(. NATIVE_DIR_SRCS)

add_library( # Sets the name of the library.
        native-lib

        # Sets the library as a shared library.
        SHARED

        # Provides a relative path to your source file(s).
        ${NATIVE_DIR_SRCS})

find_library( # Sets the name of the path variable.
        log-lib

        # Specifies the name of the NDK library that
        # you want CMake to locate.
        log)

find_library( # Sets the name of the path variable.
        mediandk-lib

        # Specifies the name of the NDK library that
        # you want CMake to locate.
        mediandk)


# Specifies libraries CMake should link to your target library. You
# can link multiple libraries, such as libraries you define in this
# build script, prebuilt third-party libraries, or system libraries.

target_link_libraries( # Specifies the target library.
        native-lib
        ${log-lib}
        ${mediandk-lib})

#set(Libyuv "/Users/wangzhiguo/Downloads/Learns/Projects/libyuv_commit_7e7f229af9a3a85b5a82eb343fbcfef929449a07")
#include_directories("${Libyuv}/include")
#link_directories(/${Libyuv}/libs/armeabi-v7a)
#add_library(yuv-lib SHARED IMPORTED)
#set_target_properties(yuv-lib PROPERTIES IMPORTED_LOCATION
#        /Users/wangzhiguo/Downloads/Learns/Projects/libyuv_commit_7e7f229af9a3a85b5a82eb343fbcfef929449a07/libs/armeabi-v7a/libyuv_ttt.so)