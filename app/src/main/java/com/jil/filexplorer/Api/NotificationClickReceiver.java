package com.jil.filexplorer.Api;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;

import com.jil.filexplorer.R;
import com.jil.filexplorer.ui.CopyProgressDialog;

public class NotificationClickReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        ServiceConnection serviceConnection =new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName name, IBinder service) {

            }

            @Override
            public void onServiceDisconnected(ComponentName name) {

            }
        };
        CopyProgressDialog c1 = new CopyProgressDialog(context, R.layout.progress_dialog_layout);
        int progress=intent.getIntExtra("progress",0);
        c1.setProgress(progress);
        c1.show();
    }
}
