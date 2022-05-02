package com.sil.transactor;

import static com.youTransactor.uCube.connexion.ConnectionService.ConnectionManagerType.BLE;
import static com.youTransactor.uCube.connexion.ConnectionService.ConnectionManagerType.BT;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.google.android.material.switchmaterial.SwitchMaterial;
import com.youTransactor.uCube.api.UCubeAPI;

public class MainActivity extends AppCompatActivity {

    public static final String YT_PRODUCT = "ytProduct";
    public static final String NO_DEFAULT = "no_default";
    public static final String DEFAULT_YT_PRODUCT = "default_YT_Product";
    public static final String TEST_MODE_PREF_NAME = "testMode";
    public static final String MEASURES_MODE_PREF_NAME = "measuresMode";
    public static final String RECOVERY_MODE_PERF_NAME = "recoveryMode";
    public static final String ENABLE_SDK_LOGS_PREF_NAME = "enableSDKLogs";
    public static final String SDK_LOGS_LEVEL_PREF_NAME = "SDKLogLevel";
    public static final String SETUP_SHARED_PREF_NAME = "setup";

    private CardView uCubeCardView;
    private CardView uCubeTouchCardView;
    private SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        sharedPreferences = getSharedPreferences(SETUP_SHARED_PREF_NAME, Context.MODE_PRIVATE);

        setContentView(R.layout.activity_main);


        uCubeCardView = findViewById(R.id.ucube_card_view);
        uCubeTouchCardView = findViewById(R.id.ucube_touch_card_view);



        uCubeCardView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                uCubeCardView.setCardBackgroundColor(ContextCompat.getColor(MainActivity.this, android.R.color.darker_gray));
                uCubeTouchCardView.setCardBackgroundColor(ContextCompat.getColor(MainActivity.this, android.R.color.white));

                selectProduct(YTProduct.uCube);
            }
        });


        uCubeTouchCardView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                uCubeCardView.setCardBackgroundColor(ContextCompat.getColor(MainActivity.this, android.R.color.white));
                uCubeTouchCardView.setCardBackgroundColor(ContextCompat.getColor(MainActivity.this, android.R.color.darker_gray));

                Log.d("Check", "Youcubetouch");
                selectProduct(YTProduct.uCubeTouch);
            }
        });




    }

    private void selectProduct(YTProduct product) {
      sharedPreferences.edit().putString(YT_PRODUCT, product.name()).apply();

     //  sharedPreferences.edit().putString(DEFAULT_YT_PRODUCT, product.name()).apply();

               // sharedPreferences.edit().remove(DEFAULT_YT_PRODUCT).apply();

        startMainActivity(product);
    }

    private void startMainActivity(YTProduct product) {
        //Intent intent = new Intent(this, MainActivity.class);

        if (product.name().equals("uCube")) {

            Intent intent = new Intent(this, TransactionUcubeParingActivity.class);
            intent.putExtra(YT_PRODUCT, "uCube");
            startActivity(intent);
        }
        else {

            Intent intent = new Intent(this, TransactionUcubeTouch.class);
            intent.putExtra(YT_PRODUCT, "uCubeTouch");
            startActivity(intent);

        }


    }
}