package me.liuzs.cabinetmanager.net;

import android.text.TextUtils;

public class APIJSON<T> {

    @SuppressWarnings("rawtypes")
    public static APIJSON buildServerErrorJSON(int httpCode) {
        APIJSON result = new APIJSON();
        result.status = Status.error;
        String info = null;
        if (httpCode == 401) {
            info = "用户鉴权失败";
        } else if (httpCode == 403) {
            info = "无访问权限";
        }
        result.error = "请求失败:" + httpCode + (TextUtils.isEmpty(info) ? "" : "(" + info + ")");
        return result;
    }

    @SuppressWarnings("rawtypes")
    public static APIJSON buildOtherErrorJSON(Exception exception) {
        APIJSON<?> result = new APIJSON();
        result.status = Status.other;
        result.error = "异常错误:" + exception.getMessage();
        return result;
    }

    public Status status = Status.error;
    public String error = "";
    public T data;

    public enum Status {
        ok, error, other
    }
}
