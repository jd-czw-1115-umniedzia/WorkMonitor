package pl.edu.agh.io.umniedziala.monitors;

import com.sun.jna.platform.win32.Kernel32;
import com.sun.jna.platform.win32.User32;
import com.sun.jna.platform.win32.WinDef;
import com.sun.jna.platform.win32.WinNT;
import com.sun.jna.ptr.IntByReference;

import java.util.List;
import java.util.Optional;

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

}
