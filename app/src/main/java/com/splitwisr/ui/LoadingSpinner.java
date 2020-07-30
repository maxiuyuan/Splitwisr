package com.splitwisr.ui;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.splitwisr.R;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link LoadingSpinner#newInstance} factory method to
 * create an instance of this fragment.
 */
public class LoadingSpinner extends Fragment {


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_loading_spinner, container, false);
    }
}