package com.tony.jrp.common.utils;

import com.sun.jna.Native;
import com.sun.jna.win32.StdCallLibrary;
import lombok.extern.slf4j.Slf4j;

/**
 * 控制台工具类
 */
@Slf4j
public class ConsoleUtils {
    /**
     * UTF-8代码页
     */
    private static final int CP_UTF8 = 65001;

    private ConsoleUtils() {
    }

    /**
     * Windows控制台输出代码页接口，通过JNA直接调用进程内的Windows API设置，
     * 避免使用chcp子进程因控制台继承问题导致设置不生效
     */
    private interface Kernel32 extends StdCallLibrary {
        Kernel32 INSTANCE = Native.load("kernel32", Kernel32.class);

        /**
         * 设置调用进程关联控制台的输出代码页
         *
         * @param wCodePageID 代码页ID
         * @return 是否成功
         */
        int SetConsoleOutputCP(int wCodePageID);

        /**
         * 获取调用进程关联控制台的输出代码页
         *
         * @return 代码页ID
         */
        int GetConsoleOutputCP();
    }

    /**
     * 在 Windows 控制台模式下将输出代码页切换为 UTF-8（65001），解决控制台中文乱码问题。
     * 等价于启动脚本中的 chcp 65001，仅Windows生效；无控制台或设置失败时不影响程序运行。
     */
    public static void setUtf8Console() {
        String os = System.getProperty("os.name", "");
        if (!os.toLowerCase().contains("win")) {
            return;
        }
        try {
            int before = Kernel32.INSTANCE.GetConsoleOutputCP();
            if (before == CP_UTF8) {
                return;
            }
            int result = Kernel32.INSTANCE.SetConsoleOutputCP(CP_UTF8);
            if (result != 0) {
                log.info("控制台输出代码页由[{}]切换为[{}]成功", before, CP_UTF8);
            } else {
                log.warn("控制台输出代码页切换失败，当前代码页[{}]", before);
            }
        } catch (Throwable e) {
            //非控制台或无JNA支持时忽略，不影响程序正常运行
            log.debug("切换控制台代码页异常：{}", e.getMessage());
        }
    }
}