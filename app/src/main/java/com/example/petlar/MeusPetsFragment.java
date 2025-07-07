package com.example.petlar;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.material.bottomnavigation.BottomNavigationView;

// Fragmento que exibe os pets do usuário (disponíveis/adotados)
public class MeusPetsFragment extends Fragment {

    private BottomNavigationView bottomNavigationView;
    private boolean modoPublico = false;
    private String uidUsuario = null;

    public MeusPetsFragment() {
        // Construtor vazio obrigatório
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_meus_pets, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        bottomNavigationView = view.findViewById(R.id.nav_meus_pets);

        // Recupera os dados passados por arguments
        if (getArguments() != null) {
            modoPublico = getArguments().getBoolean("modoPublico", false);
            uidUsuario = getArguments().getString("uidUsuario");
        }

        // Esconde a aba "Adotados" se estiver em modo público
        if (modoPublico) {
            bottomNavigationView.getMenu().findItem(R.id.nav_adotados).setVisible(false);
        }

        // Fragmento padrão: pets disponíveis
        if (savedInstanceState == null) {
            Fragment fragment = new MeusPetsDisponiveisFragment();
            Bundle args = new Bundle();
            args.putBoolean("modoPublico", modoPublico);
            args.putString("uidUsuario", uidUsuario);
            fragment.setArguments(args);

            requireActivity().getSupportFragmentManager().beginTransaction()
                    .replace(R.id.meusPetsFragmentContainer, fragment)
                    .commit();
        }

        // Navegação pelos botões
        bottomNavigationView.setOnItemSelectedListener(item -> {
            Fragment fragment;

            if (item.getItemId() == R.id.nav_disponiveis) {
                fragment = new MeusPetsDisponiveisFragment();
            } else if (item.getItemId() == R.id.nav_adotados) {
                fragment = new MeusPetsAdotadosFragment();
            } else {
                return false;
            }

            // Passa os mesmos argumentos para os fragments internos
            Bundle args = new Bundle();
            args.putBoolean("modoPublico", modoPublico);
            args.putString("uidUsuario", uidUsuario);
            fragment.setArguments(args);

            requireActivity().getSupportFragmentManager().beginTransaction()
                    .replace(R.id.meusPetsFragmentContainer, fragment)
                    .commit();

            return true;
        });
    }
}
