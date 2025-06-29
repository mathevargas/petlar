package com.example.petlar;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.google.firebase.auth.FirebaseAuth;

// Fragmento responsável por exibir os detalhes do pet selecionado
public class DetalhesPetFragment extends Fragment {

    private ImageView imgPet;
    private TextView txtNome, txtInfoBasica, txtGeneroRaca, txtLocalizacao, txtDescricao;
    private Button btnWhatsApp, btnEmail;

    private Pet petSelecionado;

    // URL da imagem padrão para pets sem imagem personalizada
    private static final String URL_IMAGEM_PADRAO = "https://firebasestorage.googleapis.com/v0/b/SEU_PROJETO.appspot.com/o/ic_pet_dog_cat.png?alt=media";

    public DetalhesPetFragment() {
        // Construtor vazio obrigatório
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        // Infla o layout da tela de detalhes
        View view = inflater.inflate(R.layout.fragment_detalhes_pet, container, false);

        // Referência aos componentes da interface
        imgPet = view.findViewById(R.id.imgDetalhePet);
        txtNome = view.findViewById(R.id.txtNomeDetalhe);
        txtInfoBasica = view.findViewById(R.id.txtInfoBasica);
        txtGeneroRaca = view.findViewById(R.id.txtGeneroRaca);
        txtLocalizacao = view.findViewById(R.id.txtLocalizacao);
        txtDescricao = view.findViewById(R.id.txtDescricao);
        btnWhatsApp = view.findViewById(R.id.btnWhatsApp);
        btnEmail = view.findViewById(R.id.btnEmail);

        // Verifica se o pet foi passado por argumento
        if (getArguments() != null) {
            petSelecionado = (Pet) getArguments().getSerializable("pet");

            if (petSelecionado != null) {
                exibirDadosPet(petSelecionado);
            }
        }

        return view;
    }

    // Exibe as informações do pet na tela
    private void exibirDadosPet(Pet pet) {
        // Preenche os campos de texto com as informações do pet
        txtNome.setText(pet.getNome());
        txtInfoBasica.setText(pet.getIdade() + " - " + pet.getTipo() + " - " + pet.getPorte());
        txtGeneroRaca.setText(pet.getGenero() + " - " + (!TextUtils.isEmpty(pet.getRaca()) ? pet.getRaca() : "Sem raça definida"));
        txtLocalizacao.setText(pet.getCidade() + " - " + pet.getEstado());
        txtDescricao.setText(TextUtils.isEmpty(pet.getDescricao()) ? "Sem descrição disponível." : pet.getDescricao());

        // Carrega a imagem do pet com Glide, usando imagem padrão se a URL estiver vazia ou nula
        String url = pet.getUrlImagem();
        if (TextUtils.isEmpty(url)) url = URL_IMAGEM_PADRAO;

        Glide.with(this)
                .load(url)
                .placeholder(R.drawable.ic_pet_dog_cat) // Mostra imagem local enquanto carrega
                .error(R.drawable.ic_pet_dog_cat)       // Mostra imagem padrão se houver erro
                .diskCacheStrategy(DiskCacheStrategy.ALL) // Cache de imagens para melhor desempenho
                .into(imgPet);

        // Verifica se o usuário está logado
        boolean estaLogado = FirebaseAuth.getInstance().getCurrentUser() != null;

        if (estaLogado) {
            // Mostra os botões de contato
            btnWhatsApp.setVisibility(View.VISIBLE);
            btnEmail.setVisibility(View.VISIBLE);

            // Ação do botão WhatsApp com mensagem personalizada
            btnWhatsApp.setOnClickListener(v -> {
                String numero = pet.getWhatsapp().replaceAll("\\D", ""); // Remove tudo que não for número
                String mensagem = "Olá! Vi o pet " + pet.getNome() + " no app PetLar e gostaria de saber mais.";
                String uri = "https://wa.me/" + numero + "?text=" + Uri.encode(mensagem);

                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(uri)));
            });

            // Ação do botão E-mail com assunto e corpo pré-preenchidos
            btnEmail.setOnClickListener(v -> {
                Intent emailIntent = new Intent(Intent.ACTION_SENDTO);
                emailIntent.setData(Uri.parse("mailto:" + pet.getEmail()));
                emailIntent.putExtra(Intent.EXTRA_SUBJECT, "Interesse no pet: " + pet.getNome());
                emailIntent.putExtra(Intent.EXTRA_TEXT,
                        "Olá! Vi o pet " + pet.getNome() + " no aplicativo PetLar e estou interessado(a) em saber mais informações. Você pode me contar mais sobre ele?");

                startActivity(Intent.createChooser(emailIntent, "Enviar e-mail"));
            });

        } else {
            // Esconde os botões de contato se o usuário não estiver logado
            btnWhatsApp.setVisibility(View.GONE);
            btnEmail.setVisibility(View.GONE);
        }
    }
}