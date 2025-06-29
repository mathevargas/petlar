package com.example.petlar;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;

public class PerfilFragment extends Fragment {

    public PerfilFragment() {
        // Construtor padrão vazio
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_perfil, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        Button btnVerMeusPets = view.findViewById(R.id.btnVerMeusPets);
        Button btnEditarPerfil = view.findViewById(R.id.btnEditarPerfil);
        Button btnSair = view.findViewById(R.id.btnSair);

        btnVerMeusPets.setOnClickListener(v -> {
            startActivity(new android.content.Intent(getActivity(), MeusPetsActivity.class));
        });

        btnEditarPerfil.setOnClickListener(v -> {
            // Futuro: abrir EditarPerfilActivity
        });

        btnSair.setOnClickListener(v -> {
            // 1. Desloga do Firebase
            FirebaseAuth.getInstance().signOut();

            // 2. Atualiza visual da BottomNavigation (opcional)
            BottomNavigationView nav = getActivity().findViewById(R.id.bottom_navigation);
            nav.setSelectedItemId(R.id.menu_profile); // força seleção do menu para atualizar

            // 3. Redireciona para o LoginFragment
            requireActivity().getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, new LoginFragment())
                    .commit();
        });
    }
}
