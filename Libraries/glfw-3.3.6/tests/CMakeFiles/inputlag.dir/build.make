# CMAKE generated file: DO NOT EDIT!
# Generated by "Unix Makefiles" Generator, CMake Version 3.22

# Delete rule output on recipe failure.
.DELETE_ON_ERROR:

#=============================================================================
# Special targets provided by cmake.

# Disable implicit rules so canonical targets will work.
.SUFFIXES:

# Disable VCS-based implicit rules.
% : %,v

# Disable VCS-based implicit rules.
% : RCS/%

# Disable VCS-based implicit rules.
% : RCS/%,v

# Disable VCS-based implicit rules.
% : SCCS/s.%

# Disable VCS-based implicit rules.
% : s.%

.SUFFIXES: .hpux_make_needs_suffix_list

# Command-line flag to silence nested $(MAKE).
$(VERBOSE)MAKESILENT = -s

#Suppress display of executed commands.
$(VERBOSE).SILENT:

# A target that is always out of date.
cmake_force:
.PHONY : cmake_force

#=============================================================================
# Set environment variables for the build.

# The shell in which to execute make rules.
SHELL = /bin/sh

# The CMake executable.
CMAKE_COMMAND = /snap/cmake/1000/bin/cmake

# The command to remove a file.
RM = /snap/cmake/1000/bin/cmake -E rm -f

# Escaping for special characters.
EQUALS = =

# The top-level source directory on which CMake was run.
CMAKE_SOURCE_DIR = /home/jh/repos/RacingmaybeCpp

# The top-level build directory on which CMake was run.
CMAKE_BINARY_DIR = /home/jh/repos/RacingmaybeCpp

# Include any dependencies generated for this target.
include Libraries/glfw-3.3.6/tests/CMakeFiles/inputlag.dir/depend.make
# Include any dependencies generated by the compiler for this target.
include Libraries/glfw-3.3.6/tests/CMakeFiles/inputlag.dir/compiler_depend.make

# Include the progress variables for this target.
include Libraries/glfw-3.3.6/tests/CMakeFiles/inputlag.dir/progress.make

# Include the compile flags for this target's objects.
include Libraries/glfw-3.3.6/tests/CMakeFiles/inputlag.dir/flags.make

Libraries/glfw-3.3.6/tests/CMakeFiles/inputlag.dir/inputlag.c.o: Libraries/glfw-3.3.6/tests/CMakeFiles/inputlag.dir/flags.make
Libraries/glfw-3.3.6/tests/CMakeFiles/inputlag.dir/inputlag.c.o: Libraries/glfw-3.3.6/tests/inputlag.c
Libraries/glfw-3.3.6/tests/CMakeFiles/inputlag.dir/inputlag.c.o: Libraries/glfw-3.3.6/tests/CMakeFiles/inputlag.dir/compiler_depend.ts
	@$(CMAKE_COMMAND) -E cmake_echo_color --switch=$(COLOR) --green --progress-dir=/home/jh/repos/RacingmaybeCpp/CMakeFiles --progress-num=$(CMAKE_PROGRESS_1) "Building C object Libraries/glfw-3.3.6/tests/CMakeFiles/inputlag.dir/inputlag.c.o"
	cd /home/jh/repos/RacingmaybeCpp/Libraries/glfw-3.3.6/tests && /usr/bin/cc $(C_DEFINES) $(C_INCLUDES) $(C_FLAGS) -MD -MT Libraries/glfw-3.3.6/tests/CMakeFiles/inputlag.dir/inputlag.c.o -MF CMakeFiles/inputlag.dir/inputlag.c.o.d -o CMakeFiles/inputlag.dir/inputlag.c.o -c /home/jh/repos/RacingmaybeCpp/Libraries/glfw-3.3.6/tests/inputlag.c

Libraries/glfw-3.3.6/tests/CMakeFiles/inputlag.dir/inputlag.c.i: cmake_force
	@$(CMAKE_COMMAND) -E cmake_echo_color --switch=$(COLOR) --green "Preprocessing C source to CMakeFiles/inputlag.dir/inputlag.c.i"
	cd /home/jh/repos/RacingmaybeCpp/Libraries/glfw-3.3.6/tests && /usr/bin/cc $(C_DEFINES) $(C_INCLUDES) $(C_FLAGS) -E /home/jh/repos/RacingmaybeCpp/Libraries/glfw-3.3.6/tests/inputlag.c > CMakeFiles/inputlag.dir/inputlag.c.i

Libraries/glfw-3.3.6/tests/CMakeFiles/inputlag.dir/inputlag.c.s: cmake_force
	@$(CMAKE_COMMAND) -E cmake_echo_color --switch=$(COLOR) --green "Compiling C source to assembly CMakeFiles/inputlag.dir/inputlag.c.s"
	cd /home/jh/repos/RacingmaybeCpp/Libraries/glfw-3.3.6/tests && /usr/bin/cc $(C_DEFINES) $(C_INCLUDES) $(C_FLAGS) -S /home/jh/repos/RacingmaybeCpp/Libraries/glfw-3.3.6/tests/inputlag.c -o CMakeFiles/inputlag.dir/inputlag.c.s

Libraries/glfw-3.3.6/tests/CMakeFiles/inputlag.dir/__/deps/getopt.c.o: Libraries/glfw-3.3.6/tests/CMakeFiles/inputlag.dir/flags.make
Libraries/glfw-3.3.6/tests/CMakeFiles/inputlag.dir/__/deps/getopt.c.o: Libraries/glfw-3.3.6/deps/getopt.c
Libraries/glfw-3.3.6/tests/CMakeFiles/inputlag.dir/__/deps/getopt.c.o: Libraries/glfw-3.3.6/tests/CMakeFiles/inputlag.dir/compiler_depend.ts
	@$(CMAKE_COMMAND) -E cmake_echo_color --switch=$(COLOR) --green --progress-dir=/home/jh/repos/RacingmaybeCpp/CMakeFiles --progress-num=$(CMAKE_PROGRESS_2) "Building C object Libraries/glfw-3.3.6/tests/CMakeFiles/inputlag.dir/__/deps/getopt.c.o"
	cd /home/jh/repos/RacingmaybeCpp/Libraries/glfw-3.3.6/tests && /usr/bin/cc $(C_DEFINES) $(C_INCLUDES) $(C_FLAGS) -MD -MT Libraries/glfw-3.3.6/tests/CMakeFiles/inputlag.dir/__/deps/getopt.c.o -MF CMakeFiles/inputlag.dir/__/deps/getopt.c.o.d -o CMakeFiles/inputlag.dir/__/deps/getopt.c.o -c /home/jh/repos/RacingmaybeCpp/Libraries/glfw-3.3.6/deps/getopt.c

Libraries/glfw-3.3.6/tests/CMakeFiles/inputlag.dir/__/deps/getopt.c.i: cmake_force
	@$(CMAKE_COMMAND) -E cmake_echo_color --switch=$(COLOR) --green "Preprocessing C source to CMakeFiles/inputlag.dir/__/deps/getopt.c.i"
	cd /home/jh/repos/RacingmaybeCpp/Libraries/glfw-3.3.6/tests && /usr/bin/cc $(C_DEFINES) $(C_INCLUDES) $(C_FLAGS) -E /home/jh/repos/RacingmaybeCpp/Libraries/glfw-3.3.6/deps/getopt.c > CMakeFiles/inputlag.dir/__/deps/getopt.c.i

Libraries/glfw-3.3.6/tests/CMakeFiles/inputlag.dir/__/deps/getopt.c.s: cmake_force
	@$(CMAKE_COMMAND) -E cmake_echo_color --switch=$(COLOR) --green "Compiling C source to assembly CMakeFiles/inputlag.dir/__/deps/getopt.c.s"
	cd /home/jh/repos/RacingmaybeCpp/Libraries/glfw-3.3.6/tests && /usr/bin/cc $(C_DEFINES) $(C_INCLUDES) $(C_FLAGS) -S /home/jh/repos/RacingmaybeCpp/Libraries/glfw-3.3.6/deps/getopt.c -o CMakeFiles/inputlag.dir/__/deps/getopt.c.s

Libraries/glfw-3.3.6/tests/CMakeFiles/inputlag.dir/__/deps/glad_gl.c.o: Libraries/glfw-3.3.6/tests/CMakeFiles/inputlag.dir/flags.make
Libraries/glfw-3.3.6/tests/CMakeFiles/inputlag.dir/__/deps/glad_gl.c.o: Libraries/glfw-3.3.6/deps/glad_gl.c
Libraries/glfw-3.3.6/tests/CMakeFiles/inputlag.dir/__/deps/glad_gl.c.o: Libraries/glfw-3.3.6/tests/CMakeFiles/inputlag.dir/compiler_depend.ts
	@$(CMAKE_COMMAND) -E cmake_echo_color --switch=$(COLOR) --green --progress-dir=/home/jh/repos/RacingmaybeCpp/CMakeFiles --progress-num=$(CMAKE_PROGRESS_3) "Building C object Libraries/glfw-3.3.6/tests/CMakeFiles/inputlag.dir/__/deps/glad_gl.c.o"
	cd /home/jh/repos/RacingmaybeCpp/Libraries/glfw-3.3.6/tests && /usr/bin/cc $(C_DEFINES) $(C_INCLUDES) $(C_FLAGS) -MD -MT Libraries/glfw-3.3.6/tests/CMakeFiles/inputlag.dir/__/deps/glad_gl.c.o -MF CMakeFiles/inputlag.dir/__/deps/glad_gl.c.o.d -o CMakeFiles/inputlag.dir/__/deps/glad_gl.c.o -c /home/jh/repos/RacingmaybeCpp/Libraries/glfw-3.3.6/deps/glad_gl.c

Libraries/glfw-3.3.6/tests/CMakeFiles/inputlag.dir/__/deps/glad_gl.c.i: cmake_force
	@$(CMAKE_COMMAND) -E cmake_echo_color --switch=$(COLOR) --green "Preprocessing C source to CMakeFiles/inputlag.dir/__/deps/glad_gl.c.i"
	cd /home/jh/repos/RacingmaybeCpp/Libraries/glfw-3.3.6/tests && /usr/bin/cc $(C_DEFINES) $(C_INCLUDES) $(C_FLAGS) -E /home/jh/repos/RacingmaybeCpp/Libraries/glfw-3.3.6/deps/glad_gl.c > CMakeFiles/inputlag.dir/__/deps/glad_gl.c.i

Libraries/glfw-3.3.6/tests/CMakeFiles/inputlag.dir/__/deps/glad_gl.c.s: cmake_force
	@$(CMAKE_COMMAND) -E cmake_echo_color --switch=$(COLOR) --green "Compiling C source to assembly CMakeFiles/inputlag.dir/__/deps/glad_gl.c.s"
	cd /home/jh/repos/RacingmaybeCpp/Libraries/glfw-3.3.6/tests && /usr/bin/cc $(C_DEFINES) $(C_INCLUDES) $(C_FLAGS) -S /home/jh/repos/RacingmaybeCpp/Libraries/glfw-3.3.6/deps/glad_gl.c -o CMakeFiles/inputlag.dir/__/deps/glad_gl.c.s

# Object files for target inputlag
inputlag_OBJECTS = \
"CMakeFiles/inputlag.dir/inputlag.c.o" \
"CMakeFiles/inputlag.dir/__/deps/getopt.c.o" \
"CMakeFiles/inputlag.dir/__/deps/glad_gl.c.o"

# External object files for target inputlag
inputlag_EXTERNAL_OBJECTS =

Libraries/glfw-3.3.6/tests/inputlag: Libraries/glfw-3.3.6/tests/CMakeFiles/inputlag.dir/inputlag.c.o
Libraries/glfw-3.3.6/tests/inputlag: Libraries/glfw-3.3.6/tests/CMakeFiles/inputlag.dir/__/deps/getopt.c.o
Libraries/glfw-3.3.6/tests/inputlag: Libraries/glfw-3.3.6/tests/CMakeFiles/inputlag.dir/__/deps/glad_gl.c.o
Libraries/glfw-3.3.6/tests/inputlag: Libraries/glfw-3.3.6/tests/CMakeFiles/inputlag.dir/build.make
Libraries/glfw-3.3.6/tests/inputlag: Libraries/glfw-3.3.6/src/libglfw3.a
Libraries/glfw-3.3.6/tests/inputlag: /usr/lib/x86_64-linux-gnu/libm.so
Libraries/glfw-3.3.6/tests/inputlag: /usr/lib/x86_64-linux-gnu/librt.a
Libraries/glfw-3.3.6/tests/inputlag: /usr/lib/x86_64-linux-gnu/libm.so
Libraries/glfw-3.3.6/tests/inputlag: /usr/lib/x86_64-linux-gnu/libX11.so
Libraries/glfw-3.3.6/tests/inputlag: Libraries/glfw-3.3.6/tests/CMakeFiles/inputlag.dir/link.txt
	@$(CMAKE_COMMAND) -E cmake_echo_color --switch=$(COLOR) --green --bold --progress-dir=/home/jh/repos/RacingmaybeCpp/CMakeFiles --progress-num=$(CMAKE_PROGRESS_4) "Linking C executable inputlag"
	cd /home/jh/repos/RacingmaybeCpp/Libraries/glfw-3.3.6/tests && $(CMAKE_COMMAND) -E cmake_link_script CMakeFiles/inputlag.dir/link.txt --verbose=$(VERBOSE)

# Rule to build all files generated by this target.
Libraries/glfw-3.3.6/tests/CMakeFiles/inputlag.dir/build: Libraries/glfw-3.3.6/tests/inputlag
.PHONY : Libraries/glfw-3.3.6/tests/CMakeFiles/inputlag.dir/build

Libraries/glfw-3.3.6/tests/CMakeFiles/inputlag.dir/clean:
	cd /home/jh/repos/RacingmaybeCpp/Libraries/glfw-3.3.6/tests && $(CMAKE_COMMAND) -P CMakeFiles/inputlag.dir/cmake_clean.cmake
.PHONY : Libraries/glfw-3.3.6/tests/CMakeFiles/inputlag.dir/clean

Libraries/glfw-3.3.6/tests/CMakeFiles/inputlag.dir/depend:
	cd /home/jh/repos/RacingmaybeCpp && $(CMAKE_COMMAND) -E cmake_depends "Unix Makefiles" /home/jh/repos/RacingmaybeCpp /home/jh/repos/RacingmaybeCpp/Libraries/glfw-3.3.6/tests /home/jh/repos/RacingmaybeCpp /home/jh/repos/RacingmaybeCpp/Libraries/glfw-3.3.6/tests /home/jh/repos/RacingmaybeCpp/Libraries/glfw-3.3.6/tests/CMakeFiles/inputlag.dir/DependInfo.cmake --color=$(COLOR)
.PHONY : Libraries/glfw-3.3.6/tests/CMakeFiles/inputlag.dir/depend

