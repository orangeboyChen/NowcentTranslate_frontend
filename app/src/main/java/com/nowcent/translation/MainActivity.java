package com.nowcent.translation;

import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.AudioManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Vibrator;
import android.provider.Settings;
import android.support.wearable.activity.WearableActivity;
import android.support.wearable.input.RotaryEncoder;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ListAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.wear.widget.WearableLinearLayoutManager;
import androidx.wear.widget.WearableRecyclerView;

import com.baidu.translate.asr.OnRecognizeListener;
import com.baidu.translate.asr.TransAsrClient;
import com.baidu.translate.asr.TransAsrConfig;
import com.baidu.translate.asr.data.RecognitionResult;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;


public class MainActivity extends WearableActivity {

    private TextView mTextView;

    private Button button;

    private WearableRecyclerView wearableRecyclerView;

    private ListAdapter listAdapter;

    private List<String> data;

    private boolean needCheckBackLocation = false;
    private boolean isOpenActivity = false;

    TransAsrConfig config;
    TransAsrClient client;


    /**
     * 需要进行检测的权限数组
     */
    protected String[] needPermissions = {
            android.Manifest.permission.ACCESS_COARSE_LOCATION,
            android.Manifest.permission.ACCESS_FINE_LOCATION,
            android.Manifest.permission.WRITE_EXTERNAL_STORAGE,
            android.Manifest.permission.READ_EXTERNAL_STORAGE,
            android.Manifest.permission.READ_PHONE_STATE
    };

    private static String BACKGROUND_LOCATION_PERMISSION = "android.permission.ACCESS_BACKGROUND_LOCATION";



    //Todo: 从服务端获取
    String APP_ID = "20200803000532380";
    String SECRET_KEY = "VQoeTSBUg1YkptcUNeLr";
    private boolean isNeedCheck = true;


    private final String TAG = this.getClass().getName();

    private static final int PERMISSON_REQUESTCODE = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);



//        mTextView = (TextView) findViewById(R.id.text);

//        button.setOnClickListener((l) -> mTextView.setText("23333"));

//        wearableRecyclerView = findViewById(R.id.listView);
//        data = new ArrayList<>();
//        data.add("1111111111111111");
//        data.add("22222222222");
//        data.add("3333333333");
//        data.add("4444444444444444444");
//        data.add("1111111111111111");
//        data.add("22222222222");
//        data.add("3333333333");
//        data.add("4444444444444444444");
//        data.add("1111111111111111");
//        data.add("22222222222");
//        data.add("3333333333");
//        data.add("4444444444444444444");
//        data.add("1111111111111111");
//        data.add("22222222222");
//        data.add("3333333333");
//        data.add("4444444444444444444");
//
//        wearableRecyclerView.setEdgeItemsCenteringEnabled(true);
//        WearableLinearLayoutManager wearableLinearLayoutManager = new WearableLinearLayoutManager(this);
//        wearableRecyclerView.setLayoutManager(wearableLinearLayoutManager);
//        com.nowcent.translation.adapter.ListAdapter listAdapter = new com.nowcent.translation.adapter.ListAdapter(this, data);
//        wearableRecyclerView.setAdapter(listAdapter);

//        wearableRecyclerView.setOnGenericMotionListener(new View.OnGenericMotionListener() {
//            @Override
//            public boolean onGenericMotion(View v, MotionEvent ev) {
//                if (ev.getAction() == MotionEvent.ACTION_SCROLL && RotaryEncoder.isFromRotaryEncoder(ev)) {
//                    // Don't forget the negation here
//                    float delta = -RotaryEncoder.getRotaryAxisValue(ev) * RotaryEncoder.getScaledScrollFactor(
//                            getApplicationContext());
//
//                    // Swap these axes if you want to do horizontal scrolling instead
//                    v.scrollBy(0, Math.round(delta));
//
////                    Vibrator vibrator = (Vibrator)getApplication().getSystemService(VIBRATOR_SERVICE);
////                    vibrator.vibrate(1);
//
//
//                    return true;
//                }
//
//                return false;
//            }
//        });

//        if(Build.VERSION.SDK_INT > 28
//                && getApplicationContext().getApplicationInfo().targetSdkVersion > 28) {
//            needPermissions = new String[]{
//                    android.Manifest.permission.ACCESS_COARSE_LOCATION,
//                    android.Manifest.permission.ACCESS_FINE_LOCATION,
//                    android.Manifest.permission.WRITE_EXTERNAL_STORAGE,
//                    android.Manifest.permission.READ_EXTERNAL_STORAGE,
//                    android.Manifest.permission.READ_PHONE_STATE,
//                    BACKGROUND_LOCATION_PERMISSION
//            };
//        }

//        if(Build.VERSION.SDK_INT > 28
//                && getApplicationContext().getApplicationInfo().targetSdkVersion > 28) {
            needPermissions = new String[]{
                    android.Manifest.permission.INTERNET,
                    android.Manifest.permission.ACCESS_NETWORK_STATE,
                    android.Manifest.permission.ACCESS_WIFI_STATE,
                    android.Manifest.permission.READ_PHONE_STATE,
                    android.Manifest.permission.RECORD_AUDIO,
            };
//        }

        if (getApplicationInfo().targetSdkVersion >= 23) {
            if (isNeedCheck) {
                checkPermissions(needPermissions);
            }
        }


        // Enables Always-on
        setAmbientEnabled();
    }


    void initBD(){
        config = new TransAsrConfig(APP_ID, SECRET_KEY);
        config.setPartialCallbackEnabled(true);
        config.setLogEnabled(false);
        config.setRecognizeStartAudioRes(TransAsrConfig.TTS_ENGLISH_TYPE_UK);

        client = new TransAsrClient(this, config);
        client.setRecognizeListener(new OnRecognizeListener() {
            @Override
            public void onRecognized(int resultType, @NonNull RecognitionResult result) {
                if (resultType == OnRecognizeListener.TYPE_PARTIAL_RESULT) { // 中间结果
                    Log.d(TAG, "中间识别结果：" + result.getAsrResult());

                } else if (resultType == OnRecognizeListener.TYPE_FINAL_RESULT) { // 最终结果
                    if (result.getError() == 0) { // 表示正常，有识别结果
                        // 语音识别结果
                        Log.d(TAG, "最终识别结果：" + result.getAsrResult());
                        Log.d(TAG, "翻译结果：" + result.getTransResult());

                    } else { // 翻译出错
                        Log.d(TAG, "语音翻译出错 错误码：" + result.getError() + " 错误信息：" + result.getErrorMsg());

                    }
                }
            }
        });
//        audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, AudioManager.MA);
        AudioManager audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        int max = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
        audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, max, AudioManager.STREAM_VOICE_CALL);

        client.startRecognize("zh", "en");

        new Thread(() -> {
            try {
                Thread.sleep(3000);
                Log.e("stop", "停止");

            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            client.stopRecognize();
        }).start();
    }


//    public class CustomScrollingLayoutCallback extends WearableLinearLayoutManager.LayoutCallback {
//        /** How much should we scale the icon at most. */
//        private static final float MAX_ICON_PROGRESS = 0.65f;
//
//        private float progressToCenter;
//
//        @Override
//        public void onLayoutFinished(View child, RecyclerView parent) {
//
//            // Figure out % progress from top to bottom
//            float centerOffset = ((float) child.getHeight() / 2.0f) / (float) parent.getHeight();
//            float yRelativeToCenterOffset = (child.getY() / parent.getHeight()) + centerOffset;
//
//            // Normalize for center
//            progressToCenter = Math.abs(0.5f - yRelativeToCenterOffset);
//            // Adjust to the maximum scale
//            progressToCenter = Math.min(progressToCenter, MAX_ICON_PROGRESS);
//
//            child.setScaleX(1 - progressToCenter);
//            child.setScaleY(1 - progressToCenter);
//        }
//    }

    /**
     *
     * @param permissions
     * @since 2.5.0
     *
     */
    private void checkPermissions(String... permissions) {
        try {
            if (getApplicationInfo().targetSdkVersion >= 23) {
                List<String> needRequestPermissonList = findDeniedPermissions(permissions);
                if (needRequestPermissonList.size() > 0) {
                    String[] array = needRequestPermissonList.toArray(new String[needRequestPermissonList.size()]);
                    Method method = getClass().getMethod("requestPermissions", String[].class,
                            int.class);

                    method.invoke(this, array, PERMISSON_REQUESTCODE);
                }
                else{
                    if(!isOpenActivity){
//                        checkInitData();
                        initBD();
                        isOpenActivity = true;
                    }
                }
            }
        } catch (Throwable e) {
        }
    }

    /**
     * 获取权限集中需要申请权限的列表
     *
     * @param permissions
     * @return
     * @since 2.5.0
     *
     */
    private List<String> findDeniedPermissions(String[] permissions) {
        List<String> needRequestPermissonList = new ArrayList<String>();
        if (getApplicationInfo().targetSdkVersion >= 23){
            try {
                for (String perm : permissions) {
                    Method checkSelfMethod = getClass().getMethod("checkSelfPermission", String.class);
                    Method shouldShowRequestPermissionRationaleMethod = getClass().getMethod("shouldShowRequestPermissionRationale",
                            String.class);
                    if ((Integer)checkSelfMethod.invoke(this, perm) != PackageManager.PERMISSION_GRANTED
                            || (Boolean)shouldShowRequestPermissionRationaleMethod.invoke(this, perm)) {
                        if(!needCheckBackLocation
                                && BACKGROUND_LOCATION_PERMISSION.equals(perm)) {
                            continue;
                        }
                        needRequestPermissonList.add(perm);
                    }
                }
            } catch (Throwable e) {

            }
        }
        return needRequestPermissonList;
    }

    /**
     * 检测是否所有的权限都已经授权
     * @param grantResults
     * @return
     * @since 2.5.0
     *
     */
    private boolean verifyPermissions(int[] grantResults) {
        for (int result : grantResults) {
            if (result != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }
//        checkInitData();
        return true;
    }

    @Override
    @TargetApi(23)
    public void onRequestPermissionsResult(int requestCode,
                                           String[] permissions, @NonNull int[] paramArrayOfInt) {
        if (requestCode == PERMISSON_REQUESTCODE) {
            if (!verifyPermissions(paramArrayOfInt)) {
                showMissingPermissionDialog();
                isNeedCheck = false;
            }
            else{
                Log.e("权限都有了", "123");
            }
        }
    }

    /**
     * 显示提示信息
     *
     * @since 2.5.0
     *
     */
    private void showMissingPermissionDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("缺少权限");
        builder.setMessage("定位服务用于确定您当地的天气信息。请授予权限，以获得最佳体验。");

        // 拒绝, 退出应用
        builder.setNegativeButton("返回",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        finish();
                    }
                });

        builder.setPositiveButton("打开设置",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        startAppSettings();
                    }
                });

        builder.setCancelable(false);

        builder.show();
    }

    /**
     *  启动应用的设置
     *
     * @since 2.5.0
     *
     */
    private void startAppSettings() {
        Intent intent = new Intent(
                Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        intent.setData(Uri.parse("package:" + getPackageName()));
        startActivity(intent);
    }


}
