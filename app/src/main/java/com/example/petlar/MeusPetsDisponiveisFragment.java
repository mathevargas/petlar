package com.example.petlar;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

// Fragmento que mostra os pets disponíveis publicados pelo usuário logado ou em modo público
public class MeusPetsDisponiveisFragment extends Fragment {

    private RecyclerView recyclerMeusPets;
    private PetAdapter petAdapter;
    private List<Pet> listaPets;
    private TextView txtNenhumPet;

    private boolean modoPublico = false;
    private String uidUsuario = "";

    public MeusPetsDisponiveisFragment() {
        // Construtor vazio obrigatório
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_meus_pets_disponiveis, container, false);

        recyclerMeusPets = view.findViewById(R.id.recyclerMeusPets);
        recyclerMeusPets.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerMeusPets.setHasFixedSize(true);

        // Mensagem de "nenhum pet encontrado"
        txtNenhumPet = new TextView(getContext());
        txtNenhumPet.setText("Nenhum pet disponível encontrado.");
        txtNenhumPet.setVisibility(View.GONE);
        ((ViewGroup) view).addView(txtNenhumPet);

        listaPets = new ArrayList<>();

        // Verifica se veio no modo público e qual UID usar
        if (getArguments() != null) {
            modoPublico = getArguments().getBoolean("modoPublico", false);
            uidUsuario = getArguments().getString("uidUsuario", "");
        }

        if (!modoPublico && (uidUsuario == null || uidUsuario.isEmpty())) {
            uidUsuario = FirebaseAuth.getInstance().getCurrentUser().getUid();
        }

        // Inicializa o adapter com base no modo (público ou privado)
        petAdapter = new PetAdapter(getContext(), listaPets, pet -> {
            DetalhesPetFragment detalhesFragment = new DetalhesPetFragment();
            Bundle bundle = new Bundle();
            bundle.putSerializable("pet", pet);
            detalhesFragment.setArguments(bundle);

            requireActivity().getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragment_container, detalhesFragment)
                    .addToBackStack(null)
                    .commit();
        }, modoPublico); // <- passa o modo correto

        recyclerMeusPets.setAdapter(petAdapter);

        // Carrega pets publicados pelo usuário
        carregarPetsDoUsuario();

        return view;
    }

    private void carregarPetsDoUsuario() {
        CollectionReference petsRef = FirebaseFirestore.getInstance().collection("pets");

        // ❗️Ajustado: o campo correto é "idUsuario"
        petsRef.whereEqualTo("idUsuario", uidUsuario)
                .whereEqualTo("adotado", false)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    listaPets.clear();
                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        Pet pet = doc.toObject(Pet.class);

                        // ✅ Ajustado: usa o método correto para atribuir o ID do documento
                        pet.setIdPet(doc.getId());

                        listaPets.add(pet);
                    }
                    petAdapter.notifyDataSetChanged();

                    // Mostra ou oculta a mensagem caso a lista esteja vazia
                    txtNenhumPet.setVisibility(listaPets.isEmpty() ? View.VISIBLE : View.GONE);
                })
                .addOnFailureListener(e ->
                        Toast.makeText(getContext(), "Erro ao carregar pets disponíveis", Toast.LENGTH_SHORT).show());
    }
}
