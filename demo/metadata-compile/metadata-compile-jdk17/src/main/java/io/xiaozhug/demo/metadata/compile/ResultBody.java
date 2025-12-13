//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package io.xiaozhug.demo.metadata.compile;

public class ResultBody<T> {
    public static final String OK = "200";
    public static final String BAD_REQUEST = "400";
    public static final String UNAUTHORIZED = "401";
    public static final String FORBIDDEN = "403";
    public static final String NOT_FOUND = "404";
    public static final String INTERNAL_SERVER_ERROR = "500";
    protected boolean success;
    protected String message;
    protected String code;
    protected T data;
    private long timestamp;

    public ResultBody() {
        this.timestamp = System.currentTimeMillis();
    }

    public ResultBody(boolean success, String code, String message, T data) {
        this(success, code, message, data, System.currentTimeMillis());
    }

    public ResultBody(boolean success, String code, String message, T data, long timestamp) {
        this.success = success;
        this.message = message;
        this.code = code;
        this.data = data;
        this.timestamp = timestamp;
    }

    public static <T> ResultBody<T> success() {
        return new ResultBody(true, "200", "success", (Object)null);
    }

    public static <T> ResultBody<T> success(T data) {
        return new ResultBody(true, "200", "success", data);
    }

    public static <T> ResultBody<T> fail(String code, String message) {
        return new ResultBody(false, code, message, (Object)null);
    }

    public boolean isSuccess() {
        return this.success;
    }

    public String getMessage() {
        return this.message;
    }

    public String getCode() {
        return this.code;
    }

    public T getData() {
        return this.data;
    }

    public long getTimestamp() {
        return this.timestamp;
    }

    public void setSuccess(final boolean success) {
        this.success = success;
    }

    public void setMessage(final String message) {
        this.message = message;
    }

    public void setCode(final String code) {
        this.code = code;
    }

    public void setData(final T data) {
        this.data = data;
    }

    public void setTimestamp(final long timestamp) {
        this.timestamp = timestamp;
    }

}
