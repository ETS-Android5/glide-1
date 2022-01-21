package com.bumptech.glide;


import android.text.TextUtils;
import android.util.Log;
import android.util.Pair;
import androidx.annotation.NonNull;
import androidx.collection.ArrayMap;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class PrettyLogger {
    //   private static final Pair<String, String> clazzAndMethodPair = new Pair<>("", "");
//   private static final Pair<Pair<String, String>, StackTraceElement[]> StackTraceInfo = new Pair<>(clazzAndMethodPair, null);
    private static final String PRETTY_LOGGER = "PRETTY_LOGGER";
    private static final String GLIDE_FLOW = "GLIDE_FLOW";
    private static final String GLIDE_REQUEST = "GLIDE_REQUEST";
    private static final String GLIDE_DECODE = "GLIDE_DECODE";
    private static final String GLIDE_CACHE = "GLIDE_CACHE";
    private static final String INVOKE_TRACK = "INVOKE_TRACK";

    public static String identityHashCode(Object o) {
        return o + "@MEM:" + System.identityHashCode(o);
    }

//   private static final Pattern PATTERN_OPERATE_INCLUDE = Pattern.compile("^[-].*");

    public static void commonLog() {
        justLog("", "");
    }

    public static void commonLog(String filter) {
        justLog(filter, "");
    }

    public static void commonLog(Object... objects) {
        justLog("", objects);
    }

    public static void commonLog(String filter, Object... objects) {
        justLog(filter, objects);
    }

    //region invokeTrack
    public static void invokeTrack() {
        Pair<Integer, StackTraceElement[]> pair = getStackInfo().second;
        StringBuilder builder = new StringBuilder();
        builder.append(INVOKE_TRACK).append(" ▶▶ ").append("\n");
        for (int i = pair.first; i < pair.second.length; i++) {
            builder.append(pair.second[i].toString()).append("\n");
        }
        Log.i(INVOKE_TRACK, builder.toString());
    }

    /*
     * 追踪一个/多个方法调用，精准过滤
     * @param clazzAndMethods, combine clazz and multi methods with "#" to track these methods at the same time
     *                         同时追踪多个方法调用（为了支持过滤），用“#”拼接
     */
    public static void invokeTrack(String clazzAndMethods) {
        String[] pair = clazzAndMethods.trim().split("#", -1);
        if (pair.length < 2) {
            throw new IllegalStateException("params of invokeTrack must contain at least one \"#\"");
        }
        justTrack(pair[0], Arrays.copyOfRange(pair, 1, pair.length));
    }
    //endregion

    private static void justTrack(@NonNull String clazz, @NonNull String... methods) {
        Pair<Pair<String, String>, Pair<Integer, StackTraceElement[]>> stackInfo = getStackInfo();
        Pair<ArrayMap<String, FILTER_OPERATE>, List<String>> opsAndMethods = classifyOperatesAndMethods(methods);
        if (clazz.equals(stackInfo.first.first)) {
            boolean isEnableTrack = false;
            FILTER_OPERATE threadFilter = FILTER_OPERATE.JUST;
            if (opsAndMethods.first.containsKey(Thread.currentThread().getName())) {
                threadFilter = opsAndMethods.first.get(Thread.currentThread().getName());
            }
            if (threadFilter == FILTER_OPERATE.THREAD_EXCLUDE) {
                return;
            }
            for (String method : opsAndMethods.second) {
                if (method.equals(stackInfo.first.second)) {
                    isEnableTrack = true;
                    break;
                }
            }
            if (isEnableTrack) {
                StringBuilder builder = new StringBuilder();
                builder.append(INVOKE_TRACK).append(" ▶▶ ").append(clazz);
                for (String method : opsAndMethods.second) {
                    builder.append("#").append(method);
                }
                builder.append("\n").append("◉ Thread : ").append(Thread.currentThread().getName()).append("\n");
                for (int i = stackInfo.second.first; i < stackInfo.second.second.length; i++) {
                    builder.append(i == stackInfo.second.first ? "◉ Trace  : \n           " : "           ").append(stackInfo.second.second[i].toString()).append("\n");
                }
                Log.i(INVOKE_TRACK, builder.toString());
            }
        }
    }

    /*
     * ↳ ➤ ● ❏ ▶ ◉ ↴
     */
    private static void justLog(String filter, Object... contents) {
        Pair<String, String> clazzAndMethod = getStackInfo().first;
        if (TextUtils.isEmpty(filter)) {
            filter = "▶▶";
        }
        StringBuilder builder = new StringBuilder();
        builder.append(filter).append("\n");
        builder.append("◉ Thread : ").append(Thread.currentThread().getName()).append("\n");
        builder.append("◉ Class  : ").append(clazzAndMethod.first).append("\n");
        builder.append("◉ Method : ").append(clazzAndMethod.second).append("\n");
        if (contents.length == 0) {
            return;
        }
        for (int i = 0; i < contents.length; i++) {
            String toAppend = contents[i] instanceof String ? contents[i].toString() : identityHashCode(contents[i]);
            if (i == 0) {
                builder.append("◉ Content: ").append("\n");
            }
            builder.append("           ● ").append(toAppend).append("\n");
        }
        Log.i(PRETTY_LOGGER, builder.toString());
    }

    /*
     *                                                                                            ▶ justLog   ▶ commonLog
     * dalvik.system.VMStack.getThreadStackTrace ▶ java.lang.Thread.getStackTrace ▶ getStackInfo                           ▶ "realClassAndMethod"
     *                                                                                            ▶ justTrack ▶ invokeTrack
     *
     * @return Pair<clazz, method> and Pair<lineStartPrint, stackTraceElements>
     */
    private static Pair<Pair<String, String>, Pair<Integer, StackTraceElement[]>> getStackInfo() {
        Log.i(PRETTY_LOGGER, "▶▶");//修复打印多个相同的Log不显示完全
        StackTraceElement[] elementArray = Thread.currentThread().getStackTrace();
        int lineIndexBeforePrint = 0;
        String clazz = "", method = "";
        /*
         * 0:dalvik.system.VMStack.getThreadStackTrace
         * 1:java.lang.Thread.getStackTrace
         * 2:PrettyLogger.getStackInfo  ▶▶ this
         */
        for (int i = 3; i < elementArray.length; i++) {
            //exclude stack info inside of PrettyLogger
            if (!elementArray[i].toString().contains(PrettyLogger.class.getSimpleName())) {
                clazz = elementArray[i].getClassName();
                method = elementArray[i].getMethodName();
                String[] clazzPaths = clazz.trim().split("\\.", -1);
                clazz = clazzPaths[clazzPaths.length - 1];
                break;
            } else {
                lineIndexBeforePrint = i; //record which line before print
            }
        }
        return new Pair<>(new Pair<>(clazz, method), new Pair<>(++lineIndexBeforePrint, elementArray));
    }

    /**
     * @param operatesAndMethods: @main、-main、@xxx、-xxx、etc. "-" means {@link FILTER_OPERATE#THREAD_EXCLUDE} and "@" means {@link FILTER_OPERATE#JUST}... and methods included too.
     */
    private static Pair<ArrayMap<String, FILTER_OPERATE>, List<String>> classifyOperatesAndMethods(String... operatesAndMethods) {
        ArrayMap<String, FILTER_OPERATE> ops = new ArrayMap<>();
        List<String> methods = new ArrayList<>();
        for (String opOrMethod : operatesAndMethods) {
            opOrMethod = opOrMethod.trim();
            if (opOrMethod.startsWith("@")) {
                ops.put(opOrMethod.substring(1), FILTER_OPERATE.JUST);
            } else if (opOrMethod.startsWith("-")) {
                ops.put(opOrMethod.substring(1), FILTER_OPERATE.THREAD_EXCLUDE);
            } else {
                methods.add(opOrMethod);
            }
        }
        return new Pair<>(ops, methods);
    }

    /*
     * 用于过滤：线程名称、
     */
    enum FILTER_OPERATE {
        JUST,    // "@"
        THREAD_EXCLUDE, // "-"
    }
}
