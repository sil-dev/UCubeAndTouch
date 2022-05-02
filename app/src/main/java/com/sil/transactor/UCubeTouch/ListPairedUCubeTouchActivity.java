package com.sil.transactor.UCubeTouch;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.Window;
import android.widget.Toast;

import com.sil.transactor.R;
import com.sil.transactor.TransactionUcubeTouch;
import com.sil.transactor.UIUtils;
import com.youTransactor.uCube.api.UCubeAPI;
import com.youTransactor.uCube.connexion.UCubeDevice;

import java.util.ArrayList;
import java.util.List;

public class ListPairedUCubeTouchActivity extends AppCompatActivity {

    private static final int ENABLE_BT_REQUEST_CODE = 1234;

    uCubeTouchPairedListAdapter adapter;

    private String filter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_ACTION_BAR);

        setContentView(R.layout.activity_ucube_touch_scan);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowTitleEnabled(true);
            getSupportActionBar().setTitle(R.string.title_list_devices);
        }
        if (getIntent() != null && getIntent().getStringExtra(TransactionUcubeTouch.SCAN_FILTER) != null)
            filter = getIntent().getStringExtra(TransactionUcubeTouch.SCAN_FILTER);

        final BluetoothManager bluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);

        if (!bluetoothManager.getAdapter().isEnabled())
            startActivityForResult(new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE), ENABLE_BT_REQUEST_CODE);
        else
            init();


    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (requestCode == ENABLE_BT_REQUEST_CODE) {
            final BluetoothManager bluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);

            if (!bluetoothManager.getAdapter().isEnabled()) {
                UIUtils.showOptionDialog(this, getString(R.string.enable_bt_msg),
                        getString(R.string.enable_bt_yes_label),
                        getString(R.string.enable_bt_no_label), (dialog, which) -> {

                            if (which == DialogInterface.BUTTON_POSITIVE)
                                startActivityForResult(new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE), ENABLE_BT_REQUEST_CODE);
                            else
                                Toast.makeText(this, getString(R.string.enable_bt_msg), Toast.LENGTH_SHORT).show();
                        });
            } else
                init();
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            final Intent intent = new Intent(this, TransactionUcubeTouch.class);
            startActivity(intent);
            finish();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void init() {
        RecyclerView recyclerView = findViewById(R.id.recycler_view);
        recyclerView.setHasFixedSize(true);

        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());

        List<UCubeDevice> devices = UCubeAPI.getConnexionManager().getPairedUCubes(filter);

        adapter = new uCubeTouchPairedListAdapter(ListPairedUCubeTouchActivity.this, devices, view -> {
            final int childAdapterPosition = recyclerView.getChildAdapterPosition(view);
            final UCubeDevice selectedDevice = adapter.getItemAtPosition(childAdapterPosition);

            final Intent intent = new Intent();
            intent.putExtra(TransactionUcubeTouch.DEVICE_NAME, selectedDevice.getName());
            intent.putExtra(TransactionUcubeTouch.DEVICE_ADDRESS, selectedDevice.getAddress());

            setResult(TransactionUcubeTouch.SCAN_REQUEST, intent);
            finish();
        });

        recyclerView.setAdapter(adapter);
    }

    @Override
    public void onBackPressed() {
        final Intent intent = new Intent(this, TransactionUcubeTouch.class);
        startActivity(intent);
        finish();
    }

    private List<UCubeDevice> filterList(List<UCubeDevice> devices, String filter) {
        List<UCubeDevice> res = new ArrayList<>();

        for (UCubeDevice device : devices) {
            if (device.getName().toLowerCase().contains(filter.toLowerCase())) {
                res.add(device);
            }
        }
        return res;
    }
}