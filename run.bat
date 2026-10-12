@echo off
setlocal

set "JAVAFX_LIB=lib\javafx-sdk-27\lib"
set "MYSQL_DRIVER=lib\mysql-connector-j-26.7.0.jar"

if not exist "%JAVAFX_LIB%" (
    echo No se encontro el SDK de JavaFX. Descomprimilo en lib\javafx-sdk-27\
    exit /b 1
)
if not exist "%MYSQL_DRIVER%" (
    echo No se encontro el driver MySQL: %MYSQL_DRIVER%
    exit /b 1
)

for /f "tokens=3" %%V in ('java -version 2^>^&1 ^| findstr /i /c:"version"') do set "JAVA_VERSION=%%~V"
if not defined JAVA_VERSION (
    echo No se pudo detectar la version de Java. Verifica que el JDK este instalado y en el PATH.
    exit /b 1
)
for /f "tokens=1 delims=." %%V in ("%JAVA_VERSION%") do set "JAVA_MAJOR=%%V"
if %JAVA_MAJOR% LSS 25 (
    echo JavaFX 27 requiere JDK 25 o superior. Se detecto Java %JAVA_VERSION%.
    echo Instala JDK 25 y asegurate de que java y javac apunten a ese JDK.
    exit /b 1
)

if not exist out mkdir out
javac --release 17 --module-path "%JAVAFX_LIB%" --add-modules javafx.controls -cp "%MYSQL_DRIVER%" -sourcepath src -d out src\BuenaCazaApp.java
if errorlevel 1 exit /b %errorlevel%

if not exist out\vista mkdir out\vista
copy /Y src\vista\buenacaza.css out\vista\buenacaza.css >nul
java --module-path "%JAVAFX_LIB%" --add-modules javafx.controls -cp "out;%MYSQL_DRIVER%" BuenaCazaApp
