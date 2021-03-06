package com.av.googledev;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothProfile;
import android.support.v4.app.NotificationCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.av.googledev.model.Battery;
import com.av.googledev.model.LeParams;

import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Set;
import java.util.UUID;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    private static final String TAG = "MainActivity";
    public static final String EXTRAS_DEVICE_NAME = "DEVICE_NAME";
    public static final String EXTRAS_DEVICE_ADDRESS = "DEVICE_ADDRESS";

    private static final UUID UUID_MILI_SERVICE = UUID
            .fromString("0000fee0-0000-1000-8000-00805f9b34fb");
    private static final UUID UUID_CHAR_LE_PARAMS = UUID
            .fromString("0000ff09-0000-1000-8000-00805f9b34fb");
    private static final UUID UUID_CHAR_DEVICE_NAME = UUID
            .fromString("0000ff02-0000-1000-8000-00805f9b34fb");
    private static final UUID UUID_CHAR_BATTERY = UUID
            .fromString("0000ff0c-0000-1000-8000-00805f9b34fb");
    private static final UUID UUID_CHAR_REALTIME_STEPS = UUID
            .fromString("0000ff06-0000-1000-8000-00805f9b34fb");
    private static final UUID UUID_CHAR_pair = UUID
            .fromString("0000ff0f-0000-1000-8000-00805f9b34fb");

    private Button mconnect, mlisten, mstart, mstop, mShort, mBattery, mNotify;
    private EditText mAddress, mInput;
    private TextView mPhysicalAddress, mState;

    BluetoothAdapter bluetoothAdapter;
    BluetoothDevice bluetoothDevice;
    BluetoothGatt bluetoothGatt;

    private String mDeviceName;
    private String mDeviceAddress;
    private int rread = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mconnect = findViewById(R.id.ble_connect);
        mlisten = findViewById(R.id.ble_listen);
        mstart = findViewById(R.id.ble_startVibrate);
        mstop = findViewById(R.id.ble_stopVibrate);
        mPhysicalAddress = findViewById(R.id.ble_physicalAddress);
        mState = findViewById(R.id.ble_state);
        mAddress = findViewById(R.id.macAddress);
        mShort = findViewById(R.id.ble_shortVibrate);
        mBattery = findViewById(R.id.ble_battery);
        mNotify = findViewById(R.id.ble_notify);
        mInput = findViewById(R.id.ble_notify_input);

        mconnect.setOnClickListener(this);
        mlisten.setOnClickListener(this);
        mstart.setOnClickListener(this);
        mstop.setOnClickListener(this);
        mShort.setOnClickListener(this);
        mState.setOnClickListener(this);
        mBattery.setOnClickListener(this);
        mNotify.setOnClickListener(this);
        mInput.setOnClickListener(this);

        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        getBoundedDevice();
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.ble_connect:
                connect();
                break;
            case R.id.ble_listen:
                listen();
                break;
            case R.id.ble_startVibrate:
                startVibrate();
                break;
            case R.id.ble_stopVibrate:
                stopVibrate();
                break;
            case R.id.ble_shortVibrate:
                shortVibrate();
                break;
            case R.id.ble_state:
                showNotification();
                break;
            case R.id.ble_battery:
                getBatteryInfo();
                break;
            case R.id.ble_notify:
                try {
                    notifyCall();
                }catch(Exception e){
                    Log.e(TAG, "onClick: Exception" );
                }
                break;
        }
    }

    private void getBatteryInfo() {
        Log.e(TAG, "listen: Battery");
//        request(UUID_CHAR_REALTIME_STEPS);
        mState.setText("...");
        BluetoothGattCharacteristic bchar = bluetoothGatt.getService(BLEConstants.Basic.service)
                .getCharacteristic(BLEConstants.Basic.batteryCharacteristic);
        if (!bluetoothGatt.readCharacteristic(bchar)) {
            Toast.makeText(this, "Failed get battery info", Toast.LENGTH_SHORT).show();
        }
//        BluetoothGattCharacteristic bchar = bluetoothGatt.getService(BLEConstants.Basic.service)
//                .getCharacteristic(BLEConstants.Basic.batteryCharacteristic);
//        bluetoothGatt.setCharacteristicNotification(bchar, true);
//        BluetoothGattDescriptor descriptor = bchar.getDescriptor(BLEConstants.HeartRate.descriptor);
//        descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
//        bluetoothGatt.writeDescriptor(descriptor);
    }

    private void pair() {
        Log.e(TAG, "pair: ");
        if (!set) {
            Log.e(TAG, "pair: set to kia hai");
            set = true;
            charac = getMiliService().getCharacteristic(UUID_MILI_SERVICE);
        }
//        BluetoothGattCharacteristic chrt = getMiliService().getCharacteristic(
//                UUID_CHAR_pair);
        Log.e(TAG, "pair: what is this" + charac);
        charac.setValue(new byte[]{2});
        bluetoothGatt.writeCharacteristic(charac);
    }

    boolean set = false;
    BluetoothGattCharacteristic charac;

    private void request(UUID what) {
//        if (!set) {
//            set = true;
//        BluetoothGattCharacteristic charac = getMiliService().getCharacteristic(what);
//        }
        Log.e(TAG, "request: " + charac);
        bluetoothGatt.writeCharacteristic(charac);
        bluetoothGatt.readCharacteristic(charac);
        Log.e(TAG, "request: called read");
    }

    private BluetoothGattService getMiliService() {
        Log.e(TAG, "getMiliService: ");
        return bluetoothGatt.getService(UUID_MILI_SERVICE);
    }

    private void showNotification() {
    }

    private void shortVibrate() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                startVibrate();
                long c = System.currentTimeMillis();
                while (System.currentTimeMillis() - c < 500) ;
                stopVibrate();
            }
        }).start();
    }

    private void connect() {
        String macAddress = mAddress.getText().toString();
        Log.e(TAG, "connect: " + macAddress);
        try {
            // get the device with this address
            bluetoothDevice = bluetoothAdapter.getRemoteDevice(macAddress);
            bluetoothGatt = bluetoothDevice.connectGatt(this, true, bluetoothGattCallback);
            if (bluetoothGatt != null)
                Log.e(TAG, "connect: Not  null");
        } catch (Exception e) {
            Log.e(TAG, "connect: Wrong mac Address = " + macAddress);
        }
    }

    void startVibrate() {
        try {
            BluetoothGattCharacteristic bchar = bluetoothGatt.getService(BLEConstants.AlertNotification.service)
                    .getCharacteristic(BLEConstants.AlertNotification.alertCharacteristic);
            bchar.setValue(new byte[]{2});
            if (!bluetoothGatt.writeCharacteristic(bchar)) {
                Toast.makeText(this, "Failed start vibrate", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            Log.e(TAG, "startVibrate: Error " + e);
            e.printStackTrace();
        }
    }

    void stopVibrate() {
        BluetoothGattCharacteristic bchar = bluetoothGatt.getService(BLEConstants.AlertNotification.service)
                .getCharacteristic(BLEConstants.AlertNotification.alertCharacteristic);
        bchar.setValue(new byte[]{0});
        if (!bluetoothGatt.writeCharacteristic(bchar)) {
            Toast.makeText(this, "Failed stop vibrate", Toast.LENGTH_SHORT).show();
        }
    }

    private void listen() {
        Log.e(TAG, "listen: Starting listening");
        BluetoothGattCharacteristic bchar = bluetoothGatt.getService(BLEConstants.Basic.service)
                .getCharacteristic(BLEConstants.Basic.btn);
        bluetoothGatt.setCharacteristicNotification(bchar, true);
        BluetoothGattDescriptor descriptor = bchar.getDescriptor(BLEConstants.HeartRate.descriptor);
        descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
        bluetoothGatt.writeDescriptor(descriptor);
    }


    void stateConnected() {
        Log.e(TAG, "stateConnected: ");
        bluetoothGatt.discoverServices();
        mState.setText("Connected");
    }

    void stateDisconnected() {
        Log.e(TAG, "stateDisconnected: ");
        bluetoothGatt.disconnect();
        mState.setText("Disconnected");
    }

    final BluetoothGattCallback bluetoothGattCallback = new BluetoothGattCallback() {
        private static final String TAG = "CALLBACK";

        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            super.onConnectionStateChange(gatt, status, newState);
            Log.e(TAG, "onConnectionStateChange");
            if (newState == BluetoothProfile.STATE_CONNECTED) {
//                gatt.discoverServices();
                stateConnected();
            } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                stateDisconnected();
            }

        }

        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            super.onServicesDiscovered(gatt, status);
            Log.e("test", "onServicesDiscovered");
//            BluetoothGattService bgs = bluetoothGatt.getService(UUID_MILI_SERVICE);
            if (status == BluetoothGatt.GATT_SUCCESS) {
                Log.e(TAG, "onServicesDiscovered: SUCCESS");
//                pair();
            }
//            listenHeartRate();
        }

        @Override
        public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            super.onCharacteristicRead(gatt, characteristic, status);
            Log.e("test", "onCharacteristicRead newone");
            byte[] b = characteristic.getValue();
            Log.e(characteristic.getUuid().toString(), "state: " + rread
                    + " value:" + Arrays.toString(b));
            byte[] tmp = {b[2], b[1]};
            int sst = new BigInteger(tmp).intValue();
            Log.e(TAG, "onCharacteristicRead: " + "Steps = " + sst);
            int step = (0xff & b[0] | (0xff & b[1]) << 8);
            Log.e(TAG, "onCharacteristicRead: " + "Steps = " + step);
        }

        @Override
        public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            super.onCharacteristicWrite(gatt, characteristic, status);
            Log.e("test", "onCharacteristicWrite notify call");
//            request(UUID_CHAR_REALTIME_STEPS); // start with steps
        }

        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
            super.onCharacteristicChanged(gatt, characteristic);
            Log.e("test", "onCharacteristicChanged " + characteristic
                    + "\n" + characteristic.getUuid()
                    + "\n" + characteristic.getStringValue(1)
            );
//            byte[] data = characteristic.getValue();
//            mPhysicalAddress.setText(Arrays.toString(data));
            byte[] data = characteristic.getValue();
            Log.e(TAG, "onCharacteristicChanged: " + Arrays.toString(data));
            mPhysicalAddress.setText(Arrays.toString(data));
        }

        @Override
        public void onPhyUpdate(BluetoothGatt gatt, int txPhy, int rxPhy, int status) {
            Log.e(TAG, "onPhyUpdate: ");
            super.onPhyUpdate(gatt, txPhy, rxPhy, status);
        }

        @Override
        public void onPhyRead(BluetoothGatt gatt, int txPhy, int rxPhy, int status) {
            Log.e(TAG, "onPhyRead: ");
            super.onPhyRead(gatt, txPhy, rxPhy, status);
        }

        @Override
        public void onDescriptorRead(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {
            Log.e(TAG, "onDescriptorRead: ");
            super.onDescriptorRead(gatt, descriptor, status);
        }

        @Override
        public void onDescriptorWrite(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {
            Log.e(TAG, "onDescriptorWrite: " + descriptor.getCharacteristic());
            Log.e(TAG, "onDescriptorWrite: " + descriptor.getUuid());
            Log.e(TAG, "onDescriptorWrite: " + Arrays.toString(descriptor.getValue()));
            Log.e(TAG, "onDescriptorWrite: " + descriptor);
            super.onDescriptorWrite(gatt, descriptor, status);
        }

        @Override
        public void onReliableWriteCompleted(BluetoothGatt gatt, int status) {
            Log.e(TAG, "onReliableWriteCompleted: ");
            super.onReliableWriteCompleted(gatt, status);
        }

        @Override
        public void onReadRemoteRssi(BluetoothGatt gatt, int rssi, int status) {
            Log.e(TAG, "onReadRemoteRssi: ");
            super.onReadRemoteRssi(gatt, rssi, status);
        }

        @Override
        public void onMtuChanged(BluetoothGatt gatt, int mtu, int status) {
            Log.e(TAG, "onMtuChanged: ");
            super.onMtuChanged(gatt, mtu, status);
        }
    };

    //[15, 70, 0, -30, 7, 9, 8, 22, 30, 19, 22, -30, 7, 10, 15, 9, 10, 38, 22, 99]
    void getBoundedDevice() {
        mDeviceName = getIntent().getStringExtra(EXTRAS_DEVICE_NAME);
        mDeviceAddress = getIntent().getStringExtra(EXTRAS_DEVICE_ADDRESS);
        mPhysicalAddress.setText(mDeviceAddress);

        Set<BluetoothDevice> boundedDevice = bluetoothAdapter.getBondedDevices();
        for (BluetoothDevice bd : boundedDevice) {
            if (bd.getName().contains("MI Band 2")) {
                mPhysicalAddress.setText(bd.getAddress());
                Log.e(TAG, "getBoundedDevice: " + bd.getName());
            } else if (bd.getName().contains("MI") || bd.getName().contains("mi")) {
                mPhysicalAddress.setText(bd.getAddress());
                Log.e(TAG, "getBoundedDevice: " + bd.getName());
            } else {
                Log.e(TAG, "getBoundedDevice: else case " + bd.getName());
            }
        }
    }

    private void notifyCall() throws UnsupportedEncodingException {
        Byte incomingCall = 3;
        Byte missedCall = 3;
        Byte sms = 5;
        Byte highPriorityAlert = 8;//Sale un corazon pero sin confirmación de vibrado;
        Byte schedule = 7;//Sale un corazon pero sin confirmación de vibrado;
        //Byte any = 255;
        Byte custom = -1;
        Byte customHuami = -6;
        Byte alarmClock = 13;
        Byte alarmClockOrdinal = 6;


        Byte ALERT_LEVEL_NONE = 0;
        Byte ALERT_LEVEL_MESSAGE = 1;
        Byte ALERT_LEVEL_PHONE_CALL = 2;
        Byte ALERT_LEVEL_VIBRATE_ONLY = 3;


        Byte WECHAT = 0;
        Byte PENGUIN_1 = 1;
        Byte MI_CHAT_2 = 2;
        Byte FACEBOOK = 3;
        Byte TWITTER = 4;
        Byte MI_APP_5 = 5;
        Byte SNAPCHAT = 6;
        Byte WHATSAPP = 7;
        Byte RED_WHITE_FIRE_8 = 8;
        Byte CHINESE_9 = 9;
        Byte ALARM_CLOCK = 10;
        Byte APP_11 = 11;
        Byte INSTAGRAM = 12; //Con la llamada (3) , simbolo de llamada
        Byte CHAT_BLUE_13 = 13;
        Byte COW_14 = 14;
        Byte CHINESE_15 = 15;
        Byte CHINESE_16 = 16;
        Byte STAR_17 = 17;
        Byte APP_18 = 18;
        Byte CHINESE_19 = 19;
        Byte CHINESE_20 = 20;
        Byte CALENDAR = 21;
        Byte FACEBOOK_MESSENGER = 22;
        Byte VIBER = 23;
        Byte LINE = 24;
        Byte TELEGRAM = 25;
        Byte KAKAOTALK = 26;
        Byte SKYPE = 27;
        Byte VKONTAKTE = 28;
        Byte POKEMONGO = 29;
        Byte HANGOUTS = 30;
        Byte MI_31 = 31;
        Byte CHINESE_32 = 32;
        Byte CHINESE_33 = 33;
        Byte EMAIL = 34;
        Byte WEATHER = 35;
        Byte HR_WARNING_36 = 36;

        String value = mInput.getText().toString();

//        String []ip=mInput.getText().toString().split(" ");
//        byte[] b = new byte[ip.length];
//        for (int i=0;i<ip.length;i++)
//            b[i]=new Byte(ip[i]);

        byte notif = incomingCall;
        byte alert = MI_APP_5;

        byte[] param = new byte[]{notif, alert};
//        byte[] param = b;
        byte[] bytes = value.getBytes(StandardCharsets.US_ASCII);

        Log.e(TAG, "notifyCall: " + Arrays.toString(param) + Arrays.toString(bytes));
        //byte[] bytes      = value.getBytes();

        //join params and bytes
        byte[] mensaje = new byte[param.length + bytes.length];
        System.arraycopy(param, 0, mensaje, 0, param.length);
        System.arraycopy(bytes, 0, mensaje, param.length, bytes.length);

        Log.e(TAG, "notifyCall: "+(new String(mensaje, "UTF-8")).toString() );

        Log.e(TAG, "notifyCall: " + Arrays.toString(mensaje));
//        byte[] msg = new bytes(parametros, bytes);

        BluetoothGattCharacteristic bchar = bluetoothGatt.getService(BLEConstants.Notify._1811)
                .getCharacteristic(BLEConstants.Notify._2a46);
//        byte[]bc=bchar.getValue();
//        Log.e(TAG, "notifyCall: "+Arrays.toString(bc) );

        Log.e(TAG, "notifyCall: " + Arrays.toString(mensaje));
        bchar.setValue(mensaje);
        bluetoothGatt.writeCharacteristic(bchar);
    }
}
