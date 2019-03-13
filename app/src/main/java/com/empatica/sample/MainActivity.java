package com.empatica.sample;
//LIBRERIAS
import android.Manifest;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.empatica.empalink.ConnectionNotAllowedException;
import com.empatica.empalink.EmpaDeviceManager;
import com.empatica.empalink.EmpaticaDevice;
import com.empatica.empalink.config.EmpaSensorStatus;
import com.empatica.empalink.config.EmpaSensorType;
import com.empatica.empalink.config.EmpaStatus;
import com.empatica.empalink.delegate.EmpaDataDelegate;
import com.empatica.empalink.delegate.EmpaStatusDelegate;

//DEFICNICION DE LA CLASE HEREDANDO E IMPLEMENTANDO INTERFACES
public class MainActivity extends AppCompatActivity implements EmpaDataDelegate, EmpaStatusDelegate {
    //VARIABLES
    private static final int REQUEST_ENABLE_BT = 1;

    private static final int REQUEST_PERMISSION_ACCESS_COARSE_LOCATION = 1;


    private static final String EMPATICA_API_KEY = ""; // TODO insert your API Key here

    //ELEMENTOS GRAFICOS
    private EmpaDeviceManager deviceManager = null;

    private TextView accel_xLabel;

    private TextView accel_yLabel;

    private TextView accel_zLabel;

    private TextView bvpLabel;

    private TextView edaLabel;

    private TextView ibiLabel;

    private TextView temperatureLabel;

    private TextView batteryLabel;

    private TextView statusLabel;

    private TextView deviceNameLabel;

    private LinearLayout dataCnt;

    // IMPLEMENTACIÓN DE LOS METODOS
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        // Inicializar variables que hacen referencia a componentes de la interfaz de usuario
        statusLabel = (TextView) findViewById(R.id.status);

        dataCnt = (LinearLayout) findViewById(R.id.dataArea);

        accel_xLabel = (TextView) findViewById(R.id.accel_x);

        accel_yLabel = (TextView) findViewById(R.id.accel_y);

        accel_zLabel = (TextView) findViewById(R.id.accel_z);

        bvpLabel = (TextView) findViewById(R.id.bvp);

        edaLabel = (TextView) findViewById(R.id.eda);

        ibiLabel = (TextView) findViewById(R.id.ibi);

        temperatureLabel = (TextView) findViewById(R.id.temperature);

        batteryLabel = (TextView) findViewById(R.id.battery);

        deviceNameLabel = (TextView) findViewById(R.id.deviceName);


        final Button disconnectButton = findViewById(R.id.disconnectButton);

        disconnectButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                if (deviceManager != null) {

                    deviceManager.disconnect();
                }
            }
        });

        initEmpaticaDeviceManager();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case REQUEST_PERMISSION_ACCESS_COARSE_LOCATION:
                // Si se cancela la solicitud, las matrices de resultados están vacías.
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // Permiso fue concedido, Hasta aqui muy bien!
                    initEmpaticaDeviceManager();
                } else {
                    // Permiso denegado, revisar!
                    final boolean needRationale = ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_COARSE_LOCATION);
                    new AlertDialog.Builder(this)
                            .setTitle("Permiso requerido")
                            .setMessage("Sin este permiso, no se pueden encontrar dispositivos Bluetooth de baja energía, permitir que se conecte al dispositivo.")
                            .setPositiveButton("Procesar de nuevo", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    // Inténtalo de nuevo
                                    if (needRationale) {
                                        // la marca "nunca volver a preguntar" no está establecido, inténtelo de nuevo con la solicitud de permiso
                                        initEmpaticaDeviceManager();
                                    } else {
                                        // la marca "nunca volver a preguntar" está activada para que las solicitudes de permiso estén deshabilitadas, intente abrir la configuración de la aplicación para habilitar el permiso
                                        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                                        Uri uri = Uri.fromParts("package", getPackageName(), null);
                                        intent.setData(uri);
                                        startActivity(intent);
                                    }
                                }
                            })
                            .setNegativeButton("Salir de la aplicacion", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    // sin permiso salir es el único camino
                                    finish();
                                }
                            })
                            .show();
                }
                break;
        }
    }

    private void initEmpaticaDeviceManager() {
        // Android 6 (nivel de API 23) ahora requiere el permiso ACCESS_COARSE_LOCATION para usar BLE
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[] { Manifest.permission.ACCESS_COARSE_LOCATION }, REQUEST_PERMISSION_ACCESS_COARSE_LOCATION);
        } else {

            if (TextUtils.isEmpty(EMPATICA_API_KEY)) {
                new AlertDialog.Builder(this)
                        .setTitle("Warning")
                        .setMessage("Please insert your API KEY")
                        .setNegativeButton("Close", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                // Sin permiso salir es el único camino
                                finish();
                            }
                        })
                        .show();
                return;
            }

            // Crear un nuevo EmpaDeviceManager. MainActivity es tanto su delegado de datos como de su estado.
            deviceManager = new EmpaDeviceManager(getApplicationContext(), this, this);

            // Inicialice el Administrador de dispositivos usando su clave API. Necesitas tener acceso a internet en este punto.
            deviceManager.authenticateWithAPIKey(EMPATICA_API_KEY);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (deviceManager != null) {
            deviceManager.stopScanning();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (deviceManager != null) {
            deviceManager.cleanUp();
        }
    }

    @Override
    public void didDiscoverDevice(EmpaticaDevice bluetoothDevice, String deviceName, int rssi, boolean allowed) {
        // Compruebe si el dispositivo descubierto se puede utilizar con su clave API. Si permitido es siempre falso,
        // el dispositivo no está vinculado con su clave API. Por favor, consulte su área de desarrollador en
        // https://www.empatica.com/connect/developer.php
        if (allowed) {
            // Deja de escanear. El primer dispositivo permitido transmitira datos.
            deviceManager.stopScanning();
            try {
                // Conectar al dispositivo
                deviceManager.connectDevice(bluetoothDevice);
                updateLabel(deviceNameLabel, "To: " + deviceName);
            } catch (ConnectionNotAllowedException e) {
                // Esto debería suceder solo si intentas conectarte cuando allowed == false.
                Toast.makeText(MainActivity.this, "Lo sentimos, no puedes conectarte a este dispositivo", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public void didRequestEnableBluetooth() {
        // Solicite al usuario que habilite Bluetooth
        Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
        startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // El usuario optó por no habilitar Bluetooth
        if (requestCode == REQUEST_ENABLE_BT && resultCode == Activity.RESULT_CANCELED) {
            // Deberias lidiar con esto
            return;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void didUpdateSensorStatus(@EmpaSensorStatus int status, EmpaSensorType type) {

        didUpdateOnWristStatus(status);
    }

    @Override
    public void didUpdateStatus(EmpaStatus status) {
        // Actualizar la interfaz de usuario
        updateLabel(statusLabel, status.name());

        // El administrador de dispositivos está listo para su uso
        if (status == EmpaStatus.READY) {
            updateLabel(statusLabel, status.name() + " - Enciende tu dispositivo");
            // Empezar a escanear
            deviceManager.startScanning();
            // El administrador de dispositivos ha establecido una conexión.

            hide();

        } else if (status == EmpaStatus.CONNECTED) {

            show();
            // El administrador de dispositivos desconectado de un dispositivo
        } else if (status == EmpaStatus.DISCONNECTED) {

            updateLabel(deviceNameLabel, "");

            hide();
        }
    }
    // ACTUALIZACIÓN DE LAS MEDIDAS FISIOLÓGICAS
    @Override
    public void didReceiveAcceleration(int x, int y, int z, double timestamp) {
        updateLabel(accel_xLabel, "" + x);
        updateLabel(accel_yLabel, "" + y);
        updateLabel(accel_zLabel, "" + z);
    }

    @Override
    public void didReceiveBVP(float bvp, double timestamp) {
        updateLabel(bvpLabel, "" + bvp);
    }

    @Override
    public void didReceiveBatteryLevel(float battery, double timestamp) {
        updateLabel(batteryLabel, String.format("%.0f %%", battery * 100));
    }

    @Override
    public void didReceiveGSR(float gsr, double timestamp) {
        updateLabel(edaLabel, "" + gsr);
    }

    @Override
    public void didReceiveIBI(float ibi, double timestamp) {
        updateLabel(ibiLabel, "" + ibi);
    }

    @Override
    public void didReceiveTemperature(float temp, double timestamp) {
        updateLabel(temperatureLabel, "" + temp);
    }

    // Actualice una etiqueta con algún texto, asegurándose de que esto se ejecuta en el hilo de la interfaz de usuario
    private void updateLabel(final TextView label, final String text) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                label.setText(text);
            }
        });
    }

    @Override
    public void didReceiveTag(double timestamp) {

    }

    @Override
    public void didEstablishConnection() {

        show();
    }

    @Override
    public void didUpdateOnWristStatus(@EmpaSensorStatus final int status) {

        runOnUiThread(new Runnable() {

            @Override
            public void run() {

                if (status == EmpaSensorStatus.ON_WRIST) {

                    ((TextView) findViewById(R.id.wrist_status_label)).setText("EN LA MUÑECA");
                }
                else {

                    ((TextView) findViewById(R.id.wrist_status_label)).setText("NO ESTA EN LA MUÑECA");
                }
            }
        });
    }

    void show() {

        runOnUiThread(new Runnable() {

            @Override
            public void run() {

                dataCnt.setVisibility(View.VISIBLE);
            }
        });
    }

    void hide() {

        runOnUiThread(new Runnable() {

            @Override
            public void run() {

                dataCnt.setVisibility(View.INVISIBLE);
            }
        });
    }
}
