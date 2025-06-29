package com.example.petlar;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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

// Fragmento responsável por exibir os pets que já foram adotados pelo usuário logado
public class MeusPetsAdotadosFragment extends Fragment {

    private RecyclerView recyclerView;
    private PetAdapter petAdapter;
    private List<Pet> listaPets;
    private FirebaseFirestore firestore;

    public MeusPetsAdotadosFragment() {
        // Construtor público vazio obrigatório
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Infla o layout XML correspondente a este fragmento
        View view = inflater.inflate(R.layout.fragment_meus_pets_adotados, container, false);

        // Referência para a RecyclerView com o ID correto conforme seu XML
        recyclerView = view.findViewById(R.id.recyclerPetsAdotados);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setHasFixedSize(true);

        // Lista de pets e inicialização do adapter
        listaPets = new ArrayList<>();
        petAdapter = new PetAdapter(getContext(), listaPets, pet -> {
            Toast.makeText(getContext(), "Este pet já foi adotado!", Toast.LENGTH_SHORT).show();
        });

        recyclerView.setAdapter(petAdapter);

        // Inicializa Firestore
        firestore = FirebaseFirestore.getInstance();

        // Busca pets adotados do usuário logado
        carregarPetsAdotados();

        return view;
    }

    // Recupera pets com "adotado = true" e que pertencem ao usuário logado
    private void carregarPetsAdotados() {
        String idUsuario = FirebaseAuth.getInstance().getCurrentUser() != null ?
                FirebaseAuth.getInstance().getCurrentUser().getUid() : "";

        CollectionReference petsRef = firestore.collection("pets");

        petsRef.whereEqualTo("adotado", true)
                .whereEqualTo("idUsuario", idUsuario)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    listaPets.clear();
                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        Pet pet = doc.toObject(Pet.class);
                        listaPets.add(pet);
                    }
                    petAdapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e ->
                        Toast.makeText(getContext(), "Erro ao carregar pets adotados", Toast.LENGTH_SHORT).show());
    }
}
