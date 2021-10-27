package me.liuzs.cabinetmanager.ui;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.TextView;

import com.apkfuns.xprogressdialog.HeartProgressView;

import me.liuzs.cabinetmanager.R;

public class NewProgressDialog extends AlertDialog {

    // theme类型
    public static final int THEME_HORIZONTAL_SPOT = 1;
    public static final int THEME_CIRCLE_PROGRESS = 2;
    public static final int THEME_HEART_PROGRESS = 3;
    public static final int THEME_HEART_PROGRESS_CANCEL = 4;//带取消按钮

    protected View progressBar;
    protected TextView message;
    protected String messageText = "正在加载中...";
    protected int theme = THEME_HORIZONTAL_SPOT;
    private TextView tvCancel;

    public NewProgressDialog(Context context) {
        super(context);
        messageText = context.getString(R.string.loading);
    }

    public NewProgressDialog(Context context, String message) {
        super(context);
        this.messageText = message;
    }

    public NewProgressDialog(Context context, int theme) {
        super(context);
        this.theme = theme;
    }

    public NewProgressDialog(Context context, String message, int theme) {
        super(context);
        this.messageText = message;
        this.theme = theme;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        switch (theme) {
            case THEME_CIRCLE_PROGRESS:
                setContentView(R.layout.view_xprogressdialog_circle_progress);
                break;
            case THEME_HEART_PROGRESS:
                setContentView(R.layout.view_xprogressdialog_heart_progress);
                break;
            default:
                setContentView(R.layout.view_xprogressdialog_spot);
                break;
        }
        message = findViewById(R.id.message);
        message.setTextSize(28);
        message.setSingleLine(true);
        tvCancel = findViewById(R.id.tv_cancel);
        progressBar = findViewById(R.id.progress);
        if (message != null && !TextUtils.isEmpty(messageText)) {
            message.setText(messageText);
        }
        getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        //setCanceledOnTouchOutside(false);
        //setCancelable(false);
    }

    public void setCancelOnClickListener(View.OnClickListener onClickListener) {
        if (tvCancel != null && onClickListener != null) {
            tvCancel.setOnClickListener(onClickListener);
        }
    }

    public void setMessage(String message) {
        this.messageText = message;
    }

    /**
     * 获取显示文本控件
     *
     * @return
     */
    public TextView getMessageView() {
        return message;
    }

    /**
     * 获取显示进度的控件
     *
     * @return
     */
    public View getProgressView() {
        return progressBar;
    }

    @Override
    public void show() {
        super.show();
        if (progressBar instanceof HeartProgressView) {
            progressBar.post(new Runnable() {
                @Override
                public void run() {
                    ((HeartProgressView) progressBar).start();
                }
            });
        }
    }
}
