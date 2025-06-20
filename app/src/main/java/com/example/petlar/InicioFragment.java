package com.example.petlar;

import android.os.Bundle;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class InicioFragment extends Fragment {

    public InicioFragment() {
        // Construtor vazio
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflar o layout para este fragment
        return inflater.inflate(R.layout.fragment_inicio, container, false);
    }
}
