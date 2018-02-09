@echo off
SET JDK=C:\Program Files\Java\jdk1.5.0_19

"%JDK%\bin\javac.exe" -sourcepath . -d . -source 1.5 -target 1.5 CoverGenerator.java

"%JDK%\bin\java.exe" CoverGenerator

pause
