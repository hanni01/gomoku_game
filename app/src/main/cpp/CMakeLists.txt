
cmake_minimum_required(VERSION 3.22.1)


project("jni_mosysf")


add_library(${CMAKE_PROJECT_NAME} SHARED
        s4210_keypad.cpp s4210_lcd.cpp)

target_link_libraries(${CMAKE_PROJECT_NAME}
        android
        log)