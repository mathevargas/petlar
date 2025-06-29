package com.example.petlar;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;

import java.util.List;

// Adapter responsável por preencher os dados dos pets na RecyclerView
public class PetAdapter extends RecyclerView.Adapter<PetAdapter.PetViewHolder> {

    private Context context;
    private List<Pet> listaPets;
    private OnPetClickListener listener;

    // Interface que permite detectar clique em um pet da lista
    public interface OnPetClickListener {
        void onPetClick(Pet pet);
    }

    // Construtor do adapter
    public PetAdapter(Context context, List<Pet> listaPets, OnPetClickListener listener) {
        this.context = context;
        this.listaPets = listaPets;
        this.listener = listener;
    }

    // Cria o layout de cada item da RecyclerView
    @NonNull
    @Override
    public PetViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_pet, parent, false);
        return new PetViewHolder(view);
    }

    // Associa os dados do pet ao layout
    @Override
    public void onBindViewHolder(@NonNull PetViewHolder holder, int position) {
        Pet pet = listaPets.get(position);

        // Define textos nos campos da interface
        holder.txtNome.setText(pet.getNome());
        holder.txtIdade.setText(pet.getIdade());
        holder.txtTipo.setText(pet.getTipo());
        holder.txtPorte.setText(pet.getPorte());
        holder.txtCidadeEstado.setText(pet.getCidade() + " - " + pet.getEstado());

        // Carrega imagem do pet (ou imagem padrão, se URL estiver vazia ou nula)
        String urlImagem = pet.getUrlImagem();
        if (urlImagem == null || urlImagem.isEmpty()) {
            holder.imgPet.setImageResource(R.drawable.ic_pet_dog_cat);
        } else {
            Glide.with(context)
                    .load(urlImagem)
                    .placeholder(R.drawable.ic_pet_dog_cat) // Mostra enquanto carrega
                    .error(R.drawable.ic_pet_dog_cat)      // Mostra se erro
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .into(holder.imgPet);
        }

        // Configura clique no item da lista
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onPetClick(pet);
            }
        });
    }

    @Override
    public int getItemCount() {
        return listaPets.size();
    }

    // ViewHolder: representa os componentes do layout do item
    public static class PetViewHolder extends RecyclerView.ViewHolder {

        TextView txtNome, txtIdade, txtTipo, txtPorte, txtCidadeEstado;
        ImageView imgPet;

        public PetViewHolder(@NonNull View itemView) {
            super(itemView);
            txtNome = itemView.findViewById(R.id.txtNomePet);
            txtIdade = itemView.findViewById(R.id.txtIdadePet);
            txtTipo = itemView.findViewById(R.id.txtTipoPet);
            txtPorte = itemView.findViewById(R.id.txtPortePet);
            txtCidadeEstado = itemView.findViewById(R.id.txtCidadeEstado);
            imgPet = itemView.findViewById(R.id.imgPet);
        }
    }
}
