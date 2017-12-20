package com.jansen.sander.carrgbapp;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.ParcelUuid;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.SeekBar;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class MainActivity extends AppCompatActivity {
    protected String mac_arduino;

    protected boolean connected = false;

    protected int COLOR = 0;
    protected int COLOR_DELAY = 1;
    protected int IR_VALUE = 2;
    protected int red, green, blue, delay;
    protected String data;
    protected String ir_command="";
    protected SeekBar seekRed, seekGreen, seekBlue;
    private final static int REQUEST_ENABLE_BT = 1;

    IntentFilter filter = new IntentFilter();

    private static final String IR_BRIGHT_UP  = "0xF700FF";
    private static final String IR_BRIGHT_DOWN = "0xF7807F";
    private static final String IR_OFF = "0xF740BF";
    private static final String IR_ON  = "0xF7C03F";

    private static final String IR_FLASH = "0xF7D02F";
    private static final String IR_STROBE  = "0xF7F00F";
    private static final String IR_FADE  = "0xF7C837";
    private static final String IR_SMOOTH  = "0xF7E817";

    private static final String IR_RED  = "0xF720DF";
    private static final String IR_GREEN = "0xF7A05F";
    private static final String IR_BLUE  = "0xF7609F";
    private static final String IR_WHITE = "0xF7E01F";

    private static final String IR_ORANGE  = "0xF710EF";
    private static final String IR_DARK_YELLOW = "0xF730CF";
    private static final String IR_YELLOW = "0xF708F7";
    private static final String IR_STRAW_YELLOW = "0xF728D7";

    private static final String IR_PEA_GREEN = "0xF7906F";
    private static final String IR_CYAN  = "0xF7B04F";
    private static final String IR_LIGHT_BLUE  = "0xF78877";
    private static final String IR_SKY_BLUE  = "0xF7A857";

    private static final String IR_DARK_BLUE  = "0xF750AF";
    private static final String IR_DARK_PINK = "0xF7708F";
    private static final String IR_PINK  = "0xF748B7";
    private static final String IR_PURPLE  = "0xF76897";

    private FloatingActionButton
            fabBR_UP, fabBR_DO, fabOFF, fabON,
            fabRED, fabGREEN, fabBLUE, fabWHITE,
            fabORANGE, fabPEAGREEN, fabDARK_BLUE,
            fabDARK_YELLOW, fabCYAN, fabDARK_PINK,
            fabYELLOW, fabLIGHT_BLUE, fabPINK,
            fabSTRAW_YELLOW, fabSKY_BLUE, fabPURPLE,
            fabFLASH, fabSTROBE, fabFADE, fabSMOOTH, fabColor;

    private OutputStream outputStream;
    private InputStream inStream;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        new Thread(new Runnable() {
            public void run() {
                seekRed = findViewById(R.id.slideRed);
                seekGreen = findViewById(R.id.slideGreen);
                seekBlue = findViewById(R.id.slideBlue);

                seekRed.setOnSeekBarChangeListener(slideListener);
                seekGreen.setOnSeekBarChangeListener(slideListener);
                seekBlue.setOnSeekBarChangeListener(slideListener);
            }
        }).start();

        new Thread(new Runnable() {
            public void run() {
                fabBR_UP = findViewById(R.id.fabBR_UP);
                fabBR_DO = findViewById(R.id.fabBR_DO);
                fabOFF = findViewById(R.id.fabOFF);
                fabON = findViewById(R.id.fabON);

                fabBR_UP.setOnClickListener(fabListener);
                fabBR_DO.setOnClickListener(fabListener);
                fabOFF.setOnClickListener(fabListener);
                fabON.setOnClickListener(fabListener);

                fabON.setImageBitmap(textAsBitmap("ON ", 40, Color.WHITE));
                fabOFF.setImageBitmap(textAsBitmap("OFF", 40, Color.WHITE));
            }
        }).start();

        new Thread(new Runnable() {
            public void run() {
                fabRED = findViewById(R.id.fabRED);
                fabGREEN = findViewById(R.id.fabGREEN);
                fabBLUE = findViewById(R.id.fabBLUE);
                fabWHITE = findViewById(R.id.fabWHITE);

                fabRED.setOnClickListener(fabListener);
                fabGREEN.setOnClickListener(fabListener);
                fabBLUE.setOnClickListener(fabListener);
                fabWHITE.setOnClickListener(fabListener);

                fabRED.setImageBitmap(textAsBitmap("R", 40, Color.WHITE));
                fabGREEN.setImageBitmap(textAsBitmap("G", 40, Color.WHITE));
                fabBLUE.setImageBitmap(textAsBitmap("B", 40, Color.WHITE));
                fabWHITE.setImageBitmap(textAsBitmap("W", 40, Color.BLACK));
            }
        }).start();

        new Thread(new Runnable() {
            public void run() {
                fabORANGE = findViewById(R.id.fabORANGE);
                fabPEAGREEN = findViewById(R.id.fabPEA_GREEN);
                fabDARK_BLUE = findViewById(R.id.fabDARK_BLUE);
                fabDARK_YELLOW = findViewById(R.id.fabDARK_YELLOW);

                fabORANGE.setOnClickListener(fabListener);
                fabPEAGREEN.setOnClickListener(fabListener);
                fabDARK_BLUE.setOnClickListener(fabListener);
                fabDARK_YELLOW.setOnClickListener(fabListener);
            }
        }).start();

        new Thread(new Runnable() {
            public void run() {
                fabCYAN = findViewById(R.id.fabCYAN);
                fabDARK_PINK = findViewById(R.id.fabDARK_PINK);
                fabYELLOW = findViewById(R.id.fabYELLOW);
                fabLIGHT_BLUE = findViewById(R.id.fabLIGHT_BLUE);

                fabCYAN.setOnClickListener(fabListener);
                fabDARK_PINK.setOnClickListener(fabListener);
                fabYELLOW.setOnClickListener(fabListener);
                fabLIGHT_BLUE.setOnClickListener(fabListener);
            }
        }).start();

        new Thread(new Runnable() {
            public void run() {
                fabPINK = findViewById(R.id.fabPINK);
                fabSTRAW_YELLOW = findViewById(R.id.fabSTRAW_YELLOW);
                fabSKY_BLUE = findViewById(R.id.fabSKY_BLUE);
                fabPURPLE = findViewById(R.id.fabPURPLE);

                fabPINK.setOnClickListener(fabListener);
                fabSTRAW_YELLOW.setOnClickListener(fabListener);
                fabSKY_BLUE.setOnClickListener(fabListener);
                fabPURPLE.setOnClickListener(fabListener);
            }
        }).start();

        new Thread(new Runnable() {
            public void run() {
                fabFLASH = findViewById(R.id.fabFLASH);
                fabSTROBE = findViewById(R.id.fabSTROBE);
                fabFADE = findViewById(R.id.fabFADE);
                fabSMOOTH = findViewById(R.id.fabSMOOTH);
                fabColor = findViewById(R.id.fabColor);

                fabFLASH.setOnClickListener(fabListener);
                fabSTROBE.setOnClickListener(fabListener);
                fabFADE.setOnClickListener(fabListener);
                fabSMOOTH.setOnClickListener(fabListener);

                fabColor.setOnLongClickListener(longClickListener);
                fabColor.setOnClickListener(fabListener);
            }
        }).start();
    }

    @Override
    protected void onStart() {
        super.onStart();

        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        mac_arduino  = sharedPref.getString(SettingsActivity.MAC_ARDUINO, "98:D3:32:11:02:9D");

        try {
            init();
        } catch (IOException e) {
            e.printStackTrace();
        }

        filter.addAction(BluetoothDevice.ACTION_ACL_CONNECTED);
        filter.addAction(BluetoothDevice.ACTION_ACL_DISCONNECT_REQUESTED);
        filter.addAction(BluetoothDevice.ACTION_ACL_DISCONNECTED);
        this.registerReceiver(mReceiver, filter);
        try {
            init();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(mReceiver);
    }

    @Override
    protected void onResume() {
        super.onResume();
        registerReceiver(mReceiver, filter);
    }

    @Override
    public void onBackPressed() {
        // TODO Auto-generated method stub
        super.onBackPressed();
        try {
            init();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private final BroadcastReceiver mReceiver = new BroadcastReceiver(){
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);

            if (BluetoothDevice.ACTION_FOUND.equals(action)) {

            }
            else if (BluetoothDevice.ACTION_ACL_CONNECTED.equals(action)) {
            //Device is now connected
                Snackbar mySnackbar = Snackbar.make(getWindow().getDecorView().findViewById(android.R.id.content), "Connected", Snackbar.LENGTH_LONG);
                mySnackbar.show();
            }
            else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
            //Done searching
            }
            else if (BluetoothDevice.ACTION_ACL_DISCONNECT_REQUESTED.equals(action)) {
           //Device is about to disconnect
            }
            else if (BluetoothDevice.ACTION_ACL_DISCONNECTED.equals(action)) {
                connected = false;
                Snackbar mySnackbar = Snackbar.make(getWindow().getDecorView().findViewById(android.R.id.content), "Disconnected... Reconnecting...", Snackbar.LENGTH_LONG);
                mySnackbar.show();
                new Thread(new Runnable() {
                    public void run() {
                        try {
                            while (!connected){
                                if (connected){
                                    Snackbar mySnackbar = Snackbar.make(getWindow().getDecorView().findViewById(android.R.id.content), "Reconnected!", Snackbar.LENGTH_LONG);
                                    mySnackbar.show();
                                    break;
                                } else {
                                    init();
                                    Thread.sleep(3000);
                                }
                            }
                        } catch (IOException | InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }).start();

            }
        }
    };

    protected  FloatingActionButton.OnLongClickListener longClickListener = new FloatingActionButton.OnLongClickListener(){


        @Override
        public boolean onLongClick(View v) {
            if (v.getId() == findViewById(R.id.fabColor).getId()){
                saveNewColor();
            }
            return false;
        }
    };

    protected FloatingActionButton.OnClickListener fabListener = new FloatingActionButton.OnClickListener(){
        @Override
        public void onClick(View v) {
            if(v.getId() == findViewById(R.id.fabColor).getId()){
                Snackbar mySnackbar = Snackbar.make(getWindow().getDecorView().findViewById(android.R.id.content), "Long press to save custom color", Snackbar.LENGTH_LONG);
                mySnackbar.show();
                return;
            }
            if(v.getId() == findViewById(R.id.fabBR_UP).getId()) {
                ir_command = IR_BRIGHT_UP;
            }
            if(v.getId() == findViewById(R.id.fabBR_DO).getId()) {
                ir_command = IR_BRIGHT_DOWN;
            }
            if(v.getId() == findViewById(R.id.fabOFF).getId()) {
                ir_command = IR_OFF;
            }
            if(v.getId() == findViewById(R.id.fabON).getId()) {
                ir_command = IR_ON;
            }
            if(v.getId() == findViewById(R.id.fabRED).getId()) {
                ir_command = IR_RED;
            }
            if(v.getId() == findViewById(R.id.fabGREEN).getId()) {
                ir_command = IR_GREEN;
            }
            if(v.getId() == findViewById(R.id.fabBLUE).getId()) {
                ir_command = IR_BLUE;
            }
            if(v.getId() == findViewById(R.id.fabWHITE).getId()) {
                ir_command = IR_WHITE;
            }
            if(v.getId() == findViewById(R.id.fabORANGE).getId()) {
                ir_command = IR_ORANGE;
            }
            if(v.getId() == findViewById(R.id.fabDARK_YELLOW).getId()) {
                ir_command = IR_DARK_YELLOW;
            }
            if(v.getId() == findViewById(R.id.fabYELLOW).getId()) {
                ir_command = IR_YELLOW;
            }
            if(v.getId() == findViewById(R.id.fabSTRAW_YELLOW).getId()) {
                ir_command = IR_STRAW_YELLOW;
            }
            if(v.getId() == findViewById(R.id.fabPEA_GREEN).getId()) {
                ir_command = IR_PEA_GREEN;
            }
            if(v.getId() == findViewById(R.id.fabCYAN).getId()) {
                ir_command = IR_CYAN;
            }
            if(v.getId() == findViewById(R.id.fabLIGHT_BLUE).getId()) {
                ir_command = IR_LIGHT_BLUE;
            }
            if(v.getId() == findViewById(R.id.fabSKY_BLUE).getId()) {
                ir_command = IR_SKY_BLUE;
            }
            if(v.getId() == findViewById(R.id.fabDARK_BLUE).getId()) {
                ir_command = IR_DARK_BLUE;
            }
            if(v.getId() == findViewById(R.id.fabPINK).getId()) {
                ir_command = IR_PINK;
            }
            if(v.getId() == findViewById(R.id.fabDARK_PINK).getId()) {
                ir_command = IR_DARK_PINK;
            }
            if(v.getId() == findViewById(R.id.fabPURPLE).getId()) {
                ir_command = IR_PURPLE;
            }
            if(v.getId() == findViewById(R.id.fabFLASH).getId()) {
                ir_command = IR_FLASH;
            }
            if(v.getId() == findViewById(R.id.fabSTROBE).getId()) {
                ir_command = IR_STROBE;
            }
            if(v.getId() == findViewById(R.id.fabFADE).getId()) {
                ir_command = IR_FADE;
            }
            if(v.getId() == findViewById(R.id.fabSMOOTH).getId()) {
                ir_command = IR_SMOOTH;
            }
            try {
                send_data(IR_VALUE);
            } catch (IOException e) {
                e.printStackTrace();
            }

        }
    };


    protected SeekBar.OnSeekBarChangeListener slideListener = new SeekBar.OnSeekBarChangeListener() {
        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            red = ((SeekBar) findViewById(R.id.slideRed)).getProgress();
            green = ((SeekBar) findViewById(R.id.slideGreen)).getProgress();
            blue = ((SeekBar) findViewById(R.id.slideBlue)).getProgress();
            fabColor.setBackgroundTintList(ColorStateList.valueOf(Color.rgb(red,green,blue)));
        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {

        }

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {
            red = ((SeekBar) findViewById(R.id.slideRed)).getProgress();
            green = ((SeekBar) findViewById(R.id.slideGreen)).getProgress();
            blue = ((SeekBar) findViewById(R.id.slideBlue)).getProgress();
            try {
                send_data(COLOR);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    };

    protected void send_data(int type) throws IOException {
        if (type == COLOR) {

            data = "{\"red\":" + red + ",\"green\":" + green + ",\"blue\":" + blue + "}";
        }
        if (type == COLOR_DELAY){
            data = "{\"red\":" + red + ",\"green\":" + green + ",\"blue\":" + blue + ",\"delay\":" + delay +"}";
        }

        if (type == IR_VALUE){
            data = "{\"ir_val\":"+ ir_command +"}";
        }
        write(data);
    }

    private void init() throws IOException {

        new Thread(new Runnable() {
            public void run() {
                Snackbar mySnackbar;// = Snackbar.make(getWindow().getDecorView().findViewById(android.R.id.content), "Connecting...", Snackbar.LENGTH_INDEFINITE);
                //mySnackbar.show();
                boolean isPaired = false;
                BluetoothAdapter blueAdapter = BluetoothAdapter.getDefaultAdapter();

                if (blueAdapter != null) {
                    if (blueAdapter.isEnabled()) {
                        Set<BluetoothDevice> pairedDevices = blueAdapter.getBondedDevices();

                        if (pairedDevices.size() > 0) {
                            // There are paired devices. Get the name and address of each paired device.
                            for (BluetoothDevice device : pairedDevices) {
                                String deviceName = device.getName();
                                String deviceHardwareAddress = device.getAddress(); // MAC address
                                Log.e("mac", deviceHardwareAddress);
                                if(deviceHardwareAddress.equalsIgnoreCase(mac_arduino)){
                                    Log.e("Bluetooth:", "Should be connected");
                                    ParcelUuid[] uuids = device.getUuids();
                                    BluetoothSocket socket = null;
                                    try {
                                        socket = device.createRfcommSocketToServiceRecord(uuids[0].getUuid());
                                        socket.connect();
                                        outputStream = socket.getOutputStream();
                                        inStream = socket.getInputStream();
                                        connected = true;
                                    } catch (IOException e) {
                                        e.printStackTrace();
                                    }

                                    isPaired = true;
                                    break;
                                }
                            }
                        }
                        if(!isPaired){
                            mySnackbar = Snackbar.make(getWindow().getDecorView().findViewById(android.R.id.content), "Your device is not yet paired or the MAC-address is wrong in the settings.", Snackbar.LENGTH_LONG);
                            mySnackbar.show();
                            Log.e("error", "No appropriate paired devices.");
                        }
                    } else {
                        Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                        startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
                        Log.e("error", "Bluetooth is disabled.");
                    }
                }

            }
        }).start();
    }

    public void write(String s) throws IOException {
        try {
            if (outputStream != null) {
                Log.v("Data", data);
                outputStream.write(s.getBytes());
            } else{
                Snackbar mySnackbar = Snackbar.make(getWindow().getDecorView().findViewById(android.R.id.content), "Your device is probably not paired or connected", Snackbar.LENGTH_LONG);
                mySnackbar.show();
                init();
            }
        } catch (Exception e){

        }

    }

    public void run() {
        final int BUFFER_SIZE = 1024;
        byte[] buffer = new byte[BUFFER_SIZE];
        int bytes = 0;
        int b = BUFFER_SIZE;

        while (true) {
            try {
                bytes = inStream.read(buffer, bytes, BUFFER_SIZE - bytes);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static Bitmap textAsBitmap(String text, float textSize, int textColor) {
        Paint paint = new Paint(1);
        paint.setTextSize(textSize);
        paint.setColor(textColor);
        paint.setTextAlign(Paint.Align.LEFT);
        float baseline = -paint.ascent(); // ascent() is negative
        int width = (int) (paint.measureText(text) + 0.0f); // round
        int height = (int) (baseline + paint.descent() + 0.0f);
        Bitmap image = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);

        Canvas canvas = new Canvas(image);
        canvas.drawText(text, 0, baseline, paint);
        return image;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.settings_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            startActivity(new Intent(this, SettingsActivity.class));
            return true;
        }

        if (id == R.id.action_load_color) {
            startActivity(new Intent(this, ColorBrowsingActivity.class));
            return true;
        }
        if (id == R.id.action_reconnect) {
            try {
                init();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    protected Boolean saveNewColor(){
        try {
            new SaveNewColorTask(new CustomColor(red, green, blue)).execute((Void)null);
            return true;
        } catch (Exception e){
            Snackbar mySnackbar = Snackbar.make(getWindow().getDecorView().findViewById(android.R.id.content), "Could not save color", Snackbar.LENGTH_LONG);
            mySnackbar.show();
        }
        return false;
    }

    public class SaveNewColorTask extends AsyncTask<Void, Void, Boolean> {
        private final CustomColor newColor;

        SaveNewColorTask(CustomColor newColor){
            this.newColor = newColor;
            Log.e("Color", "RGB: " + newColor.getRed()+","+ newColor.getGreen()+","+newColor.getBlue()+ "   id:" + newColor.getCid() );
        }

        @Override
        protected Boolean doInBackground(Void... voids) {
            AppDatabase.getInstance(getApplicationContext()).color_db_api()
                    .insertAllColors(newColor);
            return true;
        }

        @Override
        protected void onPostExecute(final Boolean success) {
            if (success) {

            } else {
                Snackbar mySnackbar = Snackbar.make(getWindow().getDecorView().findViewById(android.R.id.content), "Could not save color", Snackbar.LENGTH_LONG);
                mySnackbar.show();
            }
        }
    }
}
