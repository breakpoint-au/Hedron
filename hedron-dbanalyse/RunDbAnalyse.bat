@echo off

IF "%1%" == "" (
    ECHO Argument missing, eg:
    ECHO    RunDbAnalyse.bat DbAnalyse-options-test.xml
    goto theend
)

call setjavahome.bat jse7

set LIBPATH=../hedron/lib

set DBAPATH=build/DbAnalyse.jar
set DBAPATH=%DBAPATH%;%LIBPATH%/args4j-2.0.21.jar
set DBAPATH=%DBAPATH%;%LIBPATH%/ojdbc14.jar
echo classpath [%DBAPATH%]

REM // B:\Projects\hedron\lib\args4j-2.0.21.jar
REM // B:\Projects\hedron\lib\commons-dbcp-1.4.jar
REM // B:\Projects\hedron\lib\commons-logging-1.1.1.jar
REM // B:\Projects\hedron\lib\commons-pool-1.6.jar
REM // B:\Projects\hedron\lib\gson-2.2.2.jar
REM // B:\Projects\hedron\lib\javaee.jar
REM // B:\Projects\hedron\lib\javax.mail.jar
REM // B:\Projects\hedron\lib\org.springframework.asm-3.1.1.RELEASE.jar
REM // B:\Projects\hedron\lib\org.springframework.beans-3.1.1.RELEASE.jar
REM // B:\Projects\hedron\lib\org.springframework.core-3.1.1.RELEASE.jar
REM // B:\Projects\hedron\lib\org.springframework.jdbc-3.1.1.RELEASE.jar
REM // B:\Projects\hedron\lib\org.springframework.transaction-3.1.1.RELEASE.jar
REM // B:\Projects\hedron\lib\org.springframework.web-3.1.1.RELEASE.jar

"%JAVA_HOME%"\bin\java.exe -classpath %DBAPATH% au.com.breakpoint.dbanalyse.DbAnalyse -options %1 %2 %3 %4 %5 %6 %7 %8 %9

:theend
