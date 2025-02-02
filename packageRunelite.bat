::----------------------------------------------------------------------
:: Freds Runelite startup script.
::----------------------------------------------------------------------
SET CURDIR=%cd%
echo %CURDIR%
cd ..\runelite
mvn package --projects runelite-client -DskipTests

echo %CURDIR%
cd %CURDIR%
rem cd C:\Users\natru\Dev\runelite\runelite-client\target\

rem java -jar client-%RUNELITE_VERSION%-shaded.jar 