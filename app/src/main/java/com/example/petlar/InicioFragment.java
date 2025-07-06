package com.example.petlar;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class InicioFragment extends Fragment {

    private RecyclerView recyclerViewPets;
    private PetAdapter petAdapter;
    private List<Pet> listaPets;
    private FirebaseFirestore firestore;
    private TextView txtNenhumPet;
    private Button btnAbrirFiltro;

    // Filtros atuais
    private String filtroEstado = "Todos";
    private String filtroCidade = "";
    private String filtroTipo = "Todos";
    private String filtroPorte = "Todos";
    private String filtroRaca = ""; // Novo filtro por raça

    public InicioFragment() {
        // Construtor vazio obrigatório
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_inicio, container, false);

        // Inicializa componentes da interface
        recyclerViewPets = view.findViewById(R.id.recyclerViewPets);
        txtNenhumPet = view.findViewById(R.id.txtNenhumPet);
        btnAbrirFiltro = view.findViewById(R.id.btnAbrirFiltro);

        recyclerViewPets.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerViewPets.setHasFixedSize(true);

        // Lista e Adapter
        listaPets = new ArrayList<>();
        petAdapter = new PetAdapter(getContext(), listaPets, pet -> {
            if (FirebaseAuth.getInstance().getCurrentUser() != null) {
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

        // Carrega todos os pets inicialmente (sem filtros)
        carregarPetsDoFirebase();

        // Abre painel de filtro personalizado
        btnAbrirFiltro.setOnClickListener(v -> {
            PainelFiltroFragment painelFiltro = new PainelFiltroFragment((estado, cidade, tipo, porte, raca) -> {
                // Atualiza filtros com os dados selecionados
                filtroEstado = estado;
                filtroCidade = cidade;
                filtroTipo = tipo;
                filtroPorte = porte;
                filtroRaca = raca;
                carregarPetsDoFirebase(); // Recarrega a lista com os novos filtros
            });
            painelFiltro.show(getParentFragmentManager(), painelFiltro.getTag());
        });

        return view;
    }

    // Carrega os pets do Firestore aplicando os filtros selecionados
    private void carregarPetsDoFirebase() {
        CollectionReference petsRef = firestore.collection("pets");
        Query query = petsRef.whereEqualTo("adotado", false);

        if (!filtroEstado.equals("Todos")) {
            query = query.whereEqualTo("estado", filtroEstado);
        }

        if (!filtroCidade.isEmpty()) {
            query = query.whereEqualTo("cidade", filtroCidade);
        }

        if (!filtroTipo.equals("Todos")) {
            query = query.whereEqualTo("tipo", filtroTipo);
        }

        if (!filtroPorte.equals("Todos")) {
            query = query.whereEqualTo("porte", filtroPorte);
        }

        if (!filtroRaca.isEmpty()) {
            query = query.whereEqualTo("raca", filtroRaca);
        }

        // Executa a consulta e atualiza a lista
        query.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                listaPets.clear();
                for (QueryDocumentSnapshot doc : task.getResult()) {
                    Pet pet = doc.toObject(Pet.class);
                    listaPets.add(pet);
                }
                petAdapter.notifyDataSetChanged();
                txtNenhumPet.setVisibility(listaPets.isEmpty() ? View.VISIBLE : View.GONE);
            } else {
                Toast.makeText(getContext(), "Erro ao carregar pets", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
