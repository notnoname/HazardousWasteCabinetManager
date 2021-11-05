package me.liuzs.cabinetmanager;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.util.LinkedList;
import java.util.List;

import me.liuzs.cabinetmanager.db.CDatabase;
import me.liuzs.cabinetmanager.model.AlertLog;
import me.liuzs.cabinetmanager.model.OptLog;
import me.liuzs.cabinetmanager.ui.log.LogAdapter;

public class LogViewActivity extends BaseActivity {


    public static final String TAG = "LogViewActivity";
    private final Gson mGson = new GsonBuilder().setPrettyPrinting().create();
    private RecyclerView mLeft, mRight;
    private LogAdapter mLeftAdapter, mRightAdapter;

    @Override
    void afterRequestPermission(int requestCode, boolean isAllGranted) {

    }

    public static void start(Context context) {
        Intent intent = new Intent(context, LogViewActivity.class);
        if (!(context instanceof Activity)) {
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        }
        context.startActivity(intent);
    }

    private boolean isLeftCanLoadMore = true, isRightCanLoadMore = true;

    private int mLeftDataSize, mRightDataSize = 0;

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_log_view);
        mLeft = findViewById(R.id.rvRecordLeft);
        mLeft.setLayoutManager(new LinearLayoutManager(this, RecyclerView.VERTICAL, false));
        mRight = findViewById(R.id.rvRecordRight);
        mRight.setLayoutManager(new LinearLayoutManager(this, RecyclerView.VERTICAL, false));

        mLeftAdapter = new LogAdapter();
        mRightAdapter = new LogAdapter();
        mLeft.setAdapter(mLeftAdapter);
        mRight.setAdapter(mRightAdapter);
        mLeft.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                LinearLayoutManager linearLayoutManager = (LinearLayoutManager) recyclerView.getLayoutManager();
                assert linearLayoutManager != null;
                if (linearLayoutManager.findLastCompletelyVisibleItemPosition() == mLeftAdapter.getItemCount() - 1 && isLeftCanLoadMore) {
                    getOptLog();
                }
            }
        });
        mRight.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                LinearLayoutManager linearLayoutManager = (LinearLayoutManager) recyclerView.getLayoutManager();
                assert linearLayoutManager != null;
                if (linearLayoutManager.findLastCompletelyVisibleItemPosition() == mRightAdapter.getItemCount() - 1 && isRightCanLoadMore) {
                    getAlertLog();
                }
            }
        });
        getOptLog();
        getAlertLog();
    }

    public static final int PageSize = 100;

    public synchronized void getOptLog() {
        List<OptLog> list = CDatabase.getInstance().getOptLogList(CDatabase.Filter.All, true, PageSize, mLeftDataSize);
        if (list.size() == 0) {
            isLeftCanLoadMore = false;
        } else {
            mLeftDataSize += list.size();
            List<String> logList = new LinkedList<>();
            for (OptLog log : list) {
                String l = "[ " + log.time + " ] " + log.who + log.opt + log.obj;
                logList.add(l);
            }
            mLeftAdapter.addLog(logList);
        }
    }

    public synchronized void getAlertLog() {
        List<AlertLog> list = CDatabase.getInstance().getAlertLogList(CDatabase.Filter.All, true, PageSize, mRightDataSize);
        if (list.size() == 0) {
            isRightCanLoadMore = false;
        } else {
            mRightDataSize += list.size();
            List<String> logList = new LinkedList<>();
            for (AlertLog log : list) {
                String l = "[ " + log.time + " ] " + log.alert;
                logList.add(l);
            }
            mRightAdapter.addLog(logList);
        }
    }

    public void onBackButtonClick(View view) {
        finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

}