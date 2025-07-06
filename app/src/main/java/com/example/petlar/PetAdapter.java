package com.example.petlar;

import android.app.AlertDialog;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.List;

// Adapter responsável por preencher a RecyclerView com a lista de pets
public class PetAdapter extends RecyclerView.Adapter<PetAdapter.PetViewHolder> {

    private Context context;
    private List<Pet> listaPets;
    private OnPetClickListener listener;
    private boolean modoPublico = false; // Se for perfil público, oculta botões de editar/adotar/excluir

    // Interface para lidar com clique no item (para abrir detalhes, por exemplo)
    public interface OnPetClickListener {
        void onPetClick(Pet pet);
    }

    // Construtor padrão (modo privado)
    public PetAdapter(Context context, List<Pet> listaPets, OnPetClickListener listener) {
        this.context = context;
        this.listaPets = listaPets;
        this.listener = listener;
    }

    // Construtor para quando for usada em modo público (sem botões)
    public PetAdapter(Context context, List<Pet> listaPets, OnPetClickListener listener, boolean modoPublico) {
        this.context = context;
        this.listaPets = listaPets;
        this.listener = listener;
        this.modoPublico = modoPublico;
    }

    @NonNull
    @Override
    public PetViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Infla o layout de cada item da lista
        View view = LayoutInflater.from(context).inflate(R.layout.item_pet, parent, false);
        return new PetViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PetViewHolder holder, int position) {
        Pet pet = listaPets.get(position);

        // Preenche os dados no item
        holder.txtNome.setText(pet.getNome());
        holder.txtIdade.setText(pet.getIdade());
        holder.txtTipo.setText(pet.getTipo());
        holder.txtPorte.setText(pet.getPorte());
        holder.txtCidadeEstado.setText(pet.getCidade() + " - " + pet.getEstado());

        // Carrega imagem do pet (ou imagem padrão)
        String urlImagem = pet.getUrlImagem();
        if (urlImagem == null || urlImagem.isEmpty()) {
            holder.imgPet.setImageResource(R.drawable.ic_pet_dog_cat);
        } else {
            Glide.with(context)
                    .load(urlImagem)
                    .placeholder(R.drawable.ic_pet_dog_cat)
                    .error(R.drawable.ic_pet_dog_cat)
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .into(holder.imgPet);
        }

        // Clique no item chama o listener (abre tela de detalhes)
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onPetClick(pet);
            }
        });

        // Verifica se o usuário logado é o dono do pet
        String uidAtual = FirebaseAuth.getInstance().getCurrentUser() != null ?
                FirebaseAuth.getInstance().getCurrentUser().getUid() : "";

        // Se for modo público ou outro usuário, esconde os botões de controle
        if (modoPublico || !uidAtual.equals(pet.getUidUsuario())) {
            holder.btnMarcarAdotado.setVisibility(View.GONE);
            holder.btnEditar.setVisibility(View.GONE);
            holder.btnExcluir.setVisibility(View.GONE);
            return;
        }

        // Mostra os botões apenas se for o dono do pet
        holder.btnMarcarAdotado.setVisibility(View.VISIBLE);
        holder.btnEditar.setVisibility(View.VISIBLE);
        holder.btnExcluir.setVisibility(View.VISIBLE);

        // Botão "Marcar como adotado"
        holder.btnMarcarAdotado.setOnClickListener(v -> {
            FirebaseFirestore db = FirebaseFirestore.getInstance();
            String uid = uidAtual; // Necessário para uso dentro da lambda

            // Atualiza o campo "adotado" no documento com o ID correto (idPet)
            db.collection("pets")
                    .document(pet.getIdPet()) // substituído setId → setIdPet
                    .update("adotado", true)
                    .addOnSuccessListener(unused -> {
                        // Cria o objeto de adoção
                        Adocao adocao = new Adocao(
                                pet.getIdPet(),
                                uid,
                                Timestamp.now(),
                                "Adotado via app"
                        );

                        // Registra na coleção "adotados"
                        db.collection("adotados")
                                .add(adocao)
                                .addOnSuccessListener(ref -> {
                                    // Remove da lista local e atualiza a RecyclerView
                                    listaPets.remove(position);
                                    notifyItemRemoved(position);
                                    Toast.makeText(context, "Pet marcado como adotado!", Toast.LENGTH_SHORT).show();
                                })
                                .addOnFailureListener(e -> {
                                    Toast.makeText(context, "Pet adotado, mas falha ao registrar adoção", Toast.LENGTH_LONG).show();
                                });
                    })
                    .addOnFailureListener(e ->
                            Toast.makeText(context, "Erro ao marcar como adotado", Toast.LENGTH_SHORT).show());
        });

        // Botão "Excluir pet"
        holder.btnExcluir.setOnClickListener(v -> {
            new AlertDialog.Builder(context)
                    .setTitle("Excluir pet")
                    .setMessage("Tem certeza que deseja excluir este pet?")
                    .setPositiveButton("Sim", (dialog, which) -> {
                        FirebaseFirestore.getInstance().collection("pets")
                                .document(pet.getIdPet()) // substituído getId() por getIdPet()
                                .delete()
                                .addOnSuccessListener(unused -> {
                                    listaPets.remove(position);
                                    notifyItemRemoved(position);
                                    Toast.makeText(context, "Pet excluído com sucesso", Toast.LENGTH_SHORT).show();
                                })
                                .addOnFailureListener(e ->
                                        Toast.makeText(context, "Erro ao excluir pet", Toast.LENGTH_SHORT).show());
                    })
                    .setNegativeButton("Cancelar", null)
                    .show();
        });

        // Botão "Editar pet"
        holder.btnEditar.setOnClickListener(v -> {
            // Substitui o uso de Intent por navegação via Fragment
            EditarPetFragment editarFragment = new EditarPetFragment();
            Bundle bundle = new Bundle();
            bundle.putSerializable("pet", pet);
            editarFragment.setArguments(bundle);

            if (context instanceof MainActivity) {
                ((MainActivity) context).getSupportFragmentManager()
                        .beginTransaction()
                        .replace(R.id.fragment_container, editarFragment)
                        .addToBackStack(null)
                        .commit();
            }
        });
    }

    @Override
    public int getItemCount() {
        return listaPets.size();
    }

    // ViewHolder representa os elementos visuais de cada item da lista
    public static class PetViewHolder extends RecyclerView.ViewHolder {
        TextView txtNome, txtIdade, txtTipo, txtPorte, txtCidadeEstado;
        ImageView imgPet;
        Button btnMarcarAdotado, btnEditar, btnExcluir;

        public PetViewHolder(@NonNull View itemView) {
            super(itemView);
            txtNome = itemView.findViewById(R.id.txtNomePet);
            txtIdade = itemView.findViewById(R.id.txtIdadePet);
            txtTipo = itemView.findViewById(R.id.txtTipoPet);
            txtPorte = itemView.findViewById(R.id.txtPortePet);
            txtCidadeEstado = itemView.findViewById(R.id.txtCidadeEstado);
            imgPet = itemView.findViewById(R.id.imgPet);
            btnMarcarAdotado = itemView.findViewById(R.id.btnMarcarAdotado);
            btnEditar = itemView.findViewById(R.id.btnEditarPet);
            btnExcluir = itemView.findViewById(R.id.btnExcluirPet);
        }
    }
}
