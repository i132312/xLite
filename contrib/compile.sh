#! /bin/bash -e

while getopts t: o
do
    case "$o" in
        t)      target="${OPTARG}";;
        [?])    echo >&2 "Usage: $0 -t {android|host}"
        exit 1;;
    esac
done
shift $(($OPTIND-1))

case ${target} in
        android|host) ;;
        *)             echo >&2 "Invalid target. Usage: $0 -t {android|host}"
                       exit 1;;
esac;

BUILD_DIR=$(mktemp -d build-XXXXXXXXXX)
export BASE_DIR="$( cd "$( dirname $0 )" && pwd )"

# update submodules if necessary
if [ -e libasn1c ];
then
	(cd .. && git submodule init contrib/libasn1c && git submodule update contrib/libasn1c)
fi

if [ -e libosmocore ];
then
	(cd .. && git submodule init contrib/libosmocore && git submodule update contrib/libosmocore)
fi

if [ -e metagsm ];
then
	(cd .. && git submodule init contrib/metagsm && git submodule update contrib/metagsm)
fi

# set platform
MACH=$(uname -m)
KERN=$(uname -s)

case ${KERN} in
        Darwin) HOST="darwin-${MACH}";;
        Linux)  HOST="linux-${MACH}";;
        *)      echo "Unknown platform ${KERN}-${MACH}!"; exit 1;;
esac

echo "Building on ${HOST} for ${target}..."

cd ${BUILD_DIR}
OUTPUT_DIR=`pwd`

export MSD_DESTDIR="${OUTPUT_DIR}/out"

case ${target} in
	android)
		export MSD_CONFIGURE_OPTS="--host arm-linux-androideabi --prefix=${MSD_DESTDIR}"
		SYSROOT="${NDK_DIR}/platforms/android-19/arch-arm"
		export PATH=${PATH}:${NDK_DIR}/toolchains/arm-linux-androideabi-4.8/prebuilt/${HOST}/bin/
		export CC=arm-linux-androideabi-gcc
		export CFLAGS="--sysroot=${SYSROOT} -nostdlib"
		export CPPFLAGS="-I${NDK_DIR}/platforms/android-19/arch-arm/usr/include/"
		export LDFLAGS="--sysroot=${SYSROOT} -Wl,-rpath-link=${NDK_DIR}/platforms/android-19/arch-arm/usr/lib/,-L${NDK_DIR}/platforms/android-19/arch-arm/usr/lib/"
		export LIBS="-lc -lm"
		export METAGSM_MAKE_ARGS="-f Makefile.Android DESTDIR=${MSD_DESTDIR}/metagsm install"
		;;
	host)
		export MSD_CONFIGURE_OPTS="--prefix=${MSD_DESTDIR}"
		export EXTRA_CFLAGS="-I${MSD_DESTDIR}/include -I${MSD_DESTDIR}/include/asn1c"
		export EXTRA_LDFLAGS="-L${MSD_DESTDIR}/lib"
		export METAGSM_MAKE_ARGS="install"
		;;
	*)
		# Shouldn't happen
		echo "Invalid target \"${target}\""
		exit 1;
esac

mkdir -p ${MSD_DESTDIR}

for i in libosmocore libasn1c libosmo-asn1-rrc metagsm; do
    echo -n "Building $i..."
    cd $OUTPUT_DIR
    if ${BASE_DIR}/scripts/compile_$i.sh > $OUTPUT_DIR/$i.compile_log 2>&1;then
	echo OK
    else
	echo "Failed!"
	echo "Please view log file $OUTPUT_DIR/$i.compile_log"
	exit 1
    fi
done

if [ "x${target}" = "xandroid" ];
then
	# Install parser
	PARSER_DIR=${OUTPUT_DIR}/parser
	install -d ${PARSER_DIR}
	install -m 755 ${OUTPUT_DIR}/out/lib/libasn1c.so.0         ${PARSER_DIR}/libasn1c.so
	install -m 755 ${OUTPUT_DIR}/out/lib/libosmo-asn1-rrc.so.0 ${PARSER_DIR}/libosmo-asn1-rrc.so
	install -m 755 ${OUTPUT_DIR}/out/lib/libosmocore.so.5      ${PARSER_DIR}/libosmocore.so
	install -m 755 ${OUTPUT_DIR}/out/lib/libosmogsm.so.5       ${PARSER_DIR}/libosmogsm.so
	install -m 755 ${OUTPUT_DIR}/out/metagsm/diag_import       ${PARSER_DIR}/libdiag_import.so
	install -m 755 ${OUTPUT_DIR}/out/metagsm/libcompat.so      ${PARSER_DIR}/libcompat.so
	
	# Really dirty hack: The Android build system and package installer require 
	# all files in the native library dir to have a filename like libXXX.so. If
	# the file extension ends with .so.5, it will not be copied to the APK file. 
	# So the following line of perl patches all references so that the libraries
	# are found with a .so extension instead of .so.[digit]
	perl -i -pe 's/libasn1c\.so\.0/libasn1c.so\0\0/gs;s/libosmo-asn1-rrc\.so\.0/libosmo-asn1-rrc.so\0\0/gs;s/libosmocore\.so\.5/libosmocore.so\0\0/gs;s/libosmogsm\.so\.5/libosmogsm.so\0\0/gs' ${PARSER_DIR}/*.so
fi

ln -sf ${BUILD_DIR} ../build-${HOST}-${target}-latest
echo DONE
