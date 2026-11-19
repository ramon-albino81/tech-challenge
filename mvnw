#!/bin/sh
set -eu
MAVEN_PROJECTBASEDIR=$(CDPATH= cd -P "$(dirname "$0")" && pwd)
JAVACMD=java
if [ -n "${JAVA_HOME-}" ]; then
	JAVACMD="$JAVA_HOME/bin/java"
fi
exec "$JAVACMD" "-Dmaven.multiModuleProjectDirectory=$MAVEN_PROJECTBASEDIR" -classpath "$MAVEN_PROJECTBASEDIR/.mvn/wrapper/maven-wrapper.jar" org.apache.maven.wrapper.MavenWrapperMain "$@"
