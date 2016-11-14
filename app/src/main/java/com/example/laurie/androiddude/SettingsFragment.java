package com.example.laurie.androiddude;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import java.util.ArrayList;
import java.util.List;


/**
 * A simple {@link Fragment} subclass.
 * Use the {@link SettingsFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class SettingsFragment extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public static String programmer = "C232HM";
    public static String chip = "m644p";

    Spinner chipSpinner,programmerSpinner;

    public SettingsFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment SettingsFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static SettingsFragment newInstance(String param1, String param2) {
        SettingsFragment fragment = new SettingsFragment();
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

        View v = inflater.inflate(R.layout.fragment_settings, container, false);

        chipSpinner = (Spinner) v.findViewById(R.id.chip);
        programmerSpinner = (Spinner) v.findViewById(R.id.programmer);

        // Chip spinner
        List<String> chipArray = new ArrayList<String>();
        chipArray.add("m644p");
        chipArray.add("m128p");

        ArrayAdapter<String> chipAdapter = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_spinner_item, chipArray);
        chipAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        chipSpinner.setAdapter(chipAdapter);

        // Programmer spinner
        List<String> programmerArray = new ArrayList<String>();
        programmerArray.add("C232HM");

        ArrayAdapter<String> programmerAdapter = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_spinner_item, programmerArray);
        programmerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        programmerSpinner.setAdapter(programmerAdapter);

        chipSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                chip = chipSpinner.getSelectedItem().toString();
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
                System.out.println("Nothing selected");
            }
        });

        programmerSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                programmer = programmerSpinner.getSelectedItem().toString();
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });


        return v;
    }

}
