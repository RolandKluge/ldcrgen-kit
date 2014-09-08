@echo off
REM %~dp0 contains the current path of the script
@echo on
java -cp %~dp0\ldcrgen.jar edu.kit.iti.ldcrgen.Main %*
