package com.madfactory.madyoutubefilter;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;

import com.google.firebase.iid.FirebaseInstanceId;
import com.madfactory.madyoutubefilter.Data.GVal;
import com.madfactory.madyoutubefilter.KBONetwork.KBONetworkInfo;

public class SplashActivity extends AppCompatActivity {

    class CheckUpdateHandler extends Handler {
        public CheckUpdateHandler(){}

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);

            if(msg.arg1 == -1) {
                finishApp();
                return;
            }

            if( msg.arg1 == 0 ) {
                LoadCommunityList();
            }
            else {
                if( KBONetworkInfo.IsWifiAvailable(getApplicationContext()) ) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(SplashActivity.this);

                    builder.setTitle("업데이트 확인");
                    builder.setMessage("마켓에 새 버전이 있습니다. 업데이트 하시겠습니까?");


                    builder.setPositiveButton("네", new DialogInterface.OnClickListener() {

                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            Intent marketLaunch = new Intent(Intent.ACTION_VIEW);
                            marketLaunch.setData(Uri.parse("market://details?id=com.madfactory.madyoutubefilter"));
                            startActivity(marketLaunch);
                            finish();
                            dialog.dismiss();
                        }

                    });

                    builder.setNegativeButton("아니오", new DialogInterface.OnClickListener() {

                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            LoadCommunityList();
                            dialog.dismiss();
                        }
                    });

                    AlertDialog alert = builder.create();
                    alert.show();
                }
                else {
                    LoadCommunityList();
                }
            }
        }
    }

    CheckUpdateHandler handlerUpdate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        FirebaseInstanceId.getInstance().getToken();
        CheckUpdate();
    }

    private void CheckUpdate() {
        handlerUpdate = new CheckUpdateHandler();

        new Thread(new Runnable() {
            @Override
            public void run() {
                Message msg = handlerUpdate.obtainMessage();
                try {
                    String device_version = getPackageManager().getPackageInfo(getPackageName(), 0).versionName;
                    int nState = MarketVersionChecker.getVersionState(device_version);
                    msg.arg1 = nState;
                    handlerUpdate.sendMessage(msg);
                } catch (PackageManager.NameNotFoundException e) {
                    msg.arg1 = -1;
                    handlerUpdate.sendMessage(msg);
                    return;
                }
            }
        }).start();
    }

    private void LoadCommunityList() {
        try {
            Thread.sleep(750);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        GVal.LoadReadedArticle(getApplicationContext());

        // 메인으로 이동
        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
        intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
        startActivity(intent);
    }

    public void finishApp() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        builder.setTitle("앱 종료");
        builder.setMessage("네트워크 연결을 확인해 주십시오.");
        builder.setCancelable(false);


        builder.setPositiveButton("그..그럴게요", new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                finish();

                dialog.dismiss();
            }

        });

        AlertDialog alert = builder.create();
        alert.show();
    }
}