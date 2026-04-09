package com.easyforge.util;

import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class LogUtil {
    private static final String LOG_DIR = "logs";
    private static final DateTimeFormatter FILE_DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final DateTimeFormatter TIME_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS");

    static {
        File dir = new File(LOG_DIR);
        if (!dir.exists()) dir.mkdirs();
        cleanOldLogs();
    }

    private static void cleanOldLogs() {
        File dir = new File(LOG_DIR);
        File[] files = dir.listFiles((d, name) -> name.endsWith(".log"));
        if (files == null) return;
        long thirtyDaysAgo = System.currentTimeMillis() - 30L * 24 * 60 * 60 * 1000;
        for (File f : files) {
            if (f.lastModified() < thirtyDaysAgo) {
                f.delete();
            }
        }
    }

    private static String getLogFileName() {
        return LOG_DIR + File.separator + "error_" + LocalDateTime.now().format(FILE_DATE_FORMAT) + ".log";
    }

    public static void error(String message, Throwable e) {
        String timestamp = LocalDateTime.now().format(TIME_FORMAT);
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        e.printStackTrace(pw);
        String stackTrace = sw.toString();
        String logEntry = String.format("[%s] %s\n%s\n\n", timestamp, message, stackTrace);
        try (FileWriter fw = new FileWriter(getLogFileName(), true)) {
            fw.write(logEntry);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        // 同时打印到控制台
        System.err.println(logEntry);
    }

    public static void error(String message) {
        error(message, new Exception("No stack trace"));
    }

    public static void info(String message) {
        String timestamp = LocalDateTime.now().format(TIME_FORMAT);
        String logEntry = String.format("[%s] INFO: %s\n", timestamp, message);
        try (FileWriter fw = new FileWriter(getLogFileName(), true)) {
            fw.write(logEntry);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        System.out.println(message);
    }

    public static File getLogDirectory() {
        return new File(LOG_DIR);
    }
}