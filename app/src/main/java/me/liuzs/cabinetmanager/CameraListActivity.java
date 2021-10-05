package me.liuzs.cabinetmanager;

import android.os.AsyncTask;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.videogo.openapi.EZOpenSDK;
import com.videogo.openapi.bean.EZDeviceInfo;

import java.lang.ref.WeakReference;
import java.net.URL;
import java.util.LinkedList;
import java.util.List;

import me.liuzs.cabinetmanager.model.Cabinet;
import me.liuzs.cabinetmanager.model.SurveillanceCamera;
import me.liuzs.cabinetmanager.net.APIJSON;
import me.liuzs.cabinetmanager.net.RemoteAPI;
import me.liuzs.cabinetmanager.ui.cameralist.CameraListAdapter;
import me.liuzs.cabinetmanager.util.Util;

/**
 * 子板环境信息
 */
public class CameraListActivity extends BaseActivity {

    public static final String TAG = "LockerManageActivity";
    private final CameraListAdapter mAdapter = new CameraListAdapter(this);
    private final List<SurveillanceCamera> mCamera = new LinkedList<>();
    private RecyclerView mRecyclerView;

    @Override
    void afterRequestPermission(int requestCode, boolean isAllGranted) {
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera_list);
        Util.fullScreen(this);

        mRecyclerView = findViewById(R.id.rvRecord);
        mRecyclerView.setAdapter(mAdapter);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this, RecyclerView.VERTICAL, false));

        mAdapter.setResult(mCamera);
    }

    @Override
    protected void onStart() {
        super.onStart();
        Cabinet info = CabinetCore.getCabinetInfo();
        if (info != null && !TextUtils.isEmpty(info.id)) {
            new GetCameraListTask(this).execute(info.id);
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    public void onBackButtonClick(View view) {
        finish();
    }

    static class GetCameraListTask extends AsyncTask<String, Void, APIJSON<List<SurveillanceCamera>>> {

        private final WeakReference<CameraListActivity> mActivity;

        public GetCameraListTask(CameraListActivity activity) {
            this.mActivity = new WeakReference<>(activity);
        }

        @Override
        protected APIJSON<List<SurveillanceCamera>> doInBackground(String... strings) {
            APIJSON<List<SurveillanceCamera>> result = RemoteAPI.System.getCameraList(strings[0]);
            EZOpenSDK sdk = EZOpenSDK.getInstance();
            for (SurveillanceCamera camera : result.data) {
                try {
                    String fakeUrl = camera.url.replace("ezopen", "http");

                    URL url = new URL(fakeUrl);
                    String path = url.getPath();
                    String[] paths = path.split("/");
                    Log.d("VideoPlayer", path);
                    for (String p : paths) {
                        Log.d("VideoPlayer", p);
                    }
                    camera.serialNo = paths[1];
                    camera.channelNo = Integer.parseInt(paths[2].substring(0, paths[2].indexOf('.')));
                    Log.d("VideoPlayer", camera.serialNo + ":" + camera.channelNo);
                    EZDeviceInfo info = sdk.getDeviceInfo(camera.serialNo);
                    sdk.setAccessToken(camera.accessToken);
                    camera.status = info.getStatus();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            return result;
        }


        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            mActivity.get().showProgressDialog();
        }

        @Override
        protected void onPostExecute(APIJSON<List<SurveillanceCamera>> json) {
            super.onPostExecute(json);
            mActivity.get().dismissProgressDialog();
            mActivity.get().mAdapter.setResult(json.data);
        }
    }
}
