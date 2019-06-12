reg add "HKEY_CURRENT_USER\Software\Microsoft\Windows\CurrentVersion\Run" /v *WorkMonitor /t REG_EXPAND_SZ /d %cd%\workmonitor.exe
