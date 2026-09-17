#!/bin/sh

##############################################################################
# Gradle start up script for POSIX generated for OpenDialer
##############################################################################

# Attempt to set APP_HOME
APP_HOME=$(cd "`dirname \"$0\"`" > /dev/null && pwd -P)
APP_NAME="Gradle"
APP_BASE_NAME=`basename "$0"`

# Download wrapper jar if not present
WRAPPER_JAR="$APP_HOME/gradle/wrapper/gradle-wrapper.jar"
if [ ! -f "$WRAPPER_JAR" ]; then
    mkdir -p "$APP_HOME/gradle/wrapper"
    echo "Downloading Gradle wrapper jar..."
    curl -sLo "$WRAPPER_JAR" "https://raw.githubusercontent.com/gradle/gradle/v8.6.0/gradle/wrapper/gradle-wrapper.jar" || \
    wget -qO "$WRAPPER_JAR" "https://raw.githubusercontent.com/gradle/gradle/v8.6.0/gradle/wrapper/gradle-wrapper.jar" || true
fi

# Determine the Java command to use to start the JVM.
if [ -n "$JAVA_HOME" ] ; then
    if [ -x "$JAVA_HOME/jre/sh/java" ] ; then
        JAVACMD="$JAVA_HOME/jre/sh/java"
    else
        JAVACMD="$JAVA_HOME/bin/java"
    fi
    if [ ! -x "$JAVACMD" ] ; then
        die "ERROR: JAVA_HOME is set to an invalid directory: $JAVA_HOME"
    fi
else
    JAVACMD="java"
    which java >/dev/null 2>&1 || die "ERROR: JAVA_HOME is not set and no 'java' command could be found in your PATH."
fi

# Increase the maximum file descriptors if possible
MAX_FD="maximum"
warn () {
    echo "$*"
}
die () {
    echo
    echo "$*"
    echo
    exit 1
}

# Execute Gradle
exec "$JAVACMD" "-Dorg.gradle.appname=$APP_BASE_NAME" -classpath "$WRAPPER_JAR" org.gradle.wrapper.GradleWrapperMain "$@"
