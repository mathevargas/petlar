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

        // Se não for modo público, usa o usuário logado
        if (!modoPublico && (uidUsuario == null || uidUsuario.isEmpty())) {
            uidUsuario = FirebaseAuth.getInstance().getCurrentUser().getUid();
        }

        // Inicializa o adapter
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
        });

        recyclerMeusPets.setAdapter(petAdapter);

        // Carrega pets publicados pelo usuário
        carregarPetsDoUsuario();

        return view;
    }

    // Método para buscar pets do usuário logado (ou público) com campo adotado = false
    private void carregarPetsDoUsuario() {
        CollectionReference petsRef = FirebaseFirestore.getInstance().collection("pets");

        petsRef.whereEqualTo("uidUsuario", uidUsuario)
                .whereEqualTo("adotado", false)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    listaPets.clear();
                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        Pet pet = doc.toObject(Pet.class);
                        pet.setIdPet(doc.getId()); // garante que o ID do documento esteja no objeto
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
