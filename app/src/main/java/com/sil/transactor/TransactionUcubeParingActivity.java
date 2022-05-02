package com.sil.transactor;

import static com.sil.transactor.MainActivity.YT_PRODUCT;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.sil.transactor.UCubeTouch.ListPairedUCubeTouchActivity;
import com.sil.transactor.UCubeTouch.UCubeTouchScanActivity;
import com.sil.transactor.uCube.ListPairedUCubeActivity;
import com.youTransactor.uCube.api.UCubeAPI;
import com.youTransactor.uCube.connexion.BatteryLevelListener;
import com.youTransactor.uCube.connexion.SVPPRestartListener;
import com.youTransactor.uCube.connexion.UCubeDevice;
import com.youTransactor.uCube.rpc.LostPacketListener;


public class TransactionUcubeParingActivity extends AppCompatActivity implements BatteryLevelListener, SVPPRestartListener, LostPacketListener {

    private static final String TAG = MainActivity.class.getName();
    private static final String SHARED_PREF_NAME = "main";

    public static final int SCAN_REQUEST = 1234;
    public static final String SCAN_FILTER = "SCAN_FILTER";
    public static final String DEVICE_NAME = "DEVICE_NAME";
    public static final String DEVICE_ADDRESS = "DEVICE_ADDRESS";

    /* Device */
    private YTProduct ytProduct;


    Button ucubeScanButton;

    enum State {
        NO_DEVICE_SELECTED,
        DEVICE_NOT_CONNECTED,
        DEVICE_CONNECTED,
    }

    enum MDMState {
        IDLE,
        DEVICE_REGISTERED,
        DEVICE_NOT_REGISTERED,
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_transaction_paring);


        if (getIntent() != null) {
            if (getIntent().hasExtra(YT_PRODUCT))
                ytProduct = YTProduct.valueOf(getIntent().getStringExtra(YT_PRODUCT));
        }

        if (ytProduct == null) {
            finish();
            return;
        }

        Log.d("YTProduct", "check"+ytProduct);

        if (getDevice() != null) {
            UCubeAPI.getConnexionManager().setDevice(getDevice());
        }

        UCubeAPI.mdmSetup(this);


        ucubeScanButton = findViewById(R.id.new_device);

        ucubeScanButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                    scan();
            }
        });

    }


    @Override
    protected void onDestroy() {
        UCubeAPI.unregisterSVPPRestartListener();
        UCubeAPI.unregisterLostPacketListener();
        super.onDestroy();
    }

    private void scan() {
        //disconnect
        // disconnect();

        // if user app will use YT TMS
        // an unregister of last device should be called
        // to delete current ssl certificate
        boolean res = UCubeAPI.mdmUnregister(this);
        if (!res) {
            Log.e(TAG, "FATAL Error! error to unregister current device");
        }

        //remove saved device
        // removeDevice();

        Intent intent;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            intent = new Intent(this, ytProduct == YTProduct.uCubeTouch ?
                    UCubeTouchScanActivity.class :
                    ListPairedUCubeActivity.class);

        } else {
            intent = new Intent(this, ytProduct == YTProduct.uCubeTouch ?
                    ListPairedUCubeTouchActivity.class :
                    ListPairedUCubeActivity.class);
        }

        // String filter = scanFilter.getText().toString();
        intent.putExtra(TransactionUcubeTouch.SCAN_FILTER, "uCube");

        startActivityForResult(intent, SCAN_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == SCAN_REQUEST && data != null) {

            String deviceName = data.getStringExtra(DEVICE_NAME);
            String deviceAddress = data.getStringExtra(DEVICE_ADDRESS);

            Log.d(TAG, "device : " + deviceName + " : " + deviceAddress);

            UCubeDevice device = new UCubeDevice(deviceName, deviceAddress);

            //2- initialise the connexion manager with selected device
            UCubeAPI.getConnexionManager().setDevice(device);

            //save device
            saveDevice(device);
        }
    }

    private void saveDevice(UCubeDevice device) {
        getSharedPreferences(SHARED_PREF_NAME, Context.MODE_PRIVATE).edit()
                .putString(DEVICE_NAME, device.getName())
                .putString(DEVICE_ADDRESS, device.getAddress())
                .apply();
    }

    private UCubeDevice getDevice() {
        SharedPreferences mainSharedPref = getSharedPreferences(SHARED_PREF_NAME, Context.MODE_PRIVATE);
        String name = mainSharedPref.getString(DEVICE_NAME, null);
        String address = mainSharedPref.getString(DEVICE_ADDRESS, null);

        if (name != null && address != null)
            return new UCubeDevice(name, address);

        return null;
    }


    @Override
    public void onLevelChanged(int newLevel) {
        runOnUiThread(() -> Toast.makeText(TransactionUcubeParingActivity.this, String.format(getString(R.string.battery_level), newLevel), Toast.LENGTH_LONG).show());
        Log.d(TAG,String.format(getString(R.string.battery_level), newLevel));
    }

    @Override
    public void onSVPPRestart() {
        runOnUiThread(() -> UIUtils.showMessageDialog(TransactionUcubeParingActivity.this, getString(R.string.device_get_stuck)));
        Log.d(TAG, "Notification of SVPP Restart event !");
    }

    @Override
    public void onPacketLost() {
        runOnUiThread(() -> UIUtils.showMessageDialog(TransactionUcubeParingActivity.this, getString(R.string.lost_packet)));
        Log.d(TAG, "Notification of lost packet !");
    }


}