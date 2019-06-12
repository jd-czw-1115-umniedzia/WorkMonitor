package pl.edu.agh.io.umniedziala.windowsHandlers;

import com.sun.jna.Native;
import com.sun.jna.platform.win32.Kernel32;
import com.sun.jna.platform.win32.User32;
import com.sun.jna.platform.win32.WinDef;
import com.sun.jna.platform.win32.WinNT;
import com.sun.jna.ptr.IntByReference;
import com.sun.jna.win32.W32APIOptions;
import com.sun.jna.platform.win32.Tlhelp32;


import java.util.*;

public class WindowsFunctionHandler {
    private static WindowsFunctionHandler instance;

    public static WindowsFunctionHandler getInstance() {
        if (instance == null) {
            instance = new WindowsFunctionHandler();
        }
        return instance;
    }

    private WindowsFunctionHandler() {
        WindowsFunctionHandler.getInstance();
    }


    public static Optional<String> getCurrentActiveWindowName() {
        WinDef.HWND hwnd = User32.INSTANCE.GetForegroundWindow();

        String fgImageName = getImageName(hwnd);
        if (fgImageName == null) {
            return Optional.empty();
        } else {
            return Optional.of(fgImageName);
        }
    }

    private static String getImageName(WinDef.HWND hwnd) {
        IntByReference processId = new IntByReference();
        User32.INSTANCE.GetWindowThreadProcessId(hwnd, processId);

        // Open the process to get permissions to the image name
        WinNT.HANDLE processHandle = Kernel32.INSTANCE.OpenProcess(
                Kernel32.PROCESS_QUERY_LIMITED_INFORMATION,
                false,
                processId.getValue()
        );

        char[] buffer = new char[4096];
        IntByReference bufferSize = new IntByReference(buffer.length);
        boolean success = Kernel32.INSTANCE.QueryFullProcessImageName(processHandle, 0, buffer, bufferSize);

        Kernel32.INSTANCE.CloseHandle(processHandle);

        return success ? new String(buffer, 0, bufferSize.getValue()) : null;
    }

    public static Map<String, String> getAllRunningProcesses() {
        Map<String, String> processesAndExecutePath = new HashMap<>();

        Kernel32 kernel32 = (Kernel32) Native.loadLibrary(Kernel32.class, W32APIOptions.DEFAULT_OPTIONS);
        Tlhelp32.PROCESSENTRY32.ByReference processEntry = new Tlhelp32.PROCESSENTRY32.ByReference();
        WinNT.HANDLE processSnapshot =
                kernel32.CreateToolhelp32Snapshot(Tlhelp32.TH32CS_SNAPPROCESS, new WinDef.DWORD(0));
        try {

            while (kernel32.Process32Next(processSnapshot, processEntry)) {
                // looks for a specific process
                System.out.print(processEntry.th32ProcessID + "\t" + Native.toString(processEntry.szExeFile) + "\t");
                WinNT.HANDLE moduleSnapshot =
                        kernel32.CreateToolhelp32Snapshot(Tlhelp32.TH32CS_SNAPMODULE, processEntry.th32ProcessID);
                try {
                    ProcessPathKernel32.MODULEENTRY32.ByReference me = new ProcessPathKernel32.MODULEENTRY32.ByReference();
                    ProcessPathKernel32.INSTANCE.Module32First(moduleSnapshot, me);
                    if (!me.szExePath().isEmpty())
                        processesAndExecutePath.put(String.valueOf(processEntry.szExeFile), me.szExePath());
                }
                finally {
                    kernel32.CloseHandle(moduleSnapshot);
                }
            }
        }
        finally {
            kernel32.CloseHandle(processSnapshot);
        }

        return processesAndExecutePath;
    }
}
