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

// Fragmento inicial que exibe a lista de pets disponíveis para adoção
public class InicioFragment extends Fragment {

    private RecyclerView recyclerViewPets;
    private PetAdapter petAdapter;
    private List<Pet> listaPets;
    private FirebaseFirestore firestore;
    private TextView txtNenhumPet;

    public InicioFragment() {
        // Construtor vazio obrigatório para o Fragment
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_inicio, container, false);

        // Inicializa os componentes visuais
        recyclerViewPets = view.findViewById(R.id.recyclerViewPets);
        recyclerViewPets.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerViewPets.setHasFixedSize(true);

        txtNenhumPet = view.findViewById(R.id.txtNenhumPet);
        txtNenhumPet.setVisibility(View.GONE); // Inicialmente invisível

        // Inicializa lista e adapter
        listaPets = new ArrayList<>();

        // Adapter com clique para ir aos detalhes do pet
        petAdapter = new PetAdapter(getContext(), listaPets, pet -> {
            if (FirebaseAuth.getInstance().getCurrentUser() != null) {
                // Se usuário estiver logado, abre detalhes do pet
                DetalhesPetFragment detalhesFragment = new DetalhesPetFragment();
                Bundle bundle = new Bundle();
                bundle.putSerializable("pet", pet);
                detalhesFragment.setArguments(bundle);

                requireActivity().getSupportFragmentManager()
                        .beginTransaction()
                        .replace(R.id.fragment_container, detalhesFragment)
                        .addToBackStack(null)
                        .commit();
            } else {
                // Se não estiver logado, redireciona para tela de login
                Toast.makeText(getContext(), "Faça login para ver os detalhes do pet", Toast.LENGTH_SHORT).show();

                requireActivity().getSupportFragmentManager()
                        .beginTransaction()
                        .replace(R.id.fragment_container, new LoginFragment())
                        .addToBackStack(null)
                        .commit();
            }
        });

        recyclerViewPets.setAdapter(petAdapter);

        firestore = FirebaseFirestore.getInstance();

        // Carrega pets disponíveis (não adotados)
        carregarPetsDoFirebase();

        return view;
    }

    // Recupera pets do Firestore que ainda não foram adotados
    private void carregarPetsDoFirebase() {
        CollectionReference petsRef = firestore.collection("pets");

        petsRef.whereEqualTo("adotado", false)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        listaPets.clear();
                        for (QueryDocumentSnapshot doc : task.getResult()) {
                            Pet pet = doc.toObject(Pet.class);
                            listaPets.add(pet);
                        }
                        petAdapter.notifyDataSetChanged();

                        // Exibe mensagem se lista estiver vazia
                        txtNenhumPet.setVisibility(listaPets.isEmpty() ? View.VISIBLE : View.GONE);
                    } else {
                        Toast.makeText(getContext(), "Erro ao carregar pets", Toast.LENGTH_SHORT).show();
                    }
                });
    }
}
