cmake_minimum_required(VERSION 3.25)
project(launcher)

set(CMAKE_CXX_STANDARD 23)
set(CMAKE_CXX_FLAGS "${CMAKE_CXX_FLAGS} -static-libstdc++ -static-libgcc")

add_executable(launcher main.cpp)

if(WIN32)
    # Link against libmingw32.a
    target_link_libraries(launcher PRIVATE mingw32)
endif()
