package com.example.petlar;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.bumptech.glide.Glide;
import com.google.firebase.firestore.FirebaseFirestore;

public class PerfilPublicoFragment extends Fragment {

    private String publicadorId;

    private ImageView imgFotoPublicador;
    private TextView txtNomePublicador, txtBioPublicador;
    private Button btnVerPetsDisponiveis;

    public PerfilPublicoFragment() {
        // Construtor vazio obrigatório
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_perfil_publico, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        imgFotoPublicador = view.findViewById(R.id.imgFotoPublicador);
        txtNomePublicador = view.findViewById(R.id.txtNomePublicador);
        txtBioPublicador = view.findViewById(R.id.txtBioPublicador);
        btnVerPetsDisponiveis = view.findViewById(R.id.btnVerPetsDisponiveisPublicador);

        // Recupera o ID do publicador enviado por argumentos
        if (getArguments() != null) {
            publicadorId = getArguments().getString("uidUsuario");
        }

        if (publicadorId != null) {
            carregarDadosDoPublicador(publicadorId);
        }

        btnVerPetsDisponiveis.setOnClickListener(v -> {
            MeusPetsFragment fragment = new MeusPetsFragment();
            Bundle bundle = new Bundle();
            bundle.putString("uidUsuario", publicadorId);
            bundle.putBoolean("modoPublico", true);
            fragment.setArguments(bundle);

            FragmentTransaction transaction = requireActivity()
                    .getSupportFragmentManager()
                    .beginTransaction();
            transaction.replace(R.id.fragment_container, fragment);
            transaction.addToBackStack(null);
            transaction.commit();
        });
    }

    private void carregarDadosDoPublicador(String id) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("usuarios").document(id).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        String nome = documentSnapshot.getString("nome");
                        String bio = documentSnapshot.getString("bio");
                        String urlImagem = documentSnapshot.getString("urlImagem");

                        txtNomePublicador.setText(nome != null ? nome : "Sem nome");
                        txtBioPublicador.setText(bio != null ? bio : "");

                        if (urlImagem != null && !urlImagem.isEmpty()) {
                            Glide.with(requireContext())
                                    .load(urlImagem)
                                    .placeholder(R.drawable.img_perfil_default)
                                    .error(R.drawable.img_perfil_default)
                                    .into(imgFotoPublicador);
                        } else {
                            imgFotoPublicador.setImageResource(R.drawable.img_perfil_default);
                        }
                    } else {
                        txtNomePublicador.setText("Usuário não encontrado");
                        txtBioPublicador.setText("");
                        imgFotoPublicador.setImageResource(R.drawable.img_perfil_default);
                    }
                })
                .addOnFailureListener(e -> {
                    txtNomePublicador.setText("Erro ao carregar");
                    txtBioPublicador.setText("");
                    imgFotoPublicador.setImageResource(R.drawable.img_perfil_default);
                });
    }
}
