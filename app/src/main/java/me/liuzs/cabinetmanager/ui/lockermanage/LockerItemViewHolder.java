package me.liuzs.cabinetmanager.ui.lockermanage;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import me.liuxy.cabinet.SubBoard;
import me.liuzs.cabinetmanager.CabinetApplication;
import me.liuzs.cabinetmanager.CabinetCore;
import me.liuzs.cabinetmanager.LockerManageActivity;
import me.liuzs.cabinetmanager.R;
import me.liuzs.cabinetmanager.model.Cabinet;
import me.liuzs.cabinetmanager.model.LockerStatus;
import me.liuzs.cabinetmanager.service.HardwareService;

public class LockerItemViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

    private static final String TAG = "LockerItemViewHolder";
    TextView deviceName;
    ImageView status;
    Button lock, unlock;
    LockerStatus lockerStatus;
    LockerManageActivity activity;
    private boolean isWantUnlock = true;
    private final ServiceConnection mlockServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            Log.d(TAG, "LockerServiceConnected success");
            HardwareService.HardwareServiceBinder binder = (HardwareService.HardwareServiceBinder) service;
            Cabinet info = CabinetCore.getCabinetInfo();
//            int index = CabinetCore.getDevIndex(info, lockerStatus.devId);
//            lockerStatus.lock = LockerStatus.Status.Unknown;
//            activity.notifyDataSetChanged();
//            SubBoard.ControlResult result = null;
//            if (isWantUnlock) {
//                result = binder.getHardwareService().switchLockerControl(index);
//            } else {
//                result = binder.getHardwareService().switchLockerControlUnlock(index);
//            }
//            activity.showToast(result);
            activity.unbindService(mlockServiceConnection);
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            activity.unbindService(mlockServiceConnection);
            Log.d(TAG, "LockerServiceConnected fail");
        }
    };

    public LockerItemViewHolder(@NonNull View itemView) {
        super(itemView);
        deviceName = itemView.findViewById(R.id.tvDeviceName);
        status = itemView.findViewById(R.id.ivLockerValue);
        lock = itemView.findViewById(R.id.btnLock);
        unlock = itemView.findViewById(R.id.btnUnlock);
        lock.setOnClickListener(this);
        unlock.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        if (v == unlock) {
            isWantUnlock = true;
            Intent intent = new Intent();
            intent.setClassName(activity.getPackageName(), HardwareService.class.getName());
            activity.bindService(intent, mlockServiceConnection, Context.BIND_AUTO_CREATE);
        } else if (v == lock) {
            isWantUnlock = false;
            Intent intent = new Intent();
            intent.setClassName(activity.getPackageName(), HardwareService.class.getName());
            activity.bindService(intent, mlockServiceConnection, Context.BIND_AUTO_CREATE);
        }
    }
}