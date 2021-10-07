package me.liuzs.cabinetmanager.net;

public class APIJSON<T> {

    @SuppressWarnings("rawtypes")
    public static APIJSON buildServerErrorJSON(int httpCode) {
        APIJSON result = new APIJSON();
        result.status = Status.error;
        result.errors = "服务器异常：" + httpCode;
        return result;
    }

    @SuppressWarnings("rawtypes")
    public static APIJSON buildOtherErrorJSON(Exception exception) {
        APIJSON<?> result = new APIJSON();
        result.status = Status.other;
        result.errors = "异常错误：" + exception.getMessage();
        return result;
    }

    public Status status = Status.error;
    public String errors = "";
    public T data;

    public enum Status {
        ok, error, other
    }
}
