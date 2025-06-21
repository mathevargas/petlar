package com.example.petlar;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class InicioFragment extends Fragment {

    private RecyclerView recyclerViewPets;
    private PetAdapter petAdapter;
    private List<Pet> listaPets;
    private FirebaseFirestore firestore;

    public InicioFragment() {
        // Construtor vazio obrigatório
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Infla o layout para o fragmento
        View view = inflater.inflate(R.layout.fragment_inicio, container, false);

        // Configura a RecyclerView
        recyclerViewPets = view.findViewById(R.id.recyclerViewPets);
        recyclerViewPets.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerViewPets.setHasFixedSize(true);

        // Inicializa a lista e o adaptador
        listaPets = new ArrayList<>();
        petAdapter = new PetAdapter(getContext(), listaPets);
        recyclerViewPets.setAdapter(petAdapter);

        // Inicializa o Firestore
        firestore = FirebaseFirestore.getInstance();

        // Carrega os dados do Firestore
        carregarPetsDoFirebase();

        return view;
    }

    // Busca todos os pets da coleção "pets" no Firestore
    private void carregarPetsDoFirebase() {
        CollectionReference petsRef = firestore.collection("pets");

        petsRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                listaPets.clear(); // Limpa lista anterior
                for (QueryDocumentSnapshot doc : task.getResult()) {
                    Pet pet = doc.toObject(Pet.class); // Converte o documento para um objeto Pet
                    listaPets.add(pet);
                }
                petAdapter.notifyDataSetChanged(); // Atualiza a RecyclerView com os novos dados
            }
        });
    }
}
