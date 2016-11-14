package com.example.laurie.androiddude;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;


/**
 * A simple {@link Fragment} subclass.
 * Use the {@link TestFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class TestFragment extends Fragment implements ConnectionStatusable {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;


    public TestFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment TestFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static TestFragment newInstance(String param1, String param2) {
        TestFragment fragment = new TestFragment();
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

    Button b;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_test, container, false);

        final TextView t = (TextView) v.findViewById(R.id.testView);
        b = (Button) v.findViewById(R.id.button);

        b.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(MainActivity.prog.programmer==null) {
                    // We haven't connected to the programmer
                    MainActivity.prog.displayNoConnection(getActivity());
                }
                t.setText(""); // clear the view
                try{
                    Process testProcess = Runtime.getRuntime().exec("su && " + MainActivity.pathToAvrdude + " -C /sdcard/avrdude.conf -c " + SettingsFragment.programmer + " -p " + SettingsFragment.chip + " -v");
                    BufferedReader reader = new BufferedReader(
                            new InputStreamReader(testProcess.getErrorStream()));
                    int read;
                    char[] buffer = new char[200];
                    StringBuffer output = new StringBuffer();
                    while ((read = reader.read(buffer)) > 0) {
                        output.append(buffer, 0, read);
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
        });


        return v;
    }


    public void connectionStatus(boolean status) {
        b.setClickable(status);
    }

}
