@echo off
set /p IP=IP:
set /p MASK=MASK:
set /p GATEWAY=GATEWAY:
set /p DNS=DNS:

netsh interface ip set address   "�ϰ�s�u" static %IP% %MASK% %GATEWAY% 1
netsh interface ip set dnsserver "�ϰ�s�u" static %DNS% primary
echo enjoy the internet :)
pause
:: powered by JohnDoe