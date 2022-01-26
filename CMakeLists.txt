cmake_minimum_required(VERSION 3.16)
project(Racingmaybe)

set(CMAKE_CXX_STANDARD 20)
#add_compile_definitions(__cplusplus)

add_executable(${PROJECT_NAME} 
src/main.cpp 
src/main.h 
#src/gl.c 
src/window.cpp 
src/window.h 
src/timer.cpp 
src/timer.h 
src/scene_handler.cpp 
src/scene_handler.h 
"src/scene_data.cpp" 
"src/scene_data.h" 
 "src/audio.cpp" 
 "src/audio.h" "src/g_pipeline.h" "src/g_pipeline.cpp")

#find_package(OpenGL REQUIRED COMPONENTS OpenGL)
#include_directories( ${OPENGL_INCLUDE_DIRS} )
#add_dependencies(${PROJECT_NAME} OpenGL::GL)
#target_link_libraries(${PROJECT_NAME} OpenGL::GL)

find_package(Vulkan REQUIRED)
target_include_directories(${PROJECT_NAME} PUBLIC ${Vulkan_INCLUDE_DIRS})
target_link_libraries(${PROJECT_NAME} Vulkan::Vulkan)

add_subdirectory (${CMAKE_CURRENT_SOURCE_DIR}/Libraries/glm)
target_link_libraries(${PROJECT_NAME} glm::glm)

add_subdirectory (${CMAKE_CURRENT_SOURCE_DIR}/Libraries/glfw-3.3.6)
include_directories(${CMAKE_CURRENT_SOURCE_DIR}/Libraries/glfw-3.3.6/include)
target_link_libraries(${PROJECT_NAME} glfw)

#include_directories(${CMAKE_CURRENT_SOURCE_DIR}/Libraries/openal-soft-1.21.1/include)

add_subdirectory (${CMAKE_CURRENT_SOURCE_DIR}/Libraries/openal-soft-1.21.1)
target_link_libraries(${PROJECT_NAME} OpenAL)


include_directories(${CMAKE_CURRENT_SOURCE_DIR}/Libraries/glm)
include_directories(${CMAKE_CURRENT_SOURCE_DIR}/Libraries/stb)
include_directories(${CMAKE_CURRENT_SOURCE_DIR}/Libraries)

include_directories(${CMAKE_CURRENT_SOURCE_DIR}/src)


#target_link_libraries(${PROJECT_NAME} ${CMAKE_CURRENT_SOURCE_DIR}/Libraries/glad/gl.h)

set(CMAKE_INCLUDE_CURRENT_DIR ON)

set(CMAKE_AUTOGEN_VERBOSE ON)
set(CMAKE_VERBOSE_MAKEFILE ON)

#set(CMAKE_ARCHIVE_OUTPUT_DIRECTORY ${CMAKE_BINARY_DIR}/lib)
#set(CMAKE_LIBRARY_OUTPUT_DIRECTORY ${CMAKE_BINARY_DIR}/lib)
#set(CMAKE_RUNTIME_OUTPUT_DIRECTORY ${CMAKE_BINARY_DIR}/bin)

set_target_properties(${PROJECT_NAME}
    PROPERTIES
    RUNTIME_OUTPUT_DIRECTORY "${CMAKE_SOURCE_DIR}/bin"
)

set_property(TARGET ${PROJECT_NAME} PROPERTY VS_DEBUGGER_WORKING_DIRECTORY "${CMAKE_SOURCE_DIR}/bin")

#add_custom_target(copy-runtime-files ALL
#    COMMAND ${CMAKE_COMMAND} -E copy_directory ${CMAKE_SOURCE_DIR}/res ${CMAKE_BINARY_DIR}/res)