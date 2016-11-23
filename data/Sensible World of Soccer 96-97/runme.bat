@echo off
echo Thank you for downloading from Home of the Underdogs!
echo Are these files (swos.*, runme.bat) in the folder that you
echo would like to use as the unRAR folder?
CHOICE
IF ERRORLEVEL 2 GOTO lastline
call swos.exe
cls
echo Installation complete!
:lastline
pause