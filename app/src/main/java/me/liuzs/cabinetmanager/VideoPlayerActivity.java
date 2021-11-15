package me.liuzs.cabinetmanager;

import android.content.Context;
import android.content.Intent;
import android.graphics.SurfaceTexture;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.view.TextureView;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;

import com.videogo.errorlayer.ErrorInfo;
import com.videogo.openapi.EZConstants;
import com.videogo.openapi.EZOpenSDK;
import com.videogo.openapi.EZPlayer;

import me.liuzs.cabinetmanager.model.Camera;

public class VideoPlayerActivity extends BaseActivity {

    private TextureView mVideoView;
    private Camera mCamera;
    private EZPlayer mPlayer;
    private final Handler mVideoHandler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(@NonNull Message msg) {
            super.handleMessage(msg);
            Log.d("VideoPlayer:", String.valueOf(msg.what));
            switch (msg.what) {
                case EZConstants.EZRealPlayConstants.MSG_REALPLAY_PLAY_SUCCESS:
                    //播放成功
                    break;
                case EZConstants.EZRealPlayConstants.MSG_REALPLAY_PLAY_FAIL:
                    //播放失败,得到失败信息
                    ErrorInfo errorinfo = (ErrorInfo) msg.obj;
                    //得到播放失败错误码
                    int code = errorinfo.errorCode;
                    //得到播放失败模块错误码
                    String codeStr = errorinfo.moduleCode;
                    //得到播放失败描述
                    String description = errorinfo.description;
                    //得到播放失败解决方方案
                    String solution = errorinfo.sulution;
                    Log.d("VideoPlayer:", code + codeStr + description);
                    if (code == 400035 || code == 400036) {
                        final EditText et = new EditText(VideoPlayerActivity.this);
                        new AlertDialog.Builder(VideoPlayerActivity.this).setTitle("请输入摄像头验证码")
                                .setView(et)
                                .setPositiveButton("确定", (dialog, which) -> {
                                    String input = et.getText().toString().trim();
                                    CabinetCore.saveCameraVerifyCode(VideoPlayerActivity.this, mCamera.serial_no, mCamera.channel_no, input);
                                    startRealPlay();
                                })
                                .setNegativeButton("取消", (dialogInterface, i) -> finish())
                                .show();
                    }
                    break;
                case EZConstants.MSG_VIDEO_SIZE_CHANGED:
                    //解析出视频画面分辨率回调
                    try {
                        String temp = (String) msg.obj;
                        String[] strings = temp.split(":");
                        int mVideoWidth = Integer.parseInt(strings[0]);
                        int mVideoHeight = Integer.parseInt(strings[1]);
                        Log.d("VideoPlay", "Video Size:" + mVideoWidth + ":" + mVideoHeight);
                        //解析出视频分辨率
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    break;
                default:
                    break;
            }
        }
    };

    public static void start(Camera camera, Context context) {
        Intent intent = new Intent(context, VideoPlayerActivity.class);
        intent.putExtra("CameraParam", CabinetCore.GSON.toJson(camera));
        context.startActivity(intent);
    }

    @Override
    void afterRequestPermission(int requestCode, boolean isAllGranted) {
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_player);
        mCamera = CabinetCore.GSON.fromJson(getIntent().getStringExtra("CameraParam"), Camera.class);
        ((TextView)findViewById(R.id.toolbar_title)).setText(mCamera.video_name);
        EZOpenSDK sdk = EZOpenSDK.getInstance();
        sdk.setAccessToken(mCamera.access_token);
        mPlayer = sdk.createPlayer(mCamera.serial_no, mCamera.channel_no);
        mPlayer.setHandler(mVideoHandler);

        mVideoView = findViewById(R.id.textureView);
        mVideoView.setSurfaceTextureListener(new TextureView.SurfaceTextureListener() {
            @Override
            public void onSurfaceTextureAvailable(@NonNull SurfaceTexture surfaceTexture, int i, int i1) {
                mPlayer.setSurfaceEx(surfaceTexture);
                startRealPlay();
            }

            @Override
            public void onSurfaceTextureSizeChanged(@NonNull SurfaceTexture surfaceTexture, int i, int i1) {

            }

            @Override
            public boolean onSurfaceTextureDestroyed(@NonNull SurfaceTexture surfaceTexture) {
                if (mPlayer != null) {
                    mPlayer.stopRealPlay();
                    mPlayer.release();
                }
                return false;
            }

            @Override
            public void onSurfaceTextureUpdated(@NonNull SurfaceTexture surfaceTexture) {

            }
        });

    }

    private void startRealPlay() {
        if (mPlayer != null) {
            mPlayer.setPlayVerifyCode(CabinetCore.getCameraVerifyCode(this, mCamera.serial_no, mCamera.channel_no));
            mPlayer.startRealPlay();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    public void onBackButtonClick(View view) {
        finish();
    }
}