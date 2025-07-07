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

// Fragmento responsável por exibir os pets que já foram adotados pelo usuário logado
public class MeusPetsAdotadosFragment extends Fragment {

    private RecyclerView recyclerView;
    private PetAdapter petAdapter;
    private List<Pet> listaPets;
    private FirebaseFirestore firestore;

    private boolean modoPublico = false;
    private String uidUsuario = "";

    private TextView txtNenhumPet;

    public MeusPetsAdotadosFragment() {
        // Construtor público vazio obrigatório
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_meus_pets_adotados, container, false);

        recyclerView = view.findViewById(R.id.recyclerPetsAdotados);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setHasFixedSize(true);

        txtNenhumPet = new TextView(getContext());
        txtNenhumPet.setText("Nenhum pet adotado encontrado.");
        txtNenhumPet.setVisibility(View.GONE);
        ((ViewGroup) view).addView(txtNenhumPet);

        listaPets = new ArrayList<>();

        // Adapter configurado apenas para exibir os pets (sem botões de controle)
        petAdapter = new PetAdapter(getContext(), listaPets, pet ->
                Toast.makeText(getContext(), "Este pet já foi adotado!", Toast.LENGTH_SHORT).show()
        );
        recyclerView.setAdapter(petAdapter);

        firestore = FirebaseFirestore.getInstance();

        // Recupera argumentos do bundle para saber se é modo público e o UID do usuário
        if (getArguments() != null) {
            modoPublico = getArguments().getBoolean("modoPublico", false);
            uidUsuario = getArguments().getString("uidUsuario", "");
        }

        // Em modo público, não deve carregar pets adotados (apenas disponíveis)
        if (!modoPublico) {
            if (uidUsuario == null || uidUsuario.isEmpty()) {
                if (FirebaseAuth.getInstance().getCurrentUser() != null) {
                    uidUsuario = FirebaseAuth.getInstance().getCurrentUser().getUid();
                }
            }

            // Carrega pets adotados do usuário logado
            carregarPetsAdotados();
        }

        return view;
    }

    // Carrega os pets marcados como adotados pelo usuário logado
    private void carregarPetsAdotados() {
        CollectionReference petsRef = firestore.collection("pets");

        petsRef.whereEqualTo("adotado", true)
                .whereEqualTo("uidUsuario", uidUsuario)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    listaPets.clear();
                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        Pet pet = doc.toObject(Pet.class);
                        pet.setIdPet(doc.getId());
                        listaPets.add(pet);
                    }

                    petAdapter.notifyDataSetChanged();

                    txtNenhumPet.setVisibility(listaPets.isEmpty() ? View.VISIBLE : View.GONE);
                })
                .addOnFailureListener(e ->
                        Toast.makeText(getContext(), "Erro ao carregar pets adotados", Toast.LENGTH_SHORT).show());
    }
}
