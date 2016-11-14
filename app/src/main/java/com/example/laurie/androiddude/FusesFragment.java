package com.example.laurie.androiddude;


import android.Manifest;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.GridLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link FusesFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class FusesFragment extends Fragment implements ConnectionStatusable, Chipchange {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    ArrayList<CheckBox> highFuse = new ArrayList<>();
    ArrayList<CheckBox> lowFuse = new ArrayList<>();
    ArrayList<CheckBox> extFuse = new ArrayList<>();
    Button readFuses,writeFuses;

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    TextView t;


    public FusesFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment FusesFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static FusesFragment newInstance(String param1, String param2) {
        FusesFragment fragment = new FusesFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public void onResume() {
        System.out.println("Resumed with chip "+SettingsFragment.chip);
        chipchange(SettingsFragment.chip);
        super.onResume();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v =  inflater.inflate(R.layout.fragment_fuses, container, false);
        GridLayout fuses = (GridLayout) v.findViewById(R.id.fusesContainer);

        highFuse.clear();
        lowFuse.clear();
        extFuse.clear();

        for (int i = 0; i < fuses.getChildCount(); i=i+2) {
            //int resourceId = fuses.getChildAt(i).getId();
            // Come out in order HF0, LF0, HF1, LF1...
            if(fuses.getChildAt(i) instanceof CheckBox) highFuse.add((CheckBox) fuses.getChildAt(i));
            if(fuses.getChildAt(i+1) instanceof CheckBox) lowFuse.add((CheckBox) fuses.getChildAt(i+1));
        }
        // Now get the extension fuses
        extFuse.add((CheckBox) v.findViewById(R.id.EF0));
        extFuse.add((CheckBox) v.findViewById(R.id.EF1));
        extFuse.add((CheckBox) v.findViewById(R.id.EF2));

        readFuses = (Button) v.findViewById(R.id.read);
        writeFuses = (Button) v.findViewById(R.id.write);
        t = (TextView) v.findViewById(R.id.text);

        System.out.println("Re-generated fuses view");
        chipchange(SettingsFragment.chip); // Set the text of the checkboxes


        readFuses.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(MainActivity.prog.programmer==null) {
                    // We haven't connected to the programmer
                    MainActivity.prog.displayNoConnection(getActivity());
                    return;
                }
                t.setText("");
                try{
                    Process testProcess = Runtime.getRuntime().exec("su && " + MainActivity.pathToAvrdude + " -C /sdcard/avrdude.conf -c " + SettingsFragment.programmer + " -p " + SettingsFragment.chip + " -U lfuse:r:/sdcard/low_fuse.hex:h -U hfuse:r:/sdcard/high_fuse.hex:h -U efuse:r:/sdcard/ex_fuse.hex:h");
                    BufferedReader reader = new BufferedReader(
                            new InputStreamReader(testProcess.getErrorStream()));
                    int read;
                    char[] buffer = new char[200];
                    StringBuffer output = new StringBuffer();
                    while ((read = reader.read(buffer)) > 0) {
                        output.append(buffer, 0, read);
                        // write it out here
                        t.setText(output.toString());

                    }
                    reader.close();

                    testProcess.waitFor();
                } catch(IOException e) {
                    Log.e("AndroidDude",e.toString());
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                // Fuses are active low
                setCheckboxes(lowFuse,~Integer.parseInt(readFuse("/sdcard/low_fuse.hex").replace("0x","").replace("\n",""),16));
                setCheckboxes(highFuse,~Integer.parseInt(readFuse("/sdcard/high_fuse.hex").replace("0x","").replace("\n",""),16));
                setCheckboxes(extFuse,~Integer.parseInt(readFuse("/sdcard/ex_fuse.hex").replace("0x","").replace("\n",""),16));


            }
        });

        writeFuses.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(MainActivity.prog.programmer==null) {
                    // We haven't connected to the programmer
                    MainActivity.prog.displayNoConnection(getActivity());
                    return;
                }
                t.setText("");
                int hFuse = 0;
                int lFuse = 0;
                int eFuse = 0;

                for (int i = 0; i < highFuse.size(); i++) {
                    if(!highFuse.get(i).isChecked()) hFuse += Math.pow(2,i);
                    if(!lowFuse.get(i).isChecked()) lFuse += Math.pow(2,i);
                }
                for (int i = 0; i < extFuse.size(); i++) {
                    if(!extFuse.get(i).isChecked()) eFuse += Math.pow(2,i);
                }
                eFuse+=255-7; // The unused bits get set to 1 (disabled)
                //t.setText("Added things up and got: "+hFuse+" "+lFuse+" "+eFuse);

                final int final_hFuse=hFuse; // Allow the variables to actually be passed through to a callback
                final int final_lFuse=lFuse;
                final int final_eFuse=eFuse;

                new AlertDialog.Builder(getActivity())
                        .setTitle("Are you sure?")
                        .setMessage("You will be setting the fuses to: LF=0x"+Integer.toHexString(lFuse)+" HF=0x"+Integer.toHexString(hFuse)+" EF=0x"+Integer.toHexString(eFuse))
                        .setCancelable(true)
                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                // Write the fuses!
                                t.setText("");

                                try{
                                    Process testProcess = Runtime.getRuntime().exec("su && " + MainActivity.pathToAvrdude + " -C /sdcard/avrdude.conf -c " + SettingsFragment.programmer + " -p " + SettingsFragment.chip + " -U lfuse:w:0x"+Integer.toHexString(final_lFuse)+":m -U hfuse:w:0x"+Integer.toHexString(final_hFuse)+":m -U efuse:w:0x"+Integer.toHexString(final_eFuse)+":m");
                                    BufferedReader reader = new BufferedReader(
                                            new InputStreamReader(testProcess.getErrorStream()));
                                    int read;
                                    char[] buffer = new char[200];
                                    StringBuffer output = new StringBuffer();
                                    while ((read = reader.read(buffer)) > 0) {
                                        output.append(buffer, 0, read);
                                        // write it out here
                                        t.setText(output.toString());

                                    }
                                    reader.close();

                                    testProcess.waitFor();
                                } catch(IOException e) {
                                    Log.e("AndroidDude",e.toString());
                                } catch (InterruptedException e) {
                                    e.printStackTrace();
                                }
                            }
                        })
                        .setNegativeButton("No", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                // Do nothing
                            }
                        })
                        .setIcon(android.R.drawable.ic_dialog_alert)
                        .show();
            }
        });




        return v;
    }

    private void setCheckboxes(ArrayList<CheckBox> list, int fuses) {
        Log.i("Android Dude","Setting some checkboxes to "+fuses);
        for (int i = 0; i < list.size(); i++) {
            if(((fuses >> i) & 1)==1) list.get(i).setChecked(true);
            else list.get(i).setChecked(false);
        }
    }

    private String readFuse(String path) {

        try (BufferedReader reader = new BufferedReader(new FileReader(path))) {
            StringBuilder builder = new StringBuilder();
            String line = reader.readLine();

            while (line!=null) {
                builder.append(line);
                builder.append(System.lineSeparator());
                line = reader.readLine();
            }
            String fuse = builder.toString();
            return fuse;
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }


    public void connectionStatus(boolean status) {

    }

    public void chipchange(String newChip) { // should be called if we change which chip we are using
        if(newChip.contains("m644p")) {
            m644p();
            System.out.println(" Was a m644p");
        } else {
            System.out.println("Not a m644p");
            highFuse.get(0).setText("[0]");
            highFuse.get(1).setText("[1]");
            highFuse.get(2).setText("[2]");
            highFuse.get(3).setText("[3]");
            highFuse.get(4).setText("[4]");
            highFuse.get(5).setText("[5]");
            highFuse.get(6).setText("[6]");
            highFuse.get(7).setText("[7]");

            lowFuse.get(0).setText("[0]");
            lowFuse.get(1).setText("[1]");
            lowFuse.get(2).setText("[2]");
            lowFuse.get(3).setText("[3]");
            lowFuse.get(4).setText("[4]");
            lowFuse.get(5).setText("[5]");
            lowFuse.get(6).setText("[6]");
            lowFuse.get(7).setText("[7]");

            extFuse.get(0).setText("[0]");
            extFuse.get(1).setText("[1]");
            extFuse.get(2).setText("[2]");
        }
    }

    private void m644p() {
        highFuse.get(0).setText("[0] Boot Reset Vector");
        highFuse.get(1).setText("[1] Boot Size bit0");
        highFuse.get(2).setText("[2] Boot size bit1");
        highFuse.get(3).setText("[3] EEPROM preserve");
        highFuse.get(4).setText("[4] Watchdog Timer");
        highFuse.get(5).setText("[5] Serial Communication");
        highFuse.get(6).setText("[6] JTAG enable");
        highFuse.get(7).setText("[7] Boot Reset Vector");

        lowFuse.get(0).setText("[0] Clock Select bit0");
        lowFuse.get(1).setText("[1] Clock Select bit1");
        lowFuse.get(2).setText("[2] Clock Select bit2");
        lowFuse.get(3).setText("[3] Clock Select bit3");
        lowFuse.get(4).setText("[4] Start-up time bit0");
        lowFuse.get(5).setText("[5] Start-up time bit1");
        lowFuse.get(6).setText("[6] Clock output");
        lowFuse.get(7).setText("[7] Clock divide by 8");

        extFuse.get(0).setText("[0] Brown-out bit0");
        extFuse.get(1).setText("[1] Brown-out bit1");
        extFuse.get(2).setText("[2] Brown-out bit2");
    }

}
