cmake_minimum_required(VERSION 3.16)
project(Racingmaybe)

set(CMAKE_CXX_STANDARD 20)
#add_compile_definitions(__cplusplus)

add_executable(${PROJECT_NAME} 
src/main/Game.cpp 
src/main/Game.h 
src/gl.c 
src/engine/io/Window.cpp 
src/engine/io/Window.h 
src/engine/utils/Timer.h 
src/engine/utils/Timer.cpp 
src/scenes/SceneHandler.cpp 
src/scenes/SceneHandler.h 
src/scenes/adt/ISceneManipulator.h 
src/scenes/SceneEnvir.cpp 
src/scenes/SceneEnvir.h 
src/scenes/Scenes.h 
src/engine/graphics/Renderer.cpp 
src/engine/graphics/Renderer.h 
src/engine/graphics/Camera.cpp 
src/engine/graphics/Camera.h 
#src/scenes/adt/NkContext.cpp src/scenes/adt/NkContext.h 
src/audio/AudioRemote.h 
src/engine/graphics/GameObject.cpp 
src/engine/graphics/GameObject.h
)

find_package(OpenGL REQUIRED COMPONENTS OpenGL)
include_directories( ${OPENGL_INCLUDE_DIRS} )
add_dependencies(${PROJECT_NAME} OpenGL::GL)
target_link_libraries(${PROJECT_NAME} OpenGL::GL)

add_subdirectory (${CMAKE_CURRENT_SOURCE_DIR}/Libraries/glm)
target_link_libraries(${PROJECT_NAME} glm::glm)

add_subdirectory (${CMAKE_CURRENT_SOURCE_DIR}/Libraries/glfw-3.3.6)
include_directories(${CMAKE_CURRENT_SOURCE_DIR}/Libraries/glfw-3.3.6/include)
target_link_libraries(${PROJECT_NAME} glfw)

include_directories(${CMAKE_CURRENT_SOURCE_DIR}/Libraries/glm)
include_directories(${CMAKE_CURRENT_SOURCE_DIR}/Libraries/stb)
include_directories(${CMAKE_CURRENT_SOURCE_DIR}/Libraries)

#target_link_libraries(${PROJECT_NAME} ${CMAKE_CURRENT_SOURCE_DIR}/Libraries/glad/gl.h)

set(CMAKE_INCLUDE_CURRENT_DIR ON)

set(CMAKE_AUTOGEN_VERBOSE ON)
set(CMAKE_VERBOSE_MAKEFILE ON)
