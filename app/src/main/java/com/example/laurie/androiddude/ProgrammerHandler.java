package com.example.laurie.androiddude;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbManager;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import junit.framework.Test;

/**
 * Created by Laurie on 03/11/2016.
 */

public class ProgrammerHandler {
    public UsbDevice programmer = null;
    Snackbar currentSnackbar = null;
    MainActivity m;

    private final View.OnClickListener snackbarListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            Log.d("AndroidDude","Retrying programming cable...");
            connectProgrammer();
        }
    };

    // We need to call this to get a snackbar instead of just showing one as it would only show once otherwise
    private Snackbar getSnackbar(View.OnClickListener clickListener) {
        return Snackbar.make(m.findViewById(R.id.activity_main),"Lost connection to programmer Cable", Snackbar.LENGTH_INDEFINITE).setAction("Retry", clickListener);
    }

    public UsbDevice findProgrammer() {
        UsbManager manager = (UsbManager) m.getSystemService(Context.USB_SERVICE);
        Log.d("AndroidDude", "Listing of USB devices:");
        for (UsbDevice d : manager.getDeviceList().values()) {
            if(d.getProductName().contains("C232HM")) {
                return d;
            }
        }
        return null;
    }

    public void connectProgrammer() {
        programmer = findProgrammer(); // try getting the c232
        if (programmer==null) {
            currentSnackbar = getSnackbar(snackbarListener);
            currentSnackbar.show();

        } else {
            Log.i("AndroidDude", "Programmer Connected");
            if(currentSnackbar!=null) currentSnackbar.dismiss();
            Toast.makeText(m, "Programmer Connected",Toast.LENGTH_SHORT).show();
        }
    }

    public void displayNoConnection(Context c) {
        new AlertDialog.Builder(c)
                //.setTitle("Why")
                .setMessage("Programmer is not connected!")
                .setCancelable(false)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        // Do nothing I guess?
                    }
                })
                .setIcon(android.R.drawable.ic_dialog_alert)
                .show();
    }

    public UsbDevice getProgrammer() {
        return programmer;
    }
    public void setProgrammer(UsbDevice d) {
        this.programmer = d;
    }

}
