cmake_minimum_required(VERSION 3.17)
project(untitled3)

set(CMAKE_CXX_STANDARD 20)

add_executable(${PROJECT_NAME} src/main.cpp src/glad.c)

find_package(OpenGL REQUIRED COMPONENTS OpenGL)
include_directories( ${OPENGL_INCLUDE_DIRS} )
add_dependencies(${PROJECT_NAME} OpenGL::GL)

add_subdirectory (${CMAKE_CURRENT_SOURCE_DIR}/Libraries/glm)
target_link_libraries(${PROJECT_NAME} glm::glm)

include_directories(${CMAKE_CURRENT_SOURCE_DIR}/glad)

include_directories(${CMAKE_CURRENT_SOURCE_DIR}/Libraries/glm)
include_directories(${CMAKE_CURRENT_SOURCE_DIR}/Libraries/glfw-3.3.2.bin.WIN64/include)
include_directories(${CMAKE_CURRENT_SOURCE_DIR}/Libraries/stb)

#target_link_libraries(${PROJECT_NAME} ${CMAKE_CURRENT_SOURCE_DIR}/Libraries/glfw-3.3.2.bin.WIN64/lib-mingw-w64/libglfw3dll.a)
target_link_libraries(${PROJECT_NAME} ${CMAKE_CURRENT_SOURCE_DIR}/Libraries/glfw-3.3.2.bin.WIN64/lib-mingw-w64/libglfw3.a)
target_link_libraries(${PROJECT_NAME} ${CMAKE_CURRENT_SOURCE_DIR}/glad/glad.h)
set(CMAKE_INCLUDE_CURRENT_DIR ON)