@echo off
set /p IP=IP:
set /p MASK=MASK:
set /p GATEWAY=GATEWAY:
set /p DNS=DNS:

netsh interface ip set address   "區域連線" static %IP% %MASK% %GATEWAY% 1
netsh interface ip set dnsserver "區域連線" static %DNS% primary
echo enjoy the internet :)
pause
:: powered by JohnDoe