@echo off
SET JAVA_HOME=C:\Program Files\Java\jdk-22
SET MAVEN_HOME=C:\maven
SET PATH=%JAVA_HOME%\bin;%MAVEN_HOME%\bin;%PATH%
SET MAVEN_PROJECTBASEDIR=%~dp0

"%MAVEN_HOME%\bin\mvn.cmd" %*
