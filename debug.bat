@echo off
:: Script de Inicialização Rápida com HotSwap para KriolOS POS (Windows)
:: Desenvolvido para JetBrains Runtime & DCEVM

:: DEFINE O CAMINHO PARA O TEU JETBRAINS RUNTIME NO WINDOWS
:: (Ajusta o caminho abaixo se o teu IntelliJ estiver instalado noutro local)
set "JAVA_HOME=C:\Program Files\JetBrains\IntelliJ IDEA 2026.1\jbr"
set "PATH=%JAVA_HOME%\bin;%PATH%"

echo =======================================================================
echo Starting KriolOS POS in Debug/HotSwap mode using JetBrains Runtime...
echo =======================================================================

:: Entra na pasta do módulo da aplicação a partir do diretório do script
cd /d "%~dp0kriolos-opos-app"

:: Executa o Maven Wrapper usando o profile do HotSwap
call ..\mvnw.cmd --no-transfer-progress -Prun-Hotswap exec:exec

pause

