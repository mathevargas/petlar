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

import java.util.List;

// Adapter responsável por preencher os dados dos pets na RecyclerView
public class PetAdapter extends RecyclerView.Adapter<PetAdapter.PetViewHolder> {

    private Context context;
    private List<Pet> listaPets;

    // Construtor do adapter
    public PetAdapter(Context context, List<Pet> listaPets) {
        this.context = context;
        this.listaPets = listaPets;
    }

    // Cria o layout de cada item da lista
    @NonNull
    @Override
    public PetViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_pet, parent, false);
        return new PetViewHolder(view);
    }

    // Associa os dados ao item da lista
    @Override
    public void onBindViewHolder(@NonNull PetViewHolder holder, int position) {
        Pet pet = listaPets.get(position);

        // Define os textos nos campos
        holder.txtNome.setText(pet.getNome());
        holder.txtIdade.setText(pet.getIdade());
        holder.txtTipo.setText(pet.getTipo());
        holder.txtPorte.setText(pet.getPorte());
        holder.txtCidadeEstado.setText(pet.getCidade() + " - " + pet.getEstado());

        // Carrega a imagem do pet com Glide (imagem obrigatória no cadastro)
        Glide.with(context)
                .load(pet.getUrlImagem())
                .into(holder.imgPet);
    }

    // Retorna o total de itens
    @Override
    public int getItemCount() {
        return listaPets.size();
    }

    // ViewHolder representa os componentes visuais de cada item
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
            imgPet = itemView.findViewById(R.id.imgPet); // Imagem do pet
        }
    }
}
