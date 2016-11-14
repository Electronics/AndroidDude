package com.example.laurie.androiddude;


import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.provider.OpenableColumns;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URISyntaxException;

import static android.app.Activity.RESULT_OK;


/**
 * A simple {@link Fragment} subclass.
 * Use the {@link BootloaderFragment#newInstance} factory method to
 * create an instance of this fragment.
 */

public class BootloaderFragment extends Fragment implements ConnectionStatusable {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private static final int FILE_SELECT_CODE = 0;

    Button open, upload;
    TextView t,path;

    public BootloaderFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment BootloaderFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static BootloaderFragment newInstance(String param1, String param2) {
        BootloaderFragment fragment = new BootloaderFragment();
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
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v =  inflater.inflate(R.layout.fragment_bootloader, container, false);

        open = (Button) v.findViewById(R.id.open);
        upload = (Button) v.findViewById(R.id.upload);
        t = (TextView) v.findViewById(R.id.text);
        path = (TextView) v.findViewById(R.id.path);

        open.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // File chooser from http://stackoverflow.com/questions/7856959/android-file-chooser
                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.setType("*/*");
                intent.addCategory(Intent.CATEGORY_OPENABLE);

                try {
                    startActivityForResult(
                            Intent.createChooser(intent, "Select a File to Open"),
                            FILE_SELECT_CODE);
                } catch (android.content.ActivityNotFoundException ex) {
                    // Potentially direct the user to the Market with a Dialog
                    Toast.makeText(getActivity(), "Please install a File Manager.",
                            Toast.LENGTH_SHORT).show();
                }
            }
        });

        upload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Time to upload the hex file
                /*if(MainActivity.prog.programmer==null) {
                    // We haven't connected to the programmer
                    MainActivity.prog.displayNoConnection(getActivity());
                }*/
                t.setText(""); // clear the view
                try{
                    System.out.println("Preparing to upload hex file (/sdcard/upload.hex");
                    Process testProcess = Runtime.getRuntime().exec("su && " + MainActivity.pathToAvrdude + " -C /sdcard/avrdude.conf -c " + SettingsFragment.programmer + " -p " + SettingsFragment.chip + " -U flash:w:/sdcard/upload.hex");
                    BufferedReader reader = new BufferedReader(
                            new InputStreamReader(testProcess.getErrorStream()));
                    int read;
                    char[] buffer = new char[200];
                    StringBuffer output = new StringBuffer();
                    while ((read = reader.read(buffer)) > 0) {
                        output.append(buffer, 0, read);
                        t.setText(output.toString());
                        System.out.println(output.toString());
                    }
                    reader.close();

                    testProcess.waitFor();
                } catch(IOException e) {
                    Log.e("AndroidDude",e.toString());
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });

        return v;

    }

    // File chooser from http://stackoverflow.com/questions/7856959/android-file-chooser
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case FILE_SELECT_CODE:
                if (resultCode == RESULT_OK) {
                    // Get the Uri of the selected file
                    Uri uri = data.getData();
                    Log.d("Android Dude", "File Uri: " + uri.toString());

                    path.setText(getFileName(uri));

                    try {
                        InputStream s = getContext().getContentResolver().openInputStream(uri);
                        File f = new File("/sdcard/upload.hex");
                        FileOutputStream fo = new FileOutputStream(f);
                        byte[] content = new byte[1024];

                        while(s.read(content) != -1){
//                            System.out.println(content);
                            fo.write(content);
                        }
                        s.close();
                        fo.close();
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }



//                    Log.d("Android Dude", "File Path: " + path);
                    // Get the file instance
                    // File file = new File(path);
                    // Initiate the upload
                }
                break;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    public String getFileName(Uri uri) {
        String result = null;
        if (uri.getScheme().equals("content")) {
            Cursor cursor = getContext().getContentResolver().query(uri, null, null, null, null);
            try {
                if (cursor != null && cursor.moveToFirst()) {
                    result = cursor.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME));
                }
            } finally {
                cursor.close();
            }
        }
        if (result == null) {
            result = uri.getPath();
            int cut = result.lastIndexOf('/');
            if (cut != -1) {
                result = result.substring(cut + 1);
            }
        }
        return result;
    }

    public void connectionStatus(boolean status) {
        // disable some things
    }
}
