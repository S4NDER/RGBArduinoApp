package com.jansen.sander.carrgbapp.activities;

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
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.SeekBar;

import com.jansen.sander.carrgbapp.AppDatabase;
import com.jansen.sander.carrgbapp.R;
import com.jansen.sander.carrgbapp.classes.CustomColor;
import com.jansen.sander.carrgbapp.classes.CustomColorDataAdapter;

import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class MainActivity extends AppCompatActivity {
    protected String mac_arduino;
    private static Context mContext;
    private BluetoothSocket socket = null;

    protected static Snackbar mySnackbar;

    protected boolean connected = false;
    protected boolean fabLongPressed = false;

    protected Boolean beatsEnabled = false;

    protected final int COLOR = 0;
    protected final int COLOR_DELAY = 1;
    protected final int IR_VALUE = 2;
    protected final int BEATS = 3;
    protected int red, green, blue, delay;
    protected String data;
    protected String ir_command="";
    protected SeekBar seekRed;
    protected SeekBar seekGreen;
    protected SeekBar seekBlue;
    static List<SeekBar> sliders = new ArrayList<>();
    private final static int REQUEST_ENABLE_BT = 1;

    protected IntentFilter filter = new IntentFilter();

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
            fabFLASH, fabSTROBE, fabFADE, fabSMOOTH, fabColor, fabBeats;

    private OutputStream outputStream;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mContext = this;
        mySnackbar = Snackbar.make(getWindow().getDecorView().findViewById(android.R.id.content), R.string.connected, Snackbar.LENGTH_LONG);
        View sbView = mySnackbar.getView();
        sbView.setBackgroundColor(Color.parseColor("#3C4149"));

        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        mac_arduino  = sharedPref.getString(SettingsActivity.MAC_ARDUINO, "98:D3:32:11:02:9D");

        try {
            init();
        } catch (IOException e) {
            e.printStackTrace();
        }

        new Thread(new Runnable() {
            public void run() {
                seekRed = findViewById(R.id.slideRed);
                seekGreen = findViewById(R.id.slideGreen);
                seekBlue = findViewById(R.id.slideBlue);

                seekRed.setOnSeekBarChangeListener(slideListener);
                seekGreen.setOnSeekBarChangeListener(slideListener);
                seekBlue.setOnSeekBarChangeListener(slideListener);
                sliders.add(seekRed);
                sliders.add(seekGreen);
                sliders.add(seekBlue);
            }
        }).start();

        new Thread(new Runnable() {
            public void run() {
                fabBR_UP = findViewById(R.id.fabBR_UP);
                fabBR_DO = findViewById(R.id.fabBR_DO);
                fabOFF = findViewById(R.id.fabOFF);
                fabON = findViewById(R.id.fabON);

                fabRED = findViewById(R.id.fabRED);
                fabGREEN = findViewById(R.id.fabGREEN);
                fabBLUE = findViewById(R.id.fabBLUE);
                fabWHITE = findViewById(R.id.fabWHITE);

                fabORANGE = findViewById(R.id.fabORANGE);
                fabYELLOW = findViewById(R.id.fabYELLOW);
                fabDARK_YELLOW = findViewById(R.id.fabDARK_YELLOW);
                fabSTRAW_YELLOW = findViewById(R.id.fabSTRAW_YELLOW);

                fabPEAGREEN = findViewById(R.id.fabPEA_GREEN);
                fabDARK_BLUE = findViewById(R.id.fabDARK_BLUE);
                fabCYAN = findViewById(R.id.fabCYAN);
                fabLIGHT_BLUE = findViewById(R.id.fabLIGHT_BLUE);

                fabDARK_PINK = findViewById(R.id.fabDARK_PINK);
                fabPINK = findViewById(R.id.fabPINK);
                fabSKY_BLUE = findViewById(R.id.fabSKY_BLUE);
                fabPURPLE = findViewById(R.id.fabPURPLE);

                fabFLASH = findViewById(R.id.fabFLASH);
                fabSTROBE = findViewById(R.id.fabSTROBE);
                fabFADE = findViewById(R.id.fabFADE);
                fabSMOOTH = findViewById(R.id.fabSMOOTH);
                fabColor = findViewById(R.id.fabColor);

                fabRED.setImageBitmap(textAsBitmap("R", 40, Color.WHITE));
                fabGREEN.setImageBitmap(textAsBitmap("G", 40, Color.WHITE));
                fabBLUE.setImageBitmap(textAsBitmap("B", 40, Color.WHITE));
                fabWHITE.setImageBitmap(textAsBitmap("W", 40, Color.BLACK));
                fabON.setImageBitmap(textAsBitmap("ON ", 40, Color.WHITE));
                fabOFF.setImageBitmap(textAsBitmap("OFF", 40, Color.WHITE));

                fabBeats = findViewById(R.id.fabBeat);

                FloatingActionButton[] fabs = {fabBR_UP, fabBR_DO, fabOFF, fabON, fabRED, fabGREEN,
                        fabBLUE, fabWHITE,fabORANGE, fabPEAGREEN, fabDARK_BLUE, fabDARK_YELLOW,
                        fabCYAN, fabDARK_PINK, fabYELLOW, fabLIGHT_BLUE, fabPINK, fabSTRAW_YELLOW,
                        fabSKY_BLUE, fabPURPLE, fabFLASH, fabSTROBE, fabFADE, fabSMOOTH, fabColor, fabBeats};

                for (FloatingActionButton  fabX: fabs ){
                    fabX.setOnClickListener(fabListener);
                }
                fabColor.setOnLongClickListener(longClickListener);
            }
        }).start();

        filter.addAction(BluetoothDevice.ACTION_ACL_CONNECTED);
        filter.addAction(BluetoothDevice.ACTION_ACL_DISCONNECT_REQUESTED);
        filter.addAction(BluetoothDevice.ACTION_ACL_DISCONNECTED);
        this.registerReceiver(mReceiver, filter);
    }

    @Override
    protected void onStart() {
        super.onStart();

        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        mac_arduino  = sharedPref.getString(SettingsActivity.MAC_ARDUINO, "98:D3:32:11:02:9D");
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

    public static List<SeekBar> getSliders(){
        return sliders;
    }


    private final BroadcastReceiver mReceiver = new BroadcastReceiver(){
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);

            if (BluetoothDevice.ACTION_ACL_CONNECTED.equals(action)) { //Device is now connected
                mySnackbar.setText(R.string.connected).show();
            }
            else if (BluetoothDevice.ACTION_ACL_DISCONNECTED.equals(action)) {
                connected = false;
                mySnackbar.setText(R.string.reconnecting).show();
                new Thread(new Runnable() {
                    public void run() {
                        try {
                            while (!connected){
                                if (connected){
                                    mySnackbar.setText(R.string.connected).show();
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
                fabLongPressed = true;
                saveNewColor();
            }
            return false;
        }
    };

    protected FloatingActionButton.OnClickListener fabListener = new FloatingActionButton.OnClickListener(){
        @Override
        public void onClick(View v) {
            int type = IR_VALUE;
            switch (v.getId()){
                case R.id.fabColor:if(!fabLongPressed){mySnackbar.setText(R.string.longPressToSave).show();} else {fabLongPressed = false;}return;
                case R.id.fabBR_UP : ir_command = IR_BRIGHT_UP; break;
                case R.id.fabBR_DO: ir_command = IR_BRIGHT_DOWN; break;
                case R.id.fabOFF: ir_command = IR_OFF; break;
                case R.id.fabON: ir_command = IR_ON; break;
                case R.id.fabRED: ir_command = IR_RED; break;
                case R.id.fabGREEN: ir_command = IR_GREEN; break;
                case R.id.fabBLUE: ir_command = IR_BLUE; break;
                case R.id.fabWHITE: ir_command = IR_WHITE; break;
                case R.id.fabORANGE: ir_command = IR_ORANGE; break;
                case R.id.fabDARK_YELLOW: ir_command = IR_DARK_YELLOW; break;
                case R.id.fabYELLOW: ir_command = IR_YELLOW; break;
                case R.id.fabSTRAW_YELLOW: ir_command = IR_STRAW_YELLOW; break;
                case R.id.fabPEA_GREEN: ir_command = IR_PEA_GREEN; break;
                case R.id.fabCYAN: ir_command = IR_CYAN; break;
                case R.id.fabLIGHT_BLUE: ir_command = IR_LIGHT_BLUE; break;
                case R.id.fabSKY_BLUE: ir_command = IR_SKY_BLUE; break;
                case R.id.fabDARK_BLUE: ir_command = IR_DARK_BLUE; break;
                case R.id.fabPINK: ir_command = IR_PINK; break;
                case R.id.fabDARK_PINK: ir_command = IR_DARK_PINK; break;
                case R.id.fabPURPLE: ir_command = IR_PURPLE; break;
                case R.id.fabFLASH: ir_command = IR_FLASH; break;
                case R.id.fabSTROBE: ir_command = IR_STROBE; break;
                case R.id.fabFADE: ir_command = IR_FADE; break;
                case R.id.fabSMOOTH: ir_command = IR_SMOOTH; break;
                case R.id.fabBeat : beatsEnabled = !beatsEnabled; type = BEATS; break;
            }
            try {
                if (type == BEATS){
                    send_data(BEATS);
                } else {
                    send_data(IR_VALUE);
                }
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

            if ((!fromUser)){
                try {
                    send_data(COLOR);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
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
        switch (type){
            case COLOR: data = "{\"red\":" + red + ",\"green\":" + green + ",\"blue\":" + blue + "}"; break;
            case COLOR_DELAY: data = "{\"red\":" + red + ",\"green\":" + green + ",\"blue\":" + blue + ",\"delay\":" + delay +"}"; break;
            case IR_VALUE: data = "{\"ir_val\":"+ ir_command +"}";break;
            case BEATS : data = "{\"beats\":"+ beatsEnabled +"}"; break;
        }
        write(data);
    }

    private void init() throws IOException {
        new Thread(new Runnable() {
            public void run() {
                if(socket != null){
                    try {
                        socket.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                Snackbar mySnackbar;
                mySnackbar = Snackbar.make(getWindow().getDecorView().findViewById(android.R.id.content), R.string.connected, Snackbar.LENGTH_LONG);
                View sbView = mySnackbar.getView();
                sbView.setBackgroundColor(Color.parseColor("#3C4149"));

                boolean isPaired = false;
                BluetoothAdapter blueAdapter = BluetoothAdapter.getDefaultAdapter();

                if (blueAdapter != null) {
                    if (blueAdapter.isEnabled()) {
                        Set<BluetoothDevice> pairedDevices = blueAdapter.getBondedDevices();
                        if (pairedDevices.size() > 0) {
                            // There are paired devices. Get the name and address of each paired device.
                            for (BluetoothDevice device : pairedDevices) {
                                String deviceHardwareAddress = device.getAddress(); // MAC address
                                if(deviceHardwareAddress.equalsIgnoreCase(mac_arduino)){
                                    ParcelUuid[] uuids = device.getUuids();
                                    try {
                                        socket = null;
                                        socket = device.createRfcommSocketToServiceRecord(uuids[0].getUuid());
                                        socket.connect();
                                        outputStream = socket.getOutputStream();
                                        socket.getInputStream();
                                        connected = true;
                                    } catch (IOException e) {
                                    }
                                    isPaired = true;
                                    break;
                                }
                            }
                        }
                        if(!isPaired){
                            mySnackbar.setText(R.string.notPairedWrongMac).show();
                        }
                    } else {
                        Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                        startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
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
                mySnackbar.setText(R.string.notPairedConnected).show();
                if (socket != null){socket.close();}
                init();
            }
        } catch (Exception e){
            if (socket != null){socket.close();}
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
            mySnackbar.setText(R.string.reconnecting).show();
            try {
                init();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public static Context getContext(){
        return mContext;
    }

    protected Boolean saveNewColor(){
        try {
            new SaveNewColorTask(new CustomColor(red, green, blue)).execute((Void)null);
            return true;
        } catch (Exception e){
            mySnackbar.setText(R.string.errorSave).show();
        }
        return false;
    }

    public class SaveNewColorTask extends AsyncTask<Void, Void, Boolean> {
        private final CustomColor newColor;
        private boolean success = false;
        private List<CustomColor> allSavedColors = new ArrayList<>();
        private boolean alreadySaved = false;

        SaveNewColorTask(CustomColor newColor){
            this.newColor = newColor;
        }

        @Override
        protected Boolean doInBackground(Void... voids) {
            allSavedColors = AppDatabase.getInstance(getApplicationContext()).color_db_api().getStoredColors();
            for (CustomColor colorX : allSavedColors){
                if ((colorX.getRed()==newColor.getRed()) &(colorX.getGreen()==newColor.getGreen()) &(colorX.getBlue()== newColor.getBlue())){
                    alreadySaved = true;
                    break;
                }
            }
            if(!alreadySaved){
                AppDatabase.getInstance(getApplicationContext()).color_db_api().insertAllColors(newColor);
                success = true;
            } else {
                success = false;
            }
            return success;
        }

        @Override
        protected void onPostExecute(final Boolean success) {
            if (success) {
                mySnackbar.setText(R.string.saved).show();
            } else {
                mySnackbar.setText(R.string.duplicate).show();
            }
        }
    }
}
