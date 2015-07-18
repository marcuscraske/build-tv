#!/bin/bash

# ---------------------------------------------------------------------------------------------------------------------
# Compiles shared libraries required for interfacing with Raspberry Pi GPIO hardware from Java, using JNI.
# ---------------------------------------------------------------------------------------------------------------------
# Version:      1.0
# Author(s):    limpygnome <limpygnome@gmail.com>
# ---------------------------------------------------------------------------------------------------------------------

# *********************************************************************************************************************
# Configuration
# *********************************************************************************************************************

# This defines the JDK to use for JNI header files; automatically picks first dir using ls
JDK_PATH="/usr/lib/jvm"
JDK_DIR=$(ls "${JDK_PATH}" | head -n 1)
#JDK_DIR="jdk-8-oracle-arm-vfp-hflt"
JDK_FULL_PATH="${JDK_PATH}/${JDK_DIR}"

# The libs to include when building C files using GCC
GCC_INCLUDES="-I${JDK_FULL_PATH}/include -I${JDK_FULL_PATH}/include/linux"

# Relative dir names for input/output
OUTPUT="output"
SWIG_SRC="swig"
SWIG_OUT="${OUTPUT}/swig"
SWIG_OUT_JAVA="src/main/java/com/limpygnome/ws281x/lib"
SWIG_PACKAGE_NAME="com.limpygnome.ws281x.lib"
LIB_SRC="ws281x"
LIB_OUT="${OUTPUT}/ws281x"
LIB_NAME="ws2811.so"
WRAPPER_LIB_NAME="libws281x.so"


# *********************************************************************************************************************
# Functions
# *********************************************************************************************************************

function compileSrc
{
    SRC="${1}"
    OUT="${2}"

    gcc -shared -fPIC -w -o "${OUT}" -c "${SRC}" -I./ -I/usr/lib/jvm/java-7-openjdk-amd64/include
}

function programInstalled
(
    CMD="${1}"
    EXPECTED="${2}"
    ERROR="${3}"
    SUCCESS="${4}"

    OUTPUT=$(eval ${CMD})
    if [[ "${OUTPUT}" != *"${EXPECTED}"* ]]; then
        echo "${ERROR}"
        exit 1
    else
        echo "${SUCCESS}"
    fi
)

# *********************************************************************************************************************
# Main
# *********************************************************************************************************************

echo "NeoPixel ws281x Library Compiler"
echo "****************************************************"

# Check dependencies installed
set -e
programInstalled "swig -version" "SWIG Version" "Error - SWIG is not installed, cannot continue!" "Check - SWIG installed..."
programInstalled "gcc --version" "free software" "Error - GCC is not installed, cannot continue!" "Check - GCC installed..."
programInstalled "ar --version" "free software" "Error - AR is not installed, cannot continue!" "Check - AR installed..."
programInstalled "ranlib -v" "free software" "Error - ranlib is not installed, cannot continue!" "Check - ranlib installed..."
set +e



# Create all the required dirs
echo "Creating required dirs..."
mkdir -p "${SWIG_OUT}"
mkdir -p "${SWIG_OUT_JAVA}"
mkdir -p "${LIB_OUT}"


# Building swig wrapper
echo "Building JNI interface using SWIG..."

swig -java -outdir "${SWIG_OUT_JAVA}" -package "${SWIG_PACKAGE_NAME}" -o "${SWIG_OUT}/rpi_ws281x_wrap.c" "${SWIG_SRC}/rpi_ws281x.i"



# Compile library objects
echo "Compiling ws281x library objects..."

compileSrc "${LIB_SRC}/ws2811.c"        "${LIB_OUT}/ws2811.o"
compileSrc "${LIB_SRC}/pwm.c"           "${LIB_OUT}/pwm.o"
compileSrc "${LIB_SRC}/dma.c"           "${LIB_OUT}/dma.o"
compileSrc "${LIB_SRC}/board_info.c"    "${LIB_OUT}/board_info.o"
compileSrc "${LIB_SRC}/mailbox.c"       "${LIB_OUT}/mailbox.o"



# Compile library
echo "Compiling ws281x library..."
gcc -shared -o "${OUTPUT}/${LIB_NAME}" "${LIB_OUT}/ws2811.o" "${LIB_OUT}/pwm.o" "${LIB_OUT}/dma.o" "${LIB_OUT}/board_info.o" "${LIB_OUT}/mailbox.o"

echo "Creating archive..."
ar rc "${LIB_OUT}/libws2811.a" "${LIB_OUT}/ws2811.o" "${LIB_OUT}/pwm.o" "${LIB_OUT}/dma.o" "${LIB_OUT}/board_info.o" "${LIB_OUT}/mailbox.o"

echo "Indexing archive..."
ranlib "${LIB_OUT}/libws2811.a"



# Compile wrapper into object
echo "Compiling wrapper into object..."
gcc -pthread -fno-strict-aliasing -DNDEBUG -g -fwrapv -O2 -Wall -Wstrict-prototypes -fPIC ${GCC_INCLUDES} -Ilib/ -c "${SWIG_OUT}/rpi_ws281x_wrap.c" -o "${SWIG_OUT}/rpi_ws281x_wrap.o"



# Compile wrapper into shared lib
echo "Compiling wrapper into shared library..."
gcc -pthread -shared -Wl,-O1 -Wl,-Bsymbolic-functions -Wl,-z,relro "${SWIG_OUT}/rpi_ws281x_wrap.o" -L${LIB_OUT}/ -lws2811 -o "${OUTPUT}/${WRAPPER_LIB_NAME}"



echo "Done!"

