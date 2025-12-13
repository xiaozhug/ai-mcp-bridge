package io.xiaozhug.ai.mcp.apt.util;

import io.xiaozhug.ai.mcp.apt.config.ProcessorConfig;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * 日志工具类
 * <p>
 * 提供统一的日志输出功能
 * </p>
 *
 * @author xiaozhug
 */
public class LogUtils {
    
    private static final String LOG_PREFIX = "[MCP-APT] ";
    private static final DateTimeFormatter TIME_FORMATTER = 
        DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS");
    
    private static boolean debugEnabled = false;
    private static boolean timestampEnabled = false;

    private LogUtils() {
        // 工具类，防止实例化
    }

    public static void init(ProcessorConfig config){
        if(config.isDebugMode()){
            enableDebug();
        }
    }

    /**
     * 启用调试模式
     */
    public static void enableDebug() {
        debugEnabled = true;
    }
    
    /**
     * 禁用调试模式
     */
    public static void disableDebug() {
        debugEnabled = false;
    }
    
    /**
     * 启用时间戳
     */
    public static void enableTimestamp() {
        timestampEnabled = true;
    }
    
    /**
     * 禁用时间戳
     */
    public static void disableTimestamp() {
        timestampEnabled = false;
    }
    
    /**
     * 输出信息日志
     */
    public static void log(String message) {
        System.out.println(formatMessage("INFO", message));
    }
    
    /**
     * 输出调试日志
     */
    public static void debug(String message) {
        if (debugEnabled) {
            System.out.println(formatMessage("DEBUG", message));
        }
    }
    
    /**
     * 输出警告日志
     */
    public static void warn(String message) {
        System.err.println(formatMessage("WARN", message));
    }
    
    /**
     * 输出错误日志
     */
    public static void error(String message) {
        System.err.println(formatMessage("ERROR", message));
    }
    
    /**
     * 输出错误日志（带异常）
     */
    public static void error(String message, Throwable throwable) {
        error(message);
        if (throwable != null) {
            System.err.println(formatMessage("ERROR", "异常: " + throwable.getMessage()));
            if (debugEnabled) {
                throwable.printStackTrace();
            }
        }
    }
    
    /**
     * 格式化日志消息
     */
    private static String formatMessage(String level, String message) {
        StringBuilder sb = new StringBuilder();
        
        if (timestampEnabled) {
            sb.append("[").append(LocalDateTime.now().format(TIME_FORMATTER)).append("] ");
        }
        
        sb.append(LOG_PREFIX)
          .append("[").append(level).append("] ")
          .append(message);
        
        return sb.toString();
    }
}