package com.enumerate.disease_detection.Local;


public class UserContextHolder {
    // 静态ThreadLocal，存储当前线程的用户ID
    private static final ThreadLocal<Long> USER_ID_THREAD_LOCAL = new ThreadLocal<>();
    // 用户ip
    private static final ThreadLocal<String> USER_IP_THREAD_LOCAL = new ThreadLocal<>();

    /**
     * 设置当前线程的用户ID
     */
    public static void setUserId(Long userId) {
        USER_ID_THREAD_LOCAL.set(userId);
    }

    /**
     * 获取当前线程的用户ID（核心：其他组件通过这个方法获取）
     */
    public static Long getUserId() {
        return USER_ID_THREAD_LOCAL.get();
    }

    /**
     * 移除当前线程的用户ID，防止内存泄漏（必须调用）
     */
    public static void removeUserId() {
        USER_ID_THREAD_LOCAL.remove();
    }

    public static void setUserIp(String userIp) {
        USER_IP_THREAD_LOCAL.set(userIp);
    }
    public static String getUserIp() {
        return USER_IP_THREAD_LOCAL.get();
    }
    public static void removeUserIp() {
        USER_IP_THREAD_LOCAL.remove();
    }

}
