# For more information about using CMake with Android Studio, read the
# documentation: https://d.android.com/studio/projects/add-native-code.html

# Sets the minimum version of CMake required to build the native library.
cmake_minimum_required(VERSION 3.4.1)

# 定义项目名
project("VideoCodec")

# 定义根路径
set(ROOT_PATH /Users/wangzhiguo/Downloads/Learns/Guo_Company_Svn/GitLab/3TClient)
set(LOCAL_SRC_ROOT_PATH ${ROOT_PATH}/SDK/Client/ClientController/H264EncoderDecoder)
set(LOCAL_FFMPEG_ROOT_PATH ${ROOT_PATH}/SDK/Client/android/ffmpeg_for_enc)
set(LOCAL_X264_ROOT_PATH ${ROOT_PATH}/SDK/Client/android/x264)
set(LOCAL_LIBYUV_ROOT_PATH ${ROOT_PATH}/Lib/libyuv)

# 设置普通变量
# 设置ndk路径
set(CMAKE_ANDROID_NDK /Users/wangzhiguo/Downloads/Learns/Developer-Tools/ndk-versions/android-ndk-r14b)
# 设置输出目录，这个输出的so是带符号表的，不带的还不知道怎么设置
set(CMAKE_LIBRARY_OUTPUT_DIRECTORY /Users/wangzhiguo/Downloads/Learns/MyGithubs/Android/MyCodecProject/app/libs/armeabi-v7a)

# 为 c++ 编译器设置编译选项，只能设置一次，不能重复设置，后者会覆盖前者
set(CMAKE_C_FLAGS -D__STDC_CONSTANT_MACROS)
# 设置多个参数需要用双引号，不然参数之间没空格
set(CMAKE_CXX_FLAGS "-Wdeprecated -std=c++11 -frtti -Os -fexceptions -w")
#-frtti -fexceptions -Os -std=c++11
message(CMAKE_CXX_FLAGS "CMAKE_CXX_FLAGS ---- ${CMAKE_CXX_FLAGS}")

# 将指定目录中的头文件包含到查找路径中
include_directories("${LOCAL_SRC_ROOT_PATH}/VideoEncoder")
include_directories("${LOCAL_SRC_ROOT_PATH}/VideoDecoder")
include_directories("${LOCAL_SRC_ROOT_PATH}/../AndroidJni")
include_directories("${LOCAL_LIBYUV_ROOT_PATH}/include")
include_directories("${LOCAL_FFMPEG_ROOT_PATH}/include/armeabi-v7a")
include_directories("${LOCAL_X264_ROOT_PATH}/include/armeabi-v7a")

#查找当前目录所有源文件 并将名称保存到 DIR_SRCS 变量，不能查找子目录
aux_source_directory(${LOCAL_SRC_ROOT_PATH}/VideoEncoder VIDEO_CODEC_SRC)
aux_source_directory(${LOCAL_SRC_ROOT_PATH}/VideoDecoder VIDEO_CODEC_SRC)

add_library( # Sets the name of the library.
        codec_ttt
        SHARED
        ${VIDEO_CODEC_SRC}
        ${LOCAL_SRC_ROOT_PATH}/../AndroidJni/jni_helpers.cc
        ${LOCAL_SRC_ROOT_PATH}/OnLoad.cpp)

# 添加 ffmpeg 库
add_library(ffmpeg-avcodec STATIC IMPORTED)
set_target_properties(
        ffmpeg-avcodec
        PROPERTIES
        IMPORTED_LOCATION
        ${LOCAL_FFMPEG_ROOT_PATH}/lib/libavcodec/armeabi-v7a/libavcodec.a)

#add_library(ffmpeg-swresample STATIC IMPORTED)
#set_target_properties(
#        ffmpeg-swresample
#        PROPERTIES
#        IMPORTED_LOCATION
#        ${LOCAL_FFMPEG_ROOT_PATH}/lib/libswresample/armeabi-v7a/libswresample.a)

add_library(ffmpeg-avutil STATIC IMPORTED)
set_target_properties(
        ffmpeg-avutil
        PROPERTIES
        IMPORTED_LOCATION
        ${LOCAL_FFMPEG_ROOT_PATH}/lib/libavutil/armeabi-v7a/libavutil.a)

add_library(ffmpeg-avformat STATIC IMPORTED)
set_target_properties(
        ffmpeg-avformat
        PROPERTIES
        IMPORTED_LOCATION
        ${LOCAL_FFMPEG_ROOT_PATH}/lib/libavformat/armeabi-v7a/libavformat.a)

add_library(ffmpeg-swscale STATIC IMPORTED)
set_target_properties(
        ffmpeg-swscale
        PROPERTIES
        IMPORTED_LOCATION
        ${LOCAL_FFMPEG_ROOT_PATH}/lib/libswscale/armeabi-v7a/libswscale.a)

# 添加 x264 库
add_library(x264-lib STATIC IMPORTED)
set_target_properties(
        x264-lib
        PROPERTIES
        IMPORTED_LOCATION
        ${LOCAL_X264_ROOT_PATH}/lib/armeabi-v7a/libx264.a)

# 添加 yuv 库
add_library(yuv-lib SHARED IMPORTED)
set_target_properties(
        yuv-lib
        PROPERTIES
        IMPORTED_LOCATION
        ${LOCAL_LIBYUV_ROOT_PATH}/libs/armeabi-v7a/libyuv_ttt.so)


find_library(log-lib log)
find_library(GLESv2-lib GLESv2)
find_library(android-lib android)

target_link_libraries( # Specifies the target library.
        codec_ttt
        ${log-lib}
        ${GLESv2-lib}
        ${android-lib}
        ffmpeg-avcodec
#        ffmpeg-swresample
        ffmpeg-avutil
        ffmpeg-avformat
        ffmpeg-swscale
        x264-lib
        yuv-lib)