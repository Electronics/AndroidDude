package com.example.laurie.androiddude;

import android.Manifest;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbManager;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.TextView;

import junit.framework.Test;

public class MainActivity extends AppCompatActivity {

    public static final int MY_PERMISSIONS_REQUEST = 1;

    public static final ProgrammerHandler prog = new ProgrammerHandler();
    public static final AvrDude dude = new AvrDude();
    public static final String pathToAvrdude = "/data/data/com.example.laurie.androiddude/lib/libavrdude.so";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        // Move the avrdude config file so avrdude can use it
        FileMover f = new FileMover();
        f.copyFile(this, "avrdude.conf");

        prog.m = MainActivity.this;
        prog.connectProgrammer();

        final ActionBar actionBar = getSupportActionBar();

        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
        actionBar.setDisplayShowTitleEnabled(true);

        ActionBar.Tab tab1 = actionBar
                .newTab()
                .setText("Tester")
                .setTabListener(new SupportFragmentTabListener<TestFragment>(R.id.flContainer, this,
                        "first", TestFragment.class));

        actionBar.addTab(tab1);
        actionBar.selectTab(tab1);

        ActionBar.Tab tab2 = actionBar
                .newTab()
                .setText("Upload Hex")
                .setTabListener(new SupportFragmentTabListener<BootloaderFragment>(R.id.flContainer, this,
                        "second", BootloaderFragment.class));

        actionBar.addTab(tab2);

        ActionBar.Tab tab3 = actionBar
                .newTab()
                .setText("Fuses")
                .setTabListener(new SupportFragmentTabListener<FusesFragment>(R.id.flContainer, this,
                        "second", FusesFragment.class));

        actionBar.addTab(tab3);

        ActionBar.Tab tab4 = actionBar
                .newTab()
                .setText("Settings")
                .setTabListener(new SupportFragmentTabListener<SettingsFragment>(R.id.flContainer, this,
                        "second", SettingsFragment.class));

        actionBar.addTab(tab4);

        // register for external storage
        if (ContextCompat.checkSelfPermission(MainActivity.this,
                Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {

            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(MainActivity.this,
                    Manifest.permission.READ_CONTACTS)) {

                // Show an expanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.

            } else {

                // No explanation needed, we can request the permission.

                ActivityCompat.requestPermissions(MainActivity.this,
                        new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                        MY_PERMISSIONS_REQUEST);

                // MY_PERMISSIONS_REQUEST_READ_CONTACTS is an
                // app-defined int constant. The callback method gets the
                // result of the request.
            }
        }

        // Register for device disconnects
        UsbManager mUsbManager = (UsbManager) getSystemService(Context.USB_SERVICE);

        //mPermissionIntent = PendingIntent.getBroadcast(this, 0, new Intent(ACTION_USB_PERMISSION), 0);
        IntentFilter filter = new IntentFilter();
        filter.addAction(mUsbManager.ACTION_USB_ACCESSORY_ATTACHED);
        filter.addAction(mUsbManager.ACTION_USB_ACCESSORY_DETACHED);
        registerReceiver(mUsbReceiver, filter);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.action_buttons, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            // action with ID action_settings was selected
            case R.id.action_open:
                System.out.println("Starting serial app...");
                Intent launchIntent = getPackageManager().getLaunchIntentForPackage("com.example.laurie.androidterm");
                if (launchIntent != null) {
                    startActivity(launchIntent);//null pointer check in case package name was not found
                } else {
                    System.out.println("Can't find it?...");
                }
                break;
            default:
                break;
        }

        return true;
    }

    @Override
    protected void onDestroy() {
        unregisterReceiver(mUsbReceiver);
        super.onDestroy();
    }

    private static final String ACTION_USB_PERMISSION = "com.android.example.USB_PERMISSION";
    private final BroadcastReceiver mUsbReceiver = new BroadcastReceiver() {

        public void onReceive(Context context, Intent intent) {
            System.out.println("HELLLOOOOOO");
            String action = intent.getAction();
            if (ACTION_USB_PERMISSION.equals(action)) {
                synchronized (this) {
                    UsbDevice device = (UsbDevice)intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);

                    if (intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED, false)) {
                        if(device != null){
                            //call method to set up device communication
                            prog.connectProgrammer();
                            System.out.println("TRYING TO RECONNECT");
                        }
                    }
                    else {
                        Log.d("Android Dude", "permission denied for device " + device);
                    }
                }
            }
        }
    };

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    // permission was granted, yay! Do the
                    // contacts-related task you need to do.

                } else {

                    // From http://stackoverflow.com/questions/2115758/how-do-i-display-an-alert-dialog-on-android
                    new AlertDialog.Builder(MainActivity.this)
                            //.setTitle("Why")
                            .setMessage("Why are you trying to use this application without giving me permission to files?!")
                            .setCancelable(false)
                            .setPositiveButton("exit", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    setResult(0);
                                    finish();
                                }
                            })
                            .setIcon(android.R.drawable.ic_dialog_alert)
                            .show();

                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                }
                return;
            }

            // other 'case' lines to check for other
            // permissions this app might request
        }
    }



}
