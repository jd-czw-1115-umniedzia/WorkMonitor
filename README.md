# WorkMonitor
Project for AGH IO course.

### Config
Aby dodać aplikację do śledzonych z poziomu konfiguracji, wystarczy dodać wpis do pola: 

```toml
[monitor]
applications = [
    ["cmd.exe", "C:\\Windows\\System32\\cmd.exe", "#880088"]
]
```

Uwaga! Pierwsze pole musi być oryginalną nazwą aplikacji, np. `idea64.exe`, `cmd.exe`, itp. Po tej nazwie będą śledzone aplikacje.