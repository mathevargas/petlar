package com.example.petlar;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class MeusPetsDisponiveisFragment extends Fragment {

    private RecyclerView recyclerMeusPets;
    private PetAdapter petAdapter;
    private List<Pet> listaPets;
    private TextView txtNenhumPet;

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

        txtNenhumPet = new TextView(getContext());
        txtNenhumPet.setText("Você ainda não publicou pets disponíveis.");
        txtNenhumPet.setVisibility(View.GONE);
        ((ViewGroup) view).addView(txtNenhumPet);

        listaPets = new ArrayList<>();
        petAdapter = new PetAdapter(getContext(), listaPets, pet -> {
            // Ao clicar, exibe os detalhes do pet
            DetalhesPetFragment detalhesFragment = new DetalhesPetFragment();
            Bundle bundle = new Bundle();
            bundle.putSerializable("pet", pet);
            detalhesFragment.setArguments(bundle);

            requireActivity().getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragment_container, detalhesFragment)
                    .addToBackStack(null)
                    .commit();
        });

        recyclerMeusPets.setAdapter(petAdapter);

        carregarPetsDoUsuario();

        return view;
    }

    private void carregarPetsDoUsuario() {
        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        CollectionReference petsRef = FirebaseFirestore.getInstance().collection("pets");

        petsRef.whereEqualTo("idUsuario", uid)
                .whereEqualTo("adotado", false)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    listaPets.clear();
                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        Pet pet = doc.toObject(Pet.class);
                        listaPets.add(pet);
                    }
                    petAdapter.notifyDataSetChanged();

                    txtNenhumPet.setVisibility(listaPets.isEmpty() ? View.VISIBLE : View.GONE);
                })
                .addOnFailureListener(e -> Toast.makeText(getContext(), "Erro ao carregar seus pets", Toast.LENGTH_SHORT).show());
    }
}
