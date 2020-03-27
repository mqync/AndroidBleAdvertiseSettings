package com.steelmate.androidbleadvertisesettings;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;


/**
 * 1、是否过滤
 * 2、调整发送功率
 * 3、调整发送持续时间*100ms
 * 4、发送的频率
 * 5、接收的频率
 * 6、扫描可以开关
 */
@RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
public class MainActivity extends AppCompatActivity {
    private static final String TAG = MainActivity.class.getSimpleName();

    private EditText mEditTextSend;
    private TextView mTextViewReceive;
    private View     mButtonScan;
    private View     mButtonAdvertise;

    private List<String> mPerms = new ArrayList<>();

    private static byte[] EDDYSTONE_SERVER_DATAS = {
//            0x02, 0x01, 0x02,
//            0x1b, 0x16, (byte) 0xcd, (byte) 0xab, // uuid
            0x17, 0x16, (byte) 0xAA, (byte) 0xFE, // UID type's len is 0x17
            0x00,               // UID type
            0x08,               // tx power
            0x00, 0x01, 0x02, 0x03, 0x04, 0x05, 0x06, 0x07, 0x08, 0x09, // NID
            0x01, 0x02, 0x03, 0x04, 0x05, 0x06,                         // BID
            (byte) 0xff, (byte) 0xff          // RFU
    };
    private        View   mButtonSettings;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mEditTextSend = findViewById(R.id.editTextSend);
        mTextViewReceive = findViewById(R.id.textViewReceive);
        mButtonScan = findViewById(R.id.buttonScan);
        mButtonSettings = findViewById(R.id.buttonSettings);
        mButtonAdvertise = findViewById(R.id.buttonAdvertise);
        CheckBox checkBoxScan = findViewById(R.id.checkBoxScan);


        mButtonSettings.setOnClickListener(mOnClickListener);
        mButtonScan.setOnClickListener(mOnClickListener);
        mButtonAdvertise.setOnClickListener(mOnClickListener);
        checkBoxScan.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                BleAdvertisingModel.getInstance().getBleAvertisingSettings().setScan(isChecked);
            }
        });

        mPerms.add(Manifest.permission.ACCESS_COARSE_LOCATION);
        mPerms.add(Manifest.permission.ACCESS_FINE_LOCATION);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            mPerms.add(Manifest.permission.ACCESS_BACKGROUND_LOCATION);
        }
        AppCommonPermissionsUtils.requestPermissions(this, mPerms, 1);

        BluetoothManager bluetoothManager = (BluetoothManager) AppCommonContextUtils.getApp().getSystemService(Context.BLUETOOTH_SERVICE);
        if (bluetoothManager != null) {
            BluetoothAdapter bluetoothAdapter = bluetoothManager.getAdapter();
            if (!bluetoothAdapter.isEnabled()) {
                bluetoothAdapter.enable();
            }
        }

        BleAdvertisingModel.getInstance().setOnReceiveCallback(new BleAdvertisingModel.OnReceiveCallback() {
            @Override
            public void onReceive(String hexDataRaw, ParsedAd parsedAd) {
                mTextViewReceive.setText("接收的原始数据:" +
                                                 "\n" + hexDataRaw +
                                                 "\n" + "接收的serviceData数据:" +
                                                 "\n" + AppCommonConvertUtils.bytes2HexString(parsedAd.serviceData) +
                                                 "\n" + "接收的manufacturerId:" +
                                                 "\n" + AppCommonConvertUtils.numberToHex(parsedAd.manufacturerId, 2) +
                                                 "\n" + "接收的manufacturerData数据:" +
                                                 "\n" + AppCommonConvertUtils.bytes2HexString(parsedAd.manufacturerData) +
                                                 "\n" + "接收的localName数据:" +
                                                 "\n" + parsedAd.localName
                                        );
            }
        });

        checkBoxScan.setChecked(BleAdvertisingModel.getInstance().getBleAvertisingSettings().isScan());

    }

    View.OnClickListener mOnClickListener = new View.OnClickListener() {
        @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
        @Override
        public void onClick(View view) {
            if (view == mButtonSettings) {
                startActivity(new Intent(MainActivity.this, SettingsActivity.class));
            }
            if (view == mButtonScan) {
                BleAdvertisingModel.getInstance().startScanning();
            }
            if (view == mButtonAdvertise) {
                String hex = mEditTextSend.getText().toString().trim();
                if (TextUtils.isEmpty(hex)) {
                    return;
                }
                byte[] bytes = AppCommonConvertUtils.hexString2Bytes(hex);
                if (bytes == null) {
                    return;
                }
                BleAdvertisingModel.getInstance().startAdvertising(hex);
            }
        }
    };


}