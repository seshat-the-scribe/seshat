@echo off
set SBTLAUNCHER="bin\sbt-launch.jar"
rem set SBT_OPTS=-Xmx1024m -XX:+CMSClassUnloadingEnabled -XX:MaxPermSize=384m -Dfile.encoding=UTF8
java -Xmx2048m -XX:+CMSClassUnloadingEnabled -XX:+HeapDumpOnOutOfMemoryError -XX:MaxPermSize=512m -Dfile.encoding=UTF8  -jar %SBTLAUNCHER% %*
