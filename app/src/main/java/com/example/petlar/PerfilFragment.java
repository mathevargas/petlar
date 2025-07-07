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

import com.bumptech.glide.Glide;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

// Fragmento que exibe o perfil do usuário logado
public class PerfilFragment extends Fragment {

    private TextView txtNome, txtBio;
    private ImageView imgFoto;
    private FirebaseFirestore firestore;
    private String uid;

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

        txtNome = view.findViewById(R.id.txtNomeUsuario);
        txtBio = view.findViewById(R.id.txtBioUsuario);
        imgFoto = view.findViewById(R.id.imgFotoPerfil);

        firestore = FirebaseFirestore.getInstance();
        uid = FirebaseAuth.getInstance().getCurrentUser().getUid();

        carregarDadosUsuario();

        // Ação: abrir tela com lista de pets publicados pelo usuário
        btnVerMeusPets.setOnClickListener(v -> {
            MeusPetsFragment meusPetsFragment = new MeusPetsFragment();
            Bundle args = new Bundle();
            args.putBoolean("modoPublico", false);
            args.putString("uidUsuario", uid);
            meusPetsFragment.setArguments(args);

            requireActivity().getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, meusPetsFragment)
                    .addToBackStack(null)
                    .commit();
        });

        // Ação: abrir a tela de edição do perfil
        btnEditarPerfil.setOnClickListener(v ->
                requireActivity().getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container, new EditarPerfilFragment())
                        .addToBackStack(null)
                        .commit()
        );

        // Ação: sair da conta e voltar para tela de login
        btnSair.setOnClickListener(v -> {
            FirebaseAuth.getInstance().signOut();

            BottomNavigationView nav = getActivity().findViewById(R.id.bottom_navigation);
            nav.setSelectedItemId(R.id.menu_profile);

            requireActivity().getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, new LoginFragment())
                    .commit();
        });
    }

    // Função que carrega os dados do Firestore e exibe no perfil
    private void carregarDadosUsuario() {
        firestore.collection("usuarios").document(uid)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        txtNome.setText(documentSnapshot.getString("nome"));
                        txtBio.setText(documentSnapshot.getString("bio"));

                        String urlFoto = documentSnapshot.getString("urlFotoPerfil");
                        if (urlFoto == null || urlFoto.equals("default")) {
                            imgFoto.setImageResource(R.drawable.img_perfil_default);
                        } else {
                            Glide.with(this)
                                    .load(urlFoto)
                                    .placeholder(R.drawable.img_perfil_default)
                                    .into(imgFoto);
                        }
                    }
                });
    }

    // Sempre que o usuário voltar para este fragmento, recarrega dados
    @Override
    public void onResume() {
        super.onResume();
        carregarDadosUsuario();
    }
}
