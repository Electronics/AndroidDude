package com.example.laurie.androiddude;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * Created by Laurie on 03/11/2016. from code from James Adams
 */

public class AvrDude {
    private String pathToAvrdude = "/data/data/com.example.laurie.androiddude/lib/libavrdude.so";


    interface USBCallback {
        void methodToCallBack(boolean success, String output);
    }

    public void testConnection(String programmer, String chip, USBCallback c){
        try {
            Process process = Runtime.getRuntime().exec(
                    new String[]{
                            "su","&&",
                            pathToAvrdude,"-C", "/sdcard/avrdude.conf"
                            ,"-p",chip
                            ,"-c",programmer
                            ,"-v"
                    });

            BufferedReader reader = new BufferedReader(
                    new InputStreamReader(process.getErrorStream()));
            int read;
            char[] buffer = new char[200];
            StringBuffer output = new StringBuffer();
            while ((read = reader.read(buffer)) > 0) {
                output.append(buffer, 0, read);
//                    ((TextView) findViewById(R.id.hw)).setText(output.toString());
            }
            reader.close();

            process.waitFor();

            System.out.println("OUTPUT " + output.toString());

            c.methodToCallBack((process.exitValue() == 0) && (output.toString().contains("signature"))
                    ,output.toString());

        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
